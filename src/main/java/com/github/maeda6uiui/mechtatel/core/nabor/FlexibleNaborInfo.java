package com.github.maeda6uiui.mechtatel.core.nabor;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Info for flexible nabor
 *
 * @author maeda6uiui
 */
public class FlexibleNaborInfo {
    private String vertShaderFilepath;
    private String fragShaderFilepath;
    private List<String> uniformResources;
    private String lightingType;
    private Vector3f lightingClampMin;
    private Vector3f lightingClampMax;

    public FlexibleNaborInfo(String vertShaderFilepath, String fragShaderFilepath) {
        this.vertShaderFilepath = vertShaderFilepath;
        this.fragShaderFilepath = fragShaderFilepath;
        uniformResources = new ArrayList<>();
        lightingType = "parallel_light";
        lightingClampMin = new Vector3f(0.0f, 0.0f, 0.0f);
        lightingClampMax = new Vector3f(1.0f, 1.0f, 1.0f);
    }

    public String getVertShaderFilepath() {
        return vertShaderFilepath;
    }

    public String getFragShaderFilepath() {
        return fragShaderFilepath;
    }

    public List<String> getUniformResources() {
        return uniformResources;
    }

    public void setUniformResources(List<String> uniformResources) {
        this.uniformResources = uniformResources;
    }

    public String getLightingType() {
        return lightingType;
    }

    public void setLightingType(String lightingType) {
        this.lightingType = lightingType;
    }

    public Vector3f getLightingClampMin() {
        return new Vector3f(lightingClampMin);
    }

    public void setLightingClampMin(Vector3f lightingClampMin) {
        this.lightingClampMin = lightingClampMin;
    }

    public Vector3f getLightingClampMax() {
        return new Vector3f(lightingClampMax);
    }

    public void setLightingClampMax(Vector3f lightingClampMax) {
        this.lightingClampMax = lightingClampMax;
    }
}
