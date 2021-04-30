package quarkus.extensions.combinator;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import quarkus.extensions.combinator.extensions.ExtensionsProvider;

@Disabled
public class CheckCombinationsTest {

    @Test
    public void test() {
        ExtensionsProvider extensions = new ExtensionsProvider();
        System.setProperty(Configuration.GROUP_OF.key(), "2");
        System.out.println("2: " + extensions.generateCombinations().size());

        System.setProperty(Configuration.GROUP_OF.key(), "3");
        System.out.println("3: " + extensions.generateCombinations().size());

        System.setProperty(Configuration.GROUP_OF.key(), "5");
        System.out.println("5: " + extensions.generateCombinations().size());

        System.setProperty(Configuration.GROUP_OF.key(), "1");
        List<Set<String>> combinations = extensions.generateCombinations();
        System.out.println("1: " + combinations.size());
        combinations.forEach(System.out::println);
    }
}
