package quarkus.extensions.combinator.exclusions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import quarkus.extensions.combinator.utils.OsUtils;

public class ExclusionsManager {

    private static final ExclusionsManager EXCLUSIONS_MANAGER = new ExclusionsManager();

    private static final Logger LOG = Logger.getLogger(ExclusionsManager.class.getName());
    private static final String EXCLUSIONS_FILE = "exclusions.properties";
    private static final String SKIP_ALL_VALUE = "skip-all";
    private static final String SKIP_DEV_MODE_VALUE = "skip-dev";
    private static final String SKIP_NATIVE_MODE_VALUE = "skip-native";
    private static final String SKIP_TESTS_VALUE = "skip-tests";
    private static final String SKIP_TESTS_ON_WINDOWS = "skip-only-tests-on-windows";

    private final List<ExclusionRule> skipCombinations = new ArrayList<>();
    private final List<ExclusionRule> skipDevModeCombinations = new ArrayList<>();
    private final List<ExclusionRule> skipNativeModeCombinations = new ArrayList<>();
    private final List<ExclusionRule> skipTestsCombinations = new ArrayList<>();
    private final List<ExclusionRule> skipTestsOnWindowsCombinations = new ArrayList<>();

    private ExclusionsManager() {
        initializeExclusions();
    }

    public boolean isNotExcluded(Set<String> combination) {
        return skipCombinations.isEmpty()
                || !skipCombinations.stream().anyMatch(excluded -> excluded.matches(combination));
    }

    public boolean isDevModeDisabled(Set<String> combination) {
        return !skipDevModeCombinations.isEmpty()
                && skipDevModeCombinations.stream().anyMatch(excluded -> excluded.matches(combination));
    }

    public boolean isNativeModeDisabled(Set<String> combination) {
        return !skipNativeModeCombinations.isEmpty()
                && skipNativeModeCombinations.stream().anyMatch(excluded -> excluded.matches(combination));
    }

    public boolean isTestsDisabled(Set<String> combination) {
        return !skipTestsCombinations.isEmpty()
                && skipTestsCombinations.stream().anyMatch(excluded -> excluded.matches(combination));
    }

    public boolean isTestsDisabledOnWindows(Set<String> combination) {
        return !skipTestsOnWindowsCombinations.isEmpty()
                && OsUtils.isWindows()
                && skipTestsOnWindowsCombinations.stream().anyMatch(excluded -> excluded.matches(combination));
    }

    private final List<Set<String>> initializeExclusions() {
        List<Set<String>> exclusions = new ArrayList<>();

        try (InputStream input = ExclusionsManager.class.getClassLoader().getResourceAsStream(EXCLUSIONS_FILE)) {

            Properties prop = new Properties();
            prop.load(input);
            for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                String rule = (String) entry.getValue();
                ExclusionRule exclusionRule = new ExclusionRule(Arrays.asList(((String) entry.getKey()).split(",")));
                if (SKIP_ALL_VALUE.equalsIgnoreCase(rule)) {
                    skipCombinations.add(exclusionRule);
                } else if (SKIP_DEV_MODE_VALUE.equalsIgnoreCase(rule)) {
                    skipDevModeCombinations.add(exclusionRule);
                } else if (SKIP_NATIVE_MODE_VALUE.equalsIgnoreCase(rule)) {
                    skipNativeModeCombinations.add(exclusionRule);
                } else if (SKIP_TESTS_VALUE.equalsIgnoreCase(rule)) {
                    skipTestsCombinations.add(exclusionRule);
                } else if (SKIP_TESTS_ON_WINDOWS.equalsIgnoreCase(rule)) {
                    skipTestsOnWindowsCombinations.add(exclusionRule);
                }
            }

        } catch (Exception ex) {
            LOG.warning("Failed to load exclusions. No exclusions will be used. Cause: " + ex.getMessage());
        }

        return exclusions;
    }

    public static final ExclusionsManager get() {
        return EXCLUSIONS_MANAGER;
    }
}
