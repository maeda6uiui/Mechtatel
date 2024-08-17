package com.github.maeda6uiui.mechtatel.core.operation;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanImplCommon;
import com.github.maeda6uiui.mechtatel.core.vulkan.operation.VkTextureOperation;
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
            IMttVulkanImplCommon vulkanImplCommon,
            List<MttTexture> colorTextures,
            List<MttTexture> depthTextures,
            MttScreen dstScreen,
            boolean textureCleanupDelegation) {
        var vkColorTextures = new ArrayList<VkMttTexture>();
        colorTextures.forEach(v -> vkColorTextures.add(v.getVulkanTexture()));

        var vkDepthTextures = new ArrayList<VkMttTexture>();
        depthTextures.forEach(v -> vkDepthTextures.add(v.getVulkanTexture()));

        var dq = vulkanImplCommon.getDeviceAndQueues();
        vkTextureOperation = new VkTextureOperation(
                dq.device(),
                vulkanImplCommon.getCommandPool(),
                dq.graphicsQueue(),
                vulkanImplCommon.getTextureOperationNabor(),
                vulkanImplCommon.getColorImageFormat(),
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
