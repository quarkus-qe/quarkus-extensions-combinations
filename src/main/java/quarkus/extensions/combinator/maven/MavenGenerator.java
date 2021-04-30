package quarkus.extensions.combinator.maven;

import java.io.File;
import java.util.Optional;
import java.util.Set;

import quarkus.extensions.combinator.Configuration;
import quarkus.extensions.combinator.utils.CommandBuilder;
import quarkus.extensions.combinator.utils.FileUtils;

public class MavenGenerator extends MavenCommand {

    private static final String QUARKUS_PLUGIN = "io.quarkus:quarkus-maven-plugin:%s:create";
    private static final String PROJECT_GROUP_ID = "projectGroupId";
    private static final String PROJECT_ARTIFACT_ID = "projectArtifactId";
    private static final String PROJECT_VERSION = "projectVersion";
    private static final String PLATFORM_ARTIFACT_ID = "platformArtifactId";
    private static final String PROPERTIES_FORMAT = ".properties";
    private static final String APPLICATION_PROPERTIES = "src/main/resources/application" + PROPERTIES_FORMAT;
    private static final String OUTPUT_FOLDER = "target/";

    private static final String EXTENSIONS_PARAM = "extensions";

    private final Set<String> extensions;
    private final String artifactId;
    private final File output;

    private MavenGenerator(Set<String> extensions) {
        super(targetAsWorkingDirectory());
        this.artifactId = generateArtifactId(extensions);
        this.extensions = extensions;
        this.output = new File(OUTPUT_FOLDER + artifactId + ".log");
    }

    public MavenProject generate() {
        FileUtils.clearFileContent(this.output);
        FileUtils.deleteDirectory(projectAsWorkingDirectory());

        runMavenCommand(withQuarkusPlugin(), withProjectGroupId(), withProjectArtifactId(), withProjectVersion(),
                withPlatformArtifactId(), withExtensions());
        updateApplicationProperties();
        return new MavenProject(output, projectAsWorkingDirectory());
    }

    @Override
    protected void configureCommand(CommandBuilder command) {
        command.outputToFile(output);
    }

    private String withPlatformArtifactId() {
        return withProperty(PLATFORM_ARTIFACT_ID, Configuration.COMBINATION_PLATFORM_ARTIFACT_ID.get());
    }

    private String withProjectVersion() {
        return withProperty(PROJECT_VERSION, Configuration.COMBINATION_PROJECT_VERSION.get());
    }

    private String withProjectArtifactId() {
        return withProperty(PROJECT_ARTIFACT_ID, artifactId);
    }

    private String withProjectGroupId() {
        return withProperty(PROJECT_GROUP_ID, Configuration.COMBINATION_PROJECT_GROUP_ID.get());
    }

    private String withExtensions() {
        return withProperty(EXTENSIONS_PARAM, String.join(",", extensions));
    }

    private String withQuarkusPlugin() {
        return String.format(QUARKUS_PLUGIN, Configuration.QUARKUS_VERSION.get());
    }

    private void updateApplicationProperties() {
        File applicationProperties = new File(projectAsWorkingDirectory(), APPLICATION_PROPERTIES);

        for (String extension : extensions) {
            Optional.ofNullable(getClass().getClassLoader().getResourceAsStream(extension + PROPERTIES_FORMAT))
                    .ifPresent(customPropertiesByExtension -> FileUtils.appendInputStreamIntoFile(customPropertiesByExtension,
                            applicationProperties));
        }
    }

    private File projectAsWorkingDirectory() {
        return new File("target/" + artifactId);
    }

    private static File targetAsWorkingDirectory() {
        return new File("target");
    }

    public static final MavenGenerator withExtensions(Set<String> extensions) {
        return new MavenGenerator(extensions);
    }

    private static final String generateArtifactId(Set<String> extensions) {
        return String.join("-", extensions);
    }
}
