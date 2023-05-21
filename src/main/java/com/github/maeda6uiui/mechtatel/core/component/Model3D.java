package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.util.ModelLoader;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkModel3D;

import java.io.IOException;
import java.util.Set;

/**
 * 3D model
 *
 * @author maeda6uiui
 */
public class Model3D extends Component3D {
    private String modelFilepath;
    private VkModel3D vkModel;

    public Model3D(MttVulkanInstance vulkanInstance, String screenName, String modelFilepath) throws IOException {
        super(vulkanInstance);

        this.modelFilepath = modelFilepath;
        vkModel = vulkanInstance.createModel3D(screenName, modelFilepath);
        this.associateVulkanComponent(vkModel);
    }

    public Model3D(MttVulkanInstance vulkanInstance, Model3D srcModel) {
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
