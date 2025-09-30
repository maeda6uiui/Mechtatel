package com.github.maeda6uiui.mechtatel.natives.windows;

import com.github.maeda6uiui.mechtatel.common.utils.MttResourceFileUtils;
import com.github.maeda6uiui.mechtatel.natives.MttNativeLoaderBase;

import java.io.File;
import java.io.IOException;

/**
 * Loads native libraries for Windows
 *
 * @author maeda6uiui
 */
public class MttNativeLoader2 extends MttNativeLoaderBase {
    @Override
    public void loadLibbulletjme() throws IOException {
        MttResourceFileUtils.loadNativeLib(
                this.getClass(),
                "/Bin/Windows64ReleaseSp_bulletjme.dll",
                TEMP_FILENAME_PREFIX,
                false
        );
    }

    @Override
    public File extractLibMttSlangc() throws IOException {
        return MttResourceFileUtils.extractFileIntoDir(
                this.getClass(), "/Bin/mttslangc.dll", this.getTempDir());
    }

    @Override
    public File extractLibSlang() throws IOException {
        return MttResourceFileUtils.extractFileIntoDir(
                this.getClass(), "/Bin/slang.dll", this.getTempDir());
    }
}
