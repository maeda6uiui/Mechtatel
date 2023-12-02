package com.github.maeda6uiui.mechtatel.core.component;

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
            String screenName,
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

        vkTexturedQuad = vulkanImpl.createTexturedQuad(
                screenName,
                textureResource,
                generateMipmaps,
                Arrays.asList(v1, v2, v3, v4));
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
        super(vulkanImpl, generateCreateInfo());

        vkTexturedQuad = vulkanImpl.createTexturedQuad(
                screenName,
                texture.getVulkanTexture(),
                Arrays.asList(v1, v2, v3, v4));
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

        vkTexturedQuad = vulkanImpl.duplicateTexturedQuad(
                srcQuad.vkTexturedQuad,
                Arrays.asList(v1, v2, v3, v4));
        this.associateVulkanComponent(vkTexturedQuad);
    }

    public void replaceTexture(MttTexture newTexture) {
        vkTexturedQuad.replaceTexture(newTexture.getVulkanTexture());
    }
}
