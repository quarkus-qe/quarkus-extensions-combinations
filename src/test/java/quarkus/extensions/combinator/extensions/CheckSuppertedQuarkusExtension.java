package quarkus.extensions.combinator.extensions;

import static io.restassured.RestAssured.get;

import java.util.function.Predicate;
import java.util.stream.Stream;

import quarkus.extensions.combinator.Configuration;

public final class CheckSuppertedQuarkusExtension {

    private static final String SUPPORTED_TAG = "support:full-support";

    private static final CheckSuppertedQuarkusExtension INSTANCE = new CheckSuppertedQuarkusExtension();

    private final Extension[] extensions;

    private CheckSuppertedQuarkusExtension() {
        this.extensions = get(Configuration.EXTENSIONS_ENDPOINT.get()).as(Extension[].class);
    }

    public static boolean isSupported(String extension) {
        return Stream.of(INSTANCE.extensions).anyMatch(isFound(extension).and(isTaggedAsSupported()));
    }

    private static Predicate<Extension> isTaggedAsSupported() {
        return extension -> extension.getTags().contains(SUPPORTED_TAG);
    }

    private static Predicate<Extension> isFound(String extensionName) {
        return extension -> extension.getId().endsWith(extensionName);
    }

}
