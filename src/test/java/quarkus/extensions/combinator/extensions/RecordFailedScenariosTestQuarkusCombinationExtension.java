package quarkus.extensions.combinator.extensions;

import java.io.File;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import quarkus.extensions.combinator.utils.FileUtils;

/**
 * Surefire does not report well the test names when using parallel execution:
 * https://issues.apache.org/jira/browse/SUREFIRE-1643
 * This extension is to know which combination has failed in Jenkins.
 *
 */
public class RecordFailedScenariosTestQuarkusCombinationExtension implements TestWatcher {

    private static final String FAILED_FILE = "target/failed.log";

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        FileUtils.appendLineIntoFile(context.getDisplayName(), new File(FAILED_FILE));
    }

}
