package com.github.maeda6uiui.mechtatel.audio.natives;

import java.io.File;
import java.io.IOException;

/**
 * Interface of native library extractor
 *
 * @author maeda6uiui
 */
public interface INativeExtractor {
    File extractLibSoundPlayer() throws IOException;
}
