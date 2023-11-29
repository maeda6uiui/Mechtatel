package com.github.maeda6uiui.mechtatel.core.texture;

import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanInstanceForTexture;
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
    private IMttVulkanInstanceForTexture vulkanInstance;

    public static void setImageFormat(ImageFormat imageFormat) {
        switch (imageFormat) {
            case SRGB -> VkMttTexture.setImageFormatToSRGB();
            case UNORM -> VkMttTexture.setImageFormatToUNORM();
            default -> throw new IllegalArgumentException("Unsupported image format specified: " + imageFormat);
        }
    }

    public MttTexture(
            IMttVulkanInstanceForTexture vulkanInstance,
            String screenName,
            URI textureResource,
            boolean generateMipmaps) throws FileNotFoundException {
        if (!Files.exists(Paths.get(textureResource))) {
            throw new FileNotFoundException("Specified texture file does not exist: " + textureResource.getPath());
        }

        texture = vulkanInstance.createTexture(screenName, textureResource, generateMipmaps);
        this.vulkanInstance = vulkanInstance;
    }

    public MttTexture(
            IMttVulkanInstanceForTexture vulkanInstance,
            String srcScreenName,
            String dstScreenName,
            String textureType) {
        if (textureType.equals("color")) {
            texture = vulkanInstance.texturizeColorOfScreen(srcScreenName, dstScreenName);
        } else if (textureType.equals("depth")) {
            texture = vulkanInstance.texturizeDepthOfScreen(srcScreenName, dstScreenName);
        } else {
            throw new RuntimeException("Unsupported texture type");
        }

        this.vulkanInstance = vulkanInstance;
    }

    public MttTexture(IMttVulkanInstanceForTexture vulkanInstance, VkMttTexture texture) {
        this.texture = texture;
        this.vulkanInstance = vulkanInstance;
    }

    public void cleanup() {
        texture.cleanup();
        vulkanInstance.removeTexture(texture);
    }

    public VkMttTexture getVulkanTexture() {
        return texture;
    }
}
