package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
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
            MttVulkanImpl vulkanImpl,
            MttVertex v1,
            MttVertex v2,
            MttVertex v3,
            MttVertex v4,
            boolean fill) {
        super(vulkanImpl);

        var vertices = new ArrayList<MttVertex>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkQuad = vulkanImpl.createQuad(vertices, fill);
        this.associateVulkanComponent(vkQuad);
    }
}
