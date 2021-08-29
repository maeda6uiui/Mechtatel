package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.camera.CameraUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkModel3D;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkVertex3DUV;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.*;
import com.github.maeda6uiui.mechtatel.core.vulkan.frame.Frame;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.*;
import com.github.maeda6uiui.mechtatel.core.vulkan.validation.ValidationLayers;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Provides functionality relating to Vulkan
 *
 * @author maeda
 */
public class MttVulkanInstance implements IMttVulkanInstanceForComponent {
    private static final int MAX_FRAMES_IN_FLIGHT = 2;

    private VkInstance instance;

    private boolean enableValidationLayer;
    private long debugMessenger;

    private int msaaSamples;

    private VkPhysicalDevice physicalDevice;

    private VkDevice device;
    private VkQueue graphicsQueue;

    private long window;
    private long surface;
    private VkQueue presentQueue;

    private long swapchain;
    private List<Long> swapchainImages;
    private List<Long> swapchainImageViews;
    private int swapchainImageFormat;
    private VkExtent2D swapchainExtent;
    private List<Long> swapchainFramebuffers;

    private long renderPass;
    private long descriptorSetLayout;
    private long pipelineLayout;
    private long graphicsPipeline;
    private long vertShaderModule;
    private long fragShaderModule;

    private long commandPool;

    private List<Frame> inFlightFrames;
    private Map<Integer, Frame> imagesInFlight;
    private int currentFrame;

    private List<Long> cameraUBs;
    private List<Long> cameraUBMemories;

    private long depthImage;
    private long depthImageMemory;
    private long depthImageView;

    private long colorImage;
    private long colorImageMemory;
    private long colorImageView;

    private long textureSampler;

    private ArrayList<VkComponent> components;

    private void createSwapchainObjects(boolean recreate) {
        //Create a swapchain
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.ints(0);
            IntBuffer height = stack.ints(0);
            glfwGetFramebufferSize(window, width, height);

            SwapchainUtils.SwapchainRelatingData swapchainRelatingData
                    = SwapchainUtils.createSwapchain(device, surface, width.get(0), height.get(0));
            swapchain = swapchainRelatingData.swapchain;
            swapchainImages = swapchainRelatingData.swapchainImages;
            swapchainImageFormat = swapchainRelatingData.swapchainImageFormat;
            swapchainExtent = swapchainRelatingData.swapchainExtent;
        }

        //Create image views
        swapchainImageViews = SwapchainUtils.createSwapchainImageViews(device, swapchainImages, swapchainImageFormat);

        //Create a render pass
        renderPass = RenderpassCreator.createRenderPass(device, swapchainImageFormat, msaaSamples);

        //Create color resources
        ColorResourceCreator.ColorResources colorResources = ColorResourceCreator.createColorResources(
                device,
                commandPool,
                graphicsQueue,
                swapchainExtent,
                msaaSamples,
                swapchainImageFormat);
        colorImage = colorResources.colorImage;
        colorImageMemory = colorResources.colorImageMemory;
        colorImageView = colorResources.colorImageView;

        //Create a graphics pipeline
        if (recreate) {
            GraphicsPipelineCreator.GraphicsPipelineInfo graphicsPipelineInfo = GraphicsPipelineCreator.recreateGraphicsPipeline(
                    device,
                    swapchainExtent,
                    renderPass,
                    VkVertex3DUV.getBindingDescription(),
                    VkVertex3DUV.getAttributeDescriptions(),
                    descriptorSetLayout,
                    msaaSamples,
                    vertShaderModule,
                    fragShaderModule);
            pipelineLayout = graphicsPipelineInfo.pipelineLayout;
            graphicsPipeline = graphicsPipelineInfo.graphicsPipeline;
        } else {
            GraphicsPipelineCreator.GraphicsPipelineInfo graphicsPipelineInfo = GraphicsPipelineCreator.createGraphicsPipeline(
                    device,
                    swapchainExtent,
                    renderPass,
                    VkVertex3DUV.getBindingDescription(),
                    VkVertex3DUV.getAttributeDescriptions(),
                    descriptorSetLayout,
                    msaaSamples,
                    "./Mechtatel/Shader/Standard/3D/simple.vert",
                    "./Mechtatel/Shader/Standard/3D/simple.frag");
            pipelineLayout = graphicsPipelineInfo.pipelineLayout;
            graphicsPipeline = graphicsPipelineInfo.graphicsPipeline;
            vertShaderModule = graphicsPipelineInfo.vertShaderModule;
            fragShaderModule = graphicsPipelineInfo.fragShaderModule;
        }

