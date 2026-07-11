package com.github.maeda6uiui.mechtatel.natives;

import com.github.maeda6uiui.mechtatel.common.utils.MttResourceFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Base class for native library loader
 *
 * @author maeda6uiui
 */
public abstract class MttNativeLoaderBase {
    private static final Logger logger = LoggerFactory.getLogger(MttNativeLoaderBase.class);
    protected static final String TEMP_FILENAME_PREFIX = "mttnatives";

    private Path tempDir;

    static {
        //Delete previously created temporary files and directories
        try {
            MttResourceFileUtils.deleteTemporaryFiles(TEMP_FILENAME_PREFIX, true);
        } catch (IOException e) {
            logger.warn("Failed to delete temporary files", e);
        }
    }

    public MttNativeLoaderBase() {
        //Create a new temporary directory to extract native libraries into
        try {
            tempDir = Files.createTempDirectory(TEMP_FILENAME_PREFIX);
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

    /**
     * Extracts native libraries that the Slang library depends on.
     * <p>
     * On some platforms the Slang compiler is distributed as a set of libraries instead of
     * a single self-contained one. On such platforms {@link #extractLibSlang()} extracts only
     * a thin facade that forwards its symbols to, and loads at runtime, these companion
     * libraries, which therefore have to be extracted and loaded before the main Slang library.
     * <p>
     * The returned files are expected to be loaded (via {@link System#load(String)}) in the
     * returned order before the main Slang library. The default implementation extracts nothing.
     *
     * @return List of extracted dependent library files, in the order they should be loaded
     * @throws IOException if it fails to extract the libraries
     */
    public List<File> extractDependentLibsSlang() throws IOException {
        return List.of();
    }

    public abstract void loadLibImguiJava() throws IOException;
}
