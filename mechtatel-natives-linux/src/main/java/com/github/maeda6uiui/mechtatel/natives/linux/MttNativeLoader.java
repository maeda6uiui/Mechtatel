package com.github.maeda6uiui.mechtatel.natives.linux;

import com.github.maeda6uiui.mechtatel.common.utils.MttResourceFileUtils;
import com.github.maeda6uiui.mechtatel.natives.IMttNativeLoader;

import java.io.File;
import java.io.IOException;

/**
 * Loads native libraries for Linux
 *
 * @author maeda6uiui
 */
public class MttNativeLoader implements IMttNativeLoader {
    @Override
    public void loadLibbulletjme() throws IOException {
        MttResourceFileUtils.loadNativeLib(this.getClass(), "/Bin/Linux64ReleaseSp_libbulletjme.so");
    }

    @Override
    public File extractLibMttSlangc() throws IOException {
        return MttResourceFileUtils.extractFile(this.getClass(), "/Bin/libmttslangc.so");
    }

    @Override
    public void loadLibSlang() throws IOException {
        MttResourceFileUtils.loadNativeLib(this.getClass(), "/Bin/libslang.so");
    }
}
