package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttTexturedQuad;

import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Textured quadrangle
 *
 * @author maeda6uiui
 */
public class MttTexturedQuad extends MttComponent {
    private VkMttTexturedQuad vkTexturedQuad;

    private static MttComponentCreateInfo generateCreateInfo() {
        return new MttComponentCreateInfo()
                .setVisible(true)
                .setTwoDComponent(false)
                .setCastShadow(true)
                .setDrawOrder(0);
    }

    public MttTexturedQuad(
            MttVulkanImpl vulkanImpl,
            MttScreen screen,
            URI textureResource,
            boolean generateMipmaps,
            MttVertexUV v1,
            MttVertexUV v2,
            MttVertexUV v3,
            MttVertexUV v4) throws FileNotFoundException {
        super(vulkanImpl, generateCreateInfo());

        if (!Files.exists(Paths.get(textureResource))) {
            throw new FileNotFoundException("Texture file not found: " + textureResource.getPath());
        }

        var dq = vulkanImpl.getDeviceAndQueues();
        vkTexturedQuad = new VkMttTexturedQuad(
                this,
                dq.device(),
                vulkanImpl.getCommandPool(),
                dq.graphicsQueue(),
                screen.getVulkanScreen(),
                textureResource,
                generateMipmaps,
                Arrays.asList(v1, v2, v3, v4)
        );
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public MttTexturedQuad(
            MttVulkanImpl vulkanImpl,
            MttTexturedQuad srcQuad,
            MttVertexUV v1,
            MttVertexUV v2,
            MttVertexUV v3,
            MttVertexUV v4) {
        super(vulkanImpl, generateCreateInfo());

        var dq = vulkanImpl.getDeviceAndQueues();
        vkTexturedQuad = new VkMttTexturedQuad(
                this,
                dq.device(),
                vulkanImpl.getCommandPool(),
                dq.graphicsQueue(),
                srcQuad.vkTexturedQuad,
                Arrays.asList(v1, v2, v3, v4)
        );
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public MttTexturedQuad(
            MttVulkanImpl vulkanImpl,
            MttTexture texture,
            MttVertexUV v1,
            MttVertexUV v2,
            MttVertexUV v3,
            MttVertexUV v4) {
        super(vulkanImpl, generateCreateInfo());

        var dq = vulkanImpl.getDeviceAndQueues();
        vkTexturedQuad = new VkMttTexturedQuad(
                this,
                dq.device(),
                vulkanImpl.getCommandPool(),
                dq.graphicsQueue(),
                texture.getVulkanTexture(),
                Arrays.asList(v1, v2, v3, v4)
        );
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public void replaceTexture(MttTexture newTexture) {
        vkTexturedQuad.replaceTexture(newTexture.getVulkanTexture());
    }
}
