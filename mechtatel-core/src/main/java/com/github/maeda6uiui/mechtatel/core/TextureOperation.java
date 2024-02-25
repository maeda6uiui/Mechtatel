package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.VkTextureOperation;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.texture.VkMttTexture;

import java.util.ArrayList;
import java.util.List;

/**
 * Texture operation
 *
 * @author maeda6uiui
 */
public class TextureOperation {
    private VkTextureOperation vkTextureOperation;
    private MttTexture resultTexture;
    private TextureOperationParameters parameters;

    private List<MttTexture> colorTextures;
    private List<MttTexture> depthTextures;

    private boolean textureCleanupDelegation;
    private boolean isValid;

    public TextureOperation(
            MttVulkanImpl vulkanImpl,
            List<MttTexture> colorTextures,
            List<MttTexture> depthTextures,
            MttScreen dstScreen,
            boolean textureCleanupDelegation) {
        var vkColorTextures = new ArrayList<VkMttTexture>();
        colorTextures.forEach(v -> vkColorTextures.add(v.getVulkanTexture()));

        var vkDepthTextures = new ArrayList<VkMttTexture>();
        depthTextures.forEach(v -> vkDepthTextures.add(v.getVulkanTexture()));

        var dq = vulkanImpl.getDeviceAndQueues();
        vkTextureOperation = new VkTextureOperation(
                dq.device(),
                vulkanImpl.getCommandPool(),
                dq.graphicsQueue(),
                vulkanImpl.getTextureOperationNabor(),
                vulkanImpl.getSwapchainImageFormat(),
                vkColorTextures,
                vkDepthTextures,
                dstScreen.getVulkanScreen()
        );
        resultTexture = new MttTexture(dstScreen, vkTextureOperation.getResultTexture());
        parameters = new TextureOperationParameters();

        this.colorTextures = colorTextures;
        this.depthTextures = depthTextures;

        this.textureCleanupDelegation = textureCleanupDelegation;
        isValid = true;
    }

    public void cleanup() {
        if (isValid) {
            vkTextureOperation.cleanup();
            resultTexture.cleanup();

            if (textureCleanupDelegation) {
                colorTextures.forEach(MttTexture::cleanup);
                depthTextures.forEach(MttTexture::cleanup);
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
