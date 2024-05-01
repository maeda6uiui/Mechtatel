package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import com.github.maeda6uiui.mechtatel.core.model.AssimpModelLoader;
import com.github.maeda6uiui.mechtatel.core.model.MttModelData;
import org.joml.Matrix4f;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_MAT4;

/**
 * Uniform buffer object for model animation
 *
 * @author maeda6uiui
 */
public class AnimationUBO extends UBO {
    public static final int SIZEOF = SIZEOF_MAT4 * AssimpModelLoader.MAX_NUM_BONES;

    private Matrix4f[] boneMatrices;

    /**
     * Constructor
     *
     * @param currentFrame Current frame
     */
    public AnimationUBO(MttModelData.AnimatedFrame currentFrame) {
        boneMatrices = currentFrame.boneMatrices();
    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        for (int i = 0; i < AssimpModelLoader.MAX_NUM_BONES; i++) {
            boneMatrices[i].get(SIZEOF_MAT4 * i, buffer);
        }

        buffer.rewind();
    }

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}
