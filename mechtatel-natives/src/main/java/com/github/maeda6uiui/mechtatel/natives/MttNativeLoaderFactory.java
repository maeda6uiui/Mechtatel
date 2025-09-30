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

    private static Class<?> createNativeLoaderClass(String platform) throws ClassNotFoundException {
        String className = switch (platform) {
            case "windows" -> WINDOWS_PACKAGE_PATH + "." + NATIVE_LOADER_CLASS_NAME;
            case "linux" -> LINUX_PACKAGE_PATH + "." + NATIVE_LOADER_CLASS_NAME;
            default -> throw new IllegalArgumentException("Unsupported platform: " + platform);
        };

        return Class.forName(className);
    }

    @Deprecated
    public static IMttNativeLoader createNativeLoader(String platform)
            throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = createNativeLoaderClass(platform);
        return (IMttNativeLoader) clazz.getDeclaredConstructor().newInstance();
    }

    public static MttNativeLoaderBase createNativeLoaderForPlatform(String platform)
            throws ClassNotFoundException, NoSuchMethodException,
            InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = createNativeLoaderClass(platform);
        return (MttNativeLoaderBase) clazz.getDeclaredConstructor().newInstance();
    }
}
