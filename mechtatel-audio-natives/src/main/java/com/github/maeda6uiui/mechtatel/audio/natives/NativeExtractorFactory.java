package com.github.maeda6uiui.mechtatel.audio.natives;

import java.lang.reflect.InvocationTargetException;

/**
 * Factory for native extractor
 *
 * @author maeda6uiui
 */
public class NativeExtractorFactory {
    private static final String NATIVE_EXTRACTOR_CLASS_NAME = "NativeExtractor";
    private static final String WINDOWS_PACKAGE_PATH = "com.github.maeda6uiui.mechtatel.audio.natives.windows";
    private static final String LINUX_PACKAGE_PATH = "com.github.maeda6uiui.mechtatel.audio.natives.linux";
    private static final String MACOS_PACKAGE_PATH = "com.github.maeda6uiui.mechtatel.audio.natives.macos";

    public static INativeExtractor createNativeExtractor(String platform)
            throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        String className = switch (platform) {
            case "windows" -> WINDOWS_PACKAGE_PATH + "." + NATIVE_EXTRACTOR_CLASS_NAME;
            case "linux" -> LINUX_PACKAGE_PATH + "." + NATIVE_EXTRACTOR_CLASS_NAME;
            case "macos" -> MACOS_PACKAGE_PATH + "." + NATIVE_EXTRACTOR_CLASS_NAME;
            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
        };

        Class<?> clazz = Class.forName(className);
        return (INativeExtractor) clazz.getDeclaredConstructor().newInstance();
    }
}
