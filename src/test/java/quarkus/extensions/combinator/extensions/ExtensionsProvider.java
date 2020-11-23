package quarkus.extensions.combinator.extensions;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
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

    private static final String QUARKUS = "quarkus-";

    private static final List<Identity<List<Set<String>>>> COMBINATION_FUNCTIONS = asList(randomSort(), limit());

    private final List<String> extensions;

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
    }

    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) throws Exception {
        List<Set<String>> combinations = generateCombinations();
        for (Identity<List<Set<String>>> function : COMBINATION_FUNCTIONS) {
            combinations = function.apply(combinations);
        }

        return combinations.stream().map(Arguments::of);
    }

    private List<Set<String>> generateCombinations() {
        return Sets.combinations(ImmutableSet.copyOf(extensions), Configuration.GROUP_OF.getAsInteger())
                .stream()
                .collect(Collectors.toList());
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

}
