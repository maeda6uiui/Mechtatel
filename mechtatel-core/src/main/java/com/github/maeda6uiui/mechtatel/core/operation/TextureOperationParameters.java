package com.github.maeda6uiui.mechtatel.core.operation;

import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

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
    }

    private List<Vector4f> factors;
    private OperationType operationType;

    public TextureOperationParameters() {
        factors = new ArrayList<>();
        operationType = OperationType.ADD;
    }

    public List<Vector4f> getFactors() {
        return factors;
    }

    public void setFactors(List<Vector4f> factors) {
        this.factors = factors;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }
}
