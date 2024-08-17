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
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.SurfaceCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.PresentNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.TextureOperationNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.swapchain.Swapchain;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.CommandBufferUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.DepthResourceUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.MultisamplingUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.PhysicalDevicePicker;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Provides Mechtatel functionality implemented with Vulkan
 *
 * @author maeda6uiui
 */
public class MttVulkanImpl implements IMttVulkanImplCommon {
    private static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;

    private long surface;
    private VkPhysicalDevice physicalDevice;

    private DeviceAndQueues dq;
    private long commandPool;
    private Swapchain swapchain;

    private int albedoMSAASamples;
    private int depthImageFormat;
    private int depthImageAspect;

    private PresentNabor presentNabor;
    private TextureOperationNabor textureOperationNabor;
    private QuadDrawer quadDrawer;
    private long acquireImageIndexFence;

    private VkExtent2D textureOperationInitialExtent;

    private VkExtent2D getFramebufferSize(long window) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.ints(0);
            IntBuffer height = stack.ints(0);
            while (width.get(0) == 0 && height.get(0) == 0) {
                glfwGetFramebufferSize(window, width, height);
                glfwWaitEvents();
            }

            VkExtent2D framebufferSize = VkExtent2D.create();
            framebufferSize.set(width.get(0), height.get(0));

