package com.github.maeda6uiui.mechtatel.natives.windows;

import com.github.maeda6uiui.mechtatel.common.utils.MttResourceFileUtils;
import com.github.maeda6uiui.mechtatel.natives.IMttNativeLoader;
import com.github.maeda6uiui.mechtatel.natives.MttNativeLoaderBase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Loads native libraries for Windows
 *
 * @author maeda6uiui
 */
public class MttNativeLoader extends MttNativeLoaderBase implements IMttNativeLoader {
    @Override
    public void loadLibbulletjme() throws IOException {
        MttResourceFileUtils.loadNativeLib(
                this.getClass(),
                "/Bin/Windows64ReleaseSp_bulletjme.dll",
                "mttnatives",
                false
        );
    }

    @Override
    @Deprecated
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
    @Deprecated
    public void loadLibSlang() throws IOException {
        MttResourceFileUtils.loadNativeLib(this.getClass(), "/Bin/slang.dll");
    }
}
