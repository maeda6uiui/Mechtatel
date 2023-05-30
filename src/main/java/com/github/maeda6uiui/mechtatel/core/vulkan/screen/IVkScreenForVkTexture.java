package com.github.maeda6uiui.mechtatel.core.vulkan.screen;

/**
 * Interface to VkScreen providing access to texture-relating methods
 *
 * @author maeda6uiui
 */
public interface IVkScreenForVkTexture {
    void updateTextureDescriptorSets(int allocationIndex, long textureImageView);

    void resetTextureDescriptorSets(int allocationIndex);
}
