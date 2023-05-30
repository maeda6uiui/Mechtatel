package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.util.ModelLoader;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttModel3D;

import java.io.IOException;
import java.util.Set;

/**
 * 3D model
 *
 * @author maeda6uiui
 */
public class MttModel3D extends MttComponent3D {
    private String modelFilepath;
    private VkMttModel3D vkModel;

    public MttModel3D(MttVulkanInstance vulkanInstance, String screenName, String modelFilepath) throws IOException {
        super(vulkanInstance);

        this.modelFilepath = modelFilepath;
        vkModel = vulkanInstance.createModel3D(screenName, modelFilepath);
        this.associateVulkanComponent(vkModel);
    }

    public MttModel3D(MttVulkanInstance vulkanInstance, MttModel3D srcModel) {
        super(vulkanInstance);

        this.modelFilepath = srcModel.modelFilepath;
        vkModel = vulkanInstance.duplicateModel3D(srcModel.vkModel);
        this.associateVulkanComponent(vkModel);
    }

    public String getModelFilepath() {
        return modelFilepath;
    }

    public ModelLoader.Model getModel() {
        return vkModel.getModel();
    }

    public Set<Integer> getTextureIndices() {
        return vkModel.getTextureIndices();
    }

    public void replaceTexture(int index, MttTexture newTexture) {
        vkModel.replaceTexture(index, newTexture.getVulkanTexture());
    }
}
