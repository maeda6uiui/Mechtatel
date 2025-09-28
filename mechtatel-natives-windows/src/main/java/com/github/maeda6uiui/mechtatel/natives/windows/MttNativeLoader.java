package com.github.maeda6uiui.mechtatel.natives.windows;

import com.github.maeda6uiui.mechtatel.common.utils.MttResourceFileUtils;
import com.github.maeda6uiui.mechtatel.natives.IMttNativeLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Loads native libraries for Windows
 *
 * @author maeda6uiui
 */
public class MttNativeLoader implements IMttNativeLoader {
    @Override
    public void loadLibbulletjme() throws IOException {
        MttResourceFileUtils.loadNativeLib(this.getClass(), "/Bin/Windows64ReleaseSp_bulletjme.dll");
    }

    @Override
    public File extractLibMttSlangc() throws IOException {
        return MttResourceFileUtils.extractFile(this.getClass(), "/Bin/mttslangc.dll");
    }

    @Override
    public File extractLibMttSlangc(Path tempDir) throws IOException {
        return MttResourceFileUtils.extractFileIntoDir(this.getClass(), "/Bin/mttslangc.dll", tempDir);
    }

    @Override
    public File extractLibSlang(Path tempDir) throws IOException {
        return MttResourceFileUtils.extractFileIntoDir(this.getClass(), "/Bin/slang.dll", tempDir);
    }

    @Override
    public void loadLibSlang() throws IOException {
        MttResourceFileUtils.loadNativeLib(this.getClass(), "/Bin/slang.dll");
    }
}
