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
     * Pass {@code null} to {@code clazz} if this resource is located on a local directory (outside a JAR).
     *
     * @param filepath Filepath of the resource
     * @param clazz    The class that this resource belongs to
     * @return URL of the resource
     * @throws MalformedURLException If it fails to obtain URL from the path
     */
    public static URL getResourceURL(String filepath, Class<?> clazz) throws MalformedURLException {
        if (clazz != null) {
            return clazz.getResource(filepath);
        } else {
            return Paths.get(filepath).toUri().toURL();
        }
    }

    /**
     * Returns resource URL.
     * Pass {@code "local"} to {@code className} if this resource is located on a local directory (outside a JAR).
     *
     * @param filepath  Filepath of the resource
     * @param className The name of the class that this resource belongs to
     * @return URL of the resource
     * @throws MalformedURLException  If it fails to obtain URL from the path
     * @throws ClassNotFoundException If it fails to find a class from the name specified
     */
    public static URL getResourceURL(String filepath, String className)
            throws MalformedURLException, ClassNotFoundException {
        Class<?> clazz = null;
        if (!className.equals("local")) {
            clazz = Class.forName(className);
        }

        return getResourceURL(filepath, clazz);
    }

    /**
     * Returns resource URL.
     * This method throws {@link RuntimeException} if it fails to obtain the resource URL.
     *
     * @param filepath  Filepath of the resource
     * @param className The name of the class that this resource belongs to
     * @return URL of the resource
     */
    public static URL mustGetResourceURL(String filepath, String className) {
        try {
            return getResourceURL(filepath, className);
        } catch (MalformedURLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
