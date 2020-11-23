package quarkus.extensions.combinator.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class CommandBuilder {

    private static final Logger LOG = Logger.getLogger(CommandBuilder.class.getName());

    private final String description;
    private final List<String> command;

    private File workingDirectory;
    private BiConsumer<String, InputStream> outputConsumer = logOutput();

    public CommandBuilder(String... command) {
        this(Arrays.asList(command));
    }

    public CommandBuilder(List<String> command) {
        this.description = descriptionOfProgram(command.get(0));
        this.command = command;
    }

    public CommandBuilder outputToFile(File file) {
        outputConsumer = fileOutput(file);
        return this;
    }

    public CommandBuilder outputToLog() {
        outputConsumer = logOutput();
        return this;
    }

    public CommandBuilder outputToList(List<String> list) {
        Objects.requireNonNull(list);
        outputConsumer = linesOutput(list);
        return this;
    }

    public CommandBuilder workingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    private static String descriptionOfProgram(String program) {
        if (program.contains(File.separator)) {
            return program.substring(program.lastIndexOf(File.separator) + 1);
        }
        return program;
    }

    public void runAndWait() {
        try {
            LOG.info("Running: " + String.join(" ", command));
            ProcessBuilder pb = new ProcessBuilder()
                    .redirectErrorStream(true)
                    .command(command);

            Optional.ofNullable(workingDirectory).ifPresent(pb::directory);

            Process process = pb.start();

            outputConsumer.accept(description, process.getInputStream());

            int result = process.waitFor();
            if (result != 0) {
                throw new RuntimeException(description + " failed (executed " + command + ", return code " + result + ")");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final BiConsumer<String, InputStream> fileOutput(File targetFile) {
        return (description, is) -> {
            try (OutputStream outStream = new FileOutputStream(targetFile, true)) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException ignored) {
            }
        };
    }

    private static final BiConsumer<String, InputStream> logOutput() {
        return (description, is) -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    LOG.info(line);
                }
            } catch (IOException ignored) {
            }
        };
    }

    private static final BiConsumer<String, InputStream> linesOutput(List<String> list) {
        return (description, is) -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    list.add(line);
                }
            } catch (IOException ignored) {
            }
        };
    }
}