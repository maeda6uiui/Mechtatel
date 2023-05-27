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

    private Vector4f firstTextureFactor;
    private Vector4f secondTextureFactor;
    private int operationType;

    public TextureOperationParameters() {
        firstTextureFactor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        secondTextureFactor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        operationType = TEXTURE_OPERATION_ADD;
    }

    public Vector4f getFirstTextureFactor() {
        return firstTextureFactor;
    }

    public void setFirstTextureFactor(Vector4f firstTextureFactor) {
        this.firstTextureFactor = firstTextureFactor;
    }

    public Vector4f getSecondTextureFactor() {
        return secondTextureFactor;
    }

    public void setSecondTextureFactor(Vector4f secondTextureFactor) {
        this.secondTextureFactor = secondTextureFactor;
    }

    public int getOperationType() {
        return operationType;
    }

    public void setOperationType(int operationType) {
        this.operationType = operationType;
    }
}
