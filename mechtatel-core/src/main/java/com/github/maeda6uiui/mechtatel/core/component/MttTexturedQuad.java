package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttTexturedQuad;

import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Textured quadrangle
 *
 * @author maeda6uiui
 */
public class MttTexturedQuad extends MttComponent {
    private VkMttTexturedQuad vkTexturedQuad;

    public MttTexturedQuad(
            MttVulkanImpl vulkanImpl,
            String screenName,
            URI textureResource,
            boolean generateMipmaps,
            MttVertexUV v1,
            MttVertexUV v2,
            MttVertexUV v3,
            MttVertexUV v4) throws FileNotFoundException {
        super(vulkanImpl);

        if (!Files.exists(Paths.get(textureResource))) {
            throw new FileNotFoundException("Texture file not found: " + textureResource.getPath());
        }

        var vertices = new ArrayList<MttVertexUV>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkTexturedQuad = vulkanImpl.createTexturedQuad(screenName, textureResource, generateMipmaps, vertices);
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public MttTexturedQuad(
            MttVulkanImpl vulkanImpl,
            String screenName,
            MttTexture texture,
            MttVertexUV v1,
            MttVertexUV v2,
            MttVertexUV v3,
            MttVertexUV v4) {
        super(vulkanImpl);

        var vertices = new ArrayList<MttVertexUV>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkTexturedQuad = vulkanImpl.createTexturedQuad(screenName, texture.getVulkanTexture(), vertices);
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public MttTexturedQuad(
            MttVulkanImpl vulkanImpl,
            MttTexturedQuad srcQuad,
            MttVertexUV v1,
            MttVertexUV v2,
            MttVertexUV v3,
            MttVertexUV v4) {
        super(vulkanImpl);

        var vertices = new ArrayList<MttVertexUV>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        vkTexturedQuad = vulkanImpl.duplicateTexturedQuad(srcQuad.vkTexturedQuad, vertices);
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public void replaceTexture(MttTexture newTexture) {
        vkTexturedQuad.replaceTexture(newTexture.getVulkanTexture());
    }
}
