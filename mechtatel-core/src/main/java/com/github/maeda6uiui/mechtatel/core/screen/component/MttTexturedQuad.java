package com.github.maeda6uiui.mechtatel.core.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttTexturedQuad;

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
            IMttScreenForMttComponent screen,
            URI textureResource,
            boolean generateMipmaps,
            MttVertex v1,
            MttVertex v2,
            MttVertex v3,
            MttVertex v4) throws FileNotFoundException {
        super(screen, generateCreateInfo());

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
        this.associateVulkanComponents(vkTexturedQuad);
    }

    public MttTexturedQuad(
            MttVulkanImpl vulkanImpl,
            IMttScreenForMttComponent screen,
            MttTexturedQuad srcQuad,
            MttVertex v1,
            MttVertex v2,
            MttVertex v3,
            MttVertex v4) {
        super(screen, generateCreateInfo());

        var dq = vulkanImpl.getDeviceAndQueues();
        vkTexturedQuad = new VkMttTexturedQuad(
                this,
                dq.device(),
                vulkanImpl.getCommandPool(),
                dq.graphicsQueue(),
                srcQuad.vkTexturedQuad,
                Arrays.asList(v1, v2, v3, v4)
        );
        this.associateVulkanComponents(vkTexturedQuad);
    }

    public MttTexturedQuad(
            MttVulkanImpl vulkanImpl,
            IMttScreenForMttComponent screen,
            MttTexture texture,
            MttVertex v1,
            MttVertex v2,
            MttVertex v3,
            MttVertex v4) {
        super(screen, generateCreateInfo());

        var dq = vulkanImpl.getDeviceAndQueues();
        vkTexturedQuad = new VkMttTexturedQuad(
                this,
                dq.device(),
                vulkanImpl.getCommandPool(),
                dq.graphicsQueue(),
                texture.getVulkanTexture(),
                Arrays.asList(v1, v2, v3, v4)
        );
        this.associateVulkanComponents(vkTexturedQuad);
    }

    public void replaceTexture(MttTexture newTexture) {
        vkTexturedQuad.replaceTexture(newTexture.getVulkanTexture());
    }
}
