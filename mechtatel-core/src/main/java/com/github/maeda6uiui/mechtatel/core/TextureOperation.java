package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
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
    private TextureOperationParameters parameters;

    private MttTexture firstColorTexture;
    private MttTexture firstDepthTexture;
    private MttTexture secondColorTexture;
    private MttTexture secondDepthTexture;

    private boolean textureCleanupDelegation;
    private boolean isValid;

    public TextureOperation(
            MttVulkanImpl vulkanImpl,
            MttTexture firstColorTexture,
            MttTexture firstDepthTexture,
            MttTexture secondColorTexture,
            MttTexture secondDepthTexture,
            MttScreen dstScreen,
            boolean textureCleanupDelegation) {
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
        resultTexture = new MttTexture(dstScreen, vkTextureOperation.getResultTexture());
        parameters = new TextureOperationParameters();

        this.firstColorTexture = firstColorTexture;
        this.firstDepthTexture = firstDepthTexture;
        this.secondColorTexture = secondColorTexture;
        this.secondDepthTexture = secondDepthTexture;

        this.textureCleanupDelegation = textureCleanupDelegation;
        isValid = true;
    }

    public void cleanup() {
        if (isValid) {
            vkTextureOperation.cleanup();
            resultTexture.cleanup();

            if (textureCleanupDelegation) {
                firstColorTexture.cleanup();
                firstDepthTexture.cleanup();
                secondColorTexture.cleanup();
                secondDepthTexture.cleanup();
            }
        }
        isValid = false;
    }

    public void run() {
        vkTextureOperation.run(parameters);
    }

    public MttTexture getResultTexture() {
        return resultTexture;
    }

    public void setParameters(TextureOperationParameters parameters) {
        this.parameters = parameters;
    }

    public TextureOperationParameters getParameters() {
        return parameters;
    }

    public boolean isValid() {
        return isValid;
    }
}
