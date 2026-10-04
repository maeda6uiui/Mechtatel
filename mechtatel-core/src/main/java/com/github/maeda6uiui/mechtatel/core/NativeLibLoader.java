package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.natives.MttNativeLoaderBase;
import com.github.maeda6uiui.mechtatel.natives.MttNativeLoaderFactory2;
import electrostatic4j.snaploader.LibraryInfo;
import electrostatic4j.snaploader.LoadingCriterion;
import electrostatic4j.snaploader.NativeBinaryLoader;
import electrostatic4j.snaploader.filesystem.DirectoryPath;
import electrostatic4j.snaploader.platform.NativeDynamicLibrary;
import electrostatic4j.snaploader.platform.util.PlatformPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Native library loader
 *
 * @author maeda6uiui
 */
class NativeLibLoader {
    private static final Logger logger = LoggerFactory.getLogger(NativeLibLoader.class);

    public static void loadNativeLibs() {
        MttNativeLoaderBase nativeLoader;
        try {
            nativeLoader = MttNativeLoaderFactory2.createNativeLoader(PlatformInfo.PLATFORM_WITH_ARCH);
        } catch (ClassNotFoundException
                 | NoSuchMethodException
                 | InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException e) {
            logger.error("Failed to create native loader");
            throw new RuntimeException(e);
        }

        try {
            nativeLoader.loadLibImguiJava();
        } catch (IOException e) {
            logger.error("Failed to load native library");
            throw new RuntimeException(e);
        }

        var tempDirPath = new DirectoryPath(System.getProperty("java.io.tmpdir"));
        LibraryInfo info = new LibraryInfo(null, "bulletjme", tempDirPath);
        NativeBinaryLoader loader = new NativeBinaryLoader(info);

        NativeDynamicLibrary[] libraries = {
                new NativeDynamicLibrary("native/linux/arm64", PlatformPredicate.LINUX_ARM_64),
                new NativeDynamicLibrary("native/linux/x86_64", PlatformPredicate.LINUX_X86_64),
                new NativeDynamicLibrary("native/osx/arm64", PlatformPredicate.MACOS_ARM_64),
                new NativeDynamicLibrary("native/windows/arm64", PlatformPredicate.WIN_ARM_64),
                new NativeDynamicLibrary("native/windows/x86_64", PlatformPredicate.WIN_X86_64)
        };
        loader.registerNativeLibraries(libraries).initPlatformLibrary();
        try {
            loader.loadLibrary(LoadingCriterion.CLEAN_EXTRACTION);
        } catch (Exception e) {
            logger.error("Failed to load bulletjme");
            throw new RuntimeException(e);
        }
    }
}
