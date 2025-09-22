package com.github.maeda6uiui.mechtatel.core.vulkan.shader.slang;

import com.github.maeda6uiui.mechtatel.natives.IMttNativeLoader;
import com.github.maeda6uiui.mechtatel.natives.MttNativeLoaderFactory;
import com.sun.jna.Native;
import com.sun.jna.Platform;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Native library loader for the Slang compiler
 *
 * @author maeda6uiui
 */
public class MttSlangcLoader {
    public static IMttSlangc load() {
        String platform;
        if (Platform.isWindows()) {
            platform = "windows";
        } else if (Platform.isLinux()) {
            platform = "linux";
        } else {
            throw new RuntimeException("Unsupported platform");
        }

        File libFile;
        try {
            IMttNativeLoader loader = MttNativeLoaderFactory.createNativeLoader(platform);
            loader.loadLibSlang();
            libFile = loader.extractLibMttSlangc();
        } catch (
                ClassNotFoundException
                | NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | IOException e) {
            throw new RuntimeException(e);
        }

        return Native.load(libFile.getPath(), IMttSlangc.class);
    }
}
