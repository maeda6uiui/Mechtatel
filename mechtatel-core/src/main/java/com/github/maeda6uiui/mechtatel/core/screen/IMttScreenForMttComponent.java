package com.github.maeda6uiui.mechtatel.core.screen;

import com.github.maeda6uiui.mechtatel.core.screen.component.MttComponent;
import com.github.maeda6uiui.mechtatel.core.screen.component.gui.MttGuiComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;

/**
 * Interface of {@link MttScreen} for components
 *
 * @author maeda6uiui
 */
public interface IMttScreenForMttComponent {
    void addComponents(MttComponent... cs);

    void addGuiComponents(MttGuiComponent... cs);

    VkMttScreen getVulkanScreen();
}
