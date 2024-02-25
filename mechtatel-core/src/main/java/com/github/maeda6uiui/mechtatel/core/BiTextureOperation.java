package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.VkBiTextureOperation;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.texture.VkMttTexture;

import java.util.ArrayList;
import java.util.List;

/**
 * Texture operation that consumes two textures
 *
 * @author maeda6uiui
 */
public class BiTextureOperation {
    private VkBiTextureOperation vkBiTextureOperation;
    private MttTexture resultTexture;
    private BiTextureOperationParameters biParameters;

    private List<MttTexture> colorTextures;
    private List<MttTexture> depthTextures;

    private boolean textureCleanupDelegation;
    private boolean isValid;

    public BiTextureOperation(
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
        vkBiTextureOperation = new VkBiTextureOperation(
                dq.device(),
                vulkanImpl.getCommandPool(),
                dq.graphicsQueue(),
                vulkanImpl.getTextureOperationNabor(),
                vulkanImpl.getSwapchainImageFormat(),
                vkColorTextures,
                vkDepthTextures,
                dstScreen.getVulkanScreen()
        );
        resultTexture = new MttTexture(dstScreen, vkBiTextureOperation.getResultTexture());
        biParameters = new BiTextureOperationParameters();

        this.colorTextures = colorTextures;
        this.depthTextures = depthTextures;

        this.textureCleanupDelegation = textureCleanupDelegation;
        isValid = true;
    }

    public void cleanup() {
        if (isValid) {
            vkBiTextureOperation.cleanup();
            resultTexture.cleanup();

            if (textureCleanupDelegation) {
                colorTextures.forEach(MttTexture::cleanup);
                depthTextures.forEach(MttTexture::cleanup);
            }
        }
        isValid = false;
    }

    public void run() {
        vkBiTextureOperation.run(biParameters);
    }

    public MttTexture getResultTexture() {
        return resultTexture;
    }

    public void setBiParameters(BiTextureOperationParameters biParameters) {
        this.biParameters = biParameters;
    }

    public BiTextureOperationParameters getBiParameters() {
        return biParameters;
    }

    public boolean isValid() {
        return isValid;
    }
}
