package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkTexturedQuad3D;

import java.util.List;

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
            List<Vertex3DUV> vertices) {
        super(vulkanInstance);

        vkTexturedQuad = vulkanInstance.createTexturedQuad3D(textureFilepath, generateMipmaps, vertices);
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public TexturedQuad3D(
            MttVulkanInstance vulkanInstance,
            TexturedQuad3D srcQuad,
            List<Vertex3DUV> vertices) {
        super(vulkanInstance);

        vkTexturedQuad = vulkanInstance.duplicateTexturedQuad3D(srcQuad.vkTexturedQuad, vertices);
        this.associateVulkanComponent(vkTexturedQuad);
    }
}
