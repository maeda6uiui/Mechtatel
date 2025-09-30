package com.github.maeda6uiui.mechtatel.natives;

import com.github.maeda6uiui.mechtatel.common.utils.MttResourceFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Base class for native library loader
 *
 * @author maeda6uiui
 */
public abstract class MttNativeLoaderBase {
    private static final Logger logger = LoggerFactory.getLogger(MttNativeLoaderBase.class);
    private static final String TEMP_DIR_PREFIX = "mttnatives";

    private Path tempDir;

    public MttNativeLoaderBase() {
        //Delete previously created temporary files and directories
        try {
            MttResourceFileUtils.deleteTemporaryFiles(TEMP_DIR_PREFIX, true);
        } catch (IOException e) {
            logger.warn("Failed to delete temporary files", e);
        }

        //Create a new temporary directory to extract native libraries into
        try {
            tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Path getTempDir() {
        return tempDir;
    }

    public abstract void loadLibbulletjme() throws IOException;

    public abstract File extractLibMttSlangc() throws IOException;

    public abstract File extractLibSlang() throws IOException;
}
