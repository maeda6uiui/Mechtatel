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
    private long pipelineLayout;
    private long graphicsPipeline;

    private long commandPool;

    private List<Frame> inFlightFrames;
    private Map<Integer, Frame> imagesInFlight;
    private int currentFrame;

    private long vertexBuffer;
    private long vertexBufferMemory;

    private List<Vertex2D> vertices;//For test use

    private static final int MAX_FRAMES_IN_FLIGHT = 2;

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

            SwapchainManager.SwapchainRelatingData swapchainRelatingData
                    = SwapchainManager.createSwapchain(device, surface, pWindowWidth.get(0), pWindowHeight.get(0));
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
        swapchainImageViews = SwapchainManager.createSwapchainImageViews(device, swapchainImages, swapchainImageFormat);

        //Create a render pass
        renderPass = RenderpassCreator.createRenderPass(device, swapchainImageFormat);

        //Create a graphics pipeline
        GraphicsPipelineCreator.GraphicsPipelineInfo graphicsPipelineInfo = GraphicsPipelineCreator.createGraphicsPipeline(
                device,
                swapchainExtent,
                renderPass,
                Vertex2D.getBindingDescription(),
                Vertex2D.getAttributeDescriptions(),
                "./Mechtatel/Shader/Test/2.vert",
                "./Mechtatel/Shader/Test/2.frag");
        pipelineLayout = graphicsPipelineInfo.pipelineLayout;
        graphicsPipeline = graphicsPipelineInfo.graphicsPipeline;

        //Create framebuffers
        swapchainFramebuffers = FramebufferCreator.createFramebuffers(
                device, swapchainImageViews, renderPass, swapchainExtent);

        //Create a command pool
        commandPool = CommandPoolCreator.createCommandPool(device, surface);

        //Create vertices for test
        vertices = new ArrayList<>();
        var v1 = new Vertex2D(new Vector2f(0.0f, -0.5f), new Vector3f(1.0f, 0.0f, 0.0f));
        var v2 = new Vertex2D(new Vector2f(0.5f, 0.5f), new Vector3f(0.0f, 1.0f, 0.0f));
        var v3 = new Vertex2D(new Vector2f(-0.5f, 0.5f), new Vector3f(0.0f, 0.0f, 1.0f));
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);

        //Create a vertex buffer and a vertex buffer memory
        VertexBufferCreator.VertexBufferInfo vertexBufferInfo
                = VertexBufferCreator.createVertexBuffer2D(device, commandPool, graphicsQueue, vertices);
        vertexBuffer = vertexBufferInfo.vertexBuffer;
        vertexBufferMemory = vertexBufferInfo.vertexBufferMemory;

        //Create sync objects
        inFlightFrames = SyncObjectsCreator.createSyncObjects(device, MAX_FRAMES_IN_FLIGHT);
        imagesInFlight = new HashMap<>(swapchainImages.size());
    }

    public void cleanup() {
        vkDestroyBuffer(device, vertexBuffer, null);
        vkFreeMemory(device, vertexBufferMemory, null);

        inFlightFrames.forEach(frame -> {
            vkDestroySemaphore(device, frame.renderFinishedSemaphore(), null);
            vkDestroySemaphore(device, frame.imageAvailableSemaphore(), null);
            vkDestroyFence(device, frame.fence(), null);
        });
        imagesInFlight.clear();

        vkDestroyCommandPool(device, commandPool, null);

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
                device, commandPool, renderPass, swapchainExtent,
                swapchainFramebuffers, graphicsPipeline, vertexBuffer, vertices.size());

        Frame thisFrame = inFlightFrames.get(currentFrame);
        FrameDrawer.drawFrame(device, thisFrame, swapchain, imagesInFlight, commandBuffers, graphicsQueue, presentQueue);
        currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;

        vkDeviceWaitIdle(device);
    }
}
