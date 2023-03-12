package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkFilledQuad2D;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector4fc;

import java.util.ArrayList;

/**
 * 2D filled quadrangle
 *
 * @author maeda6uiui
 */
public class FilledQuad2D extends Component {
    private VkFilledQuad2D vkFilledQuad;

    public FilledQuad2D(
            MttVulkanInstance vulkanInstance,
            Vertex2D p1,
            Vertex2D p2,
            Vertex2D p3,
            Vertex2D p4,
            float z) {
        super(vulkanInstance);

        var v1 = new Vertex3D(new Vector3f(p1.pos.x(), p1.pos.y(), z), p1.color);
        var v2 = new Vertex3D(new Vector3f(p2.pos.x(), p2.pos.y(), z), p2.color);
        var v3 = new Vertex3D(new Vector3f(p3.pos.x(), p3.pos.y(), z), p3.color);
        var v4 = new Vertex3D(new Vector3f(p4.pos.x(), p4.pos.y(), z), p4.color);

        var vertices = new ArrayList<Vertex3D>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkFilledQuad = vulkanInstance.createFilledQuad2D(vertices);
        this.associateVulkanComponent(vkFilledQuad);
    }

    public FilledQuad2D(
            MttVulkanInstance vulkanInstance,
            Vector2fc p1,
            Vector2fc p2,
            Vector2fc p3,
            Vector2fc p4,
            float z,
            Vector4fc color) {
        super(vulkanInstance);

        var v1 = new Vertex3D(new Vector3f(p1.x(), p1.y(), z), color);
        var v2 = new Vertex3D(new Vector3f(p2.x(), p2.y(), z), color);
        var v3 = new Vertex3D(new Vector3f(p3.x(), p3.y(), z), color);
        var v4 = new Vertex3D(new Vector3f(p4.x(), p4.y(), z), color);

        var vertices = new ArrayList<Vertex3D>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkFilledQuad = vulkanInstance.createFilledQuad2D(vertices);
        this.associateVulkanComponent(vkFilledQuad);
    }

    public FilledQuad2D(
            MttVulkanInstance vulkanInstance,
            Vector2fc topLeft,
            Vector2fc bottomRight,
            float z,
            Vector4fc color) {
        super(vulkanInstance);

        var v1 = new Vertex3D(new Vector3f(topLeft.x(), topLeft.y(), z), color);
        var v2 = new Vertex3D(new Vector3f(topLeft.x(), bottomRight.y(), z), color);
        var v3 = new Vertex3D(new Vector3f(bottomRight.x(), bottomRight.y(), z), color);
        var v4 = new Vertex3D(new Vector3f(bottomRight.x(), topLeft.y(), z), color);

        var vertices = new ArrayList<Vertex3D>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkFilledQuad = vulkanInstance.createFilledQuad2D(vertices);
        this.associateVulkanComponent(vkFilledQuad);
    }
}
