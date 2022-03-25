package quarkus.extensions.combinator.extensions;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import quarkus.extensions.combinator.exclusions.ExclusionsManager;
import quarkus.extensions.combinator.maven.MavenGetQuarkusExtensions;
import quarkus.extensions.combinator.utils.Identity;

public final class ExtensionsProvider implements ArgumentsProvider {

    private static final Logger LOG = Logger.getLogger(ExtensionsProvider.class.getName());

    private static final String QUARKUS = "quarkus-";
    private static final List<String> EXTENSIONS_DISALLOW_SPECIAL_CHARS = Arrays.asList(":", ".");

    private static final List<Identity<List<Set<String>>>> COMBINATION_FUNCTIONS = asList(randomSort(), limit());

    private final List<String> extensions;

    public ExtensionsProvider() {
        MavenGetQuarkusExtensions mavenCommand = new MavenGetQuarkusExtensions();
        List<String> output = mavenCommand.getExtensions();

        LOG.info("Available extensions size: " + output.size());
        this.extensions = output.stream()
                .filter(byIsAnExtension().and(byIsNotExcluded()))
                .map(extractName())
                //                .peek(System.out::println)
                .filter(bySupportedIfEnabled())
                //                .peek(System.out::println)
                .collect(Collectors.toList());
        LOG.info("Filtered extensions size: " + extensions.size());
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
                .filter(isNotExcluded().and(byIncludesIfAny()))
                .collect(Collectors.toList());
    }

    private Predicate<Set<String>> isNotExcluded() {
        return ExclusionsManager.get()::isNotExcluded;
    }

    private Predicate<Set<String>> byIncludesIfAny() {
        return combination -> {
            List<String> includesExtensions = Configuration.INCLUDES_COMBINATIONS_ONLY_WITH_EXTENSIONS.getAsList();
            return includesExtensions.isEmpty()
                    || includesExtensions.stream().allMatch(extension -> combination.contains(extension));
        };
    }

    private Predicate<String> byIsAnExtension() {
        return line -> line.contains(QUARKUS) && EXTENSIONS_DISALLOW_SPECIAL_CHARS.stream().noneMatch(line::contains);
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
        return line -> {
            // Format output line from mvn list extensions command to a list of artifact ids
            String id = line.trim().substring("[INFO] ✬ ".length());
            // If the artifact id starts with `quarkus-`, we remove it for better readability and usage
            if (id.startsWith(QUARKUS)) {
                id = StringUtils.substringAfter(id, QUARKUS);
            }

            return id;
        };
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

}
