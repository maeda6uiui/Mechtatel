package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkFilledQuad3D;

import java.util.ArrayList;

/**
 * 3D filled quadrangle
 *
 * @author maeda6uiui
 */
public class FilledQuad3D extends Component3D {
    private VkFilledQuad3D vkFilledQuad;

    public FilledQuad3D(
            MttVulkanInstance vulkanInstance,
            Vertex3D v1,
            Vertex3D v2,
            Vertex3D v3,
            Vertex3D v4) {
        super(vulkanInstance);

        var vertices = new ArrayList<Vertex3D>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkFilledQuad = vulkanInstance.createFilledQuad3D(vertices);
        this.associateVulkanComponent(vkFilledQuad);
    }
}
