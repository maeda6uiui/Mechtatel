package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttQuad3D;

import java.util.ArrayList;

/**
 * 3D quadrangle
 *
 * @author maeda6uiui
 */
public class MttQuad3D extends MttComponent {
    private VkMttQuad3D vkQuad;

    public MttQuad3D(
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

        vkQuad = vulkanInstance.createQuad3D(vertices, fill);
        this.associateVulkanComponent(vkQuad);
    }
}
