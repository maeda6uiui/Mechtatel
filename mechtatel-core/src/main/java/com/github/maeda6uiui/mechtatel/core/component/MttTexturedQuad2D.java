package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttTexturedQuad;
import org.joml.Vector3f;

import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 2D textured quadrangle
 *
 * @author maeda6uiui
 */
public class MttTexturedQuad2D extends MttComponent {
    private VkMttTexturedQuad vkTexturedQuad;

    private List<MttVertex3DUV> createVertices(MttVertex2DUV p1, MttVertex2DUV p2, MttVertex2DUV p3, MttVertex2DUV p4, float z) {
        var v1 = new MttVertex3DUV(new Vector3f(p1.pos.x(), p1.pos.y(), z), p1.color, p1.texCoords);
        var v2 = new MttVertex3DUV(new Vector3f(p2.pos.x(), p2.pos.y(), z), p2.color, p2.texCoords);
        var v3 = new MttVertex3DUV(new Vector3f(p3.pos.x(), p3.pos.y(), z), p3.color, p3.texCoords);
        var v4 = new MttVertex3DUV(new Vector3f(p4.pos.x(), p4.pos.y(), z), p4.color, p4.texCoords);

        var vertices = new ArrayList<MttVertex3DUV>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        return vertices;
    }

    public MttTexturedQuad2D(
            MttVulkanImpl vulkanImpl,
            String screenName,
            URI textureResource,
            boolean generateMipmaps,
            MttVertex2DUV p1,
            MttVertex2DUV p2,
            MttVertex2DUV p3,
            MttVertex2DUV p4,
            float z) throws FileNotFoundException {
        super(vulkanImpl);

        if (!Files.exists(Paths.get(textureResource))) {
            throw new FileNotFoundException("Texture file not found: " + textureResource.getPath());
        }

        var vertices = this.createVertices(p1, p2, p3, p4, z);
        vkTexturedQuad = vulkanImpl.createTexturedQuad(screenName, textureResource, generateMipmaps, vertices);
        vkTexturedQuad.setTwoDComponent(true);
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public MttTexturedQuad2D(
            MttVulkanImpl vulkanImpl,
            String screenName,
            MttTexture texture,
            MttVertex2DUV p1,
            MttVertex2DUV p2,
            MttVertex2DUV p3,
            MttVertex2DUV p4,
            float z) {
        super(vulkanImpl);

        var vertices = this.createVertices(p1, p2, p3, p4, z);
        vkTexturedQuad = vulkanImpl.createTexturedQuad(screenName, texture.getVulkanTexture(), vertices);
        vkTexturedQuad.setTwoDComponent(true);
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public MttTexturedQuad2D(
            MttVulkanImpl vulkanImpl,
            MttTexturedQuad2D srcQuad,
            MttVertex2DUV p1,
            MttVertex2DUV p2,
            MttVertex2DUV p3,
            MttVertex2DUV p4,
            float z) {
        super(vulkanImpl);

        var vertices = this.createVertices(p1, p2, p3, p4, z);
        vkTexturedQuad = vulkanImpl.duplicateTexturedQuad(srcQuad.vkTexturedQuad, vertices);
        vkTexturedQuad.setTwoDComponent(true);
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public void replaceTexture(MttTexture newTexture) {
        vkTexturedQuad.replaceTexture(newTexture.getVulkanTexture());
    }
}
