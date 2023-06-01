package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow;

import com.github.maeda6uiui.mechtatel.core.shadow.Pass2Info;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.UBO;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_FLOAT;
import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_VEC4;

/**
 * Uniform buffer object for pass 2 info
 *
 * @author maeda6uiui
 */
public class Pass2InfoUBO extends UBO {
    public static final int SIZEOF = SIZEOF_VEC4 * 2;

    private int numShadowMaps;
    private float biasCoefficient;
    private float maxBias;
    private float normalOffset;
    private int outputMode;
    private int outputDepthImageIndex;

    public Pass2InfoUBO(Pass2Info info) {
        numShadowMaps = info.getNumShadowMaps();
        biasCoefficient = info.getBiasCoefficient();
        maxBias = info.getMaxBias();
        normalOffset = info.getNormalOffset();
        outputMode = info.getOutputMode();
        outputDepthImageIndex = info.getOutputDepthImageIndex();
    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        buffer.putInt(0, numShadowMaps);
        buffer.putFloat(1 * SIZEOF_FLOAT, biasCoefficient);
        buffer.putFloat(2 * SIZEOF_FLOAT, maxBias);
        buffer.putFloat(3 * SIZEOF_FLOAT, normalOffset);
        buffer.putInt(1 * SIZEOF_VEC4, outputMode);
        buffer.putInt(1 * SIZEOF_VEC4 + 1 * SIZEOF_FLOAT, outputDepthImageIndex);

        buffer.rewind();
    }

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}
