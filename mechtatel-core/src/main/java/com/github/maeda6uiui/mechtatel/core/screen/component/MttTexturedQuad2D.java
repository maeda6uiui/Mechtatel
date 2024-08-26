package com.github.maeda6uiui.mechtatel.core.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanImplCommon;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttTexturedQuad;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * 2D textured quadrangle
 *
 * @author maeda6uiui
 */
public class MttTexturedQuad2D extends MttComponent {
    private VkMttTexturedQuad vkTexturedQuad;

    private List<MttVertex> createVertices(
            MttVertex2D v1, MttVertex2D v2, MttVertex2D v3, MttVertex2D v4, float z) {
        var vv1 = new MttVertex(new Vector3f(v1.pos.x(), v1.pos.y(), z), v1.color, v1.texCoords);
        var vv2 = new MttVertex(new Vector3f(v2.pos.x(), v2.pos.y(), z), v2.color, v2.texCoords);
        var vv3 = new MttVertex(new Vector3f(v3.pos.x(), v3.pos.y(), z), v3.color, v3.texCoords);
        var vv4 = new MttVertex(new Vector3f(v4.pos.x(), v4.pos.y(), z), v4.color, v4.texCoords);

        return Arrays.asList(vv1, vv2, vv3, vv4);
    }

    private List<MttVertex> createVertices(Vector2fc topLeft, Vector2fc bottomRight, float z) {
        var v1 = new MttVertex2D(
                topLeft,
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector2f(0.0f, 0.0f));
        var v2 = new MttVertex2D(
                new Vector2f(topLeft.x(), bottomRight.y()),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector2f(0.0f, 1.0f));
        var v3 = new MttVertex2D(
                bottomRight,
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector2f(1.0f, 1.0f));
        var v4 = new MttVertex2D(
                new Vector2f(bottomRight.x(), topLeft.y()),
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                new Vector2f(1.0f, 0.0f));

        return this.createVertices(v1, v2, v3, v4, z);
    }

    private void create(
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttComponent screen,
            Path textureFile,
            List<MttVertex> vertices) throws FileNotFoundException {
        if (!Files.exists(textureFile)) {
            throw new FileNotFoundException("Texture file not found: " + textureFile);
        }

        var dq = vulkanImplCommon.getDeviceAndQueues();
        vkTexturedQuad = new VkMttTexturedQuad(
                this,
                dq.device(),
                vulkanImplCommon.getCommandPool(),
                dq.graphicsQueue(),
                screen.getVulkanScreen(),
                textureFile,
                false,
                vertices
        );
        this.associateVulkanComponents(vkTexturedQuad);
    }

    private void create(
            IMttVulkanImplCommon vulkanImplCommon,
            MttTexture texture,
            List<MttVertex> vertices) {
        var dq = vulkanImplCommon.getDeviceAndQueues();
        vkTexturedQuad = new VkMttTexturedQuad(
                this,
                dq.device(),
                vulkanImplCommon.getCommandPool(),
                dq.graphicsQueue(),
                texture.getVulkanTexture(),
                vertices
        );
        this.associateVulkanComponents(vkTexturedQuad);
    }

    private void duplicate(
            IMttVulkanImplCommon vulkanImplCommon,
            MttTexturedQuad2D srcQuad,
            List<MttVertex> vertices) {
        var dq = vulkanImplCommon.getDeviceAndQueues();
        vkTexturedQuad = new VkMttTexturedQuad(
                this,
                dq.device(),
                vulkanImplCommon.getCommandPool(),
                dq.graphicsQueue(),
                srcQuad.vkTexturedQuad,
                vertices
        );
        this.associateVulkanComponents(vkTexturedQuad);
    }

    private static MttComponentCreateInfo generateCreateInfo() {
        return new MttComponentCreateInfo()
                .setVisible(true)
                .setTwoDComponent(true)
                .setCastShadow(false)
                .setDrawOrder(0);
    }

    public MttTexturedQuad2D(
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttComponent screen,
            Path textureFile,
            MttVertex2D v1,
            MttVertex2D v2,
            MttVertex2D v3,
            MttVertex2D v4,
            float z) throws FileNotFoundException {
        super(screen, generateCreateInfo());

        var vertices = this.createVertices(v1, v2, v3, v4, z);
        this.create(vulkanImplCommon, screen, textureFile, vertices);
    }

    public MttTexturedQuad2D(
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttComponent screen,
            Path textureFile,
            Vector2fc topLeft,
            Vector2fc bottomRight,
            float z) throws FileNotFoundException {
        super(screen, generateCreateInfo());

        var vertices = this.createVertices(topLeft, bottomRight, z);
        this.create(vulkanImplCommon, screen, textureFile, vertices);
    }

    public MttTexturedQuad2D(
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttComponent screen,
            MttTexture texture,
            MttVertex2D v1,
            MttVertex2D v2,
            MttVertex2D v3,
            MttVertex2D v4,
            float z) {
        super(screen, generateCreateInfo());

        var vertices = this.createVertices(v1, v2, v3, v4, z);
        this.create(vulkanImplCommon, texture, vertices);
    }

    public MttTexturedQuad2D(
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttComponent screen,
            MttTexture texture,
            Vector2fc topLeft,
            Vector2fc bottomRight,
            float z) {
        super(screen, generateCreateInfo());

        var vertices = this.createVertices(topLeft, bottomRight, z);
        this.create(vulkanImplCommon, texture, vertices);
    }

    public MttTexturedQuad2D(
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttComponent screen,
            MttTexturedQuad2D srcQuad,
            MttVertex2D v1,
            MttVertex2D v2,
            MttVertex2D v3,
            MttVertex2D v4,
            float z) {
        super(screen, generateCreateInfo());

        var vertices = this.createVertices(v1, v2, v3, v4, z);
        this.duplicate(vulkanImplCommon, srcQuad, vertices);
    }

    public MttTexturedQuad2D(
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttComponent screen,
            MttTexturedQuad2D srcQuad,
            Vector2fc topLeft,
            Vector2fc bottomRight,
            float z) {
        super(screen, generateCreateInfo());

        var vertices = this.createVertices(topLeft, bottomRight, z);
        this.duplicate(vulkanImplCommon, srcQuad, vertices);
    }

    public void replaceTexture(MttTexture newTexture) {
        vkTexturedQuad.replaceTexture(newTexture.getVulkanTexture());
    }
}
