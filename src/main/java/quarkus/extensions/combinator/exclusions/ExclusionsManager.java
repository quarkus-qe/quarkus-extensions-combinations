package quarkus.extensions.combinator.exclusions;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import quarkus.extensions.combinator.utils.OsUtils;

public class ExclusionsManager {

    public static final ExclusionsManager EXCLUSIONS_MANAGER = new ExclusionsManager();

    private static final Logger LOG = Logger.getLogger(ExclusionsManager.class.getName());
    private static final String EXCLUSIONS_FILE = "exclusions.properties";
    private static final String SKIP_ALL_VALUE = "skip-all";
    private static final String SKIP_TESTS_VALUE = "skip-tests";
    private static final String SKIP_TESTS_ON_WINDOWS = "skip-only-tests-on-windows";

    private final List<Set<String>> skipCombinations = new ArrayList<>();
    private final List<Set<String>> skipTestsCombinations = new ArrayList<>();
    private final List<Set<String>> skipTestsOnWindowsCombinations = new ArrayList<>();

    private ExclusionsManager() {
        initializeExclusions();
    }

    public boolean isNotExcluded(Set<String> combination) {
        return skipCombinations.isEmpty()
                || !skipCombinations.stream().anyMatch(excluded -> combination.containsAll(excluded));
    }

    public boolean isTestsDisabled(Set<String> combination) {
        return !skipTestsCombinations.isEmpty()
                && skipTestsCombinations.stream().anyMatch(excluded -> combination.containsAll(excluded));
    }

    public boolean isTestsDisabledOnWindows(Set<String> combination) {
        return !skipTestsOnWindowsCombinations.isEmpty()
                && OsUtils.isWindows()
                && skipTestsOnWindowsCombinations.stream().anyMatch(excluded -> combination.containsAll(excluded));
    }

    private final List<Set<String>> initializeExclusions() {
        List<Set<String>> exclusions = new ArrayList<>();

        try (InputStream input = ExclusionsManager.class.getClassLoader().getResourceAsStream(EXCLUSIONS_FILE)) {

            Properties prop = new Properties();
            prop.load(input);
            for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                String rule = (String) entry.getValue();
                Set<String> combination = Stream.of(((String) entry.getKey()).split(",")).collect(Collectors.toSet());
                if (SKIP_ALL_VALUE.equalsIgnoreCase(rule)) {
                    skipCombinations.add(combination);
                } else if (SKIP_TESTS_VALUE.equalsIgnoreCase(rule)) {
                    skipTestsCombinations.add(combination);
                } else if (SKIP_TESTS_ON_WINDOWS.equalsIgnoreCase(rule)) {
                    skipTestsOnWindowsCombinations.add(combination);
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
