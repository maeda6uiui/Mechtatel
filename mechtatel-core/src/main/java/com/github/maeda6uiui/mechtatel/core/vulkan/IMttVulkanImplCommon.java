package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.BiTextureOperationNabor;
import org.lwjgl.vulkan.VkExtent2D;

/**
 * Provides interface to common methods between normal and headless implementations
 *
 * @author maeda6uiui
 */
public interface IMttVulkanImplCommon {
    DeviceAndQueues getDeviceAndQueues();

    long getCommandPool();

    int getDepthImageFormat();

    int getDepthImageAspect();

    VkExtent2D getExtent();

    int getColorImageFormat();

    int getAlbedoMSAASamples();

    BiTextureOperationNabor getTextureOperationNabor();
}
