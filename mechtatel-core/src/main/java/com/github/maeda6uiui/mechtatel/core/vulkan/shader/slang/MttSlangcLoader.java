package com.github.maeda6uiui.mechtatel.core.vulkan.shader.slang;

import com.github.maeda6uiui.mechtatel.core.PlatformInfo;
import com.github.maeda6uiui.mechtatel.natives.MttNativeLoaderBase;
import com.github.maeda6uiui.mechtatel.natives.MttNativeLoaderFactory2;
import com.sun.jna.Native;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Native library loader for the Slang compiler
 *
 * @author maeda6uiui
 */
class MttSlangcLoader {
    public static IMttSlangc load() {
        //Extract native libs
        List<File> dependentSlangLibFiles;
        File slangLibFile;
        File mttslangcLibFile;
        try {
            MttNativeLoaderBase loader = MttNativeLoaderFactory2.createNativeLoader(PlatformInfo.PLATFORM_WITH_ARCH);
            dependentSlangLibFiles = loader.extractDependentLibsSlang();
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
        //Dependent libs are loaded first so that the main Slang library can resolve the symbols
        //it forwards to them, and so that companion libs it loads by name at runtime resolve to
        //the already-loaded modules
        dependentSlangLibFiles.forEach(f -> System.load(f.getPath()));
        System.load(slangLibFile.getPath());
        return Native.load(mttslangcLibFile.getPath(), IMttSlangc.class);
    }
}
