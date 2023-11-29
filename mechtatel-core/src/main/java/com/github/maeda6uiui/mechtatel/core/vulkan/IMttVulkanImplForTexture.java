package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.vulkan.texture.VkMttTexture;

import java.net.URI;

/**
 * Interface to {@link MttVulkanImpl} for Vulkan textures
 *
 * @author maeda6uiui
 */
public interface IMttVulkanImplForTexture {
    VkMttTexture createTexture(String screenName, URI textureResource, boolean generateMipmaps);

    VkMttTexture texturizeColorOfScreen(String srcScreenName, String dstScreenName);

    VkMttTexture texturizeDepthOfScreen(String srcScreenName, String dstScreenName);

    boolean removeTexture(VkMttTexture texture);
}
