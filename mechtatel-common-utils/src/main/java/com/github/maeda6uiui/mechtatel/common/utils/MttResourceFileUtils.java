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
     * @param clazz    Class to call {@link Class#getResourceAsStream(String)}
     * @param filepath Filepath of the file
     * @return Temporary file representing the extracted file
     * @throws IOException If it fails to extract the file
     */
    public static File extractFile(Class<?> clazz, String filepath) throws IOException {
        try (var bis = new BufferedInputStream(Objects.requireNonNull(clazz.getResourceAsStream(filepath)))) {
            File tempFile = File.createTempFile("mtt", ".tmp");
            tempFile.deleteOnExit();

            try (var bos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                bis.transferTo(bos);
            }

            return tempFile;
        }
    }

    /**
     * Loads a native library contained in a JAR.
     *
     * @param clazz    Class to call {@link Class#getResourceAsStream(String)}
     * @param filepath Filepath of the native library
     * @throws IOException If it fails to extract the file
     */
    public static void loadNativeLib(Class<?> clazz, String filepath) throws IOException {
        File tempFile = extractFile(clazz, filepath);
        System.load(tempFile.getAbsolutePath());
    }
}
