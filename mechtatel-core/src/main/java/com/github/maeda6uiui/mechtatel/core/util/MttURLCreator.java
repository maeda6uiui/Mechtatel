package com.github.maeda6uiui.mechtatel.core.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Helper methods to create URL
 *
 * @author maeda6uiui
 */
public class MttURLCreator {
    public static URL getResourceURL(String name, boolean external) throws MalformedURLException {
        if (external) {
            return Paths.get(name).toUri().toURL();
        } else {
            return MttURLCreator.class.getResource(name);
        }
    }
}
