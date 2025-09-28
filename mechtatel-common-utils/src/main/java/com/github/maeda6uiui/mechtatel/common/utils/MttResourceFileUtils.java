package com.github.maeda6uiui.mechtatel.common.utils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
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
     * Extracts a file from a JAR into a directory specified by the argument.
     * This method keeps the original filename of the extracted file in contrast to {@link #extractFile(Class, String)}.
     * The caller is responsible for cleaning up the extracted file.
     *
     * @param clazz     Class to call {@link Class#getResourceAsStream(String)}
     * @param filepath  Filepath of the file
     * @param targetDir Target directory to extract the file into
     * @return File representing the extracted file
     * @throws IOException If it fails to extract the file
     */
    public static File extractFileIntoDir(Class<?> clazz, String filepath, Path targetDir) throws IOException {
        Path file = Paths.get(filepath);
        Path filename = file.getFileName();
        Path targetPath = targetDir.resolve(filename);

        try (var bis = new BufferedInputStream(Objects.requireNonNull(clazz.getResourceAsStream(filepath)))) {
            try (var bos = new BufferedOutputStream(new FileOutputStream(targetPath.toFile()))) {
                bis.transferTo(bos);
            }
        }

        return targetPath.toFile();
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
