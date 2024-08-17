package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import com.github.maeda6uiui.mechtatel.core.operation.TextureOperationParameters;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.*;

/**
 * Uniform buffer object for parameters of texture operations
 *
 * @author maeda6uiui
 */
public class TextureOperationParametersUBO extends UBO {
    private static final Logger logger = LoggerFactory.getLogger(TextureOperationParametersUBO.class);

    public static final int MAX_NUM_TEXTURES = 8;
    public static final int SIZEOF
            = SIZEOF_VEC4 * MAX_NUM_TEXTURES + SIZEOF_FLOAT * MAX_NUM_TEXTURES + SIZEOF_INT * 2;

    private List<Vector4f> factors;
    private List<Float> fixedDepths;
    private int operationType;

    public TextureOperationParametersUBO(TextureOperationParameters parameters) {
        factors = parameters.getFactors();
        fixedDepths = parameters.getFixedDepths();
        operationType = parameters.getOperationType().ordinal();

        if (factors.size() > MAX_NUM_TEXTURES) {
            logger.warn("Number of factors exceeds maximum allowed ({}), factors will be truncated", MAX_NUM_TEXTURES);
        }
    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        int lenFactors = Math.min(factors.size(), MAX_NUM_TEXTURES);

        for (int i = 0; i < lenFactors; i++) {
            factors.get(i).get(SIZEOF_VEC4 * i, buffer);
        }
        for (int i = 0; i < lenFactors; i++) {
            buffer.putFloat(SIZEOF_VEC4 * MAX_NUM_TEXTURES + SIZEOF_FLOAT * i, fixedDepths.get(i));
        }
        buffer.putInt(SIZEOF_VEC4 * MAX_NUM_TEXTURES + SIZEOF_FLOAT * MAX_NUM_TEXTURES, operationType);
        buffer.putInt(SIZEOF_VEC4 * MAX_NUM_TEXTURES + SIZEOF_FLOAT * MAX_NUM_TEXTURES + SIZEOF_INT, lenFactors);

        buffer.rewind();
    }

    @Override
    public int getSize() {
        return SIZEOF;
    }
}
