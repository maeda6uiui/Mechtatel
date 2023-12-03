package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.VkTextureOperation;

/**
 * Texture operation
 *
 * @author maeda6uiui
 */
public class TextureOperation {
    private VkTextureOperation vkTextureOperation;
    private MttTexture resultTexture;

    public TextureOperation(
            MttVulkanImpl vulkanImpl,
            MttTexture firstColorTexture,
            MttTexture firstDepthTexture,
            MttTexture secondColorTexture,
            MttTexture secondDepthTexture,
            MttScreen dstScreen) {
        var dq = vulkanImpl.getDeviceAndQueues();
        vkTextureOperation = new VkTextureOperation(
                dq.device(),
                vulkanImpl.getCommandPool(),
                dq.graphicsQueue(),
                vulkanImpl.getTextureOperationNabor(),
                vulkanImpl.getSwapchainImageFormat(),
                firstColorTexture.getVulkanTexture(),
                firstDepthTexture.getVulkanTexture(),
                secondColorTexture.getVulkanTexture(),
                secondDepthTexture.getVulkanTexture(),
                dstScreen.getVulkanScreen()
        );

        resultTexture = new MttTexture(vulkanImpl, vkTextureOperation.getResultTexture());
    }

    public void cleanup() {
        vkTextureOperation.cleanup();
    }

    public MttTexture getResultTexture() {
        return resultTexture;
    }

    public void run(TextureOperationParameters parameters) {
        vkTextureOperation.run(parameters);
    }
}
