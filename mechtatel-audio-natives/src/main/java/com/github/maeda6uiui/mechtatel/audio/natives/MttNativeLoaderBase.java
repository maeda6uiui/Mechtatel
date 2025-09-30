package com.github.maeda6uiui.mechtatel.audio.natives;

import com.github.maeda6uiui.mechtatel.common.utils.MttResourceFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Base class for native library loader
 *
 * @author maeda6uiui
 */
public abstract class MttNativeLoaderBase {
    private static final Logger logger = LoggerFactory.getLogger(MttNativeLoaderBase.class);
    protected static final String TEMP_FILENAME_PREFIX = "mttaudionatives";

    static {
        //Delete previously created temporary files and directories
        try {
            MttResourceFileUtils.deleteTemporaryFiles(TEMP_FILENAME_PREFIX, true);
        } catch (IOException e) {
            logger.warn("Failed to delete temporary files", e);
        }
    }

    public abstract File extractLibAudioPlayer() throws IOException;
}
