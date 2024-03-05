package com.github.maeda6uiui.mechtatel.core.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Helper methods to load resources
 *
 * @author maeda6uiui
 */
public class MttResourceLoader {
    public static URL getResource(String name, boolean external) throws MalformedURLException {
        if (external) {
            return Paths.get(name).toUri().toURL();
        } else {
            return MttResourceLoader.class.getResource(name);
        }
    }
}
