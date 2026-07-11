package com.github.maeda6uiui.mechtatel.natives.windows;

import com.github.maeda6uiui.mechtatel.common.utils.MttResourceFileUtils;
import com.github.maeda6uiui.mechtatel.natives.MttNativeLoaderBase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<File> extractDependentLibsSlang() throws IOException {
        //Since Slang 2026.13 the Windows slang.dll is only a thin facade that forwards its
        //exports to slang-compiler.dll and loads the remaining companion libraries at runtime.
        //These have to be extracted and loaded alongside slang.dll for shader compilation to work.
        String[] libNames = {
                "slang-compiler.dll",
                "slang-rt.dll",
                "slang-glslang.dll",
                "slang-glsl-module.dll"
        };

        List<File> libFiles = new ArrayList<>();
        for (String libName : libNames) {
            File libFile = MttResourceFileUtils.extractFileIntoDir(
                    this.getClass(), "/Bin/" + libName, this.getTempDir());
            libFiles.add(libFile);
        }

        return libFiles;
    }

    @Override
    public void loadLibImguiJava() throws IOException {
        File nativeLibFile = MttResourceFileUtils.extractFileIntoDir(
                this.getClass(), "/Bin/imgui-java64.dll", this.getTempDir());
        System.setProperty("imgui.library.path", nativeLibFile.getParent());
    }
}
