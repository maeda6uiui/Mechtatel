package com.github.maeda6uiui.mechtatel.natives;

import com.jme3.system.NativeLibraryLoader;

import java.io.File;
import java.util.Objects;

/**
 * Loads native libraries for Windows
 *
 * @author maeda6uiui
 */
public class MttNativesWindows {
    static {
        //Libbulletjme
        NativeLibraryLoader.loadLibbulletjme(
                true,
                new File(Objects.requireNonNull(MttNativesWindows.class.getResource("/Bin")).getFile()),
                "Release",
                "Sp"
        );

        //Shaderc
        String shadercLibFilepath = Objects.requireNonNull(
                MttNativesWindows.class.getResource("/Bin/shaderc_shared.dll")).getFile();
        System.load(shadercLibFilepath);
    }
}
