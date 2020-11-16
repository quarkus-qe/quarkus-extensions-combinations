package quarkus.extensions.combinator.extensions;

import java.util.ArrayList;
import java.util.List;
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

public final class ExtensionsProvider implements ArgumentsProvider {

    private static final String QUARKUS = "quarkus-";

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
        return Sets
                .combinations(ImmutableSet.copyOf(extensions), Configuration.GROUP_OF.getAsInteger())
                .stream().map(Arguments::of);
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

}
