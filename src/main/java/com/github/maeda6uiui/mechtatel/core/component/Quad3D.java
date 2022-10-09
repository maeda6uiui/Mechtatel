package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

/**
 * 3D quadrangle
 *
 * @author maeda
 */
public class Quad3D extends Component3D {
    private Line3DSet lineSet;

    private void setupLineSet(Vertex3D v1, Vertex3D v2, Vertex3D v3, Vertex3D v4) {
        lineSet.add(v1, v2);
        lineSet.add(v2, v3);
        lineSet.add(v3, v4);
        lineSet.add(v4, v1);
        lineSet.createBuffer();
    }

    public Quad3D(MttVulkanInstance vulkanInstance, Vertex3D v1, Vertex3D v2, Vertex3D v3, Vertex3D v4) {
        super(vulkanInstance);

        lineSet = new Line3DSet(vulkanInstance);
        this.setupLineSet(v1, v2, v3, v4);
        this.associateVulkanComponent(lineSet.getVulkanComponent());
    }

    public Quad3D(MttVulkanInstance vulkanInstance, Vector3fc p1, Vector3fc p2, Vector3fc p3, Vector3fc p4, Vector4fc color) {
        super(vulkanInstance);

        lineSet = new Line3DSet(vulkanInstance);
        var v1 = new Vertex3D(p1, color);
        var v2 = new Vertex3D(p2, color);
        var v3 = new Vertex3D(p3, color);
        var v4 = new Vertex3D(p4, color);
        this.setupLineSet(v1, v2, v3, v4);
        this.associateVulkanComponent(lineSet.getVulkanComponent());
    }
}
