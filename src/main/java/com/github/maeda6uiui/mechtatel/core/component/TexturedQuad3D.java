package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkTexturedQuad3D;

import java.util.ArrayList;

/**
 * 3D textured quadrangle
 *
 * @author maeda
 */
public class TexturedQuad3D extends Component3D {
    private VkTexturedQuad3D vkTexturedQuad;

    public TexturedQuad3D(
            MttVulkanInstance vulkanInstance,
            String textureFilepath,
            boolean generateMipmaps,
            Vertex3DUV v1,
            Vertex3DUV v2,
            Vertex3DUV v3,
            Vertex3DUV v4) {
        super(vulkanInstance);

        var vertices = new ArrayList<Vertex3DUV>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkTexturedQuad = vulkanInstance.createTexturedQuad3D(textureFilepath, generateMipmaps, vertices);
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public TexturedQuad3D(
            MttVulkanInstance vulkanInstance,
            TexturedQuad3D srcQuad,
            Vertex3DUV v1,
            Vertex3DUV v2,
            Vertex3DUV v3,
            Vertex3DUV v4) {
        super(vulkanInstance);

        var vertices = new ArrayList<Vertex3DUV>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkTexturedQuad = vulkanInstance.duplicateTexturedQuad3D(srcQuad.vkTexturedQuad, vertices);
        this.associateVulkanComponent(vkTexturedQuad);
    }
}