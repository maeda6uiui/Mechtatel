package com.github.maeda6uiui.mechtatel.core.screen;

import java.util.ArrayList;
import java.util.List;

/**
 * Info for extra post-processing nabor
 *
 * @author maeda6uiui
 */
public class ExternalPostProcessingNaborInfo {
    private String vertShaderFilepath;
    private String fragShaderFilepath;
    private List<String> uniformResources;

    public ExternalPostProcessingNaborInfo(String vertShaderFilepath, String fragShaderFilepath) {
        this.vertShaderFilepath = vertShaderFilepath;
        this.fragShaderFilepath = fragShaderFilepath;
        uniformResources = new ArrayList<>();
    }

    public void clearUniformResources() {
        uniformResources.clear();
    }

    public void addUniformResource(String uniformResource) {
        uniformResources.add(uniformResource);
    }

    public String getVertShaderFilepath() {
        return vertShaderFilepath;
    }

    public String getFragShaderFilepath() {
        return fragShaderFilepath;
    }

    public List<String> getUniformResources() {
        return new ArrayList<>(uniformResources);
    }
}
