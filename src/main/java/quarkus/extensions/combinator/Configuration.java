package quarkus.extensions.combinator;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Configuration {
    ONLY_SUPPORTED_EXTENSIONS("ts.only-supported-extensions", "true"),
    INCLUDES_COMBINATIONS_ONLY_WITH_EXTENSIONS("ts.includes-combinations-only-with-extensions", ""),
    LIMIT_EXTENSIONS("ts.limit-extensions", "-1"),
    RANDOM_SORT_EXTENSIONS("ts.random-sort-extensions", "true"),
    GROUP_OF("ts.extensions-in-groups-of", "3"),
    EXTENSIONS_ENDPOINT("ts.quarkus-extensions-endpoint", "https://code.quarkus.redhat.com/api/extensions"),
    COMBINATION_PROJECT_GROUP_ID("ts.combination-project-group-id", "io.quarkus.qe"),
    COMBINATION_PROJECT_VERSION("ts.combination-project-version", "1.0.0-SNAPSHOT"),
    COMBINATION_PLATFORM_ARTIFACT_ID("quarkus.platform.artifact-id", "quarkus-bom"),
    QUARKUS_VERSION("quarkus.version", "999-SNAPSHOT");

    private final String key;
    private final String defaultValue;

    Configuration(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String key() {
        return key;
    }

    public String get() {
        return System.getProperty(key, defaultValue);
    }

    public List<String> getAsList() {
        if (get().isEmpty()) {
            return Collections.emptyList();
        }

        return Stream.of(get().split(",")).map(String::trim).collect(Collectors.toList());
    }

    public Integer getAsInteger() {
        return Integer.valueOf(get());
    }

    public Boolean getAsBoolean() {
        return Boolean.valueOf(get());
    }
}
