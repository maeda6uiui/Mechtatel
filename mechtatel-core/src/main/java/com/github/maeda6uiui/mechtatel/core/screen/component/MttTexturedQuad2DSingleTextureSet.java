package com.github.maeda6uiui.mechtatel.core.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanImplCommon;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttTexturedQuadSingleTextureSet;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Set of 2D textured quadrangles with a single texture
 *
 * @author maeda6uiui
 */
public class MttTexturedQuad2DSingleTextureSet extends MttComponent {
    private VkMttTexturedQuadSingleTextureSet vkTexturedQuadSet;

    private static MttComponentCreateInfo generateCreateInfo() {
        return new MttComponentCreateInfo()
                .setVisible(true)
                .setTwoDComponent(true)
                .setCastShadow(false)
                .setDrawOrder(0);
    }

    public MttTexturedQuad2DSingleTextureSet(
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttComponent screen,
            Path textureFile) throws FileNotFoundException {
        super(screen, generateCreateInfo());

        if (!Files.exists(textureFile)) {
            throw new FileNotFoundException("Texture file not found: " + textureFile);
        }

        var dq = vulkanImplCommon.getDeviceAndQueues();
        vkTexturedQuadSet = new VkMttTexturedQuadSingleTextureSet(
                this,
                dq.device(),
                vulkanImplCommon.getCommandPool(),
                dq.graphicsQueue(),
                screen.getVulkanScreen(),
                textureFile
        );
        this.associateVulkanComponents(vkTexturedQuadSet);
    }

    public MttTexturedQuad2DSingleTextureSet(
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttComponent screen,
            MttTexture texture) {
        super(screen, generateCreateInfo());

        var dq = vulkanImplCommon.getDeviceAndQueues();
        vkTexturedQuadSet = new VkMttTexturedQuadSingleTextureSet(
                this,
                dq.device(),
                vulkanImplCommon.getCommandPool(),
                dq.graphicsQueue(),
                texture.getVulkanTexture()
        );
        this.associateVulkanComponents(vkTexturedQuadSet);
    }

    public MttTexturedQuad2DSingleTextureSet add(
            MttVertex2D v1, MttVertex2D v2, MttVertex2D v3, MttVertex2D v4, float z) {
        var vv1 = new MttVertex(
                new Vector3f(v1.pos.x(), v1.pos.y(), z),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), v1.texCoords);
        var vv2 = new MttVertex(
                new Vector3f(v2.pos.x(), v2.pos.y(), z),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), v2.texCoords);
        var vv3 = new MttVertex(
                new Vector3f(v3.pos.x(), v3.pos.y(), z),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), v3.texCoords);
        var vv4 = new MttVertex(
                new Vector3f(v4.pos.x(), v4.pos.y(), z),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), v4.texCoords);
        vkTexturedQuadSet.add(vv1, vv2, vv3, vv4);

        return this;
    }

    public MttTexturedQuad2DSingleTextureSet add(
            Vector2fc p1, Vector2fc p1UV,
            Vector2fc p2, Vector2fc p2UV,
            Vector2fc p3, Vector2fc p3UV,
            Vector2fc p4, Vector2fc p4UV,
            float z) {
        var v1 = new MttVertex(
                new Vector3f(p1.x(), p1.y(), z), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), p1UV);
        var v2 = new MttVertex(
                new Vector3f(p2.x(), p2.y(), z), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), p2UV);
        var v3 = new MttVertex(
                new Vector3f(p3.x(), p3.y(), z), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), p3UV);
        var v4 = new MttVertex(
                new Vector3f(p4.x(), p4.y(), z), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), p4UV);
        vkTexturedQuadSet.add(v1, v2, v3, v4);

        return this;
    }

    public MttTexturedQuad2DSingleTextureSet add(Vector2fc topLeft, Vector2fc bottomRight, float z) {
        var v1 = new MttVertex(
                new Vector3f(topLeft.x(), topLeft.y(), z),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 0.0f));
        var v2 = new MttVertex(
                new Vector3f(topLeft.x(), bottomRight.y(), z),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 1.0f));
        var v3 = new MttVertex(
                new Vector3f(bottomRight.x(), bottomRight.y(), z),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 1.0f));
        var v4 = new MttVertex(
                new Vector3f(bottomRight.x(), topLeft.y(), z),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 0.0f));
        vkTexturedQuadSet.add(v1, v2, v3, v4);

        return this;
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
