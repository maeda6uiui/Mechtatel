package com.github.maeda6uiui.mechtatel.core.screen;

import com.github.maeda6uiui.mechtatel.core.component.MttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;

/**
 * Interface of {@link MttScreen} for components
 *
 * @author maeda6uiui
 */
public interface IMttScreenForMttComponent {
    void addComponents(MttComponent... cs);

    VkMttScreen getVulkanScreen();
}
