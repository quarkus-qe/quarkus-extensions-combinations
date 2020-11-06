package quarkus.extensions.combinator.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import quarkus.extensions.combinator.utils.CommandBuilder;

public abstract class MavenCommand {

    private static final String MAVEN = "mvn";
    private static final String OUTPUT_FOLDER = "target/";

    private final String artifactId;
    private final File workingDirectory;
    private final File output;

    protected MavenCommand(String artifactId, File workingDirectory) {
        this.artifactId = artifactId;
        this.workingDirectory = workingDirectory;
        this.output = new File(OUTPUT_FOLDER + artifactId + ".log");
    }

    protected File getOutput() {
        return output;
    }

    protected File getWorkingDirectory() {
        return workingDirectory;
    }

    protected String getArtifactId() {
        return artifactId;
    }

    protected void runMavenCommand(String... params) {
        List<String> arguments = new ArrayList<>();
        arguments.add(MAVEN);
        arguments.addAll(Arrays.asList(params));
        new CommandBuilder(arguments)
                .workingDirectory(workingDirectory)
                .outputToFile(this.output)
                .runAndWait();
    }

    protected String withProperty(String property, String value) {
        return String.format("-D%s=%s", property, value);
    }

}
