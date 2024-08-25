package com.github.maeda6uiui.mechtatel.natives;

import java.io.IOException;

/**
 * Interface of native library loader
 *
 * @author maeda6uiui
 */
public interface IMttNativeLoader {
    void loadLibbulletjme() throws IOException;

    void loadShaderc() throws IOException;
}
