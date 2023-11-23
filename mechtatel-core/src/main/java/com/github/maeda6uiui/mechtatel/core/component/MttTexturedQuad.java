package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttTexturedQuad;

import java.util.ArrayList;

/**
 * Textured quadrangle
 *
 * @author maeda6uiui
 */
public class MttTexturedQuad extends MttComponent {
    private VkMttTexturedQuad vkTexturedQuad;

    public MttTexturedQuad(
            MttVulkanInstance vulkanInstance,
            String screenName,
            String textureFilepath,
            boolean generateMipmaps,
            MttVertex3DUV v1,
            MttVertex3DUV v2,
            MttVertex3DUV v3,
            MttVertex3DUV v4) {
        super(vulkanInstance);

        var vertices = new ArrayList<MttVertex3DUV>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkTexturedQuad = vulkanInstance.createTexturedQuad(screenName, textureFilepath, generateMipmaps, vertices);
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public MttTexturedQuad(
            MttVulkanInstance vulkanInstance,
            String screenName,
            MttTexture texture,
            MttVertex3DUV v1,
            MttVertex3DUV v2,
            MttVertex3DUV v3,
            MttVertex3DUV v4) {
        super(vulkanInstance);

        var vertices = new ArrayList<MttVertex3DUV>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkTexturedQuad = vulkanInstance.createTexturedQuad(screenName, texture.getVulkanTexture(), vertices);
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public MttTexturedQuad(
            MttVulkanInstance vulkanInstance,
            MttTexturedQuad srcQuad,
            MttVertex3DUV v1,
            MttVertex3DUV v2,
            MttVertex3DUV v3,
            MttVertex3DUV v4) {
        super(vulkanInstance);

        var vertices = new ArrayList<MttVertex3DUV>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkTexturedQuad = vulkanInstance.duplicateTexturedQuad(srcQuad.vkTexturedQuad, vertices);
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public void replaceTexture(MttTexture newTexture) {
        vkTexturedQuad.replaceTexture(newTexture.getVulkanTexture());
    }
}
