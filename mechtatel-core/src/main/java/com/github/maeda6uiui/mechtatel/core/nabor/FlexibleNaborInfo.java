package com.github.maeda6uiui.mechtatel.core.nabor;

import jakarta.validation.constraints.NotNull;
import org.joml.Vector3f;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Info for flexible nabor
 *
 * @author maeda6uiui
 */
public class FlexibleNaborInfo {
    private URL vertShaderResource;
    private URL fragShaderResource;
    private List<String> uniformResources;
    private String lightingType;
    private Vector3f lightingClampMin;
    private Vector3f lightingClampMax;

    public FlexibleNaborInfo(@NotNull URL vertShaderResource, @NotNull URL fragShaderResource) {
        this.vertShaderResource = vertShaderResource;
        this.fragShaderResource = fragShaderResource;
        uniformResources = new ArrayList<>();
        uniformResources.add("camera");
        lightingType = "parallel_light";
        lightingClampMin = new Vector3f(0.0f, 0.0f, 0.0f);
        lightingClampMax = new Vector3f(1.0f, 1.0f, 1.0f);
    }

    public URL getVertShaderResource() {
        return vertShaderResource;
    }

    public URL getFragShaderResource() {
        return fragShaderResource;
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
