package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttQuad;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector4fc;

import java.util.ArrayList;

/**
 * 2D quadrangle
 *
 * @author maeda6uiui
 */
public class MttQuad2D extends MttComponent {
    private VkMttQuad vkQuad;

    private void setup(
            MttVulkanImpl vulkanImpl,
            MttVertex2D v1,
            MttVertex2D v2,
            MttVertex2D v3,
            MttVertex2D v4,
            float z,
            boolean fill) {
        var vv1 = new MttVertex(new Vector3f(v1.pos.x(), v1.pos.y(), z), v1.color);
        var vv2 = new MttVertex(new Vector3f(v2.pos.x(), v2.pos.y(), z), v2.color);
        var vv3 = new MttVertex(new Vector3f(v3.pos.x(), v3.pos.y(), z), v3.color);
        var vv4 = new MttVertex(new Vector3f(v4.pos.x(), v4.pos.y(), z), v4.color);

        var vertices = new ArrayList<MttVertex>();
        vertices.add(vv1);
        vertices.add(vv2);
        vertices.add(vv3);
        vertices.add(vv4);

        vkQuad = vulkanImpl.createQuad(vertices, fill);
        vkQuad.setTwoDComponent(true);
        this.associateVulkanComponent(vkQuad);
    }

    public MttQuad2D(
            MttVulkanImpl vulkanImpl,
            MttVertex2D v1,
            MttVertex2D v2,
            MttVertex2D v3,
            MttVertex2D v4,
            float z,
            boolean fill) {
        super(vulkanImpl);

        this.setup(vulkanImpl, v1, v2, v3, v4, z, fill);
    }

    public MttQuad2D(
            MttVulkanImpl vulkanImpl,
            Vector2fc p1,
            Vector2fc p2,
            Vector2fc p3,
            Vector2fc p4,
            float z,
            boolean fill,
            Vector4fc color) {
        super(vulkanImpl);

        var v1 = new MttVertex2D(new Vector2f(p1.x(), p1.y()), color);
        var v2 = new MttVertex2D(new Vector2f(p2.x(), p2.y()), color);
        var v3 = new MttVertex2D(new Vector2f(p3.x(), p3.y()), color);
        var v4 = new MttVertex2D(new Vector2f(p4.x(), p4.y()), color);
        this.setup(vulkanImpl, v1, v2, v3, v4, z, fill);
    }

    public MttQuad2D(
            MttVulkanImpl vulkanImpl,
            Vector2fc topLeft,
            Vector2fc bottomRight,
            float z,
            boolean fill,
            Vector4fc color) {
        super(vulkanImpl);

        var v1 = new MttVertex2D(new Vector2f(topLeft.x(), topLeft.y()), color);
        var v2 = new MttVertex2D(new Vector2f(topLeft.x(), bottomRight.y()), color);
        var v3 = new MttVertex2D(new Vector2f(bottomRight.x(), bottomRight.y()), color);
        var v4 = new MttVertex2D(new Vector2f(bottomRight.x(), topLeft.y()), color);
        this.setup(vulkanImpl, v1, v2, v3, v4, z, fill);
    }
}
