package com.github.maeda6uiui.mechtatel.core.util;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Utility methods related to URL
 *
 * @author maeda6uiui
 */
public class MttURLUtils {
    /**
     * Returns resource URL.
     *
     * @param filepath Filepath of the resource
     * @param external {@code false} if the resource is inside the JAR that this class belongs to
     * @return URL of the resource
     * @throws MalformedURLException If it fails to obtain URL from the path
     */
    public static URL getResourceURL(String filepath, boolean external) throws MalformedURLException {
        if (external) {
            return Paths.get(filepath).toUri().toURL();
        } else {
            return MttURLUtils.class.getResource(filepath);
        }
    }

    /**
     * Returns the URL of the resource.
     * Resource is extracted from a JAR to a temp file,
     * and the temp file is deleted when the JVM terminates.
     *
     * @param clazz    Class to call {@link Class#getResourceAsStream(String)}
     * @param filepath Filepath of the resource
     * @return URL of the resource
     * @throws IOException If it fails to load the resource
     */
    public static URL getResourceAsTempFile(Class<?> clazz, String filepath) throws IOException {
        try (var bis = new BufferedInputStream(Objects.requireNonNull(clazz.getResourceAsStream(filepath)))) {
            File tempFile = File.createTempFile("mtt", ".tmp");
            tempFile.deleteOnExit();

            try (var bos = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                var buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
            }

            return tempFile.toURI().toURL();
        }
    }
}
