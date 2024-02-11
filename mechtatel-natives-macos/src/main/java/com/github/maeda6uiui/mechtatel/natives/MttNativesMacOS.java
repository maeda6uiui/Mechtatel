package com.github.maeda6uiui.mechtatel.natives;

import com.jme3.system.NativeLibraryLoader;

import java.io.File;
import java.util.Objects;

/**
 * Loads native libraries for macOS
 *
 * @author maeda6uiui
 */
public class MttNativesMacOS {
    static {
        //Libbulletjme
        NativeLibraryLoader.loadLibbulletjme(
                true,
                new File(Objects.requireNonNull(MttNativesMacOS.class.getResource("/Bin")).getFile()),
                "Release",
                "Sp"
        );

        //Shaderc
        String shadercLibFilepath = Objects.requireNonNull(
                MttNativesMacOS.class.getResource("/Bin/libshaderc_shared.dylib")).getFile();
        System.load(shadercLibFilepath);
    }
}
