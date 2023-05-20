package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.component.Vertex3D;
import com.github.maeda6uiui.mechtatel.core.component.Vertex3DUV;
import com.github.maeda6uiui.mechtatel.core.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.*;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.*;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.frame.Frame;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.PresentNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.gbuffer.GBufferNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.swapchain.Swapchain;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.VkTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.*;
import com.github.maeda6uiui.mechtatel.core.vulkan.validation.ValidationLayers;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.awt.*;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.List;
import java.util.*;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Provides functionality relating to Vulkan
 *
 * @author maeda6uiui
 */
public class MttVulkanInstance implements IMttVulkanInstanceForComponent {
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

    private int albedoMsaaSamples;
    private int depthImageFormat;
    private int depthImageAspect;

    private Swapchain swapchain;

    private PresentNabor presentNabor;

    private Map<String, VkScreen> screens;

    private long commandPool;

    private List<Frame> inFlightFrames;
    private Map<Integer, Frame> imagesInFlight;
    private int currentFrame;

    private List<VkComponent> components;

    private QuadDrawer quadDrawer;

    private void createSwapchainObjects() {
        //Create a swapchain
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.ints(0);
            IntBuffer height = stack.ints(0);
            glfwGetFramebufferSize(window, width, height);

            swapchain = new Swapchain(
                    device,
                    surface,
                    width.get(0),
                    height.get(0));
        }

