package quarkus.extensions.combinator.exclusions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A rule made of one or more extension names. A rule matches a combination when every entry of the rule is satisfied by at
 * least one extension of the combination. An entry can contain the wildcard '*', which matches any sequence of characters
 * (e.g. "spring-*").
 */
public class ExclusionRule {

    private static final String WILDCARD = "*";

    private final Set<String> exactExtensions = new HashSet<>();
    private final List<Pattern> wildcardExtensions = new ArrayList<>();

    public ExclusionRule(Iterable<String> entries) {
        for (String entry : entries) {
            String extension = entry.trim();
            if (extension.isEmpty()) {
                continue;
            }

            if (extension.contains(WILDCARD)) {
                wildcardExtensions.add(toPattern(extension));
            } else {
                exactExtensions.add(extension);
            }
        }
    }

    public boolean matches(Set<String> combination) {
        if (!combination.containsAll(exactExtensions)) {
            return false;
        }

        for (Pattern wildcard : wildcardExtensions) {
            if (combination.stream().noneMatch(extension -> wildcard.matcher(extension).matches())) {
                return false;
            }
        }

        return true;
    }

    private static Pattern toPattern(String glob) {
        StringBuilder regex = new StringBuilder();
        String[] parts = glob.split("\\*", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                regex.append(".*");
            }

            if (!parts[i].isEmpty()) {
                regex.append(Pattern.quote(parts[i]));
            }
        }

        return Pattern.compile(regex.toString());
    }
}
