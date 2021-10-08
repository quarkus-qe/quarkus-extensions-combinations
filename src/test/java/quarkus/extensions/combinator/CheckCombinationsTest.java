package quarkus.extensions.combinator;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import quarkus.extensions.combinator.extensions.ExtensionsProvider;

public class CheckCombinationsTest {

    @Test
    public void test() {
        ExtensionsProvider extensions = new ExtensionsProvider();
        System.out.println("1: " + extensions.generateCombinations().size());
        List<Set<String>> combinations = extensions.generateCombinations();
        System.out.println("2: " + combinations.size());
        combinations.forEach(System.out::println);
    }
}
