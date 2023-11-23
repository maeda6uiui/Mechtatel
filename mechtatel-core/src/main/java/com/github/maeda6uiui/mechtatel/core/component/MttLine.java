package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttLine;

/**
 * Line
 *
 * @author maeda6uiui
 */
public class MttLine extends MttComponent {
    private VkMttLine vkLine;

    public MttLine(MttVulkanInstance vulkanInstance, MttVertex3D v1, MttVertex3D v2) {
        super(vulkanInstance);

        vkLine = vulkanInstance.createLine(v1, v2);
        this.associateVulkanComponent(vkLine);
    }
}
