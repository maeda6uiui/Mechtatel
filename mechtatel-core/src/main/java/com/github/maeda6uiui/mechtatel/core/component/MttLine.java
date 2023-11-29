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

    public MttLine(MttVulkanImpl vulkanImpl, MttVertex3D v1, MttVertex3D v2) {
        super(vulkanImpl);

        vkLine = vulkanImpl.createLine(v1, v2);
        this.associateVulkanComponent(vkLine);
    }
}
