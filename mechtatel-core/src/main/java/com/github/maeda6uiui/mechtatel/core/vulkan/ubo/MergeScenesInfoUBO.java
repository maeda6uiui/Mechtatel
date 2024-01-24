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

    private int numScenes;

    public MergeScenesInfoUBO(int numScenes) {
        this.numScenes = numScenes;
    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        buffer.putInt(0, numScenes);
        buffer.rewind();
    }

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}
