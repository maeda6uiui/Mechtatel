package com.github.maeda6uiui.mechtatel.core.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

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
}
