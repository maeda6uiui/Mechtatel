package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkLine2DSet;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector4fc;

/**
 * Set of 2D lines
 *
 * @author maeda
 */
public class Line2DSet extends Component {
    private VkLine2DSet vkLineSet;

    public Line2DSet(MttVulkanInstance vulkanInstance) {
        super(vulkanInstance);

        vkLineSet = vulkanInstance.createLine2DSet();
        this.associateVulkanComponent(vkLineSet);
    }

    public void add(Vertex2D p1, Vertex2D p2, float z) {
        var v1 = new Vertex3D(new Vector3f(p1.pos.x(), p1.pos.y(), z), p1.color, new Vector3f(0.0f, 1.0f, 0.0f));
        var v2 = new Vertex3D(new Vector3f(p2.pos.x(), p2.pos.y(), z), p2.color, new Vector3f(0.0f, 1.0f, 0.0f));
        vkLineSet.add(v1, v2);
    }

    public void add(Vector2fc p1, Vector4fc color1, Vector2fc p2, Vector4fc color2, float z) {
        var v1 = new Vertex3D(new Vector3f(p1.x(), p1.y(), z), color1, new Vector3f(0.0f, 1.0f, 0.0f));
        var v2 = new Vertex3D(new Vector3f(p2.x(), p2.y(), z), color2, new Vector3f(0.0f, 1.0f, 0.0f));
        vkLineSet.add(v1, v2);
    }

    public void add(Vector2fc p1, Vector2fc p2, Vector4fc color, float z) {
        var v1 = new Vertex3D(new Vector3f(p1.x(), p1.y(), z), color, new Vector3f(0.0f, 1.0f, 0.0f));
        var v2 = new Vertex3D(new Vector3f(p2.x(), p2.y(), z), color, new Vector3f(0.0f, 1.0f, 0.0f));
        vkLineSet.add(v1, v2);
    }

    public void clear(boolean doCleanup) {
        vkLineSet.clear(doCleanup);
    }

    public void createBuffer() {
        vkLineSet.createBuffer();
    }
}
