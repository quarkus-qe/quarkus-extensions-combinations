package quarkus.extensions.combinator.maven;

import java.util.ArrayList;
import java.util.List;

import quarkus.extensions.combinator.Configuration;
import quarkus.extensions.combinator.utils.CommandBuilder;

public class MavenGetQuarkusExtensions extends MavenCommand {

    private final List<String> extensions = new ArrayList<>();

    public List<String> getExtensions() {
        extensions.clear();
        String quarkusMavenPlugin = getQuarkusMavenPlugin();
        runMavenCommandAndWait("-f", "target/test-classes/list-extensions.pom.xml", quarkusMavenPlugin + ":list-extensions",
                "-Dformat=id");
        return extensions;
    }

    private String getQuarkusMavenPlugin() {
        return Configuration.QUARKUS_MAVEN_PLUGIN.get() + ":" + Configuration.QUARKUS_VERSION.get();
    }

    @Override
    protected void configureCommand(CommandBuilder command) {
        command.outputToList(extensions);
    }
}
