package quarkus.extensions.combinator.maven;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import quarkus.extensions.combinator.utils.CommandBuilder;
import quarkus.extensions.combinator.utils.OsUtils;

public abstract class MavenCommand {

    private static final String MAVEN = "mvn";
    private static final String MAVEN_WINDOWS = "mvn.cmd";
    private static final String MAVEN_REPO_LOCAL = "maven.repo.local";
    private static final String QUARKUS_PLUGIN_VERSION = "quarkus-plugin.version";
    private static final String QUARKUS_PLATFORM_VERSION = "quarkus.platform.version";
    private static final String QUARKUS_PLATFORM_GROUP_ID = "quarkus.platform.group-id";
    private static final String QUARKUS_PLATFORM_ARTIFACT_ID = "quarkus.platform.artifact-id";
    private static final List<String> PROPERTIES_TO_PROPAGATE = Arrays.asList(MAVEN_REPO_LOCAL, QUARKUS_PLUGIN_VERSION,
            QUARKUS_PLATFORM_VERSION, QUARKUS_PLATFORM_GROUP_ID, QUARKUS_PLATFORM_ARTIFACT_ID);
    private static final String QUARKUS_REGISTRY_CLIENT = "quarkusRegistryClient";

    private final File workingDirectory;

    protected MavenCommand() {
        this(Paths.get(".").toFile());
    }

    protected MavenCommand(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    protected File getWorkingDirectory() {
        return workingDirectory;
    }

    protected void configureCommand(CommandBuilder command) {

    }

    protected Process runMavenCommand(String... params) {
        return configureMavenCommand(params).run();
    }

    protected void runMavenCommandAndWait(String... params) {
        configureMavenCommand(params).runAndWait();
    }

    protected String withProperty(String property, String value) {
        return String.format("-D%s=%s", property, value);
    }

    protected String withoutQuarkusRegistryClient() {
        return withProperty(QUARKUS_REGISTRY_CLIENT, "false");
    }

    private CommandBuilder configureMavenCommand(String[] params) {
        List<String> arguments = new ArrayList<>();
        addMavenCommand(arguments);
        propagateProperties(arguments);
        Stream.of(params).filter(StringUtils::isNotEmpty).forEach(arguments::add);
        CommandBuilder command = new CommandBuilder(arguments).workingDirectory(workingDirectory);
        configureCommand(command);
        return command;
    }

    private void propagateProperties(List<String> arguments) {
        for (String property : PROPERTIES_TO_PROPAGATE) {
            String value = System.getProperty(property);
            if (StringUtils.isNotEmpty(value)) {
                arguments.add(withProperty(property, value));
            }
        }
    }

    private void addMavenCommand(List<String> arguments) {
        if (OsUtils.isWindows()) {
            arguments.add(MAVEN_WINDOWS);
        } else {
            arguments.add(MAVEN);
        }
    }

}
