package com.github.maeda6uiui.mechtatel.audio;

import com.github.maeda6uiui.mechtatel.audio.natives.INativeExtractor;
import com.github.maeda6uiui.mechtatel.audio.natives.NativeExtractorFactory;
import com.github.maeda6uiui.mechtatel.common.utils.MttResourceFileUtils;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Native library loader
 *
 * @author maeda6uiui
 */
public class NativeLoader {
    private static final Logger logger = LoggerFactory.getLogger(NativeLoader.class);

    public static IAudioPlayer load() {
        String platform;
        if (Platform.isWindows()) {
            platform = "windows";
        } else if (Platform.isLinux()) {
            platform = "linux";
        } else {
            throw new RuntimeException("Unsupported platform");
        }

        try {
            MttResourceFileUtils.deleteTemporaryFiles("mttaudionatives", false);
        } catch (IOException e) {
            logger.warn("Failed to delete temporary files", e);
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
