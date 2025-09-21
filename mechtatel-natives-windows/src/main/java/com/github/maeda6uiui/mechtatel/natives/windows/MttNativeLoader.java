package com.github.maeda6uiui.mechtatel.natives.windows;

import com.github.maeda6uiui.mechtatel.common.utils.MttResourceFileUtils;
import com.github.maeda6uiui.mechtatel.natives.IMttNativeLoader;

import java.io.IOException;

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
}
