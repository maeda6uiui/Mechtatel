package com.github.maeda6uiui.mechtatel.core.vulkan.shader.slang;

import com.github.maeda6uiui.mechtatel.core.PlatformInfo;
import com.github.maeda6uiui.mechtatel.natives.MttNativeLoaderBase;
import com.github.maeda6uiui.mechtatel.natives.MttNativeLoaderFactory2;
import com.sun.jna.Native;

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
        //Extract native libs
        File slangLibFile;
        File mttslangcLibFile;
        try {
            MttNativeLoaderBase loader = MttNativeLoaderFactory2.createNativeLoader(PlatformInfo.PLATFORM_WITH_ARCH);
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
