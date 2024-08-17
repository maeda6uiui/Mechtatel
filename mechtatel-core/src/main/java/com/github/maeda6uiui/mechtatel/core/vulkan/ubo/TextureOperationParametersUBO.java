package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import com.github.maeda6uiui.mechtatel.core.operation.TextureOperationParameters;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_INT;
import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_VEC4;

/**
 * Uniform buffer object for parameters of texture operations
 *
 * @author maeda6uiui
 */
public class TextureOperationParametersUBO extends UBO {
    private static final Logger logger = LoggerFactory.getLogger(TextureOperationParametersUBO.class);

    public static final int MAX_NUM_TEXTURES = 8;
    public static final int SIZEOF = SIZEOF_VEC4 * MAX_NUM_TEXTURES + SIZEOF_INT * 2;

    private List<Vector4f> factors;
    private int operationType;
    private int numTextures;

    public TextureOperationParametersUBO(TextureOperationParameters parameters) {
        factors = parameters.getFactors();
        operationType = parameters.getOperationType().ordinal();
        numTextures = Math.min(factors.size(), MAX_NUM_TEXTURES);

        if (factors.size() > MAX_NUM_TEXTURES) {
            logger.warn(
                    "Factors are truncated because the number of factors exceeds maximum allowed ({})",
                    MAX_NUM_TEXTURES
            );
        }
    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        for (int i = 0; i < numTextures; i++) {
            factors.get(i).get(SIZEOF_VEC4 * i, buffer);
        }
        buffer.putInt(SIZEOF_VEC4 * MAX_NUM_TEXTURES, operationType);
        buffer.putInt(SIZEOF_VEC4 * MAX_NUM_TEXTURES + SIZEOF_INT, numTextures);

        buffer.rewind();
    }

    @Override
    public int getSize() {
        return SIZEOF;
    }
}
