package com.github.maeda6uiui.mechtatel.audio.natives.windows;

import com.github.maeda6uiui.mechtatel.audio.natives.INativeExtractor;
import com.github.maeda6uiui.mechtatel.common.utils.MttResourceFileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Extracts native libraries for Windows
 *
 * @author maeda6uiui
 */
public class NativeExtractor implements INativeExtractor {
    @Override
    public File extractLibAudioPlayer() throws IOException {
        return MttResourceFileUtils.extractFile(this.getClass(), "/Bin/audioplayer.dll");
    }
}
