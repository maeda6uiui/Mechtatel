package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttQuad;

import java.util.ArrayList;

/**
 * Quadrangle
 *
 * @author maeda6uiui
 */
public class MttQuad extends MttComponent {
    private VkMttQuad vkQuad;

    public MttQuad(
            MttVulkanInstance vulkanInstance,
            MttVertex3D v1,
            MttVertex3D v2,
            MttVertex3D v3,
            MttVertex3D v4,
            boolean fill) {
        super(vulkanInstance);

        var vertices = new ArrayList<MttVertex3D>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkQuad = vulkanInstance.createQuad(vertices, fill);
        this.associateVulkanComponent(vkQuad);
    }
}
