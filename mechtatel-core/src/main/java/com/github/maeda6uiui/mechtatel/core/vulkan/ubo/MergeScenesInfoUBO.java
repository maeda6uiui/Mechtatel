package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_INT;

/**
 * Uniform buffer object for scene merging
 *
 * @author maeda6uiui
 */
public class MergeScenesInfoUBO extends UBO {
    public static final int SIZEOF = SIZEOF_INT;

    private int numTextures;

    public MergeScenesInfoUBO(int numTextures) {
        this.numTextures = numTextures;
    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        buffer.putInt(0, numTextures);
        buffer.rewind();
    }

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}