        //Create a present nabor
        if (presentNabor == null) {
            presentNabor = new PresentNabor(device);
            presentNabor.compile(
                    swapchain.getSwapchainImageFormat(),
                    swapchain.getSwapchainExtent(),
                    commandPool,
                    graphicsQueue,
                    swapchain.getNumSwapchainImages());
        } else {
            presentNabor.recreate(
                    swapchain.getSwapchainImageFormat(),
                    swapchain.getSwapchainExtent());
        }
        swapchain.createFramebuffers(presentNabor.getRenderPass());
    }

    private void recreateNabors() {
        for (var screen : screens.values()) {
            screen.recreate(swapchain.getSwapchainImageFormat(), swapchain.getSwapchainExtent());
        }
    }

    public MttVulkanInstance(
            boolean enableValidationLayer,
            long window,
            int albedoMsaaSamples) {
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
        this.albedoMsaaSamples = albedoMsaaSamples < 0 ? MultisamplingUtils.getMaxUsableSampleCount(device) : albedoMsaaSamples;

        //Get the image format for depth
        depthImageFormat = DepthResourceUtils.findDepthFormat(device);
        depthImageAspect = VK_IMAGE_ASPECT_DEPTH_BIT;

        boolean hasStencilComponent = DepthResourceUtils.hasStencilComponent(depthImageFormat);
        if (hasStencilComponent) {
            depthImageAspect |= VK_IMAGE_ASPECT_STENCIL_BIT;
        }

        commandPool = CommandPoolCreator.createCommandPool(device, surface);

        this.createSwapchainObjects();

        screens = new HashMap<>();

        inFlightFrames = SyncObjectsCreator.createSyncObjects(device, MAX_FRAMES_IN_FLIGHT);
        imagesInFlight = new HashMap<>(swapchain.getNumSwapchainImages());

        components = new ArrayList<>();

        quadDrawer = new QuadDrawer(device, commandPool, graphicsQueue);
    }

    public void cleanup() {
        quadDrawer.cleanup();

        components.forEach(component -> component.cleanup());

        inFlightFrames.forEach(frame -> {
            vkDestroySemaphore(device, frame.renderFinishedSemaphore(), null);
            vkDestroySemaphore(device, frame.imageAvailableSemaphore(), null);
            vkDestroyFence(device, frame.fence(), null);
        });
        imagesInFlight.clear();

        swapchain.cleanup();
        presentNabor.cleanup(false);
        screens.forEach((k, screen) -> screen.cleanup());

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

        swapchain.cleanup();
        this.createSwapchainObjects();

        this.recreateNabors();
    }

    public void createScreen(
            String screenName,
            int depthImageWidth,
            int depthImageHeight,
            int screenWidth,
            int screenHeight,
            boolean shouldChangeExtentOnRecreate,
            List<String> ppNaborNames) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkExtent2D extent = VkExtent2D.calloc(stack);
            if (screenWidth < 0) {
                extent.width(swapchain.getSwapchainExtent().width());
            } else {
                extent.width(screenWidth);
            }
            if (screenHeight < 0) {
                extent.height(swapchain.getSwapchainExtent().height());
            } else {
                extent.height(screenHeight);
            }

            var screen = new VkScreen(
                    device,
                    commandPool,
                    graphicsQueue,
                    depthImageFormat,
                    depthImageWidth,
                    depthImageHeight,
                    depthImageAspect,
                    swapchain.getSwapchainImageFormat(),
                    albedoMsaaSamples,
                    extent,
                    shouldChangeExtentOnRecreate,
                    ppNaborNames
            );
            screens.put(screenName, screen);
        }
    }

    public boolean removeScreen(String screenName) {
        if (screens.containsKey(screenName)) {
            screens.get(screenName).cleanup();
            screens.remove(screenName);

            return true;
        }

        return false;
    }

    public void removeAllScreens() {
        screens.forEach((k, screen) -> screen.cleanup());
        screens.clear();
    }

    private void presentToFrontScreen(String screenName) {
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

            VkScreen screen = screens.get(screenName);
            long colorImageView = screen.getColorImageView();

            var commandBuffers
                    = CommandBufferUtils.createCommandBuffers(device, commandPool, swapchain.getNumSwapchainImages());

            for (int i = 0; i < commandBuffers.size(); i++) {
                VkCommandBuffer commandBuffer = commandBuffers.get(i);
                if (vkBeginCommandBuffer(commandBuffer, beginInfo) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to begin recording a command buffer");
                }

                renderPassInfo.framebuffer(swapchain.getSwapchainFramebuffer(i));

                vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
                {
                    vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, presentNabor.getGraphicsPipeline(0));

                    presentNabor.bindBackScreen(commandBuffer, i, colorImageView);
                    quadDrawer.draw(commandBuffer);
                }
                vkCmdEndRenderPass(commandBuffer);

                if (vkEndCommandBuffer(commandBuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to record a command buffer");
                }
            }

            Frame thisFrame = inFlightFrames.get(currentFrame);
            int result = thisFrame.present(
                    swapchain.getSwapchain(),
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

    //ここを修正する
    public void draw(
            String screenName,
            Vector4f backgroundColor,
            Camera camera,
            Fog fog,
            List<ParallelLight> parallelLights,
            Vector3f parallelLightAmbientColor,
            List<PointLight> pointLights,
            Vector3f pointLightAmbientColor,
            List<Spotlight> spotlights,
            Vector3f spotlightAmbientColor,
            ShadowMappingSettings shadowMappingSettings) {
        VkScreen screen = screens.get(screenName);
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
                components
        );
    }

    public void present(String screenName) {
        this.presentToFrontScreen(screenName);
    }

    //=== Methods relating to components ===
    @Override
    public boolean removeComponent(VkComponent component) {
        return components.remove(component);
    }

    public void sortComponents() {
        Collections.sort(components);
    }

    public VkModel3D createModel3D(String screenName, String modelFilepath) throws IOException {
        GBufferNabor gBufferNabor = screens.get(screenName).getgBufferNabor();

        int numDescriptorSets = gBufferNabor.getNumDescriptorSets(0);
        var descriptorSets = new ArrayList<Long>();
        for (int i = 0; i < numDescriptorSets; i++) {
            descriptorSets.add(gBufferNabor.getDescriptorSet(0, i));
        }

        var model = new VkModel3D(
                device,
                commandPool,
                graphicsQueue,
                descriptorSets,
                gBufferNabor.getSetCount(0),
                modelFilepath);
        components.add(model);

        return model;
    }

    public VkModel3D duplicateModel3D(VkModel3D srcModel) {
        var model = new VkModel3D(device, commandPool, graphicsQueue, srcModel);
        components.add(model);

        return model;
    }

    public VkLine3D createLine3D(Vertex3D v1, Vertex3D v2) {
        var line = new VkLine3D(device, commandPool, graphicsQueue, v1, v2);
        components.add(line);

        return line;
    }

    public VkLine3DSet createLine3DSet() {
        var lineSet = new VkLine3DSet(device, commandPool, graphicsQueue);
        components.add(lineSet);

        return lineSet;
    }

    public VkSphere3D createSphere3D(
            Vector3fc center,
            float radius,
            int numVDivs,
            int numHDivs,
            Vector4fc color) {
        var sphere = new VkSphere3D(device, commandPool, graphicsQueue, center, radius, numVDivs, numHDivs, color);
        components.add(sphere);

        return sphere;
    }

    public VkCapsule3D createCapsule3D(
            Vector3fc center,
            float length,
            float radius,
            int numVDivs,
            int numHDivs,
            Vector4fc color) {
        var capsule = new VkCapsule3D(device, commandPool, graphicsQueue, center, length, radius, numVDivs, numHDivs, color);
        components.add(capsule);

        return capsule;
    }

    public VkLine2D createLine2D(Vertex3D v1, Vertex3D v2) {
        var line = new VkLine2D(device, commandPool, graphicsQueue, v1, v2);
        components.add(line);

        return line;
    }

    public VkLine2DSet createLine2DSet() {
        var lineSet = new VkLine2DSet(device, commandPool, graphicsQueue);
        components.add(lineSet);

        return lineSet;
    }

    public VkFilledQuad3D createFilledQuad3D(List<Vertex3D> vertices) {
        var filledQuad = new VkFilledQuad3D(device, commandPool, graphicsQueue, vertices);
        components.add(filledQuad);

        return filledQuad;
    }

    public VkFilledQuad2D createFilledQuad2D(List<Vertex3D> vertices) {
        var filledQuad = new VkFilledQuad2D(device, commandPool, graphicsQueue, vertices);
        components.add(filledQuad);

        return filledQuad;
    }

    public VkTexturedQuad3D createTexturedQuad3D(
            String screenName, String textureFilepath, boolean generateMipmaps, List<Vertex3DUV> vertices) {
        GBufferNabor gBufferNabor = screens.get(screenName).getgBufferNabor();

        int numDescriptorSets = gBufferNabor.getNumDescriptorSets(0);
        var descriptorSets = new ArrayList<Long>();
        for (int i = 0; i < numDescriptorSets; i++) {
            descriptorSets.add(gBufferNabor.getDescriptorSet(0, i));
        }

        var texturedQuad = new VkTexturedQuad3D(
                device,
                commandPool,
                graphicsQueue,
                descriptorSets,
                gBufferNabor.getSetCount(0),
                textureFilepath,
                generateMipmaps,
                vertices);
        components.add(texturedQuad);

        return texturedQuad;
    }

    public VkTexturedQuad3D duplicateTexturedQuad3D(
            String screenName, VkTexturedQuad3D srcQuad, List<Vertex3DUV> vertices) {
        GBufferNabor gBufferNabor = screens.get(screenName).getgBufferNabor();

        int numDescriptorSets = gBufferNabor.getNumDescriptorSets(0);
        var descriptorSets = new ArrayList<Long>();
        for (int i = 0; i < numDescriptorSets; i++) {
            descriptorSets.add(gBufferNabor.getDescriptorSet(0, i));
        }

        var texturedQuad = new VkTexturedQuad3D(
                device,
                commandPool,
                graphicsQueue,
                srcQuad,
                vertices);
        components.add(texturedQuad);

        return texturedQuad;
    }

    public VkTexturedQuad2D createTexturedQuad2D(String screenName, String textureFilepath, List<Vertex3DUV> vertices) {
        GBufferNabor gBufferNabor = screens.get(screenName).getgBufferNabor();

        int numDescriptorSets = gBufferNabor.getNumDescriptorSets(0);
        var descriptorSets = new ArrayList<Long>();
        for (int i = 0; i < numDescriptorSets; i++) {
            descriptorSets.add(gBufferNabor.getDescriptorSet(0, i));
        }

        var texturedQuad = new VkTexturedQuad2D(
                device,
                commandPool,
                graphicsQueue,
                descriptorSets,
                gBufferNabor.getSetCount(0),
                textureFilepath,
                false,
                vertices);
        components.add(texturedQuad);

        return texturedQuad;
    }

    public VkTexturedQuad2D duplicateTexturedQuad2D(
            String screenName, VkTexturedQuad2D srcQuad, List<Vertex3DUV> vertices) {
        GBufferNabor gBufferNabor = screens.get(screenName).getgBufferNabor();

        int numDescriptorSets = gBufferNabor.getNumDescriptorSets(0);
        var descriptorSets = new ArrayList<Long>();
        for (int i = 0; i < numDescriptorSets; i++) {
            descriptorSets.add(gBufferNabor.getDescriptorSet(0, i));
        }

        var texturedQuad = new VkTexturedQuad2D(
                device,
                commandPool,
                graphicsQueue,
                srcQuad,
                vertices);
        components.add(texturedQuad);

        return texturedQuad;
    }

    public VkTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(VkTexture texture) {
        var texturedQuadSet = new VkTexturedQuad2DSingleTextureSet(device, commandPool, graphicsQueue, texture);
        components.add(texturedQuadSet);

        return texturedQuadSet;
    }

    public VkTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(
            String screenName, String textureFilepath) {
        GBufferNabor gBufferNabor = screens.get(screenName).getgBufferNabor();

        int numDescriptorSets = gBufferNabor.getNumDescriptorSets(0);
        var descriptorSets = new ArrayList<Long>();
        for (int i = 0; i < numDescriptorSets; i++) {
            descriptorSets.add(gBufferNabor.getDescriptorSet(0, i));
        }

        var texturedQuadSet = new VkTexturedQuad2DSingleTextureSet(
                device,
                commandPool,
                graphicsQueue,
                descriptorSets,
                gBufferNabor.getSetCount(0),
                textureFilepath);
        components.add(texturedQuadSet);

        return texturedQuadSet;
    }

    public VkMttFont createMttFont(
            String screenName, Font font, boolean antiAlias, Color fontColor, String requiredChars) {
        GBufferNabor gBufferNabor = screens.get(screenName).getgBufferNabor();

        int numDescriptorSets = gBufferNabor.getNumDescriptorSets(0);
        var descriptorSets = new ArrayList<Long>();
        for (int i = 0; i < numDescriptorSets; i++) {
            descriptorSets.add(gBufferNabor.getDescriptorSet(0, i));
        }

        var mttFont = new VkMttFont(
                device,
                commandPool,
                graphicsQueue,
                descriptorSets,
                gBufferNabor.getSetCount(0),
                font,
                antiAlias,
                fontColor,
                requiredChars);
        components.add(mttFont);

        return mttFont;
    }

    public void saveScreenshot(String screenName, String srcImageFormat, String outputFilepath) throws IOException {
        VkScreen screen = screens.get(screenName);
        screen.save(srcImageFormat, outputFilepath);
    }
}
