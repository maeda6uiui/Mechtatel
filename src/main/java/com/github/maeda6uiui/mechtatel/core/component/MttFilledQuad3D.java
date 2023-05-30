package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttFilledQuad3D;

import java.util.ArrayList;

/**
 * 3D filled quadrangle
 *
 * @author maeda6uiui
 */
public class MttFilledQuad3D extends MttComponent3D {
    private VkMttFilledQuad3D vkFilledQuad;

    public MttFilledQuad3D(
            MttVulkanInstance vulkanInstance,
            MttVertex3D v1,
            MttVertex3D v2,
            MttVertex3D v3,
            MttVertex3D v4) {
        super(vulkanInstance);

        var vertices = new ArrayList<MttVertex3D>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkFilledQuad = vulkanInstance.createFilledQuad3D(vertices);
        this.associateVulkanComponent(vkFilledQuad);
    }
}
