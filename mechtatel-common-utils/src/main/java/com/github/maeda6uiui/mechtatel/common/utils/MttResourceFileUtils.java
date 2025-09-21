package com.github.maeda6uiui.mechtatel.common.utils;

import java.io.*;
import java.util.Objects;

/**
 * Utility methods for resource files contained in a JAR
 *
 * @author maeda6uiui
 */
public class MttResourceFileUtils {
    /**
     * Extracts a file from a JAR and writes it out to a temporary file.
     *
     * @param clazz          Class to call {@link Class#getResourceAsStream(String)}
     * @param filepath       Filepath of the file
     * @param tempFilePrefix Prefix for the filename of the temporary file
     * @param tempFileSuffix Suffix for the filename of the temporary file
     * @return Temporary file representing the extracted file
     * @throws IOException If it fails to extract the file
     */
    public static File extractFile(
            Class<?> clazz, String filepath, String tempFilePrefix, String tempFileSuffix) throws IOException {
        try (var bis = new BufferedInputStream(Objects.requireNonNull(clazz.getResourceAsStream(filepath)))) {
            File tempFile = File.createTempFile(tempFilePrefix, tempFileSuffix);
            tempFile.deleteOnExit();

            try (var bos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                var buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
            }

            return tempFile;
        }
    }
}
