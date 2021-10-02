package com.github.maeda6uiui.mechtatel.core.light;

import org.joml.Vector3f;

/**
 * Lighting info
 *
 * @author maeda
 */
public class LightingInfo {
    private Vector3f lightingClampMin;
    private Vector3f lightingClampMax;
    private int numLights;

    public LightingInfo() {
        lightingClampMin = new Vector3f(0.0f, 0.0f, 0.0f);
        lightingClampMax = new Vector3f(1.0f, 1.0f, 1.0f);
        numLights = 1;
    }

    public Vector3f getLightingClampMin() {
        return lightingClampMin;
    }

    public void setLightingClampMin(Vector3f lightingClampMin) {
        this.lightingClampMin = lightingClampMin;
    }

    public Vector3f getLightingClampMax() {
        return lightingClampMax;
    }

    public void setLightingClampMax(Vector3f lightingClampMax) {
        this.lightingClampMax = lightingClampMax;
    }

    public int getNumLights() {
        return numLights;
    }

    public void setNumLights(int numLights) {
        this.numLights = numLights;
    }
}