            return framebufferSize;
        }
    }

    public void recreateResourcesOnResize(long window) {
        vkDeviceWaitIdle(dq.device());
        swapchain.cleanup();

        VkExtent2D framebufferSize = this.getFramebufferSize(window);
        swapchain = new Swapchain(
                dq.device(),
                surface,
                dq.graphicsFamilyIndex(),
                dq.presentFamilyIndex(),
                framebufferSize.width(),
                framebufferSize.height()
        );

        presentNabor.recreate(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent());
        swapchain.createFramebuffers(presentNabor.getRenderPass());

        MttSettings settings = MttSettings.get().orElse(new MttSettings());
        VkExtent2D textureOperationExtent;
        if (settings.textureOperation.changeExtentOnRecreate) {
            textureOperationExtent = swapchain.getSwapchainExtent();
        } else {
            textureOperationExtent = textureOperationInitialExtent;
        }

        textureOperationNabor.recreate(
                swapchain.getSwapchainImageFormat(),
                textureOperationExtent);
        textureOperationNabor.cleanupUserDefImages();
    }

    public MttVulkanImpl(long window) {
        MttSettings settings = MttSettings.get().orElse(new MttSettings());

        MttVulkanInstance
                .get()
                .ifPresent(v -> {
                    surface = SurfaceCreator.createSurface(v.getVkInstance(), window);
                    physicalDevice = PhysicalDevicePicker.pickPhysicalDevice(
                            v.getVkInstance(),
                            surface,
                            settings.vulkanSettings.preferablePhysicalDeviceIndex,
                            true
                    );
                });

        dq = LogicalDeviceCreator.createLogicalDevice(
                physicalDevice,
                surface,
                settings.vulkanSettings.preferableGraphicsFamilyIndex,
                settings.vulkanSettings.preferablePresentFamilyIndex,
                settings.vulkanSettings.enableValidationLayer
        );

        commandPool = CommandPoolCreator.createCommandPool(dq.device(), dq.graphicsFamilyIndex());

        VkExtent2D framebufferSize = this.getFramebufferSize(window);
        swapchain = new Swapchain(
                dq.device(),
                surface,
                dq.graphicsFamilyIndex(),
                dq.presentFamilyIndex(),
                framebufferSize.width(),
                framebufferSize.height());

        albedoMSAASamples = settings.vulkanSettings.albedoMSAASamples < 0
                ? MultisamplingUtils.getMaxUsableSampleCount(dq.device())
                : settings.vulkanSettings.albedoMSAASamples;
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

        URL presentVertShaderResource;
        URL presentFragShaderResource;
        URL textureOperationVertShaderResource;
        URL textureOperationFragShaderResource;
        try {
            presentVertShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.present.main.vert.filepath,
                    shaderSettings.present.main.vert.external
            );
            presentFragShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.present.main.frag.filepath,
                    shaderSettings.present.main.frag.external
            );

            textureOperationVertShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.textureOperation.main.vert.filepath,
                    shaderSettings.textureOperation.main.vert.external
            );
            textureOperationFragShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.textureOperation.main.frag.filepath,
                    shaderSettings.textureOperation.main.frag.external
            );
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        //==========

        presentNabor = new PresentNabor(dq.device(), presentVertShaderResource, presentFragShaderResource);
        presentNabor.compile(
                swapchain.getSwapchainImageFormat(),
                VK_FILTER_NEAREST,
                VK_SAMPLER_MIPMAP_MODE_NEAREST,
                VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER,
                swapchain.getSwapchainExtent(),
                commandPool,
                dq.graphicsQueue(),
                swapchain.getNumSwapchainImages());
        swapchain.createFramebuffers(presentNabor.getRenderPass());

        int textureOperationWidth;
        if (settings.textureOperation.width < 0) {
            textureOperationWidth = swapchain.getSwapchainExtent().width();
        } else {
            textureOperationWidth = settings.textureOperation.width;
        }

        int textureOperationHeight;
        if (settings.textureOperation.height < 0) {
            textureOperationHeight = swapchain.getSwapchainExtent().height();
        } else {
            textureOperationHeight = settings.textureOperation.height;
        }

        VkExtent2D textureOperationExtent = VkExtent2D.create().set(textureOperationWidth, textureOperationHeight);
        textureOperationInitialExtent = textureOperationExtent;

        textureOperationNabor = new TextureOperationNabor(
                dq.device(), textureOperationVertShaderResource, textureOperationFragShaderResource);
        textureOperationNabor.compile(
                swapchain.getSwapchainImageFormat(),
                VK_FILTER_NEAREST,
                VK_SAMPLER_MIPMAP_MODE_NEAREST,
                VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER,
                textureOperationExtent,
                commandPool,
                dq.graphicsQueue(),
                1
        );

        quadDrawer = new QuadDrawer(dq.device(), commandPool, dq.graphicsQueue());

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack);
            fenceInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

            LongBuffer pFence = stack.mallocLong(1);
            vkCreateFence(dq.device(), fenceInfo, null, pFence);
            acquireImageIndexFence = pFence.get(0);
        }
    }

    public void cleanup() {
        vkDeviceWaitIdle(dq.device());

        quadDrawer.cleanup();

        swapchain.cleanup();
        textureOperationNabor.cleanup(false);
        presentNabor.cleanup(false);
        vkDestroyFence(dq.device(), acquireImageIndexFence, null);

        vkDestroyCommandPool(dq.device(), commandPool, null);
        vkDestroyDevice(dq.device(), null);
        MttVulkanInstance.get().ifPresent(v -> vkDestroySurfaceKHR(v.getVkInstance(), surface, null));
    }

    public boolean presentToFrontScreen(VkMttScreen screen) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            //Get next image index
            vkResetFences(dq.device(), acquireImageIndexFence);

            IntBuffer pImageIndex = stack.mallocInt(1);
            int vkResult = vkAcquireNextImageKHR(
                    dq.device(), swapchain.getSwapchain(), UINT64_MAX, VK_NULL_HANDLE, acquireImageIndexFence, pImageIndex);
            if (vkResult == VK_ERROR_OUT_OF_DATE_KHR) {
                return true;
            } else if (vkResult != VK_SUCCESS) {
                throw new RuntimeException("Cannot get image: " + vkResult);
            }

            int imageIndex = pImageIndex.get(0);

            vkWaitForFences(dq.device(), acquireImageIndexFence, true, UINT64_MAX);

            //Rendering
            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(presentNabor.getRenderPass());
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(presentNabor.getExtent());
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
            clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
            renderPassInfo.pClearValues(clearValues);
            renderPassInfo.framebuffer(swapchain.getSwapchainFramebuffer(imageIndex));

            VkCommandBuffer renderingCommandBuffer = CommandBufferUtils.beginSingleTimeCommands(dq.device(), commandPool);
            vkCmdBeginRenderPass(renderingCommandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(
                        renderingCommandBuffer,
                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                        presentNabor.getGraphicsPipeline(0));

                long colorImageView = screen.getColorImageView();
                presentNabor.bindBackScreen(renderingCommandBuffer, imageIndex, colorImageView);
                quadDrawer.draw(renderingCommandBuffer);
            }
            vkCmdEndRenderPass(renderingCommandBuffer);
            CommandBufferUtils.endSingleTimeCommands(
                    dq.device(), commandPool, renderingCommandBuffer, dq.graphicsQueue());

            //Presentation
            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
            presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
            presentInfo.swapchainCount(1);
            presentInfo.pSwapchains(stack.longs(swapchain.getSwapchain()));
            presentInfo.pImageIndices(pImageIndex);

            boolean mustRecreate = false;
            vkResult = vkQueuePresentKHR(dq.presentQueue(), presentInfo);
            if (vkResult == VK_ERROR_OUT_OF_DATE_KHR || vkResult == VK_SUBOPTIMAL_KHR) {
                mustRecreate = true;
            } else if (vkResult != VK_SUCCESS) {
                throw new RuntimeException("Failed to present a swapchain image: " + vkResult);
            }

            return mustRecreate;
        }
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

    /**
     * Returns the extent of the swapchain.
     *
     * @return Extent of swapchain
     */
    @Override
    public VkExtent2D getExtent() {
        return swapchain.getSwapchainExtent();
    }

    /**
     * Returns the image format of the swapchain.
     *
     * @return Image format of swapchain
     */
    @Override
    public int getColorImageFormat() {
        return swapchain.getSwapchainImageFormat();
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
