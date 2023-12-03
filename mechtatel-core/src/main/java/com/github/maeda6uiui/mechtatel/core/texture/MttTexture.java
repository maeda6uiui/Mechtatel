package com.github.maeda6uiui.mechtatel.core.texture;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.VkMttTexture;

import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Texture
 *
 * @author maeda6uiui
 */
public class MttTexture {
    public enum ImageFormat {
        SRGB,
        UNORM
    }

    private VkMttTexture texture;
    private boolean isOwner;

    public static void setImageFormat(ImageFormat imageFormat) {
        switch (imageFormat) {
            case SRGB -> VkMttTexture.setImageFormatToSRGB();
            case UNORM -> VkMttTexture.setImageFormatToUNORM();
            default -> throw new IllegalArgumentException("Unsupported image format specified: " + imageFormat);
        }
    }

    public MttTexture(
            MttVulkanImpl vulkanImpl,
            MttScreen screen,
            URI textureResource,
            boolean generateMipmaps) throws FileNotFoundException {
        if (!Files.exists(Paths.get(textureResource))) {
            throw new FileNotFoundException("Specified texture file does not exist: " + textureResource.getPath());
        }

        var dq = vulkanImpl.getDeviceAndQueues();
        texture = new VkMttTexture(
                dq.device(),
                vulkanImpl.getCommandPool(),
                dq.graphicsQueue(),
                screen.getVulkanScreen(),
                textureResource,
                generateMipmaps
        );

        isOwner = true;
    }

    public MttTexture(VkMttTexture texture) {
        this.texture = texture;
        isOwner = false;
    }

    public void cleanup() {
        if (isOwner) {
            texture.cleanup();
        }
    }

    public VkMttTexture getVulkanTexture() {
        return texture;
    }
}
