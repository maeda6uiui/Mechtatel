package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttTexturedQuad2DSingleTextureSet;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Set of 2D textured quadrangles with a single texture
 *
 * @author maeda6uiui
 */
public class MttTexturedQuad2DSingleTextureSet extends MttComponent {
    private VkMttTexturedQuad2DSingleTextureSet vkTexturedQuadSet;

    public MttTexturedQuad2DSingleTextureSet(
            MttVulkanInstance vulkanInstance, String screenName, String textureFilepath) {
        super(vulkanInstance);

        vkTexturedQuadSet = vulkanInstance.createTexturedQuad2DSingleTextureSet(screenName, textureFilepath);
        this.associateVulkanComponent(vkTexturedQuadSet);
    }

    public MttTexturedQuad2DSingleTextureSet(
            MttVulkanInstance vulkanInstance,
            String screenName,
            MttTexture texture) {
        super(vulkanInstance);

        vkTexturedQuadSet = vulkanInstance.createTexturedQuad2DSingleTextureSet(screenName, texture.getVulkanTexture());
        this.associateVulkanComponent(vkTexturedQuadSet);
    }

    public void add(MttVertex2DUV p1, MttVertex2DUV p2, MttVertex2DUV p3, MttVertex2DUV p4, float z) {
        var v1 = new MttVertex3DUV(new Vector3f(p1.pos.x(), p1.pos.y(), z), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), p1.texCoords);
        var v2 = new MttVertex3DUV(new Vector3f(p2.pos.x(), p2.pos.y(), z), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), p2.texCoords);
        var v3 = new MttVertex3DUV(new Vector3f(p3.pos.x(), p3.pos.y(), z), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), p3.texCoords);
        var v4 = new MttVertex3DUV(new Vector3f(p4.pos.x(), p4.pos.y(), z), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), p4.texCoords);
        vkTexturedQuadSet.add(v1, v2, v3, v4);
    }

    public void add(
            Vector2fc p1,
            Vector2fc p1UV,
            Vector2fc p2,
            Vector2fc p2UV,
            Vector2fc p3,
            Vector2fc p3UV,
            Vector2fc p4,
            Vector2fc p4UV,
            float z) {
        var v1 = new MttVertex3DUV(new Vector3f(p1.x(), p1.y(), z), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), p1UV);
        var v2 = new MttVertex3DUV(new Vector3f(p2.x(), p2.y(), z), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), p2UV);
        var v3 = new MttVertex3DUV(new Vector3f(p3.x(), p3.y(), z), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), p3UV);
        var v4 = new MttVertex3DUV(new Vector3f(p4.x(), p4.y(), z), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), p4UV);
        vkTexturedQuadSet.add(v1, v2, v3, v4);
    }

    public void add(Vector2fc topLeft, Vector2fc bottomRight, float z) {
        var v1 = new MttVertex3DUV(new Vector3f(topLeft.x(), topLeft.y(), z), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 0.0f));
        var v2 = new MttVertex3DUV(new Vector3f(topLeft.x(), bottomRight.y(), z), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 1.0f));
        var v3 = new MttVertex3DUV(new Vector3f(bottomRight.x(), bottomRight.y(), z), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 1.0f));
        var v4 = new MttVertex3DUV(new Vector3f(bottomRight.x(), topLeft.y(), z), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 0.0f));
        vkTexturedQuadSet.add(v1, v2, v3, v4);
    }

    public void clear() {
        vkTexturedQuadSet.clear();
    }

    public void createBuffers() {
        vkTexturedQuadSet.createBuffers();
    }

    public void replaceTexture(MttTexture newTexture) {
        vkTexturedQuadSet.replaceTexture(newTexture.getVulkanTexture());
    }
}