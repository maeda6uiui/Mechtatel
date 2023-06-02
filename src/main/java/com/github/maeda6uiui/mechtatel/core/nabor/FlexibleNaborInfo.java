package com.github.maeda6uiui.mechtatel.core.nabor;

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

    public FlexibleNaborInfo(String vertShaderFilepath, String fragShaderFilepath) {
        this.vertShaderFilepath = vertShaderFilepath;
        this.fragShaderFilepath = fragShaderFilepath;
        uniformResources = new ArrayList<>();
        lightingType = "parallel_light";
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
}
