package quarkus.extensions.combinator.maven;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.File;
import java.util.Set;

import quarkus.extensions.combinator.utils.CommandBuilder;
import quarkus.extensions.combinator.utils.FileUtils;

public class MavenProject extends MavenCommand {

    private static final String PACKAGE = "package";
    private static final String VERIFY = "verify";
    private static final String DEV_MODE = "quarkus:dev";
    private static final String NATIVE_MODE = "-Dnative";
    private static final String CLEAN = "clean";
    private static final String SKIP_TESTS = "-DskipTests";
    private static final String SKIP_INTEGRATION_TESTS = "-DskipITs";
    private static final String RANDOM_PORT_FOR_TESTS = "-Dquarkus.http.test-port=0";
    private static final String RANDOM_PORT_FOR_TESTS_LAMBDA = "-Dquarkus.lambda.mock-event-server.test-port=0";
    private static final String RANDOM_PORT_FOR_RUNNING = "-Dquarkus.http.port=0";
    private static final String XMX_MEMORY_LIMIT = "-Dquarkus.native.native-image-xmx=4g";

    private final File output;
    private final Set<String> projectExtensions;

    private Process currentProcess;
    private boolean skipTests = false;

    protected MavenProject(File output, File workingDirectory, Set<String> extensions) {
        super(workingDirectory);
        this.projectExtensions = requireNonNull(extensions);
        this.output = output;
    }

    public MavenProject compile() {
        runMavenCommandAndWait(PACKAGE, SKIP_TESTS, SKIP_INTEGRATION_TESTS);
        return this;
    }

    public MavenProject verify() {

        if (isAmazonLambdaExtensionPresent()) {
            runMavenCommandAndWait(VERIFY, RANDOM_PORT_FOR_TESTS, RANDOM_PORT_FOR_TESTS_LAMBDA, optionalSkipTests(),
                    optionalSkipITs());
        } else {
            runMavenCommandAndWait(VERIFY, RANDOM_PORT_FOR_TESTS, optionalSkipTests(), optionalSkipITs());
        }
        return this;
    }

    public MavenProject devMode() {
        currentProcess = runMavenCommand(DEV_MODE, RANDOM_PORT_FOR_RUNNING, optionalSkipTests(),
                optionalSkipITs());
        return this;
    }

    public MavenProject nativeMode() {

        if (isAmazonLambdaExtensionPresent()) {
            runMavenCommandAndWait(CLEAN, VERIFY, NATIVE_MODE, XMX_MEMORY_LIMIT, RANDOM_PORT_FOR_TESTS,
                    RANDOM_PORT_FOR_TESTS_LAMBDA,
                    optionalSkipTests(), optionalSkipITs());
        } else {
            runMavenCommandAndWait(CLEAN, VERIFY, NATIVE_MODE, XMX_MEMORY_LIMIT, RANDOM_PORT_FOR_TESTS,
                    optionalSkipTests(), optionalSkipITs());
        }
        return this;
    }

    public MavenProject skipTests() {
        skipTests = true;
        return this;
    }

    public void delete() {
        FileUtils.deleteDirectory(getWorkingDirectory());
    }

    public Process getCurrentProcess() {
        return currentProcess;
    }

    public File getOutput() {
        return output;
    }

    @Override
    protected void configureCommand(CommandBuilder command) {
        command.outputToFile(output);
    }

    private String optionalSkipTests() {
        return skipTests ? SKIP_TESTS : EMPTY;
    }

    private String optionalSkipITs() {
        return skipTests ? SKIP_INTEGRATION_TESTS : EMPTY;
    }

    private boolean isAmazonLambdaExtensionPresent() {
        // special handling for Amazon Lambda as if quarkus.lambda.mock-event-server.test-port=0 is specified in project
        // without Amazon Lambda extension, an exception is thrown 'java.lang.IllegalArgumentException: Port must be greater than 0'
        for (String projectExtension : projectExtensions) {
            // we use 'contains' as we also need to match funqy-amazon-lambda
            if (projectExtension.contains("amazon-lambda")) {
                return true;
            }
        }
        return false;
    }
}
