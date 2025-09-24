package com.github.maeda6uiui.mechtatel.core.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utility methods related to URL
 *
 * @author maeda6uiui
 */
public class MttURLUtils {
    /**
     * Returns resource URL.
     * Pass {@code null} to {@code clazz} if this resource is located in a local directory (outside a JAR).
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
     * Pass {@code "local"} to {@code className} if this resource is located in a local directory (outside a JAR).
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

    /**
     * Searches in a directory with a path matcher and returns URLs of matched files.
     *
     * @param startDir Directory to start searching in
     * @param matcher  Path matcher
     * @return URLs of matched files
     * @throws IOException If it fails to enumerate paths or acquire its URL
     */
    public static List<URL> getMatchedResourceURLs(Path startDir, PathMatcher matcher) throws IOException {
        var targetPaths = new ArrayList<Path>();
        try (Stream<Path> stream = Files.walk(startDir)) {
            stream.filter(matcher::matches).forEach(targetPaths::add);
        }

        var ret = new ArrayList<URL>();
        for (var path : targetPaths) {
            URL url = path.toUri().toURL();
            ret.add(url);
        }

        return ret;
    }
}
