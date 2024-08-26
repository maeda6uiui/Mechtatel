package com.github.maeda6uiui.mechtatel.core.screen.texture;

import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanImplCommon;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.texture.VkMttTexture;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

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
    private boolean isValid;

    public static void setImageFormat(ImageFormat imageFormat) {
        switch (imageFormat) {
            case SRGB -> VkMttTexture.setImageFormatToSRGB();
            case UNORM -> VkMttTexture.setImageFormatToUNORM();
            default -> throw new IllegalArgumentException("Unsupported image format specified: " + imageFormat);
        }
    }

    public MttTexture(
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttTexture screen,
            Path textureFile,
            boolean generateMipmaps) throws FileNotFoundException {
        if (!Files.exists(textureFile)) {
            throw new FileNotFoundException("Specified texture file does not exist: " + textureFile);
        }

        var dq = vulkanImplCommon.getDeviceAndQueues();
        texture = new VkMttTexture(
                dq.device(),
                vulkanImplCommon.getCommandPool(),
                dq.graphicsQueue(),
                screen.getVulkanScreen(),
                textureFile,
                generateMipmaps
        );

        isValid = true;

        screen.addTexture(this);
    }

    public MttTexture(
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttTexture screen,
            ByteBuffer pixels,
            int width,
            int height) {
        var dq = vulkanImplCommon.getDeviceAndQueues();
        texture = new VkMttTexture(
                dq.device(),
                vulkanImplCommon.getCommandPool(),
                dq.graphicsQueue(),
                screen.getVulkanScreen(),
                pixels,
                width,
                height,
                false
        );

        isValid = true;

        screen.addTexture(this);
    }

    public MttTexture(IMttScreenForMttTexture screen, VkMttTexture texture) {
        this.texture = texture;
        isValid = true;

        screen.addTexture(this);
    }

    public void cleanup() {
        if (isValid) {
            texture.cleanup();
        }
        isValid = false;
    }

    public VkMttTexture getVulkanTexture() {
        return texture;
    }

    public boolean isValid() {
        return isValid;
    }
}
