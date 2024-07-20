package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.fseffect;

import com.github.maeda6uiui.mechtatel.core.fseffect.GaussianBlurInfo;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.UBO;
import org.joml.Vector2i;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_FLOAT;
import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_INT;

/**
 * Uniform buffer object for Gaussian blur info
 *
 * @author maeda6uiui
 */
public class GaussianBlurInfoUBO extends UBO {
    public static final int MAX_NUM_WEIGHTS = 32;
    public static final int SIZEOF = SIZEOF_INT * 2 + SIZEOF_INT + SIZEOF_FLOAT * MAX_NUM_WEIGHTS;

    private Vector2i textureSize;
    private float[] weights;

    public GaussianBlurInfoUBO(GaussianBlurInfo blurInfo) {
        textureSize = blurInfo.getTextureSize();
        weights = blurInfo.getWeights();

        if (weights.length > MAX_NUM_WEIGHTS) {
            throw new RuntimeException(String.format("Number of weights exceeds maximum allowed (%d)", MAX_NUM_WEIGHTS));
        }
    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        textureSize.get(0, buffer);
        buffer.putInt(SIZEOF_INT * 2, weights.length);
        for (int i = 0; i < weights.length; i++) {
            buffer.putFloat(SIZEOF_INT * 3 + SIZEOF_FLOAT * i, weights[i]);
        }
    }

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}
