package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkTexturedQuad2D;
import org.joml.Vector3f;

import java.util.ArrayList;

/**
 * 2D textured quadrangle
 *
 * @author maeda6uiui
 */
public class TexturedQuad2D extends Component {
    private VkTexturedQuad2D vkTexturedQuad;

    public TexturedQuad2D(
            MttVulkanInstance vulkanInstance,
            String textureFilepath,
            Vertex2DUV p1,
            Vertex2DUV p2,
            Vertex2DUV p3,
            Vertex2DUV p4,
            float z) {
        super(vulkanInstance);

        var v1 = new Vertex3DUV(new Vector3f(p1.pos.x(), p1.pos.y(), z), p1.color, p1.texCoords);
        var v2 = new Vertex3DUV(new Vector3f(p2.pos.x(), p2.pos.y(), z), p2.color, p2.texCoords);
        var v3 = new Vertex3DUV(new Vector3f(p3.pos.x(), p3.pos.y(), z), p3.color, p3.texCoords);
        var v4 = new Vertex3DUV(new Vector3f(p4.pos.x(), p4.pos.y(), z), p4.color, p4.texCoords);

        var vertices = new ArrayList<Vertex3DUV>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkTexturedQuad = vulkanInstance.createTexturedQuad2D(textureFilepath, vertices);
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public TexturedQuad2D(
            MttVulkanInstance vulkanInstance,
            TexturedQuad2D srcQuad,
            Vertex2DUV p1,
            Vertex2DUV p2,
            Vertex2DUV p3,
            Vertex2DUV p4,
            float z) {
        super(vulkanInstance);

        var v1 = new Vertex3DUV(new Vector3f(p1.pos.x(), p1.pos.y(), z), p1.color, p1.texCoords);
        var v2 = new Vertex3DUV(new Vector3f(p2.pos.x(), p2.pos.y(), z), p2.color, p2.texCoords);
        var v3 = new Vertex3DUV(new Vector3f(p3.pos.x(), p3.pos.y(), z), p3.color, p3.texCoords);
        var v4 = new Vertex3DUV(new Vector3f(p4.pos.x(), p4.pos.y(), z), p4.color, p4.texCoords);

        var vertices = new ArrayList<Vertex3DUV>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkTexturedQuad = vulkanInstance.duplicateTexturedQuad2D(srcQuad.vkTexturedQuad, vertices);
        this.associateVulkanComponent(vkTexturedQuad);
    }
}
