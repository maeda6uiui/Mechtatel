package com.github.maeda6uiui.mechtatel.audio.natives.windows;

import com.github.maeda6uiui.mechtatel.audio.natives.INativeExtractor;
import com.github.maeda6uiui.mechtatel.audio.natives.NativeExtractorUtils;

import java.io.File;
import java.io.IOException;

/**
 * Extracts native libraries for Windows
 *
 * @author maeda6uiui
 */
public class NativeExtractor implements INativeExtractor {
    @Override
    public File extractLibSoundPlayer() throws IOException {
        return NativeExtractorUtils.extractNativeLibFromJar(this.getClass(), "/Bin/soundplayer.dll");
    }
}
