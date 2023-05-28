package com.github.maeda6uiui.mechtatel.core.texture;

import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanInstanceForTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.VkTexture;

/**
 * Texture
 *
 * @author maeda6uiui
 */
public class MttTexture {
    private VkTexture texture;
    private IMttVulkanInstanceForTexture vulkanInstance;

    public MttTexture(
            IMttVulkanInstanceForTexture vulkanInstance,
            String screenName,
            String textureFilepath,
            boolean generateMipmaps) {
        texture = vulkanInstance.createTexture(screenName, textureFilepath, generateMipmaps);
        this.vulkanInstance = vulkanInstance;
    }

    public MttTexture(
            IMttVulkanInstanceForTexture vulkanInstance,
            String srcScreenName,
            String dstScreenName,
            String textureType) {
        if (textureType.equals("color")) {
            texture = vulkanInstance.texturizeColorOfScreen(srcScreenName, dstScreenName);
        } else if (textureType.equals("depth")) {
            texture = vulkanInstance.texturizeDepthOfScreen(srcScreenName, dstScreenName);
        } else {
            throw new RuntimeException("Unsupported texture type");
        }

        this.vulkanInstance = vulkanInstance;
    }

    public MttTexture(IMttVulkanInstanceForTexture vulkanInstance, VkTexture texture) {
        this.texture = texture;
        this.vulkanInstance = vulkanInstance;
    }

    public void cleanup() {
        texture.cleanup();
        vulkanInstance.removeTexture(texture);
    }

    public VkTexture getVulkanTexture() {
        return texture;
    }
}
