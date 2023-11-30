package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttLine;

/**
 * Line
 *
 * @author maeda6uiui
 */
public class MttLine extends MttComponent {
    private VkMttLine vkLine;

    public MttLine(MttVulkanImpl vulkanImpl, MttVertex v1, MttVertex v2) {
        super(vulkanImpl);

        vkLine = vulkanImpl.createLine(v1, v2);
        this.associateVulkanComponent(vkLine);
    }
}
