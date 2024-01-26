package quarkus.extensions.combinator.maven;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import quarkus.extensions.combinator.Configuration;
import quarkus.extensions.combinator.utils.CommandBuilder;

public class MavenGetQuarkusExtensions extends MavenCommand {

    private static final Logger LOG = Logger.getLogger(MavenGetQuarkusExtensions.class.getName());
    private final List<String> extensions = new ArrayList<>();

    public List<String> getExtensions() {
        extensions.clear();
        String quarkusMavenPlugin = getQuarkusMavenPlugin();
        withRetry(() -> runMavenCommandAndWait("-f", "target/test-classes/list-extensions.pom.xml",
                quarkusMavenPlugin + ":list-extensions", "-Dformat=id"), 3);
        return extensions;
    }

    private void withRetry(Runnable cmd, int retryCount) {
        int i = 1;
        while (retryCount >= i++) {
            try {
                cmd.run();
            } catch (RuntimeException e) {
                LOG.severe("Output of failed Maven command: " + Arrays.toString(extensions.toArray()));
                if (retryCount < i) {
                    throw e;
                }
                extensions.clear();
                continue;
            }
            break;
        }
    }

    private String getQuarkusMavenPlugin() {
        return Configuration.QUARKUS_MAVEN_PLUGIN.get() + ":" + Configuration.QUARKUS_VERSION.get();
    }

    @Override
    protected void configureCommand(CommandBuilder command) {
        command.outputToList(extensions);
    }
}
