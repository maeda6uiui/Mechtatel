package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.postprocessing.blur.SimpleBlurInfo;
import com.github.maeda6uiui.mechtatel.core.postprocessing.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.CommandPoolCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.LogicalDeviceCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.SurfaceCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.SyncObjectsCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.frame.Frame;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.PresentNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.TextureOperationNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.swapchain.Swapchain;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.*;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Provides Mechtatel functionality implemented with Vulkan
 *
 * @author maeda6uiui
 */
public class MttVulkanImpl {
    private long surface;
    private VkPhysicalDevice physicalDevice;

    private LogicalDeviceCreator.DeviceAndQueues dq;
    private long commandPool;
    private Swapchain swapchain;

    private int albedoMSAASamples;
    private int depthImageFormat;
    private int depthImageAspect;

    private PresentNabor presentNabor;
    private TextureOperationNabor textureOperationNabor;

    private int maxNumFramesInFlight;
    private List<Frame> inFlightFrames;
    private Map<Integer, Frame> imagesInFlight;
    private int currentFrame;

    private QuadDrawer quadDrawer;

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

        textureOperationNabor.recreate(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent());
        textureOperationNabor.cleanupUserDefImages();

        imagesInFlight.clear();
    }

    public MttVulkanImpl(long window, MttSettings.VulkanSettings vulkanSettings) {
        MttVulkanInstance
                .get()
                .ifPresent(v -> {
                    surface = SurfaceCreator.createSurface(v.getVkInstance(), window);
                    physicalDevice = PhysicalDevicePicker.pickPhysicalDevice(
                            v.getVkInstance(),
                            surface,
                            vulkanSettings.preferablePhysicalDeviceIndex
                    );
                });

        dq = LogicalDeviceCreator.createLogicalDevice(
                physicalDevice,
                surface,
                vulkanSettings.preferableGraphicsFamilyIndex,
                vulkanSettings.preferablePresentFamilyIndex,
                vulkanSettings.enableValidationLayer
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

        albedoMSAASamples = vulkanSettings.albedoMSAASamples < 0
                ? MultisamplingUtils.getMaxUsableSampleCount(dq.device())
                : vulkanSettings.albedoMSAASamples;
        depthImageFormat = DepthResourceUtils.findDepthFormat(dq.device());
        depthImageAspect = VK_IMAGE_ASPECT_DEPTH_BIT;

        boolean hasStencilComponent = DepthResourceUtils.hasStencilComponent(depthImageFormat);
        if (hasStencilComponent) {
            depthImageAspect |= VK_IMAGE_ASPECT_STENCIL_BIT;
        }

        presentNabor = new PresentNabor(dq.device());
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

        textureOperationNabor = new TextureOperationNabor(dq.device());
        textureOperationNabor.compile(
                swapchain.getSwapchainImageFormat(),
                VK_FILTER_NEAREST,
                VK_SAMPLER_MIPMAP_MODE_NEAREST,
                VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER,
                swapchain.getSwapchainExtent(),
                commandPool,
                dq.graphicsQueue(),
                1
        );

        maxNumFramesInFlight = vulkanSettings.maxNumFramesInFlight;
        inFlightFrames = SyncObjectsCreator.createSyncObjects(dq.device(), maxNumFramesInFlight);
        imagesInFlight = new HashMap<>(swapchain.getNumSwapchainImages());

        quadDrawer = new QuadDrawer(dq.device(), commandPool, dq.graphicsQueue());
    }

    public void cleanup() {
        quadDrawer.cleanup();

        inFlightFrames.forEach(frame -> {
            vkDestroySemaphore(dq.device(), frame.renderFinishedSemaphore(), null);
            vkDestroySemaphore(dq.device(), frame.imageAvailableSemaphore(), null);
            vkDestroyFence(dq.device(), frame.fence(), null);
        });
        imagesInFlight.clear();

        swapchain.cleanup();
        textureOperationNabor.cleanup(false);
        presentNabor.cleanup(false);

        vkDestroyCommandPool(dq.device(), commandPool, null);
        vkDestroyDevice(dq.device(), null);
        MttVulkanInstance.get().ifPresent(v -> vkDestroySurfaceKHR(v.getVkInstance(), surface, null));
    }

    public void presentToFrontScreen(VkMttScreen screen) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

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

            long colorImageView = screen.getColorImageView();

            var commandBuffers = CommandBufferUtils.createCommandBuffers(
                    dq.device(), commandPool, swapchain.getNumSwapchainImages());

            for (int i = 0; i < commandBuffers.size(); i++) {
                VkCommandBuffer commandBuffer = commandBuffers.get(i);
                if (vkBeginCommandBuffer(commandBuffer, beginInfo) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to begin recording a command buffer");
                }

                renderPassInfo.framebuffer(swapchain.getSwapchainFramebuffer(i));

                vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
                {
                    vkCmdBindPipeline(
                            commandBuffer,
                            VK_PIPELINE_BIND_POINT_GRAPHICS,
                            presentNabor.getGraphicsPipeline(0));

                    presentNabor.bindBackScreen(commandBuffer, i, colorImageView);
                    quadDrawer.draw(commandBuffer);
                }
                vkCmdEndRenderPass(commandBuffer);

                if (vkEndCommandBuffer(commandBuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to record a command buffer");
                }
            }

            Frame thisFrame = inFlightFrames.get(currentFrame);
            thisFrame.present(
                    swapchain.getSwapchain(),
                    imagesInFlight,
                    commandBuffers,
                    dq.graphicsQueue(),
                    dq.presentQueue()
            );
            currentFrame = (currentFrame + 1) % maxNumFramesInFlight;

            vkDeviceWaitIdle(dq.device());
            vkFreeCommandBuffers(dq.device(), commandPool, PointerBufferUtils.asPointerBuffer(commandBuffers));
        }
    }

    public void draw(
            VkMttScreen screen,
            Vector4f backgroundColor,
            Camera camera,
            Fog fog,
            List<ParallelLight> parallelLights,
            Vector3f parallelLightAmbientColor,
            List<PointLight> pointLights,
            Vector3f pointLightAmbientColor,
            List<Spotlight> spotlights,
            Vector3f spotlightAmbientColor,
            ShadowMappingSettings shadowMappingSettings,
            SimpleBlurInfo simpleBlurInfo,
            List<VkMttComponent> components) {
        screen.run(
                backgroundColor,
                camera,
                fog,
                parallelLights,
                parallelLightAmbientColor,
                pointLights,
                pointLightAmbientColor,
                spotlights,
                spotlightAmbientColor,
                shadowMappingSettings,
                simpleBlurInfo,
                components
        );
    }

    public LogicalDeviceCreator.DeviceAndQueues getDeviceAndQueues() {
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

    public VkExtent2D getSwapchainExtent() {
        return swapchain.getSwapchainExtent();
    }

    public int getSwapchainImageFormat() {
        return swapchain.getSwapchainImageFormat();
    }

    public int getAlbedoMSAASamples() {
        return albedoMSAASamples;
    }

    public TextureOperationNabor getTextureOperationNabor() {
        return textureOperationNabor;
    }
}
