package com.github.maeda6uiui.mechtatel.natives.macos;

import com.github.maeda6uiui.mechtatel.natives.IMttNativeLoader;
import com.github.maeda6uiui.mechtatel.natives.NativeLoaderUtils;

import java.io.IOException;

/**
 * Loads native libraries for macOS
 *
 * @author maeda6uiui
 */
public class MttNativeLoader implements IMttNativeLoader {
    @Override
    public void loadLibbulletjme() throws IOException {
        NativeLoaderUtils.loadNativeLibFromJar(this.getClass(), "/Bin/MacOSX64ReleaseSp_libbulletjme.dylib");
    }

    @Override
    public void loadShaderc() throws IOException {
        NativeLoaderUtils.loadNativeLibFromJar(this.getClass(), "/Bin/libshaderc_shared.dylib");
    }
}
