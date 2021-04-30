package quarkus.extensions.combinator.extensions;

import static java.util.Arrays.asList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import quarkus.extensions.combinator.Configuration;
import quarkus.extensions.combinator.utils.CommandBuilder;
import quarkus.extensions.combinator.utils.Identity;

public final class ExtensionsProvider implements ArgumentsProvider {

    private static final Logger LOG = Logger.getLogger(ExtensionsProvider.class.getName());

    private static final String QUARKUS = "quarkus-";
    private static final String EXCLUSIONS_FILE = "exclusions.properties";

    private static final List<Identity<List<Set<String>>>> COMBINATION_FUNCTIONS = asList(randomSort(), limit());

    private final List<String> extensions;
    private final List<Set<String>> exclusions;

    public ExtensionsProvider() {
        List<String> output = new ArrayList<>();
        new CommandBuilder("mvn", "-f", "target/test-classes/list-extensions.pom.xml", "quarkus:list-extensions")
                .outputToList(output)
                .runAndWait();

        this.extensions = output.stream()
                .filter(byIsAnExtension().and(byIsNotExcluded()))
                .map(extractName())
                .filter(bySupportedIfEnabled())
                .collect(Collectors.toList());
        this.exclusions = initializeExclusions();
    }

    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
        List<Set<String>> combinations = generateCombinations();
        for (Identity<List<Set<String>>> function : COMBINATION_FUNCTIONS) {
            combinations = function.apply(combinations);
        }

        return combinations.stream().map(Arguments::of);
    }

    public List<Set<String>> generateCombinations() {
        return Sets.combinations(ImmutableSet.copyOf(extensions), Configuration.GROUP_OF.getAsInteger())
                .stream()
                .filter(byExcludes().and(byIncludesIfAny()))
                .collect(Collectors.toList());
    }

    private Predicate<Set<String>> byExcludes() {
        return combination -> {
            return exclusions.isEmpty()
                    || !exclusions.stream().anyMatch(excluded -> combination.containsAll(excluded));
        };
    }

    private Predicate<Set<String>> byIncludesIfAny() {
        return combination -> {
            List<String> includesExtensions = Configuration.INCLUDES_COMBINATIONS_ONLY_WITH_EXTENSIONS.getAsList();
            return includesExtensions.isEmpty()
                    || includesExtensions.stream().allMatch(extension -> combination.contains(extension));
        };
    }

    private Predicate<String> byIsAnExtension() {
        return line -> line.contains(QUARKUS);
    }

    private Predicate<String> byIsNotExcluded() {
        return line -> !line.contains("extensions-combinator");
    }

    private Predicate<String> bySupportedIfEnabled() {
        return extension -> {
            if (Configuration.ONLY_SUPPORTED_EXTENSIONS.getAsBoolean()) {
                return CheckSuppertedQuarkusExtension.isSupported(extension);
            }

            return true;
        };
    }

    private Function<String, String> extractName() {
        return line -> StringUtils.substringAfter(line, QUARKUS).trim();
    }

    private static final Identity<List<Set<String>>> limit() {
        return combinations -> {
            int limit = Configuration.LIMIT_EXTENSIONS.getAsInteger();
            if (limit > 0) {
                return combinations.subList(0, limit);
            }

            return combinations;
        };
    }

    private static final Identity<List<Set<String>>> randomSort() {
        return combinations -> {
            if (Configuration.RANDOM_SORT_EXTENSIONS.getAsBoolean()) {
                Collections.shuffle(combinations, ThreadLocalRandom.current());
            }

            return combinations;
        };
    }

    private static final List<Set<String>> initializeExclusions() {
        List<Set<String>> exclusions = new ArrayList<>();

        try (InputStream input = ExtensionsProvider.class.getClassLoader().getResourceAsStream(EXCLUSIONS_FILE)) {

            Properties prop = new Properties();
            prop.load(input);
            for (Entry<Object, Object> entry : prop.entrySet()) {
                Boolean isEnabled = Boolean.parseBoolean((String) entry.getValue());
                String combination = (String) entry.getKey();
                if (isEnabled) {
                    exclusions.add(Stream.of(combination.split(",")).collect(Collectors.toSet()));
                }
            }

        } catch (Exception ex) {
            LOG.warning("Failed to load exclusions. No exclusions will be used. Cause: " + ex.getMessage());
        }

        return exclusions;
    }

}
