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
        MERGE_BY_DEPTH
    }

    private List<Vector4f> factors;
    private List<Float> fixedDepths;
    private OperationType operationType;

    public TextureOperationParameters() {
        factors = new ArrayList<>();
        fixedDepths = new ArrayList<>();
        operationType = OperationType.ADD;
    }

    public List<Vector4f> getFactors() {
        return factors;
    }

    public void setFactors(List<Vector4f> factors) {
        this.factors = factors;
    }

    public List<Float> getFixedDepths() {
        return fixedDepths;
    }

    public void setFixedDepths(List<Float> fixedDepths) {
        this.fixedDepths = fixedDepths;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }
}
