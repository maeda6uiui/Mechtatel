package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.util.ModelLoader;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttModel;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * Model
 *
 * @author maeda6uiui
 */
public class MttModel extends MttComponent {
    private URL modelResource;
    private VkMttModel vkModel;

    public MttModel(MttVulkanInstance vulkanInstance, String screenName, URL modelResource) throws IOException {
        super(vulkanInstance);

        this.modelResource = modelResource;
        vkModel = vulkanInstance.createModel(screenName, modelResource);
        this.associateVulkanComponent(vkModel);
    }

    public MttModel(MttVulkanInstance vulkanInstance, MttModel srcModel) {
        super(vulkanInstance);

        this.modelResource = srcModel.modelResource;
        vkModel = vulkanInstance.duplicateModel(srcModel.vkModel);
        this.associateVulkanComponent(vkModel);
    }

    public URL getModelResource() {
        return modelResource;
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

    public void setDrawMeshIndices(List<Integer> drawMeshIndices) {
        vkModel.setDrawMeshIndices(drawMeshIndices);
    }
}
