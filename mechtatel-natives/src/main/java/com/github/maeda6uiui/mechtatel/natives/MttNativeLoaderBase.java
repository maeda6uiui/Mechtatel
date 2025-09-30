package com.github.maeda6uiui.mechtatel.natives;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Base class for native library loader
 *
 * @author maeda6uiui
 */
public abstract class MttNativeLoaderBase {
    public MttNativeLoaderBase() {

    }

    public abstract void loadLibbulletjme() throws IOException;

    public abstract File extractLibMttSlangc(Path tempDir) throws IOException;

    public abstract File extractLibSlang(Path tempDir) throws IOException;
}
