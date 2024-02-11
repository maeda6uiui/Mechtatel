package com.github.maeda6uiui.mechtatel.natives.windows;

import com.github.maeda6uiui.mechtatel.natives.IMttNativeLoader;
import com.jme3.system.NativeLibraryLoader;

import java.io.File;
import java.util.Objects;

/**
 * Loads native libraries for Windows
 *
 * @author maeda6uiui
 */
public class MttNativeLoaderWindows implements IMttNativeLoader {
    @Override
    public void loadLibbulletjme() {
        NativeLibraryLoader.loadLibbulletjme(
                true,
                new File(Objects.requireNonNull(MttNativeLoaderWindows.class.getResource("/Bin")).getFile()),
                "Release",
                "Sp"
        );
    }

    @Override
    public void loadShaderc() {
        String shadercLibFilepath = Objects.requireNonNull(
                MttNativeLoaderWindows.class.getResource("/Bin/shaderc_shared.dll")).getFile();
        System.load(shadercLibFilepath);
    }
}
