package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.vulkan.texture.VkTexture;

/**
 * Interface to MttVulkanInstance providing access to required methods for textures
 *
 * @author maeda6uiui
 */
public interface IMttVulkanInstanceForTexture {
    VkTexture createTexture(String screenName, String textureFilepath, boolean generateMipmaps);

    VkTexture texturizeScreen(String srcScreenName, String dstScreenName);
}
