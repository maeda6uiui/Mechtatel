package com.github.maeda6uiui.mechtatel.natives;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface of native library loader
 *
 * @author maeda6uiui
 */
public interface IMttNativeLoader {
    void loadLibbulletjme() throws IOException;

    @Deprecated
    File extractLibMttSlangc() throws IOException;

    File extractLibMttSlangc(Path tempDir) throws IOException;

    File extractLibSlang(Path tempDir) throws IOException;

    @Deprecated
    void loadLibSlang() throws IOException;
}
