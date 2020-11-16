package quarkus.extensions.combinator;

import java.util.Set;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import quarkus.extensions.combinator.extensions.ExtensionsProvider;
import quarkus.extensions.combinator.extensions.RecordFailedScenariosTestQuarkusCombinationExtension;
import quarkus.extensions.combinator.maven.MavenGenerator;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(RecordFailedScenariosTestQuarkusCombinationExtension.class)
class QuarkusExtensionsCombinationTest {

    @ParameterizedTest(name = "#{index} - With {0}")
    @ArgumentsSource(ExtensionsProvider.class)
    void testExtensions(Set<String> extensions) {
        MavenGenerator.withExtensions(extensions)
                .generate()
                .compile()
                .verify()
                .delete();
    }
}
