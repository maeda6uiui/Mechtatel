package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.fseffect.FullScreenEffectProperties;
import com.github.maeda6uiui.mechtatel.core.postprocessing.PostProcessingProperties;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.TextureOperationNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttComponent;
import org.joml.Vector4f;
import org.lwjgl.vulkan.VkExtent2D;

import java.util.List;

/**
 * Provides interface to common methods between normal and headless implementations
 *
 * @author maeda6uiui
 */
public interface IMttVulkanImplCommon {
    void draw(
            VkMttScreen screen,
            Vector4f backgroundColor,
            Camera camera,
            ShadowMappingSettings shadowMappingSettings,
            PostProcessingProperties ppProperties,
            FullScreenEffectProperties fseProperties,
            List<VkMttComponent> components);

    DeviceAndQueues getDeviceAndQueues();

    long getCommandPool();

    int getDepthImageFormat();

    int getDepthImageAspect();

    VkExtent2D getExtent();

    int getColorImageFormat();

    int getAlbedoMSAASamples();

    TextureOperationNabor getTextureOperationNabor();
}
