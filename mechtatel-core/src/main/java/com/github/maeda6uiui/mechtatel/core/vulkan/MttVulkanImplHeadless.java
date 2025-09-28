package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttShaderConfig;
import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.fseffect.FullScreenEffectProperties;
import com.github.maeda6uiui.mechtatel.core.postprocessing.PostProcessingProperties;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.CommandPoolCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.LogicalDeviceCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.TextureOperationNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.DepthResourceUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.MultisamplingUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.PhysicalDevicePicker;
import org.joml.Vector4f;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkPhysicalDevice;

import java.net.URL;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Provides Mechtatel functionality of headless mode implemented with Vulkan
 *
 * @author maeda6uiui
 */
public class MttVulkanImplHeadless implements IMttVulkanImplCommon {
    private static final int COLOR_IMAGE_FORMAT = VK_FORMAT_B8G8R8A8_SRGB;

    private VkPhysicalDevice physicalDevice;

    private DeviceAndQueues dq;
    private long commandPool;

    private int albedoMSAASamples;
    private int depthImageFormat;
    private int depthImageAspect;

    private TextureOperationNabor textureOperationNabor;
    private QuadDrawer quadDrawer;

    private int width;
    private int height;

    public MttVulkanImplHeadless(int width, int height) {
        MttSettings settings = MttSettings.get().orElse(new MttSettings());

        MttVulkanInstance
                .get()
                .ifPresent(v -> {
                    physicalDevice = PhysicalDevicePicker.pickPhysicalDevice(
                            v.getVkInstance(),
                            -1,
                            settings.vulkanSettings.preferablePhysicalDeviceIndex,
                            false
                    );
                });

        dq = LogicalDeviceCreator.createLogicalDevice(
                physicalDevice,
                settings.vulkanSettings.preferableGraphicsFamilyIndex,
                settings.vulkanSettings.enableValidationLayer
        );

        commandPool = CommandPoolCreator.createCommandPool(dq.device(), dq.graphicsFamilyIndex());

        albedoMSAASamples = settings.vulkanSettings.albedoMSAASamples < 0
                ? MultisamplingUtils.getMaxUsableSampleCount(dq.device())
                : settings.vulkanSettings.albedoMSAASamples;
        depthImageFormat = DepthResourceUtils.findDepthFormat(dq.device());
        depthImageAspect = VK_IMAGE_ASPECT_DEPTH_BIT;

        boolean hasStencilComponent = DepthResourceUtils.hasStencilComponent(depthImageFormat);
        if (hasStencilComponent) {
            depthImageAspect |= VK_IMAGE_ASPECT_STENCIL_BIT;
        }

        MttShaderConfig shaderConfig = MttShaderConfig
                .get()
                .orElse(MttShaderConfig.create());

        List<URL> textureOperationVertShaderResources = shaderConfig.textureOperation.vertex.mustGetResourceURLs();
        List<URL> textureOperationFragShaderResources = shaderConfig.textureOperation.fragment.mustGetResourceURLs();

        int textureOperationWidth;
        if (settings.textureOperation.width < 0) {
            textureOperationWidth = width;
        } else {
            textureOperationWidth = settings.textureOperation.width;
        }

        int textureOperationHeight;
        if (settings.textureOperation.height < 0) {
            textureOperationHeight = height;
        } else {
            textureOperationHeight = settings.textureOperation.height;
        }

        VkExtent2D textureOperationExtent = VkExtent2D.create().set(textureOperationWidth, textureOperationHeight);

        textureOperationNabor = new TextureOperationNabor(
                dq.device(), textureOperationVertShaderResources, textureOperationFragShaderResources);
        textureOperationNabor.compile(
                COLOR_IMAGE_FORMAT,
                VK_FILTER_NEAREST,
                VK_SAMPLER_MIPMAP_MODE_NEAREST,
                VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER,
                textureOperationExtent,
                commandPool,
                dq.graphicsQueue(),
                1
        );

        quadDrawer = new QuadDrawer(dq.device(), commandPool, dq.graphicsQueue());

        this.width = width;
        this.height = height;
    }

    public void cleanup() {
        vkDeviceWaitIdle(dq.device());
        quadDrawer.cleanup();
        textureOperationNabor.cleanup(false);

        vkDestroyCommandPool(dq.device(), commandPool, null);
        vkDestroyDevice(dq.device(), null);
    }

    @Override
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

    @Override
    public DeviceAndQueues getDeviceAndQueues() {
        return dq;
    }

    @Override
    public long getCommandPool() {
        return commandPool;
    }

    @Override
    public int getDepthImageFormat() {
        return depthImageFormat;
    }

    @Override
    public int getDepthImageAspect() {
        return depthImageAspect;
    }

    @Override
    public VkExtent2D getExtent() {
        return VkExtent2D.create().set(width, height);
    }

    @Override
    public int getColorImageFormat() {
        return COLOR_IMAGE_FORMAT;
    }

    @Override
    public int getAlbedoMSAASamples() {
        return albedoMSAASamples;
    }

    @Override
    public TextureOperationNabor getTextureOperationNabor() {
        return textureOperationNabor;
    }
}
