package com.github.maeda6uiui.mechtatel.core.vulkan.screen;

/**
 * Interface to vulkan screen providing access to texture-relating methods
 *
 * @author maeda6uiui
 */
public interface IVkMttScreenForVkMttTexture {
    void updateTextureDescriptorSets(int allocationIndex, long textureImageView);

    void resetTextureDescriptorSets(int allocationIndex);
}
