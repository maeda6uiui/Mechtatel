package com.github.maeda6uiui.mechtatel.core.shadow;

import org.joml.Vector3f;

/**
 * Shadow info
 *
 * @author maeda
 */
public class ShadowInfo {
    public static final int PROJECTION_TYPE_ORTHOGRAPHIC = 0;
    public static final int PROJECTION_TYPE_PERSPECTIVE = 1;

    private int projectionType;
    private Vector3f lightDirection;
    private Vector3f attenuations;
    private float biasCoefficient;
    private float maxBias;

    public ShadowInfo() {
        projectionType = PROJECTION_TYPE_ORTHOGRAPHIC;
        lightDirection = new Vector3f(-1.0f, -1.0f, -1.0f).normalize();
        attenuations = new Vector3f(0.5f, 0.5f, 0.5f);
        biasCoefficient = 0.01f;
        maxBias = 0.1f;
    }

    public int getProjectionType() {
        return projectionType;
    }

    public void setProjectionType(int projectionType) {
        this.projectionType = projectionType;
    }

    public Vector3f getLightDirection() {
        return lightDirection;
    }

    public void setLightDirection(Vector3f lightDirection) {
        this.lightDirection = lightDirection;
    }

    public Vector3f getAttenuations() {
        return attenuations;
    }

    public void setAttenuations(Vector3f attenuations) {
        this.attenuations = attenuations;
    }

    public float getBiasCoefficient() {
        return biasCoefficient;
    }

    public void setBiasCoefficient(float biasCoefficient) {
        this.biasCoefficient = biasCoefficient;
    }

    public float getMaxBias() {
        return maxBias;
    }

    public void setMaxBias(float maxBias) {
        this.maxBias = maxBias;
    }
}
