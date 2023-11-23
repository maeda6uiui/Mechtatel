package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttLine;
import org.joml.Vector3f;

/**
 * 2D line
 *
 * @author maeda6uiui
 */
public class MttLine2D extends MttComponent {
    private VkMttLine vkLine;

    public MttLine2D(MttVulkanInstance vulkanInstance, MttVertex2D p1, MttVertex2D p2, float z) {
        super(vulkanInstance);

        var v1 = new MttVertex3D(new Vector3f(p1.pos.x(), p1.pos.y(), z), p1.color);
        var v2 = new MttVertex3D(new Vector3f(p2.pos.x(), p2.pos.y(), z), p2.color);
        vkLine = vulkanInstance.createLine(v1, v2);
        vkLine.setTwoDComponent(true);
        this.associateVulkanComponent(vkLine);
    }
}
