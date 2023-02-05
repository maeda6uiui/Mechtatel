package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Vector2fc;
import org.joml.Vector4fc;

/**
 * 2D quadrangle
 *
 * @author maeda6uiui
 */
public class Quad2D extends Component {
    private Line2DSet lineSet;

    private void setupLineSet(Vertex2D v1, Vertex2D v2, Vertex2D v3, Vertex2D v4, float z) {
        lineSet.add(v1, v2, z);
        lineSet.add(v2, v3, z);
        lineSet.add(v3, v4, z);
        lineSet.add(v4, v1, z);
        lineSet.createBuffer();
    }

    public Quad2D(MttVulkanInstance vulkanInstance, Vertex2D v1, Vertex2D v2, Vertex2D v3, Vertex2D v4, float z) {
        super(vulkanInstance);

        lineSet = new Line2DSet(vulkanInstance);
        this.setupLineSet(v1, v2, v3, v4, z);
        this.associateVulkanComponent(lineSet.getVulkanComponent());
    }

    public Quad2D(MttVulkanInstance vulkanInstance, Vector2fc p1, Vector2fc p2, Vector2fc p3, Vector2fc p4, float z, Vector4fc color) {
        super(vulkanInstance);

        lineSet = new Line2DSet(vulkanInstance);
        var v1 = new Vertex2D(p1, color);
        var v2 = new Vertex2D(p2, color);
        var v3 = new Vertex2D(p3, color);
        var v4 = new Vertex2D(p4, color);
        this.setupLineSet(v1, v2, v3, v4, z);
        this.associateVulkanComponent(lineSet.getVulkanComponent());
    }
}
