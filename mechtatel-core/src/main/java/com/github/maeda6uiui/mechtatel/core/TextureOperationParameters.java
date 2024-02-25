package com.github.maeda6uiui.mechtatel.core;

import org.joml.Vector4f;

/**
 * Parameters for texture operations
 *
 * @author maeda6uiui
 */
public class TextureOperationParameters {
    public enum OperationType {
        ADD,
        SUB,
        MUL,
        DIV,
        MERGE_BY_DEPTH
    }

    private Vector4f firstTextureFactor;
    private Vector4f secondTextureFactor;
    private OperationType operationType;
    private float firstTextureFixedDepth;
    private float secondTextureFixedDepth;

    public TextureOperationParameters() {
        firstTextureFactor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        secondTextureFactor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        operationType = OperationType.ADD;
        firstTextureFixedDepth = -1.0f;
        secondTextureFixedDepth = -1.0f;
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

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public float getFirstTextureFixedDepth() {
        return firstTextureFixedDepth;
    }

    public void setFirstTextureFixedDepth(float firstTextureFixedDepth) {
        this.firstTextureFixedDepth = firstTextureFixedDepth;
    }

    public float getSecondTextureFixedDepth() {
        return secondTextureFixedDepth;
    }

    public void setSecondTextureFixedDepth(float secondTextureFixedDepth) {
        this.secondTextureFixedDepth = secondTextureFixedDepth;
    }
}
