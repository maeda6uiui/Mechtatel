package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import com.github.maeda6uiui.mechtatel.core.TextureOperationParameters;
import org.joml.Vector4f;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.*;

/**
 * Uniform buffer object for texture operation parameters
 *
 * @author maeda6uiui
 */
public class TextureOperationParametersUBO extends UBO {
    public static final int SIZEOF = SIZEOF_VEC4 * 2 + SIZEOF_INT + SIZEOF_FLOAT * 2;

    private Vector4f firstTextureFactor;
    private Vector4f secondTextureFactor;
    private int operationType;
    private float firstTextureFixedDepth;
    private float secondTextureFixedDepth;

    public TextureOperationParametersUBO(TextureOperationParameters parameters) {
        firstTextureFactor = parameters.getFirstTextureFactor();
        secondTextureFactor = parameters.getSecondTextureFactor();
        operationType = parameters.getOperationType().ordinal();
        firstTextureFixedDepth = parameters.getFirstTextureFixedDepth();
        secondTextureFixedDepth = parameters.getSecondTextureFixedDepth();
    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        firstTextureFactor.get(0, buffer);
        secondTextureFactor.get(SIZEOF_VEC4, buffer);
        buffer.putInt(SIZEOF_VEC4 * 2, operationType);
        buffer.putFloat(SIZEOF_VEC4 * 2 + SIZEOF_INT, firstTextureFixedDepth);
        buffer.putFloat(SIZEOF_VEC4 * 2 + SIZEOF_INT + SIZEOF_FLOAT, secondTextureFixedDepth);

        buffer.rewind();
    }

    @Override
    public int getSize() {
        return SIZEOF;
    }
}
