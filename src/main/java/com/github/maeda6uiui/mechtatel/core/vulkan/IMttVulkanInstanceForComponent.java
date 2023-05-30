package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttComponent;

/**
 * Interface to MttVulkanInstance providing access to deleteComponent()
 *
 * @author maeda6uiui
 */
public interface IMttVulkanInstanceForComponent {
    boolean removeComponent(VkMttComponent component);
}
