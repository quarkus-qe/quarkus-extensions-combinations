package quarkus.extensions.combinator.maven;

import java.util.ArrayList;
import java.util.List;

import quarkus.extensions.combinator.utils.CommandBuilder;

public class MavenGetQuarkusExtensions extends MavenCommand {

    private final List<String> extensions = new ArrayList<>();

    public List<String> getExtensions() {
        extensions.clear();
        runMavenCommandAndWait("-f", "target/test-classes/list-extensions.pom.xml", "quarkus:list-extensions");
        return extensions;
    }

    @Override
    protected void configureCommand(CommandBuilder command) {
        command.outputToList(extensions);
    }
}
