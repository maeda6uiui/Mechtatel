package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.fseffect;

import com.github.maeda6uiui.mechtatel.core.fseffect.MonochromeEffectInfo;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.UBO;

import java.nio.ByteBuffer;

/**
 * Uniform buffer object for monochrome effect info
 *
 * @author maeda6uiui
 */
public class MonochromeEffectInfoUBO extends UBO {
    public static final int SIZEOF = 0;

    public MonochromeEffectInfoUBO(MonochromeEffectInfo effectInfo) {

    }

    @Override
    protected void memcpy(ByteBuffer buffer) {

    }

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}
