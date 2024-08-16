package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttShaderSettings;
import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.fseffect.FullScreenEffectProperties;
import com.github.maeda6uiui.mechtatel.core.postprocessing.PostProcessingProperties;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.util.MttURLUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.CommandPoolCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.LogicalDeviceCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.BiTextureOperationNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.DepthResourceUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.MultisamplingUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.PhysicalDevicePicker;
import org.joml.Vector4f;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPhysicalDevice;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Provides Mechtatel functionality of headless mode implemented with Vulkan
 *
 * @author maeda6uiui
 */
public class MttVulkanImplHeadless {
    private VkPhysicalDevice physicalDevice;

    private DeviceAndQueues dq;
    private long commandPool;

    private int albedoMSAASamples;
    private int depthImageFormat;
    private int depthImageAspect;

    private BiTextureOperationNabor biTextureOperationNabor;
    private QuadDrawer quadDrawer;

    public MttVulkanImplHeadless(
            MttSettings.VulkanSettings vulkanSettings,
            int biTextureOperationWidth,
            int biTextureOperationHeight) {
        MttVulkanInstance
                .get()
                .ifPresent(v -> {
                    physicalDevice = PhysicalDevicePicker.pickPhysicalDevice(
                            v.getVkInstance(),
                            -1,
                            vulkanSettings.preferablePhysicalDeviceIndex,
                            false
                    );
                });

        dq = LogicalDeviceCreator.createLogicalDevice(
                physicalDevice,
                vulkanSettings.preferableGraphicsFamilyIndex,
                vulkanSettings.enableValidationLayer
        );

        commandPool = CommandPoolCreator.createCommandPool(dq.device(), dq.graphicsFamilyIndex());

        albedoMSAASamples = vulkanSettings.albedoMSAASamples < 0
                ? MultisamplingUtils.getMaxUsableSampleCount(dq.device())
                : vulkanSettings.albedoMSAASamples;
        depthImageFormat = DepthResourceUtils.findDepthFormat(dq.device());
        depthImageAspect = VK_IMAGE_ASPECT_DEPTH_BIT;

        boolean hasStencilComponent = DepthResourceUtils.hasStencilComponent(depthImageFormat);
        if (hasStencilComponent) {
            depthImageAspect |= VK_IMAGE_ASPECT_STENCIL_BIT;
        }

        //Get shader URLs =====
        MttShaderSettings shaderSettings = MttShaderSettings
                .get()
                .orElse(MttShaderSettings.create());

        URL biTextureOperationVertShaderResource;
        URL biTextureOperationFragShaderResource;
        try {
            biTextureOperationVertShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.biTextureOperation.main.vert.filepath,
                    shaderSettings.biTextureOperation.main.vert.external
            );
            biTextureOperationFragShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.biTextureOperation.main.frag.filepath,
                    shaderSettings.biTextureOperation.main.frag.external
            );
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        //==========

        biTextureOperationNabor = new BiTextureOperationNabor(
                dq.device(), biTextureOperationVertShaderResource, biTextureOperationFragShaderResource);

        VkExtent2D extent = VkExtent2D.create();
        extent.set(biTextureOperationWidth, biTextureOperationHeight);

        biTextureOperationNabor.compile(
                VK_FORMAT_B8G8R8A8_UNORM,
                VK_FILTER_NEAREST,
                VK_SAMPLER_MIPMAP_MODE_NEAREST,
                VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER,
                extent,
                commandPool,
                dq.graphicsQueue(),
                1
        );

        quadDrawer = new QuadDrawer(dq.device(), commandPool, dq.graphicsQueue());
    }

    public void cleanup() {
        vkDeviceWaitIdle(dq.device());
        quadDrawer.cleanup();
        biTextureOperationNabor.cleanup(false);

        vkDestroyCommandPool(dq.device(), commandPool, null);
        vkDestroyDevice(dq.device(), null);
    }

    public void draw(
            VkMttScreen screen,
            Vector4f backgroundColor,
            Camera camera,
            ShadowMappingSettings shadowMappingSettings,
            PostProcessingProperties ppProperties,
            FullScreenEffectProperties fseProperties,
            List<VkMttComponent> components) {
        screen.run(
                backgroundColor,
                camera,
                shadowMappingSettings,
                ppProperties,
                fseProperties,
                components
        );
    }

    public DeviceAndQueues getDeviceAndQueues() {
        return dq;
    }

    public long getCommandPool() {
        return commandPool;
    }

    public int getDepthImageFormat() {
        return depthImageFormat;
    }

    public int getDepthImageAspect() {
        return depthImageAspect;
    }

    public int getAlbedoMSAASamples() {
        return albedoMSAASamples;
    }

    public BiTextureOperationNabor getTextureOperationNabor() {
        return biTextureOperationNabor;
    }
}
