package com.github.maeda6uiui.mechtatel.natives.linux;

import com.github.maeda6uiui.mechtatel.natives.IMttNativeLoader;
import com.jme3.system.NativeLibraryLoader;

import java.io.File;
import java.util.Objects;

/**
 * Loads native libraries for Linux
 *
 * @author maeda6uiui
 */
public class MttNativeLoader implements IMttNativeLoader {
    @Override
    public void loadLibbulletjme() {
        NativeLibraryLoader.loadLibbulletjme(
                true,
                new File(Objects.requireNonNull(MttNativeLoader.class.getResource("/Bin")).getFile()),
                "Release",
                "Sp"
        );
    }

    @Override
    public void loadShaderc() {
        String shadercLibFilepath = Objects.requireNonNull(
                MttNativeLoader.class.getResource("/Bin/libshaderc_shared.so")).getFile();
        System.load(shadercLibFilepath);
    }
}
