package com.github.maeda6uiui.mechtatel.audio;

import com.github.maeda6uiui.mechtatel.audio.natives.MttNativeLoaderBase;
import com.github.maeda6uiui.mechtatel.audio.natives.MttNativeLoaderFactory;
import com.sun.jna.Native;
import com.sun.jna.Platform;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Native library loader
 *
 * @author maeda6uiui
 */
public class NativeLoader {
    private static String getPlatform() {
        return resolvePlatform(
                Platform.isWindows(),
                Platform.isLinux(),
                Platform.isMac(),
                Platform.isIntel(),
                Platform.isARM());
    }

    static String resolvePlatform(boolean isWindows, boolean isLinux, boolean isMac, boolean isIntel, boolean isArm) {
        if (isWindows) {
            if (isIntel) {
                return "windows";
            } else if (isArm) {
                return "windowsarm64";
            }
        } else if (isLinux) {
            if (isIntel) {
                return "linux";
            } else if (isArm) {
                return "linuxarm64";
            }
        } else if (isMac) {
            if (isArm) {
                return "macosarm64";
            }
        }
        throw new RuntimeException("Unsupported platform");
    }

    public static IAudioPlayer load() {
        String platform = getPlatform();

        File libFile;
        try {
            MttNativeLoaderBase loader = MttNativeLoaderFactory.createNativeLoader(platform);
            libFile = loader.extractLibAudioPlayer();
        } catch (
                ClassNotFoundException
                | NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | IOException e) {
            throw new RuntimeException(e);
        }

        return Native.load(libFile.getPath(), IAudioPlayer.class);
    }
}
