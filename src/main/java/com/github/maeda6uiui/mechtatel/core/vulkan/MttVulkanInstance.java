package com.github.maeda6uiui.mechtatel.core.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Provides functionality relating to Vulkan
 *
 * @author maeda
 */
public class MttVulkanInstance {
    private static final int MAX_FRAMES_IN_FLIGHT = 2;

    private VkInstance instance;

    private boolean enableValidationLayer;
    private long debugMessenger;

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

    private long commandPool;

    private List<Frame> inFlightFrames;
    private Map<Integer, Frame> imagesInFlight;
    private int currentFrame;

    private List<Long> uniformBuffers;
    private List<Long> uniformBufferMemories;

    private long depthImage;
    private long depthImageMemory;
    private long depthImageView;

    private long colorImage;
    private long colorImageMemory;
    private long colorImageView;

    private long textureSampler;

    private Model model;
    private Model model2;

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

        //Create a swapchain
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWindowWidth = stack.ints(0);
            IntBuffer pWindowHeight = stack.ints(0);
            glfwGetWindowSize(window, pWindowWidth, pWindowHeight);

            SwapchainUtils.SwapchainRelatingData swapchainRelatingData
                    = SwapchainUtils.createSwapchain(device, surface, pWindowWidth.get(0), pWindowHeight.get(0));
            swapchain = swapchainRelatingData.swapchain;
            swapchainImages = swapchainRelatingData.swapchainImages;
            swapchainImageFormat = swapchainRelatingData.swapchainImageFormat;
            swapchainExtent = swapchainRelatingData.swapchainExtent;
        }

        //Create image views
        swapchainImageViews = SwapchainUtils.createSwapchainImageViews(device, swapchainImages, swapchainImageFormat);

        //Create a command pool
        commandPool = CommandPoolCreator.createCommandPool(device, surface);

        //Create color resources
        if (msaaSamples < 0) {
            msaaSamples = MultisamplingUtils.getMaxUsableSampleCount(device);
        }
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

        //Create a render pass
        renderPass = RenderpassCreator.createRenderPass(device, swapchainImageFormat, msaaSamples);

        //Create a descriptor set layout
        descriptorSetLayout = DescriptorSetLayoutCreator.createDescriptorSetLayout(device);

        //Create a graphics pipeline
        GraphicsPipelineCreator.GraphicsPipelineInfo graphicsPipelineInfo = GraphicsPipelineCreator.createGraphicsPipeline(
                device,
                swapchainExtent,
                renderPass,
                Vertex3DUV.getBindingDescription(),
                Vertex3DUV.getAttributeDescriptions(),
                descriptorSetLayout,
                msaaSamples,
                "./Mechtatel/Shader/Test/5.vert",
                "./Mechtatel/Shader/Test/5.frag");
        pipelineLayout = graphicsPipelineInfo.pipelineLayout;
        graphicsPipeline = graphicsPipelineInfo.graphicsPipeline;

        //Create depth resources
        DepthResourceUtils.DepthResources depthResources
                = DepthResourceUtils.createDepthResources(device, commandPool, graphicsQueue, swapchainExtent, msaaSamples);
        depthImage = depthResources.depthImage;
        depthImageMemory = depthResources.depthImageMemory;
        depthImageView = depthResources.depthImageView;

        //Create framebuffers
        swapchainFramebuffers = FramebufferCreator.createFramebuffers(
                device, swapchainImageViews, colorImageView, depthImageView, renderPass, swapchainExtent);

        //Create uniform buffers and uniform buffer memories
        List<BufferCreator.BufferInfo> uniformBufferInfos = BufferCreator.createUniformBuffers(device, swapchainImages.size());
        uniformBuffers = new ArrayList<>();
        uniformBufferMemories = new ArrayList<>();
        for (var uniformBufferInfo : uniformBufferInfos) {
            uniformBuffers.add(uniformBufferInfo.buffer);
            uniformBufferMemories.add(uniformBufferInfo.bufferMemory);
        }

        //Create sync objects
        inFlightFrames = SyncObjectsCreator.createSyncObjects(device, MAX_FRAMES_IN_FLIGHT);
        imagesInFlight = new HashMap<>(swapchainImages.size());

        //Create a texture sampler
        textureSampler = TextureSamplerCreator.createTextureSampler(device);

        //Load models
        model = new Model(
                device,
                commandPool,
                graphicsQueue,
                textureSampler,
                swapchainImages.size(),
                descriptorSetLayout,
                uniformBuffers,
                "./Mechtatel/Model/Cube/cube.obj");
        model2 = new Model(
                device,
                commandPool,
                graphicsQueue,
                textureSampler,
                swapchainImages.size(),
                descriptorSetLayout,
                uniformBuffers,
                "./Mechtatel/Model/Cube/cube2.obj");
    }

    public void cleanup() {
        model.cleanup();
        model2.cleanup();

        vkDestroyImageView(device, colorImageView, null);
        vkDestroyImage(device, colorImage, null);
        vkFreeMemory(device, colorImageMemory, null);

        vkDestroyImageView(device, depthImageView, null);
        vkDestroyImage(device, depthImage, null);
        vkFreeMemory(device, depthImageMemory, null);

        vkDestroySampler(device, textureSampler, null);

        vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null);

        inFlightFrames.forEach(frame -> {
            vkDestroySemaphore(device, frame.renderFinishedSemaphore(), null);
            vkDestroySemaphore(device, frame.imageAvailableSemaphore(), null);
            vkDestroyFence(device, frame.fence(), null);
        });
        imagesInFlight.clear();

        vkDestroyCommandPool(device, commandPool, null);

        uniformBuffers.forEach(ubo -> vkDestroyBuffer(device, ubo, null));
        uniformBufferMemories.forEach(uboMemory -> vkFreeMemory(device, uboMemory, null));

        swapchainFramebuffers.forEach(framebuffer -> vkDestroyFramebuffer(device, framebuffer, null));

        vkDestroyPipeline(device, graphicsPipeline, null);

        vkDestroyPipelineLayout(device, pipelineLayout, null);

        vkDestroyRenderPass(device, renderPass, null);

        swapchainImageViews.forEach(imageView -> vkDestroyImageView(device, imageView, null));

        vkDestroySwapchainKHR(device, swapchain, null);

        vkDestroyDevice(device, null);

        if (enableValidationLayer) {
            ValidationLayers.destroyDebugUtilsMessengerEXT(instance, debugMessenger, null);
        }

        vkDestroySurfaceKHR(instance, surface, null);

        vkDestroyInstance(instance, null);
    }

    public void draw() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            List<VkCommandBuffer> commandBuffers
                    = CommandBufferUtils.createCommandBuffers(device, commandPool, swapchainImages.size());

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

            for (int i = 0; i < commandBuffers.size(); i++) {
                VkCommandBuffer commandBuffer = commandBuffers.get(i);
                if (vkBeginCommandBuffer(commandBuffer, beginInfo) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to begin recording a command buffer");
                }

                renderPassInfo.framebuffer(swapchainFramebuffers.get(i));

                vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
                {
                    vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);

                    model.draw(commandBuffer, i, pipelineLayout);
                    model2.draw(commandBuffer, i, pipelineLayout);
                }
                vkCmdEndRenderPass(commandBuffer);

                if (vkEndCommandBuffer(commandBuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to record a command buffer");
                }
            }

            Frame thisFrame = inFlightFrames.get(currentFrame);
            FrameUtils.drawFrame(
                    device,
                    thisFrame,
                    swapchain,
                    swapchainExtent,
                    imagesInFlight,
                    commandBuffers,
                    graphicsQueue,
                    presentQueue,
                    uniformBufferMemories);
            currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;

            vkDeviceWaitIdle(device);
        }
    }
}
