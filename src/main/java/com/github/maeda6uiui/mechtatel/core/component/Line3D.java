package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkLine3D;

/**
 * 3D line
 *
 * @author maeda
 */
public class Line3D extends Component3D {
    private VkLine3D vkLine;

    public Line3D(MttVulkanInstance vulkanInstance, Vertex3D v1, Vertex3D v2) {
        super(vulkanInstance);

        vkLine = vulkanInstance.createLine3D(v1, v2);
        this.associateVulkanComponent(vkLine);
    }
}
