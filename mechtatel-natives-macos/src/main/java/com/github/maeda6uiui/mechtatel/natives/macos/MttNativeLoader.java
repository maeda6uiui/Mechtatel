package com.github.maeda6uiui.mechtatel.natives.macos;

import com.github.maeda6uiui.mechtatel.natives.IMttNativeLoader;
import com.jme3.system.NativeLibraryLoader;

import java.io.File;
import java.util.Objects;

/**
 * Loads native libraries for macOS
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
                MttNativeLoader.class.getResource("/Bin/libshaderc_shared.dylib")).getFile();
        System.load(shadercLibFilepath);
    }
}
