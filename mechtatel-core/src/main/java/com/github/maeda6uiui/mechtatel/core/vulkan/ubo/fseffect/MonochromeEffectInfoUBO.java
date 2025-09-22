package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.fseffect;

import com.github.maeda6uiui.mechtatel.core.fseffect.MonochromeEffectInfo;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.UBO;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_INT;

/**
 * Uniform buffer object for monochrome effect info
 *
 * @author maeda6uiui
 */
public class MonochromeEffectInfoUBO extends UBO {
    public static final int SIZEOF = SIZEOF_INT;

    public MonochromeEffectInfoUBO(MonochromeEffectInfo effectInfo) {

    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        buffer.putInt(0);   //dummy value
    }

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}
