package quarkus.extensions.combinator.maven;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import quarkus.extensions.combinator.utils.CommandBuilder;

public class MavenGetQuarkusExtensions extends MavenCommand {

    private final List<String> extensions = new ArrayList<>();

    public List<String> getExtensions() {
        extensions.clear();
        runMavenCommand("-f", "target/test-classes/list-extensions.pom.xml", "quarkus:list-extensions");
        return extensions;
    }

    @Override
    protected void configureCommand(CommandBuilder command) {
        command.outputToList(extensions);
    }
}
