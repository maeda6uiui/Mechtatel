package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.util.ModelLoader;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttModel;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Model
 *
 * @author maeda6uiui
 */
public class MttModel extends MttComponent {
    private URI modelResource;
    private VkMttModel vkModel;

    public MttModel(MttVulkanImpl vulkanImpl, String screenName, URI modelResource) throws IOException {
        super(vulkanImpl);

        this.modelResource = modelResource;
        vkModel = vulkanImpl.createModel(screenName, modelResource);
        this.associateVulkanComponent(vkModel);
    }

    public MttModel(MttVulkanImpl vulkanImpl, MttModel srcModel) {
        super(vulkanImpl);

        this.modelResource = srcModel.modelResource;
        vkModel = vulkanImpl.duplicateModel(srcModel.vkModel);
        this.associateVulkanComponent(vkModel);
    }

    public URI getModelResource() {
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
