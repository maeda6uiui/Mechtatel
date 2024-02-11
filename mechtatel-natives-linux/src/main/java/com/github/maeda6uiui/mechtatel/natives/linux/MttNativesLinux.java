package com.github.maeda6uiui.mechtatel.natives.linux;

import com.jme3.system.NativeLibraryLoader;

import java.io.File;
import java.util.Objects;

/**
 * Loads native libraries for Linux
 *
 * @author maeda6uiui
 */
public class MttNativesLinux {
    static {
        //Libbulletjme
        NativeLibraryLoader.loadLibbulletjme(
                true,
                new File(Objects.requireNonNull(MttNativesLinux.class.getResource("/Bin")).getFile()),
                "Release",
                "Sp"
        );

        //Shaderc
        String shadercLibFilepath = Objects.requireNonNull(
                MttNativesLinux.class.getResource("/Bin/libshaderc_shared.so")).getFile();
        System.load(shadercLibFilepath);
    }
}
