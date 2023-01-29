package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkLine3DSet;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

/**
 * Set of 3D lines
 *
 * @author maeda6uiui
 */
public class Line3DSet extends Component3D {
    private VkLine3DSet vkLineSet;

    public Line3DSet(MttVulkanInstance vulkanInstance) {
        super(vulkanInstance);

        vkLineSet = vulkanInstance.createLine3DSet();
        this.associateVulkanComponent(vkLineSet);
    }

    public void add(Vertex3D v1, Vertex3D v2) {
        vkLineSet.add(v1, v2);
    }

    public void add(Vector3fc p1, Vector4fc color1, Vector3fc p2, Vector4fc color2) {
        vkLineSet.add(p1, color1, p2, color2);
    }

    public void add(Vector3fc p1, Vector3fc p2, Vector4fc color) {
        vkLineSet.add(p1, p2, color);
    }

    public void clear(boolean doCleanup) {
        vkLineSet.clear(doCleanup);
    }

    public void createBuffer() {
        vkLineSet.createBuffer();
    }
}
