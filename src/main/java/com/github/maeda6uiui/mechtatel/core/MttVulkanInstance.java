package com.github.maeda6uiui.mechtatel.core;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Provides functionality relating to Vulkan
 *
 * @author maeda
 */
class MttVulkanInstance {
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
    private long descriptorPool;
    private long descriptorSetLayout;
    private List<Long> descriptorSets;
    private long pipelineLayout;
    private long graphicsPipeline;

    private long commandPool;

    private List<Frame> inFlightFrames;
    private Map<Integer, Frame> imagesInFlight;
    private int currentFrame;

    private long vertexBuffer;
    private long vertexBufferMemory;

    private long indexBuffer;
    private long indexBufferMemory;

    private List<Long> uniformBuffers;
    private List<Long> uniformBufferMemories;

    private long depthImage;
    private long depthImageMemory;
    private long depthImageView;

    private long textureSampler;
    private Texture texture;

    //For test use
    private List<Vertex3DUV> vertices;
    private List<Integer> indices;

    private PointerBuffer getRequiredExtensions() {
        PointerBuffer glfwExtensions = glfwGetRequiredInstanceExtensions();

        if (enableValidationLayer) {
            MemoryStack stack = MemoryStack.stackGet();

            PointerBuffer extensions = stack.mallocPointer(glfwExtensions.capacity() + 1);
            extensions.put(glfwExtensions);
            extensions.put(stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME));

            return extensions.rewind();
        }

