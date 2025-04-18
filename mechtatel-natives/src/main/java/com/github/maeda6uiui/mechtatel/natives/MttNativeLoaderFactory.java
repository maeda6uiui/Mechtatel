package com.github.maeda6uiui.mechtatel.natives;

import java.lang.reflect.InvocationTargetException;

/**
 * Factory for native loader
 *
 * @author maeda6uiui
 */
public class MttNativeLoaderFactory {
    private static final String NATIVE_LOADER_CLASS_NAME = "MttNativeLoader";
    private static final String WINDOWS_PACKAGE_PATH = "com.github.maeda6uiui.mechtatel.natives.windows";
    private static final String LINUX_PACKAGE_PATH = "com.github.maeda6uiui.mechtatel.natives.linux";
    private static final String MACOS_PACKAGE_PATH = "com.github.maeda6uiui.mechtatel.natives.macos";

    public static IMttNativeLoader createNativeLoader(String platform)
            throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        String className = switch (platform) {
            case "windows" -> WINDOWS_PACKAGE_PATH + "." + NATIVE_LOADER_CLASS_NAME;
            case "linux" -> LINUX_PACKAGE_PATH + "." + NATIVE_LOADER_CLASS_NAME;
            case "macos" -> MACOS_PACKAGE_PATH + "." + NATIVE_LOADER_CLASS_NAME;
            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
        };

        Class<?> clazz = Class.forName(className);
        return (IMttNativeLoader) clazz.getDeclaredConstructor().newInstance();
    }
}
