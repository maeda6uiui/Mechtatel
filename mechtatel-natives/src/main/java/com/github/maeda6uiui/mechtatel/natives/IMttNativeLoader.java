package com.github.maeda6uiui.mechtatel.natives;

import java.io.File;
import java.io.IOException;

/**
 * Interface of native library loader
 *
 * @author maeda6uiui
 */
public interface IMttNativeLoader {
    void loadLibbulletjme() throws IOException;

    File extractLibMttSlangc() throws IOException;

    void loadLibSlang() throws IOException;
}
