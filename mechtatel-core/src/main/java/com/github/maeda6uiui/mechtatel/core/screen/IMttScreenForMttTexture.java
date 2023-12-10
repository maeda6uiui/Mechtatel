package com.github.maeda6uiui.mechtatel.core.screen;

import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;

/**
 * Interface of {@link MttScreen} for textures
 *
 * @author maeda6uiui
 */
public interface IMttScreenForMttTexture {
    void addTexture(MttTexture texture);

    VkMttScreen getVulkanScreen();
}
