package quarkus.extensions.combinator.maven;

import java.io.File;

import quarkus.extensions.combinator.utils.CommandBuilder;
import quarkus.extensions.combinator.utils.FileUtils;

public class MavenProject extends MavenCommand {

    private static final String PACKAGE = "package";
    private static final String VERIFY = "verify";
    private static final String CLEAN = "clean";
    private static final String SKIP_TESTS = "-DskipTests";
    private static final String SKIP_INTEGRATION_TESTS = "-DskipITs";
    private static final String RANDOM_PORT_FOR_TESTS = "-Dquarkus.http.test-port=0";

    private final File output;

    protected MavenProject(File output, File workingDirectory) {
        super(workingDirectory);

        this.output = output;
    }

    public MavenProject compile() {
        runMavenCommand(PACKAGE, SKIP_TESTS, SKIP_INTEGRATION_TESTS);
        return this;
    }

    public MavenProject verify() {
        runMavenCommand(VERIFY, RANDOM_PORT_FOR_TESTS);
        return this;
    }

    public MavenProject clean() {
        runMavenCommand(CLEAN);
        return this;
    }

    public void delete() {
        FileUtils.deleteDirectory(getWorkingDirectory());
    }

    @Override
    protected void configureCommand(CommandBuilder command) {
        command.outputToFile(output);
    }
}
