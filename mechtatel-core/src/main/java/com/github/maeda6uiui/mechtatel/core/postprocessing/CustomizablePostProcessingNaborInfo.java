package com.github.maeda6uiui.mechtatel.core.postprocessing;

import org.joml.Vector3f;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Info of customizable post-processing nabor
 *
 * @author maeda6uiui
 */
public class CustomizablePostProcessingNaborInfo {
    public enum LightingType {
        PARALLEL,
        POINT,
        SPOT
    }

    public enum UniformResourceType {
        CAMERA,
        FOG,
        LIGHTING_INFO,
        PARALLEL_LIGHT,
        POINT_LIGHT,
        SIMPLE_BLUR,
        SPOTLIGHT
    }

    private List<URL> vertShaderResources;
    private List<URL> fragShaderResources;
    private List<UniformResourceType> uniformResourceTypes;
    private LightingType lightingType;
    private Vector3f lightingClampMin;
    private Vector3f lightingClampMax;

    public CustomizablePostProcessingNaborInfo(List<URL> vertShaderResources, List<URL> fragShaderResources) {
        this.vertShaderResources = vertShaderResources;
        this.fragShaderResources = fragShaderResources;
        uniformResourceTypes = new ArrayList<>();
        uniformResourceTypes.add(UniformResourceType.CAMERA);
        lightingType = LightingType.PARALLEL;
        lightingClampMin = new Vector3f(0.0f, 0.0f, 0.0f);
        lightingClampMax = new Vector3f(1.0f, 1.0f, 1.0f);
    }

    @Deprecated
    public CustomizablePostProcessingNaborInfo(URL vertShaderResource, URL fragShaderResource) {
        this(List.of(vertShaderResource), List.of(fragShaderResource));
    }

    @Deprecated
    public URL getVertShaderResource() {
        return vertShaderResources.getFirst();
    }

    public List<URL> getVertShaderResources() {
        return vertShaderResources;
    }

    @Deprecated
    public URL getFragShaderResource() {
        return fragShaderResources.getFirst();
    }

    public List<URL> getFragShaderResources() {
        return fragShaderResources;
    }

    public List<UniformResourceType> getUniformResourceTypes() {
        return uniformResourceTypes;
    }

    public void setUniformResourceTypes(List<UniformResourceType> uniformResourceTypes) {
        this.uniformResourceTypes = uniformResourceTypes;
    }

    public LightingType getLightingType() {
        return lightingType;
    }

    public void setLightingType(LightingType lightingType) {
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
