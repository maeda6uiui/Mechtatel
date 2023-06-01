package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing;

import java.util.ArrayList;
import java.util.List;

/**
 * Info for versatile nabor
 *
 * @author maeda6uiui
 */
public class VersatileNaborInfo {
    private String vertShaderFilepath;
    private String fragShaderFilepath;
    private List<String> uniformResources;

    public VersatileNaborInfo(String vertShaderFilepath, String fragShaderFilepath) {
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