        return glfwExtensions;
    }

    private void createInstance() {
        if (enableValidationLayer && !ValidationLayers.checkValidationLayerSupport()) {
            throw new RuntimeException("Validation requested but not supported");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkApplicationInfo appInfo = VkApplicationInfo.callocStack(stack);
            appInfo.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO);
            appInfo.pApplicationName(stack.UTF8Safe("Mechtatel"));
            appInfo.applicationVersion(VK_MAKE_VERSION(1, 0, 0));
            appInfo.pEngineName(stack.UTF8Safe("No Engine"));
            appInfo.engineVersion(VK_MAKE_VERSION(1, 0, 0));
            appInfo.apiVersion(VK_API_VERSION_1_0);

            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.callocStack(stack);
            createInfo.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
            createInfo.pApplicationInfo(appInfo);
            createInfo.ppEnabledExtensionNames(this.getRequiredExtensions());

            if (enableValidationLayer) {
                createInfo.ppEnabledLayerNames(PointerBufferUtils.asPointerBuffer(ValidationLayers.VALIDATION_LAYERS));

                VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.callocStack(stack);
                ValidationLayers.populateDebugMessengerCreateInfo(debugCreateInfo);
                createInfo.pNext(debugCreateInfo.address());
            }

            PointerBuffer instancePtr = stack.mallocPointer(1);
            if (vkCreateInstance(createInfo, null, instancePtr) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a Vulkan instance");
            }

            instance = new VkInstance(instancePtr.get(0), createInfo);
        }
    }

    private void createSwapchain() {
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
    }

    public MttVulkanInstance(boolean enableValidationLayer, long window) {
        //Load the Shaderc library
        System.setProperty("java.library.path", "./Mechtatel/Bin");
        System.loadLibrary("shaderc_shared");

        this.enableValidationLayer = enableValidationLayer;
        this.window = window;

        //Create a Vulkan instance
        this.createInstance();

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
        this.createSwapchain();

        //Create image views
        swapchainImageViews = SwapchainUtils.createSwapchainImageViews(device, swapchainImages, swapchainImageFormat);

        //Create a render pass
        renderPass = RenderpassCreator.createRenderPass(device, swapchainImageFormat);

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
                "./Mechtatel/Shader/Test/5.vert",
                "./Mechtatel/Shader/Test/5.frag");
        pipelineLayout = graphicsPipelineInfo.pipelineLayout;
        graphicsPipeline = graphicsPipelineInfo.graphicsPipeline;

        //Create a command pool
        commandPool = CommandPoolCreator.createCommandPool(device, surface);

        DepthResourceUtils.DepthResources depthResources
                = DepthResourceUtils.createDepthResources(device, commandPool, graphicsQueue, swapchainExtent);
        depthImage = depthResources.depthImage;
        depthImageMemory = depthResources.depthImageMemory;
        depthImageView = depthResources.depthImageView;

        //Create framebuffers
        swapchainFramebuffers = FramebufferCreator.createFramebuffers(
                device, swapchainImageViews, depthImageView, renderPass, swapchainExtent);

        //Create vertices for test
        vertices = new ArrayList<>();
        var v1 = new Vertex3DUV(new Vector3f(-0.5f, 0.0f, 0.5f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector2f(1.0f, 0.0f));
        var v2 = new Vertex3DUV(new Vector3f(0.5f, 0.0f, 0.5f), new Vector3f(0.0f, 1.0f, 0.0f), new Vector2f(0.0f, 0.0f));
        var v3 = new Vertex3DUV(new Vector3f(0.5f, 0.0f, -0.5f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector2f(0.0f, 1.0f));
        var v4 = new Vertex3DUV(new Vector3f(-0.5f, 0.0f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 1.0f));
        var v5 = new Vertex3DUV(new Vector3f(-0.5f, 0.5f, 0.5f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector2f(1.0f, 0.0f));
        var v6 = new Vertex3DUV(new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0.0f, 1.0f, 0.0f), new Vector2f(0.0f, 0.0f));
        var v7 = new Vertex3DUV(new Vector3f(0.5f, 0.5f, -0.5f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector2f(0.0f, 1.0f));
        var v8 = new Vertex3DUV(new Vector3f(-0.5f, 0.5f, -0.5f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 1.0f));
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);
        vertices.add(v5);
        vertices.add(v6);
        vertices.add(v7);
        vertices.add(v8);

        //Create indices for test
        indices = new ArrayList<>();
        indices.add(0);
        indices.add(1);
        indices.add(2);
        indices.add(2);
        indices.add(3);
        indices.add(0);
        indices.add(4);
        indices.add(5);
        indices.add(6);
        indices.add(6);
        indices.add(7);
        indices.add(4);

        //Create a vertex buffer and a vertex buffer memory
        BufferCreator.BufferInfo bufferInfo
                = BufferCreator.createVertexBuffer3DUV(device, commandPool, graphicsQueue, vertices);
        vertexBuffer = bufferInfo.buffer;
        vertexBufferMemory = bufferInfo.bufferMemory;

        //Create an index buffer and an index buffer memory
        bufferInfo = BufferCreator.createIndexBuffer(device, commandPool, graphicsQueue, indices);
        indexBuffer = bufferInfo.buffer;
        indexBufferMemory = bufferInfo.bufferMemory;

        //Create uniform buffers and uniform buffer memories
        List<BufferCreator.BufferInfo> uniformBufferInfos = BufferCreator.createUniformBuffers(device, swapchainImages.size());
        uniformBuffers = new ArrayList<>();
        uniformBufferMemories = new ArrayList<>();
        for (var uniformBufferInfo : uniformBufferInfos) {
            uniformBuffers.add(uniformBufferInfo.buffer);
            uniformBufferMemories.add(uniformBufferInfo.bufferMemory);
        }

        //Create a descriptor pool and descriptor sets
        descriptorPool = DescriptorPoolCreator.createDescriptorPool(device, swapchainImages.size());
        descriptorSets = DescriptorSetsCreator.createDescriptorSets(
                device, swapchainImages.size(), descriptorPool, descriptorSetLayout, uniformBuffers);

        //Create sync objects
        inFlightFrames = SyncObjectsCreator.createSyncObjects(device, MAX_FRAMES_IN_FLIGHT);
        imagesInFlight = new HashMap<>(swapchainImages.size());

        //Create a texture sampler
        textureSampler = TextureSamplerCreator.createTextureSampler(device);

        //Create a texture for test
        texture = new Texture(
                device,
                commandPool,
                graphicsQueue,
                textureSampler,
                descriptorSets,
                "./Mechtatel/Texture/lenna.jpg",
                true);
    }

    public void cleanup() {
        vkDestroyImageView(device, depthImageView, null);
        vkDestroyImage(device, depthImage, null);
        vkFreeMemory(device, depthImageMemory, null);

        vkDestroySampler(device, textureSampler, null);
        texture.cleanup();

        vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null);

        vkDestroyBuffer(device, indexBuffer, null);
        vkFreeMemory(device, indexBufferMemory, null);

        vkDestroyBuffer(device, vertexBuffer, null);
        vkFreeMemory(device, vertexBufferMemory, null);

        inFlightFrames.forEach(frame -> {
            vkDestroySemaphore(device, frame.renderFinishedSemaphore(), null);
            vkDestroySemaphore(device, frame.imageAvailableSemaphore(), null);
            vkDestroyFence(device, frame.fence(), null);
        });
        imagesInFlight.clear();

        vkDestroyCommandPool(device, commandPool, null);

        uniformBuffers.forEach(ubo -> vkDestroyBuffer(device, ubo, null));
        uniformBufferMemories.forEach(uboMemory -> vkFreeMemory(device, uboMemory, null));

        vkDestroyDescriptorPool(device, descriptorPool, null);

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

    //This is a test method for development
    public void draw() {
        List<VkCommandBuffer> commandBuffers = DrawCommandDispatcher.dispatchDrawCommand2D(
                device,
                commandPool,
                renderPass,
                swapchainExtent,
                swapchainFramebuffers,
                graphicsPipeline,
                vertexBuffer,
                indexBuffer,
                indices.size(),
                pipelineLayout,
                descriptorSets);

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
