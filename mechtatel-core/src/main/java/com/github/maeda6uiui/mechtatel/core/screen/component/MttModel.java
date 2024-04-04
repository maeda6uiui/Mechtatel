package com.github.maeda6uiui.mechtatel.core.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.util.model.Model;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttModel;

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

    public MttModel(MttVulkanImpl vulkanImpl, IMttScreenForMttComponent screen, URI modelResource) throws IOException {
        super(
                screen,
                new MttComponentCreateInfo()
                        .setVisible(true)
                        .setTwoDComponent(false)
                        .setCastShadow(true)
                        .setDrawOrder(0)
        );

        this.modelResource = modelResource;

        var dq = vulkanImpl.getDeviceAndQueues();
        vkModel = new VkMttModel(
                this, dq.device(),
                vulkanImpl.getCommandPool(),
                dq.graphicsQueue(),
                screen.getVulkanScreen(),
                modelResource
        );
        this.associateVulkanComponents(vkModel);
    }

    public MttModel(MttVulkanImpl vulkanImpl, IMttScreenForMttComponent screen, MttModel srcModel) {
        super(
                screen,
                new MttComponentCreateInfo()
                        .setVisible(true)
                        .setTwoDComponent(false)
                        .setCastShadow(true)
                        .setDrawOrder(0)
        );

        this.modelResource = srcModel.modelResource;

        var dq = vulkanImpl.getDeviceAndQueues();
        vkModel = new VkMttModel(
                this, dq.device(),
                vulkanImpl.getCommandPool(),
                dq.graphicsQueue(),
                srcModel.vkModel
        );
        this.associateVulkanComponents(vkModel);
    }

    public URI getModelResource() {
        return modelResource;
    }

    public Model getModel() {
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
