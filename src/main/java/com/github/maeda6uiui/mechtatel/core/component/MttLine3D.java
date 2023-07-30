package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttLine3D;

/**
 * 3D line
 *
 * @author maeda6uiui
 */
public class MttLine3D extends MttComponent {
    private VkMttLine3D vkLine;

    public MttLine3D(MttVulkanInstance vulkanInstance, MttVertex3D v1, MttVertex3D v2) {
        super(vulkanInstance);

        vkLine = vulkanInstance.createLine3D(v1, v2);
        this.associateVulkanComponent(vkLine);
    }
}
