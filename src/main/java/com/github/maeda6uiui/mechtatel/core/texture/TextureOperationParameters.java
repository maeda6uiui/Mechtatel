package com.github.maeda6uiui.mechtatel.core.texture;

import org.joml.Vector4f;

/**
 * Parameters for texture operations
 *
 * @author maeda6uiui
 */
public class TextureOperationParameters {
    public static final int TEXTURE_OPERATION_ADD = 0;
    public static final int TEXTURE_OPERATION_SUB = 1;
    public static final int TEXTURE_OPERATION_MUL = 2;
    public static final int TEXTURE_OPERATION_DIV = 3;
    public static final int TEXTURE_OPERATION_MERGE_BY_DEPTH = 4;

    public Vector4f firstTextureFactor;
    public Vector4f secondTextureFactor;
    public int operationType;

    public TextureOperationParameters() {
        firstTextureFactor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        secondTextureFactor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        operationType = TEXTURE_OPERATION_ADD;
    }
}
