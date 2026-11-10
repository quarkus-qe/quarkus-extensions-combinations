package quarkus.extensions.combinator.exclusions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class ExclusionRuleTest {

    private static final ExclusionRule SPRING_WITH_CAMEL = new ExclusionRule(Arrays.asList("spring-*", "camel-quarkus-*"));

    @Test
    public void exactNamesRequireAllEntries() {
        ExclusionRule rule = new ExclusionRule(Arrays.asList("rest", "resteasy"));
        assertTrue(rule.matches(combination("rest", "resteasy", "oidc")));
        assertFalse(rule.matches(combination("rest", "oidc")));
    }

    @Test
    public void exactNameDoesNotMatchPrefix() {
        assertFalse(new ExclusionRule(Arrays.asList("rest")).matches(combination("resteasy", "oidc")));
    }

    @Test
    public void wildcardsMatchWhenBothFamiliesArePresent() {
        assertTrue(SPRING_WITH_CAMEL.matches(combination("spring-di", "camel-quarkus-core", "oidc")));
    }

    @Test
    public void wildcardsDoNotMatchWhenOnlyOneFamilyIsPresent() {
        assertFalse(SPRING_WITH_CAMEL.matches(combination("spring-di", "spring-web", "oidc")));
        assertFalse(SPRING_WITH_CAMEL.matches(combination("camel-quarkus-core", "camel-quarkus-jpa", "oidc")));
    }

    @Test
    public void wildcardCanBeAtAnyPosition() {
        assertTrue(new ExclusionRule(Arrays.asList("*-jackson")).matches(combination("rest-jackson", "oidc")));
        assertTrue(new ExclusionRule(Arrays.asList("rest-*-oidc")).matches(combination("rest-client-oidc", "arc")));
        assertTrue(new ExclusionRule(Arrays.asList("*")).matches(combination("arc")));
    }

    @Test
    public void regexCharactersAreTreatedLiterally() {
        assertFalse(new ExclusionRule(Arrays.asList("a.c-*")).matches(combination("abc-core")));
        assertTrue(new ExclusionRule(Arrays.asList("a.c-*")).matches(combination("a.c-core")));
    }

    @Test
    public void wildcardWithDashDoesNotMatchBareName() {
        ExclusionRule rule = new ExclusionRule(Arrays.asList("rest-*"));
        assertTrue(rule.matches(combination("rest-jackson")));
        assertFalse(rule.matches(combination("rest")));
        assertFalse(rule.matches(combination("resteasy-jackson")));
        assertFalse(rule.matches(combination("elasticsearch-rest-client")));
    }

    @Test
    public void restAndResteasyRulesCoverAllPairs() {
        ExclusionRule[] rules = {
                new ExclusionRule(Arrays.asList("rest", "resteasy")),
                new ExclusionRule(Arrays.asList("rest-*", "resteasy")),
                new ExclusionRule(Arrays.asList("rest", "resteasy-*")),
                new ExclusionRule(Arrays.asList("rest-*", "resteasy-*")) };
        assertTrue(Arrays.stream(rules).anyMatch(r -> r.matches(combination("rest", "resteasy"))));
        assertTrue(Arrays.stream(rules).anyMatch(r -> r.matches(combination("rest-jackson", "resteasy"))));
        assertTrue(Arrays.stream(rules).anyMatch(r -> r.matches(combination("rest", "resteasy-jackson"))));
        assertTrue(Arrays.stream(rules).anyMatch(r -> r.matches(combination("rest-client", "resteasy-client"))));
        assertFalse(Arrays.stream(rules).anyMatch(r -> r.matches(combination("rest-jackson", "oidc"))));
        assertFalse(Arrays.stream(rules).anyMatch(r -> r.matches(combination("resteasy-jackson", "oidc"))));
    }

    private static Set<String> combination(String... extensions) {
        return new HashSet<>(Arrays.asList(extensions));
    }
}
