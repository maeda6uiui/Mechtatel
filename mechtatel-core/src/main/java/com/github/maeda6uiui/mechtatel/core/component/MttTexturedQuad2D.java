package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttTexturedQuad;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * 2D textured quadrangle
 *
 * @author maeda6uiui
 */
public class MttTexturedQuad2D extends MttComponent {
    private VkMttTexturedQuad vkTexturedQuad;

    private List<MttVertexUV> createVertices(
            MttVertex2DUV v1, MttVertex2DUV v2, MttVertex2DUV v3, MttVertex2DUV v4, float z) {
        var vv1 = new MttVertexUV(new Vector3f(v1.pos.x(), v1.pos.y(), z), v1.color, v1.texCoords);
        var vv2 = new MttVertexUV(new Vector3f(v2.pos.x(), v2.pos.y(), z), v2.color, v2.texCoords);
        var vv3 = new MttVertexUV(new Vector3f(v3.pos.x(), v3.pos.y(), z), v3.color, v3.texCoords);
        var vv4 = new MttVertexUV(new Vector3f(v4.pos.x(), v4.pos.y(), z), v4.color, v4.texCoords);

        return Arrays.asList(vv1, vv2, vv3, vv4);
    }

    private List<MttVertexUV> createVertices(Vector2fc topLeft, Vector2fc bottomRight, float z) {
        var v1 = new MttVertex2DUV(
                topLeft,
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector2f(0.0f, 0.0f));
        var v2 = new MttVertex2DUV(
                new Vector2f(topLeft.x(), bottomRight.y()),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector2f(0.0f, 1.0f));
        var v3 = new MttVertex2DUV(
                bottomRight,
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector2f(1.0f, 1.0f));
        var v4 = new MttVertex2DUV(
                new Vector2f(bottomRight.x(), topLeft.y()),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector2f(1.0f, 0.0f));

        return this.createVertices(v1, v2, v3, v4, z);
    }

    private void create(
            MttVulkanImpl vulkanImpl,
            String screenName,
            URI textureResource,
            boolean generateMipmaps,
            List<MttVertexUV> vertices) throws FileNotFoundException {
        if (!Files.exists(Paths.get(textureResource))) {
            throw new FileNotFoundException("Texture file not found: " + textureResource.getPath());
        }

        vkTexturedQuad = vulkanImpl.createTexturedQuad(screenName, textureResource, generateMipmaps, vertices);
        vkTexturedQuad.setTwoDComponent(true);
        this.associateVulkanComponent(vkTexturedQuad);
    }

    private void create(
            MttVulkanImpl vulkanImpl,
            String screenName,
            MttTexture texture,
            List<MttVertexUV> vertices) {
        vkTexturedQuad = vulkanImpl.createTexturedQuad(screenName, texture.getVulkanTexture(), vertices);
        vkTexturedQuad.setTwoDComponent(true);
        this.associateVulkanComponent(vkTexturedQuad);
    }

    private void duplicate(
            MttVulkanImpl vulkanImpl,
            MttTexturedQuad2D srcQuad,
            List<MttVertexUV> vertices) {
        vkTexturedQuad = vulkanImpl.duplicateTexturedQuad(srcQuad.vkTexturedQuad, vertices);
        vkTexturedQuad.setTwoDComponent(true);
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public MttTexturedQuad2D(
            MttVulkanImpl vulkanImpl,
            String screenName,
            URI textureResource,
            boolean generateMipmaps,
            MttVertex2DUV v1,
            MttVertex2DUV v2,
            MttVertex2DUV v3,
            MttVertex2DUV v4,
            float z) throws FileNotFoundException {
        super(vulkanImpl);

        var vertices = this.createVertices(v1, v2, v3, v4, z);
        this.create(vulkanImpl, screenName, textureResource, generateMipmaps, vertices);
    }

    public MttTexturedQuad2D(
            MttVulkanImpl vulkanImpl,
            String screenName,
            URI textureResource,
            boolean generateMipmaps,
            Vector2fc topLeft,
            Vector2fc bottomRight,
            float z) throws FileNotFoundException {
        super(vulkanImpl);

        var vertices = this.createVertices(topLeft, bottomRight, z);
        this.create(vulkanImpl, screenName, textureResource, generateMipmaps, vertices);
    }

    public MttTexturedQuad2D(
            MttVulkanImpl vulkanImpl,
            String screenName,
            MttTexture texture,
            MttVertex2DUV v1,
            MttVertex2DUV v2,
            MttVertex2DUV v3,
            MttVertex2DUV v4,
            float z) {
        super(vulkanImpl);

        var vertices = this.createVertices(v1, v2, v3, v4, z);
        this.create(vulkanImpl, screenName, texture, vertices);
    }

    public MttTexturedQuad2D(
            MttVulkanImpl vulkanImpl,
            String screenName,
            MttTexture texture,
            Vector2fc topLeft,
            Vector2fc bottomRight,
            float z) {
        super(vulkanImpl);

        var vertices = this.createVertices(topLeft, bottomRight, z);
        this.create(vulkanImpl, screenName, texture, vertices);
    }

    public MttTexturedQuad2D(
            MttVulkanImpl vulkanImpl,
            MttTexturedQuad2D srcQuad,
            MttVertex2DUV v1,
            MttVertex2DUV v2,
            MttVertex2DUV v3,
            MttVertex2DUV v4,
            float z) {
        super(vulkanImpl);

        var vertices = this.createVertices(v1, v2, v3, v4, z);
        this.duplicate(vulkanImpl, srcQuad, vertices);
    }

    public MttTexturedQuad2D(
            MttVulkanImpl vulkanImpl,
            MttTexturedQuad2D srcQuad,
            Vector2fc topLeft,
            Vector2fc bottomRight,
            float z) {
        super(vulkanImpl);

        var vertices = this.createVertices(topLeft, bottomRight, z);
        this.duplicate(vulkanImpl, srcQuad, vertices);
    }

    public void replaceTexture(MttTexture newTexture) {
        vkTexturedQuad.replaceTexture(newTexture.getVulkanTexture());
    }
}
