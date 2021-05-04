package quarkus.extensions.combinator.maven;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import quarkus.extensions.combinator.utils.CommandBuilder;
import quarkus.extensions.combinator.utils.OsUtils;

public abstract class MavenCommand {

    private static final String MAVEN = "mvn";
    private static final String MAVEN_WINDOWS = "mvn.cmd";

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

    private CommandBuilder configureMavenCommand(String[] params) {
        List<String> arguments = new ArrayList<>();
        addMavenCommand(arguments);
        Stream.of(params).filter(StringUtils::isNotEmpty).forEach(arguments::add);
        CommandBuilder command = new CommandBuilder(arguments).workingDirectory(workingDirectory);
        configureCommand(command);
        return command;
    }

    private void addMavenCommand(List<String> arguments) {
        if (OsUtils.isWindows()) {
            arguments.add(MAVEN_WINDOWS);
        } else {
            arguments.add(MAVEN);
        }
    }

}
