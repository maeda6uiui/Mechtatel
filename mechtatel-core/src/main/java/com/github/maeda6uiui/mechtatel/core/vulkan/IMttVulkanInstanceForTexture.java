package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.vulkan.texture.VkMttTexture;

import java.net.URL;

/**
 * Interface to MttVulkanInstance providing access to required methods for textures
 *
 * @author maeda6uiui
 */
public interface IMttVulkanInstanceForTexture {
    VkMttTexture createTexture(String screenName, URL textureResource, boolean generateMipmaps);

    VkMttTexture texturizeColorOfScreen(String srcScreenName, String dstScreenName);

    VkMttTexture texturizeDepthOfScreen(String srcScreenName, String dstScreenName);

    boolean removeTexture(VkMttTexture texture);
}
