package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.fseffect;

import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.UBO;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_INT;

/**
 * Uniform buffer object for full-screen effects that don't require its own UBOs
 *
 * @author maeda6uiui
 */
public class DummyUBO extends UBO {
    public static final int SIZEOF = SIZEOF_INT;

    public DummyUBO() {

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
