package com.github.maeda6uiui.mechtatel.common.utils;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
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
     * @param tempFilePrefix Prefix for the temporary file
     * @param deleteOnExit   Deletes the temporary file on exit if true
     * @return Temporary file representing the extracted file
     * @throws IOException If it fails to extract the file
     */
    public static File extractFile(
            Class<?> clazz, String filepath, String tempFilePrefix, boolean deleteOnExit) throws IOException {
        try (var bis = new BufferedInputStream(Objects.requireNonNull(clazz.getResourceAsStream(filepath)))) {
            File tempFile = File.createTempFile(tempFilePrefix, ".tmp");
            if (deleteOnExit) {
                tempFile.deleteOnExit();
            }

            try (var bos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                bis.transferTo(bos);
            }

            return tempFile;
        }
    }

    /**
     * Extracts a file from a JAR and writes it out to a temporary file.
     *
     * @param clazz    Class to call {@link Class#getResourceAsStream(String)}
     * @param filepath Filepath of the file
     * @return Temporary file representing the extracted file
     * @throws IOException If it fails to extract the file
     */
    public static File extractFile(Class<?> clazz, String filepath) throws IOException {
        return extractFile(clazz, filepath, "mtt", true);
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
     * This method first extracts the native library from inside a JAR to a temporary file,
     * and then loads it with {@link System#load(String)}.
     * The temporary file is created under the OS-dependent temp directory.
     * Note that delete-on-exit method does not work on Windows
     * because it seems that the DLL file is locked even at the moment of JVM shutdown.
     *
     * @param clazz    Class to call {@link Class#getResourceAsStream(String)}
     * @param filepath Filepath of the native library
     * @throws IOException If it fails to extract the file
     */
    public static void loadNativeLib(
            Class<?> clazz, String filepath, String tempFilePrefix, boolean deleteOnExit) throws IOException {
        File tempFile = extractFile(clazz, filepath, tempFilePrefix, deleteOnExit);
        System.load(tempFile.getAbsolutePath());
    }

    /**
     * Loads a native library contained in a JAR.
     * This method first extracts the native library from inside a JAR to a temporary file,
     * and then loads it with {@link System#load(String)}.
     * This method does not delete the temporary file because {@link File#deleteOnExit()} does not work
     * on Windows probably because Windows still locks the DLL file even at the moment of JVM shutdown.
     * The temporary file is created with a prefix of "mttnatives" under the OS-dependent temp directory.
     *
     * @param clazz    Class to call {@link Class#getResourceAsStream(String)}
     * @param filepath Filepath of the native library
     * @throws IOException If it fails to extract the file
     */
    @Deprecated
    public static void loadNativeLib(Class<?> clazz, String filepath) throws IOException {
        loadNativeLib(clazz, filepath, "mttnatives", false);
    }

    /**
     * Deletes temporary files that have a prefix specified.
     *
     * @param prefix            Prefix for the temporary files
     * @param deleteDirectories Deletes directories as well if true
     * @throws IOException If it fails to enumerate files or to delete them
     */
    public static void deleteTemporaryFiles(String prefix, boolean deleteDirectories) throws IOException {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("Prefix cannot be null, empty or blank");
        }

        Path tempRoot = Paths.get(System.getProperty("java.io.tmpdir"));

        var tempFilePaths = new ArrayList<Path>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempRoot, prefix + "*")) {
            stream.forEach(tempFilePaths::add);
        }

        for (var path : tempFilePaths) {
            if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) && deleteDirectories) {
                FileUtils.deleteDirectory(path.toFile());
            } else if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                Files.delete(path);
            }
        }
    }
}
