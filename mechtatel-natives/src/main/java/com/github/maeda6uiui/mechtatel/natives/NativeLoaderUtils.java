package com.github.maeda6uiui.mechtatel.natives;

import java.io.*;
import java.util.Objects;

/**
 * Utility methods for native loader
 *
 * @author maeda6uiui
 */
public class NativeLoaderUtils {
    /**
     * Loads a native library contained in a JAR file.
     *
     * @param clazz    Class to call {@link Class#getResourceAsStream(String)}
     * @param filepath Filepath of the native library
     * @throws IOException If it fails to load the native library
     */
    public static void loadNativeLibFromJar(Class<? extends IMttNativeLoader> clazz, String filepath) throws IOException {
        try (var bis = new BufferedInputStream(Objects.requireNonNull(clazz.getResourceAsStream(filepath)))) {
            File tempFile = File.createTempFile("lib", ".mttlib");
            tempFile.deleteOnExit();

            try (var bos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                var buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
            }

            System.load(tempFile.getAbsolutePath());
        }
    }
}
