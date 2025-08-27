package com.github.maeda6uiui.mechtatel.audio;

import com.github.maeda6uiui.mechtatel.audio.natives.INativeExtractor;
import com.github.maeda6uiui.mechtatel.audio.natives.NativeExtractorFactory;
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
    public static IAudioPlayer load() {
        String platform;
        if (Platform.isWindows()) {
            platform = "windows";
        } else if (Platform.isLinux()) {
            platform = "linux";
        } else {
            throw new RuntimeException("Unsupported platform");
        }

        File libFile;
        try {
            INativeExtractor extractor = NativeExtractorFactory.createNativeExtractor(platform);
            libFile = extractor.extractLibAudioPlayer();
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
