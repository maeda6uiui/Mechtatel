package com.github.maeda6uiui.mechtatel.audio.natives;

import java.io.*;
import java.util.Objects;

/**
 * Utility methods for native extractor
 *
 * @author maeda6uiui
 */
@Deprecated
public class NativeExtractorUtils {
    /**
     * Extracts a native library contained in a JAR file.
     *
     * @param clazz    Class to call {@link Class#getResourceAsStream(String)}
     * @param filepath Filepath of the native library
     * @return Temporary file representing the native library
     * @throws IOException If it fails to extract the native library
     */
    public static File extractNativeLibFromJar(Class<? extends INativeExtractor> clazz, String filepath) throws IOException {
        try (var bis = new BufferedInputStream(Objects.requireNonNull(clazz.getResourceAsStream(filepath)))) {
            File tempFile = File.createTempFile("lib", ".tmp");
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
