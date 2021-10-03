package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkComponent;

/**
 * Interface to MttVulkanInstance providing access to deleteComponent()
 *
 * @author maeda
 */
public interface IMttVulkanInstanceForComponent {
    public boolean removeComponent(VkComponent component);
}
