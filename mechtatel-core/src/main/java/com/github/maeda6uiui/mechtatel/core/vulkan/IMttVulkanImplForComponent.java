package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttComponent;

/**
 * Interface to {@link MttVulkanImpl} for Vulkan components
 *
 * @author maeda6uiui
 */
public interface IMttVulkanImplForComponent {
    boolean removeComponent(VkMttComponent component);
}
