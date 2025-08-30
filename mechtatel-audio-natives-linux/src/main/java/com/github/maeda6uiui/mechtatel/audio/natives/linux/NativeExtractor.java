package com.github.maeda6uiui.mechtatel.audio.natives.linux;

import com.github.maeda6uiui.mechtatel.audio.natives.INativeExtractor;
import com.github.maeda6uiui.mechtatel.audio.natives.NativeExtractorUtils;

import java.io.File;
import java.io.IOException;

/**
 * Extracts native libraries for Linux
 *
 * @author maeda6uiui
 */
public class NativeExtractor implements INativeExtractor {
    @Override
    public File extractLibAudioPlayer() throws IOException {
        return NativeExtractorUtils.extractNativeLibFromJar(this.getClass(), "/Bin/libaudioplayer.so");
    }
}
