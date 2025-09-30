package com.github.maeda6uiui.mechtatel.audio.natives.windows;

import com.github.maeda6uiui.mechtatel.audio.natives.MttNativeLoaderBase;
import com.github.maeda6uiui.mechtatel.common.utils.MttResourceFileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Native library loader for Windows
 *
 * @author maeda6uiui
 */
public class MttNativeLoader extends MttNativeLoaderBase {
    @Override
    public File extractLibAudioPlayer() throws IOException {
        return MttResourceFileUtils.extractFile(
                this.getClass(),
                "/Bin/audioplayer.dll",
                TEMP_FILENAME_PREFIX,
                false
        );
    }
}
