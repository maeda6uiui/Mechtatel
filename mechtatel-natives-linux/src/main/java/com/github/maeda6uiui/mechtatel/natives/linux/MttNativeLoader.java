package com.github.maeda6uiui.mechtatel.natives.linux;

import com.github.maeda6uiui.mechtatel.natives.IMttNativeLoader;
import com.github.maeda6uiui.mechtatel.natives.NativeLoaderUtils;

import java.io.IOException;

/**
 * Loads native libraries for Linux
 *
 * @author maeda6uiui
 */
public class MttNativeLoader implements IMttNativeLoader {
    @Override
    public void loadLibbulletjme() throws IOException {
        NativeLoaderUtils.loadNativeLibFromJar(this.getClass(), "/Bin/Linux64ReleaseSp_libbulletjme.so");
    }
}
