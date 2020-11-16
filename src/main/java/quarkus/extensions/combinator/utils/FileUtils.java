package quarkus.extensions.combinator.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public final class FileUtils {

    private static final boolean APPEND = true;

    private FileUtils() {

    }

    public static final void createIfDoesNotExist(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static void deleteDirectory(File folder) {
        if (folder.exists()) {
            try {
                org.apache.commons.io.FileUtils.forceDelete(folder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    public static void writeLine(File file, String line) {
        try {
            org.apache.commons.io.FileUtils.writeLines(file, Arrays.asList(line), APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void clearFileContent(File file) {
        if (file.exists()) {
            try {
                org.apache.commons.io.FileUtils.write(file, "", Charset.defaultCharset());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
