package com.github.maeda6uiui.mechtatel.natives.linux;

import com.github.maeda6uiui.mechtatel.common.utils.MttResourceFileUtils;
import com.github.maeda6uiui.mechtatel.natives.IMttNativeLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Loads native libraries for Linux
 *
 * @author maeda6uiui
 */
@Deprecated
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
    public File extractLibMttSlangc(Path tempDir) throws IOException {
        return MttResourceFileUtils.extractFileIntoDir(this.getClass(), "/Bin/libmttslangc.so", tempDir);
    }

    @Override
    public File extractLibSlang(Path tempDir) throws IOException {
        return MttResourceFileUtils.extractFileIntoDir(this.getClass(), "/Bin/libslang.so", tempDir);
    }

    @Override
    public void loadLibSlang() throws IOException {
        MttResourceFileUtils.loadNativeLib(this.getClass(), "/Bin/libslang.so");
    }
}