        //Create depth resources
        DepthResourceUtils.DepthResources depthResources
                = DepthResourceUtils.createDepthResources(device, commandPool, graphicsQueue, swapchainExtent, msaaSamples);
        depthImage = depthResources.depthImage;
        depthImageMemory = depthResources.depthImageMemory;
        depthImageView = depthResources.depthImageView;

        //Create swapchain framebuffers
        swapchainFramebuffers = FramebufferCreator.createFramebuffers(
                device, swapchainImageViews, colorImageView, depthImageView, renderPass, swapchainExtent);
    }

    public MttVulkanInstance(boolean enableValidationLayer, long window, int msaaSamples) {
        //Load the Shaderc library
        System.setProperty("java.library.path", "./Mechtatel/Bin");
        System.loadLibrary("shaderc_shared");

        this.enableValidationLayer = enableValidationLayer;
        this.window = window;

        //Create a Vulkan instance
        instance = InstanceCreator.createInstance(enableValidationLayer);

        //Set up a debug messenger
        if (enableValidationLayer) {
            debugMessenger = ValidationLayers.setupDebugMessenger(instance);
        }

        //Create a window surface
        surface = SurfaceCreator.createSurface(instance, window);

        //Pick up a physical device
        physicalDevice = PhysicalDevicePicker.pickPhysicalDevice(instance, surface);

        //Create a logical device and queues
        LogicalDeviceCreator.VkDeviceAndVkQueues deviceAndQueues
                = LogicalDeviceCreator.createLogicalDevice(physicalDevice, enableValidationLayer, surface);
        device = deviceAndQueues.device;
        graphicsQueue = deviceAndQueues.graphicsQueue;
        presentQueue = deviceAndQueues.presentQueue;

        //Get the MSAA sample count
        this.msaaSamples = msaaSamples < 0 ? MultisamplingUtils.getMaxUsableSampleCount(device) : msaaSamples;

        //Create a command pool
        commandPool = CommandPoolCreator.createCommandPool(device, surface);

        //Create a descriptor set layout
        descriptorSetLayout = DescriptorSetLayoutCreator.createDescriptorSetLayout(device);

        //Create swapchain objects
        this.createSwapchainObjects(false);

        //Create sync objects
        inFlightFrames = SyncObjectsCreator.createSyncObjects(device, MAX_FRAMES_IN_FLIGHT);
        imagesInFlight = new HashMap<>(swapchainImages.size());

        //Create uniform buffers and uniform buffer memories
        List<BufferCreator.BufferInfo> cameraUBInfos = BufferCreator.createUBOBuffers(
                device, swapchainImages.size(), CameraUBO.SIZEOF);
        cameraUBs = new ArrayList<>();
        cameraUBMemories = new ArrayList<>();
        for (var cameraUBInfo : cameraUBInfos) {
            cameraUBs.add(cameraUBInfo.buffer);
            cameraUBMemories.add(cameraUBInfo.bufferMemory);
        }

        //Create a texture sampler
        textureSampler = TextureSamplerCreator.createTextureSampler(device);

        components = new ArrayList<>();
    }

    private void cleanupSwapchain() {
        swapchainFramebuffers.forEach(framebuffer -> vkDestroyFramebuffer(device, framebuffer, null));

        vkDestroyImageView(device, depthImageView, null);
        vkDestroyImage(device, depthImage, null);
        vkFreeMemory(device, depthImageMemory, null);

        vkDestroyPipeline(device, graphicsPipeline, null);
        vkDestroyPipelineLayout(device, pipelineLayout, null);

        vkDestroyImageView(device, colorImageView, null);
        vkDestroyImage(device, colorImage, null);
        vkFreeMemory(device, colorImageMemory, null);

        vkDestroyRenderPass(device, renderPass, null);

        swapchainImageViews.forEach(imageView -> vkDestroyImageView(device, imageView, null));

        vkDestroySwapchainKHR(device, swapchain, null);
    }

    public void cleanup() {
        components.forEach(component -> component.cleanup());

        vkDestroySampler(device, textureSampler, null);

        cameraUBs.forEach(ubo -> vkDestroyBuffer(device, ubo, null));
        cameraUBMemories.forEach(uboMemory -> vkFreeMemory(device, uboMemory, null));

        inFlightFrames.forEach(frame -> {
            vkDestroySemaphore(device, frame.renderFinishedSemaphore(), null);
            vkDestroySemaphore(device, frame.imageAvailableSemaphore(), null);
            vkDestroyFence(device, frame.fence(), null);
        });
        imagesInFlight.clear();

        this.cleanupSwapchain();

        vkDestroyShaderModule(device, vertShaderModule, null);
        vkDestroyShaderModule(device, fragShaderModule, null);

        vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null);

        vkDestroyCommandPool(device, commandPool, null);

        vkDestroyDevice(device, null);

        if (enableValidationLayer) {
            ValidationLayers.destroyDebugUtilsMessengerEXT(instance, debugMessenger, null);
        }

        vkDestroySurfaceKHR(instance, surface, null);

        vkDestroyInstance(instance, null);
    }

    public void recreateSwapchain() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.ints(0);
            IntBuffer height = stack.ints(0);

            while (width.get(0) == 0 && height.get(0) == 0) {
                glfwGetFramebufferSize(window, width, height);
                glfwWaitEvents();
            }
        }

        vkDeviceWaitIdle(device);

        this.cleanupSwapchain();
        this.createSwapchainObjects(true);
    }

    public void draw(Camera camera) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(renderPass);
            VkRect2D renderArea = VkRect2D.callocStack(stack);
            renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
            renderArea.extent(swapchainExtent);
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.callocStack(2, stack);
            clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
            clearValues.get(1).depthStencil().set(1.0f, 0);
            renderPassInfo.pClearValues(clearValues);

            Frame thisFrame = inFlightFrames.get(currentFrame);

            var commandBuffers
                    = CommandBufferUtils.createCommandBuffers(device, commandPool, swapchainImages.size());

            for (int i = 0; i < commandBuffers.size(); i++) {
                VkCommandBuffer commandBuffer = commandBuffers.get(i);
                if (vkBeginCommandBuffer(commandBuffer, beginInfo) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to begin recording a command buffer");
                }

                renderPassInfo.framebuffer(swapchainFramebuffers.get(i));

                vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
                {
                    vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);

                    for (var component : components) {
                        ByteBuffer matBuffer = stack.calloc(1 * 16 * Float.BYTES);
                        component.getMat().get(matBuffer);

                        vkCmdPushConstants(commandBuffer, pipelineLayout, VK_SHADER_STAGE_VERTEX_BIT, 0, matBuffer);

                        component.draw(commandBuffer, i, pipelineLayout);
                    }
                }
                vkCmdEndRenderPass(commandBuffer);

                if (vkEndCommandBuffer(commandBuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to record a command buffer");
                }
            }

            CameraUBO cameraUBO = camera.createCameraUBO(true);

            UBOUtils.updateCameraUBO(device, cameraUBMemories, cameraUBO);
            int result = thisFrame.drawFrame(
                    swapchain,
                    imagesInFlight,
                    commandBuffers,
                    graphicsQueue,
                    presentQueue);
            if (result < 0) {
                this.recreateSwapchain();
            }

            currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;

            vkDeviceWaitIdle(device);

            vkFreeCommandBuffers(device, commandPool, PointerBufferUtils.asPointerBuffer(commandBuffers));
        }
    }

    //=== Methods relating to components ===
    @Override
    public boolean deleteComponent(VkComponent component) {
        if (!components.contains(component)) {
            return false;
        }

        component.cleanup();
        components.remove(component);

        return true;
    }

    public VkModel3D createModel3D(String modelFilepath) {
        var model = new VkModel3D(
                device,
                commandPool,
                graphicsQueue,
                textureSampler,
                swapchainImages.size(),
                descriptorSetLayout,
                cameraUBs,
                modelFilepath);
        components.add(model);

        return model;
    }
}
