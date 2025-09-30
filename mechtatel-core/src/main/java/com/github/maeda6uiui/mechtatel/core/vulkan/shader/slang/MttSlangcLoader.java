package com.github.maeda6uiui.mechtatel.core.vulkan.shader.slang;

import com.github.maeda6uiui.mechtatel.natives.MttNativeLoaderBase;
import com.github.maeda6uiui.mechtatel.natives.MttNativeLoaderFactory2;
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
class MttSlangcLoader {
    public static IMttSlangc load() {
        String platform;
        if (Platform.isWindows()) {
            platform = "windows";
        } else if (Platform.isLinux()) {
            platform = "linux";
        } else {
            throw new RuntimeException("Unsupported platform");
        }

        //Extract native libs
        File slangLibFile;
        File mttslangcLibFile;
        try {
            MttNativeLoaderBase loader = MttNativeLoaderFactory2.createNativeLoader(platform);
            slangLibFile = loader.extractLibSlang();
            mttslangcLibFile = loader.extractLibMttSlangc();
        } catch (
                ClassNotFoundException
                | NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | IOException e) {
            throw new RuntimeException(e);
        }

        //Load native libs
        System.load(slangLibFile.getPath());
        return Native.load(mttslangcLibFile.getPath(), IMttSlangc.class);
    }
}
