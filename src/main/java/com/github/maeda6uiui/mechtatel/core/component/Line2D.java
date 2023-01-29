package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkLine2D;
import org.joml.Vector3f;

/**
 * 2D line
 *
 * @author maeda6uiui
 */
public class Line2D extends Component {
    private VkLine2D vkLine;

    public Line2D(MttVulkanInstance vulkanInstance, Vertex2D p1, Vertex2D p2, float z) {
        super(vulkanInstance);

        var v1 = new Vertex3D(new Vector3f(p1.pos.x(), p1.pos.y(), z), p1.color);
        var v2 = new Vertex3D(new Vector3f(p2.pos.x(), p2.pos.y(), z), p2.color);
        vkLine = vulkanInstance.createLine2D(v1, v2);
        this.associateVulkanComponent(vkLine);
    }
}
