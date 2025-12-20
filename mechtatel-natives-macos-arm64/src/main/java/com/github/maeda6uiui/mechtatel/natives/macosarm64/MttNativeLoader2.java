package com.github.maeda6uiui.mechtatel.natives.macosarm64;

import com.github.maeda6uiui.mechtatel.common.utils.MttResourceFileUtils;
import com.github.maeda6uiui.mechtatel.natives.MttNativeLoaderBase;

import java.io.File;
import java.io.IOException;

/**
 * Loads native libraries for macOS arm64
 *
 * @author maeda6uiui
 */
public class MttNativeLoader2 extends MttNativeLoaderBase {
    @Override
    public void loadLibbulletjme() throws IOException {
        MttResourceFileUtils.loadNativeLib(
                this.getClass(),
                "/Bin/MacOSX_ARM64ReleaseSp_libbulletjme.dylib ",
                TEMP_FILENAME_PREFIX,
                false
        );
    }

    @Override
    public File extractLibMttSlangc() throws IOException {
        return MttResourceFileUtils.extractFileIntoDir(
                this.getClass(), "/Bin/libmttslangc.dylib", this.getTempDir());
    }

    @Override
    public File extractLibSlang() throws IOException {
        return MttResourceFileUtils.extractFileIntoDir(
                this.getClass(), "/Bin/libslang.dylib", this.getTempDir());
    }

    @Override
    public void loadLibImguiJava() throws IOException {
        File nativeLibFile = MttResourceFileUtils.extractFileIntoDir(
                this.getClass(), "/Bin/libimgui-java64.dylib", this.getTempDir());
        System.setProperty("imgui.library.path", nativeLibFile.getParent());
    }
}
