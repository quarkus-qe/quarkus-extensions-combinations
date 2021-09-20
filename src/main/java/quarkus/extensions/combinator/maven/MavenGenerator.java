package quarkus.extensions.combinator.maven;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import quarkus.extensions.combinator.Configuration;
import quarkus.extensions.combinator.utils.CommandBuilder;
import quarkus.extensions.combinator.utils.FileUtils;

public class MavenGenerator extends MavenCommand {

    private static final String PROJECT_GROUP_ID = "projectGroupId";
    private static final String PROJECT_ARTIFACT_ID = "projectArtifactId";
    private static final String PROJECT_VERSION = "projectVersion";
    private static final String PLATFORM_ARTIFACT_ID = "platformArtifactId";
    private static final String PLATFORM_GROUP_ID = "platformGroupId";
    private static final String PROPERTIES_FORMAT = ".properties";
    private static final String RESOURCES_FOLDER = "src/main/resources";
    private static final String APPLICATION_PROPERTIES = RESOURCES_FOLDER + "/application" + PROPERTIES_FORMAT;
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

        runMavenCommandAndWait(withQuarkusPluginCreate(), withProjectGroupId(), withProjectArtifactId(), withProjectVersion(),
                withPlatformGroupId(), withPlatformArtifactId(), withExtensions());
        updateApplicationProperties();
        copyResources();
        return new MavenProject(output, projectAsWorkingDirectory());
    }

    @Override
    protected void configureCommand(CommandBuilder command) {
        command.outputToFile(output);
    }

    private String withPlatformGroupId() {
        return withProperty(PLATFORM_GROUP_ID, Configuration.COMBINATION_PLATFORM_GROUP_ID.get());
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

    private String withQuarkusPluginCreate() {
        return Configuration.QUARKUS_MAVEN_PLUGIN.get() + ":" + Configuration.QUARKUS_VERSION.get() + ":create";
    }

    private void updateApplicationProperties() {
        File applicationProperties = new File(projectAsWorkingDirectory(), APPLICATION_PROPERTIES);

        for (String extension : extensions) {
            Optional.ofNullable(getClass().getClassLoader().getResourceAsStream(extension + PROPERTIES_FORMAT))
                    .ifPresent(customPropertiesByExtension -> FileUtils.appendInputStreamIntoFile(customPropertiesByExtension,
                            applicationProperties));
        }
    }

    private void copyResources() {
        File targetResourcesFolder = new File(projectAsWorkingDirectory(), RESOURCES_FOLDER);

        for (String extension : extensions) {
            File requiredExternalResourcesFolder = new File(RESOURCES_FOLDER + "/" + extension);
            if (requiredExternalResourcesFolder.exists() && requiredExternalResourcesFolder.isDirectory()) {
                Arrays.stream(requiredExternalResourcesFolder.listFiles())
                        .forEach(file -> FileUtils.copyFileTo(file, targetResourcesFolder));
            }
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
