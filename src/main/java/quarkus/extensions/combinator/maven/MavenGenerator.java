package quarkus.extensions.combinator.maven;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import quarkus.extensions.combinator.Configuration;
import quarkus.extensions.combinator.utils.CommandBuilder;
import quarkus.extensions.combinator.utils.FileUtils;

public class MavenGenerator extends MavenCommand {

    private static final Logger LOG = Logger.getLogger(MavenGenerator.class.getName());
    private static final String PROJECT_GROUP_ID = "projectGroupId";
    private static final String PROJECT_ARTIFACT_ID = "projectArtifactId";
    private static final String PROJECT_VERSION = "projectVersion";
    private static final String PLATFORM_ARTIFACT_ID = "platformArtifactId";
    private static final String PLATFORM_GROUP_ID = "platformGroupId";
    private static final String PROPERTIES_FORMAT = ".properties";
    private static final String JAVA_FOLDER = "src/main/java";
    private static final String RESOURCES_FOLDER = "src/main/resources";
    private static final String APPLICATION_PROPERTIES = RESOURCES_FOLDER + "/application" + PROPERTIES_FORMAT;
    private static final String OUTPUT_FOLDER = "target/";
    private static final String HIBERNATE_ORM_EXTENSION = "hibernate-orm";

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
                withPlatformGroupId(), withPlatformArtifactId(), withExtensions(), withoutQuarkusRegistryClient());
        updateApplicationProperties();
        dropEntityAnnotations();
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

    public void dropEntityAnnotations() {
        if (withExtensions().contains(HIBERNATE_ORM_EXTENSION)) {
            File srcMainJava = new File(projectAsWorkingDirectory(), JAVA_FOLDER);
            try {
                Files.walk(srcMainJava.toPath())
                        .filter(Files::isRegularFile)
                        .forEach(file -> adjustFileContent(file, "@Entity", ""));
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, ex.toString(), ex);
            }
        }
    }

    private void adjustFileContent(Path path, String regex, String replacement){
        if (Files.exists(path)) {
            String content = null;
            try {
                content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                content = content.replaceAll(regex, replacement);
                Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ex) {
                LOG.log( Level.SEVERE, ex.toString(), ex);
            }
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
