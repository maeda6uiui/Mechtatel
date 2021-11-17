package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.light.LightingInfo;
import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkComponent3D;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkModel3D;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.*;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.frame.Frame;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.GBufferNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.PresentNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.*;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.shadow.ShadowMappingNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.shadow.ShadowMappingNaborRunner;
import com.github.maeda6uiui.mechtatel.core.vulkan.swapchain.Swapchain;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.*;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.*;
import com.github.maeda6uiui.mechtatel.core.vulkan.validation.ValidationLayers;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
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

    private VkPhysicalDevice physicalDevice;

    private VkDevice device;
    private VkQueue graphicsQueue;

    private long window;
    private long surface;
    private VkQueue presentQueue;

    private int msaaSamples;
    private int depthImageFormat;
    private int depthImageAspect;
    private int depthImageWidth;
    private int depthImageHeight;

    private Swapchain swapchain;

    private PresentNabor presentNabor;
    private GBufferNabor gBufferNabor;
    private Map<String, PostProcessingNabor> ppNabors;
    private PostProcessingNabor lastPPNabor;

    private long commandPool;

    private List<Frame> inFlightFrames;
    private Map<Integer, Frame> imagesInFlight;
    private int currentFrame;

    private ArrayList<VkComponent3D> components;

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

    private void createShadowMappingNaborUserDefImages(PostProcessingNabor shadowMappingNabor) {
        shadowMappingNabor.cleanupUserDefImages();

        //Shadow depth
        for (int i = 0; i < ShadowMappingNabor.MAX_NUM_SHADOW_MAPS; i++) {
            shadowMappingNabor.createUserDefImage(
                    depthImageWidth,
                    depthImageHeight,
                    1,
                    VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    depthImageFormat,
                    VK_IMAGE_ASPECT_DEPTH_BIT);
        }
    }

    private void recreateNabors() {
        gBufferNabor.recreate(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent());

        for (var entry : ppNabors.entrySet()) {
            String naborName = entry.getKey();
            PostProcessingNabor ppNabor = entry.getValue();

            ppNabor.recreate(
                    swapchain.getSwapchainImageFormat(),
                    swapchain.getSwapchainExtent());

            if (naborName.equals("shadow_mapping")) {
                this.createShadowMappingNaborUserDefImages(ppNabor);
            }
        }
    }

    public MttVulkanInstance(
            boolean enableValidationLayer,
            long window,
            int msaaSamples) {
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

        //Get the image format for depth
        depthImageFormat = DepthResourceUtils.findDepthFormat(device);
        depthImageAspect = VK_IMAGE_ASPECT_DEPTH_BIT;
        depthImageWidth = 2048;
        depthImageHeight = 2048;

        boolean hasStencilComponent = DepthResourceUtils.hasStencilComponent(depthImageFormat);
        if (hasStencilComponent) {
            depthImageAspect |= VK_IMAGE_ASPECT_STENCIL_BIT;
        }

        //Create a command pool
        commandPool = CommandPoolCreator.createCommandPool(device, surface);

        //Create swapchain objects
        this.createSwapchainObjects();

        //Create a nabor for G-Buffer
        gBufferNabor = new GBufferNabor(
                device,
                depthImageFormat,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT);
        gBufferNabor.compile(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent(),
                commandPool,
                graphicsQueue,
                1);

        ppNabors = new LinkedHashMap<>();

        //Create sync objects
        inFlightFrames = SyncObjectsCreator.createSyncObjects(device, MAX_FRAMES_IN_FLIGHT);
        imagesInFlight = new HashMap<>(swapchain.getNumSwapchainImages());

        //Create components
        components = new ArrayList<>();

        //Create a quad drawer to present the back screen to the front screen
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
        gBufferNabor.cleanup(false);
        ppNabors.forEach((k, ppNabor) -> ppNabor.cleanup(false));

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

    private void compilePostProcessingNabors(List<String> naborNames) {
        for (var naborName : naborNames) {
            PostProcessingNabor ppNabor;

            switch (naborName) {
                case "fog":
                    ppNabor = new FogNabor(device);
                    break;
                case "parallel_light":
                    ppNabor = new ParallelLightNabor(device);
                    break;
                case "point_light":
                    ppNabor = new PointLightNabor(device);
                    break;
                case "shadow_mapping":
                    ppNabor = new ShadowMappingNabor(device, depthImageFormat, depthImageWidth, depthImageHeight);
                    this.createShadowMappingNaborUserDefImages(ppNabor);
                    break;
                case "spotlight":
                    ppNabor = new SpotlightNabor(device);
                    break;
                default:
                    String msg = String.format("Unsupported nabor specified: %s", naborName);
                    throw new IllegalArgumentException(msg);
            }

            ppNabor.compile(
                    swapchain.getSwapchainImageFormat(),
                    swapchain.getSwapchainExtent(),
                    commandPool,
                    graphicsQueue,
                    1);
            ppNabors.put(naborName, ppNabor);
        }
    }

    public void createPostProcessingNabors(List<String> naborNames) {
        ppNabors.forEach((k, ppNabor) -> ppNabor.cleanup(false));
        ppNabors.clear();

        this.compilePostProcessingNabors(naborNames);
    }

    private void runGBufferNabor(Vector4f backgroundColor, Camera camera) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long cameraUBOMemory = gBufferNabor.getUniformBufferMemory(0);
            var cameraUBO = new CameraUBO(camera);
            cameraUBO.update(device, cameraUBOMemory);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(gBufferNabor.getRenderPass());
            renderPassInfo.framebuffer(gBufferNabor.getFramebuffer(0));
            VkRect2D renderArea = VkRect2D.callocStack(stack);
            renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
            renderArea.extent(gBufferNabor.getExtent());
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.callocStack(8, stack);
            clearValues.get(0).color().float32(
                    stack.floats(
                            backgroundColor.x,
                            backgroundColor.y,
                            backgroundColor.z,
                            backgroundColor.w));
            clearValues.get(1).depthStencil().set(1.0f, 0);
            clearValues.get(2).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(3).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(4).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(5).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(6).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(7).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            renderPassInfo.pClearValues(clearValues);

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, gBufferNabor.getGraphicsPipeline(0));

                vkCmdBindDescriptorSets(
                        commandBuffer,
                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                        gBufferNabor.getPipelineLayout(0),
                        0,
                        gBufferNabor.pDescriptorSets(),
                        null);

                for (var component : components) {
                    ByteBuffer matBuffer = stack.calloc(1 * 16 * Float.BYTES);
                    component.getMat().get(matBuffer);

                    vkCmdPushConstants(
                            commandBuffer,
                            gBufferNabor.getPipelineLayout(0),
                            VK_SHADER_STAGE_VERTEX_BIT,
                            0,
                            matBuffer);

                    component.draw(commandBuffer, gBufferNabor.getPipelineLayout(0));
                }
            }
            vkCmdEndRenderPass(commandBuffer);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }

    private void runPostProcessingNabors(
            Camera camera,
            Fog fog,
            List<ParallelLight> parallelLights,
            Vector3f parallelLightAmbientColor,
            List<PointLight> pointLights,
            Vector3f pointLightAmbientColor,
            List<Spotlight> spotlights,
            Vector3f spotlightAmbientColor,
            ShadowMappingSettings shadowMappingSettings) {
        for (var entry : ppNabors.entrySet()) {
            String naborName = entry.getKey();
            PostProcessingNabor ppNabor = entry.getValue();

            if (naborName.equals("shadow_mapping")) {
                ShadowMappingNaborRunner.runShadowMappingNabor(
                        device,
                        commandPool,
                        graphicsQueue,
                        gBufferNabor,
                        lastPPNabor,
                        ppNabor,
                        parallelLights,
                        spotlights,
                        components,
                        depthImageAspect,
                        shadowMappingSettings,
                        quadDrawer);

                lastPPNabor = ppNabor;

                continue;
            }

            switch (naborName) {
                case "fog": {
                    long cameraUBOMemory = ppNabor.getUniformBufferMemory(0);
                    var cameraUBO = new CameraUBO(camera);
                    cameraUBO.update(device, cameraUBOMemory);

                    long fogUBOMemory = ppNabor.getUniformBufferMemory(1);
                    var fogUBO = new FogUBO(fog);
                    fogUBO.update(device, fogUBOMemory);
                }
                break;

                case "parallel_light": {
                    long cameraUBOMemory = ppNabor.getUniformBufferMemory(0);
                    var cameraUBO = new CameraUBO(camera);
                    cameraUBO.update(device, cameraUBOMemory);

                    var lightingInfo = new LightingInfo();
                    lightingInfo.setNumLights(parallelLights.size());
                    lightingInfo.setAmbientColor(parallelLightAmbientColor);

                    long lightingInfoUBOMemory = ppNabor.getUniformBufferMemory(1);
                    var lightingInfoUBO = new LightingInfoUBO(lightingInfo);
                    lightingInfoUBO.update(device, lightingInfoUBOMemory);

                    long lightUBOMemory = ppNabor.getUniformBufferMemory(2);
                    for (int i = 0; i < parallelLights.size(); i++) {
                        var lightUBO = new ParallelLightUBO(parallelLights.get(i));
                        lightUBO.update(device, lightUBOMemory, i);
                    }
                }
                break;

                case "point_light": {
                    long cameraUBOMemory = ppNabor.getUniformBufferMemory(0);
                    var cameraUBO = new CameraUBO(camera);
                    cameraUBO.update(device, cameraUBOMemory);

                    var lightingInfo = new LightingInfo();
                    lightingInfo.setNumLights(pointLights.size());
                    lightingInfo.setAmbientColor(pointLightAmbientColor);

                    long lightingInfoUBOMemory = ppNabor.getUniformBufferMemory(1);
                    var lightingInfoUBO = new LightingInfoUBO(lightingInfo);
                    lightingInfoUBO.update(device, lightingInfoUBOMemory);

                    long lightUBOMemory = ppNabor.getUniformBufferMemory(2);
                    for (int i = 0; i < pointLights.size(); i++) {
                        var lightUBO = new PointLightUBO(pointLights.get(i));
                        lightUBO.update(device, lightUBOMemory, i);
                    }
                }
                break;

                case "spotlight": {
                    long cameraUBOMemory = ppNabor.getUniformBufferMemory(0);
                    var cameraUBO = new CameraUBO(camera);
                    cameraUBO.update(device, cameraUBOMemory);

                    var lightingInfo = new LightingInfo();
                    lightingInfo.setNumLights(spotlights.size());
                    lightingInfo.setAmbientColor(spotlightAmbientColor);

                    long lightingInfoUBOMemory = ppNabor.getUniformBufferMemory(1);
                    var lightingInfoUBO = new LightingInfoUBO(lightingInfo);
                    lightingInfoUBO.update(device, lightingInfoUBOMemory);

                    long lightUBOMemory = ppNabor.getUniformBufferMemory(2);
                    for (int i = 0; i < spotlights.size(); i++) {
                        var lightUBO = new SpotlightUBO(spotlights.get(i));
                        lightUBO.update(device, lightUBOMemory, i);
                    }
                }
                break;

                default:
                    throw new RuntimeException("Unsupported nabor specified");
            }

            try (MemoryStack stack = MemoryStack.stackPush()) {
                VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
                renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
                renderPassInfo.renderPass(ppNabor.getRenderPass());
                renderPassInfo.framebuffer(ppNabor.getFramebuffer(0));
                VkRect2D renderArea = VkRect2D.callocStack(stack);
                renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
                renderArea.extent(ppNabor.getExtent());
                renderPassInfo.renderArea(renderArea);
                VkClearValue.Buffer clearValues = VkClearValue.callocStack(1, stack);
                clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
                renderPassInfo.pClearValues(clearValues);

                VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

                vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
                {
                    vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, ppNabor.getGraphicsPipeline(0));

                    //First post-processing
                    if (lastPPNabor == null) {
                        gBufferNabor.transitionAlbedoImage(commandPool, graphicsQueue);
                        gBufferNabor.transitionDepthImage(commandPool, graphicsQueue);
                        gBufferNabor.transitionPositionImage(commandPool, graphicsQueue);
                        gBufferNabor.transitionNormalImage(commandPool, graphicsQueue);

                        ppNabor.bindGBufferImages(
                                commandBuffer,
                                0,
                                gBufferNabor.getAlbedoImageView(),
                                gBufferNabor.getDepthImageView(),
                                gBufferNabor.getPositionImageView(),
                                gBufferNabor.getNormalImageView());
                    } else {
                        lastPPNabor.transitionColorImageLayout(commandPool, graphicsQueue);

                        ppNabor.bindGBufferImages(
                                commandBuffer,
                                0,
                                lastPPNabor.getColorImageView(),
                                gBufferNabor.getDepthImageView(),
                                gBufferNabor.getPositionImageView(),
                                gBufferNabor.getNormalImageView());
                    }

                    quadDrawer.draw(commandBuffer);
                }
                vkCmdEndRenderPass(commandBuffer);

                CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
            }

            lastPPNabor = ppNabor;
        }
    }

    private void presentToFrontScreen() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(presentNabor.getRenderPass());
            VkRect2D renderArea = VkRect2D.callocStack(stack);
            renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
            renderArea.extent(presentNabor.getExtent());
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.callocStack(1, stack);
            clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
            renderPassInfo.pClearValues(clearValues);

            long colorImageView;
            if (lastPPNabor == null) {
                gBufferNabor.transitionAlbedoImage(commandPool, graphicsQueue);
                colorImageView = gBufferNabor.getAlbedoImageView();
            } else {
                lastPPNabor.transitionColorImageLayout(commandPool, graphicsQueue);
                colorImageView = lastPPNabor.getColorImageView();
            }

            lastPPNabor = null;

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

    public void draw(
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
        this.runGBufferNabor(backgroundColor, camera);
        this.runPostProcessingNabors(
                camera,
                fog,
                parallelLights,
                parallelLightAmbientColor,
                pointLights,
                pointLightAmbientColor,
                spotlights,
                spotlightAmbientColor,
                shadowMappingSettings);
        this.presentToFrontScreen();
    }

    //=== Methods relating to components ===
    @Override
    public boolean removeComponent(VkComponent component) {
        if (!components.contains(component)) {
            return false;
        }

        component.cleanup();
        components.remove(component);

        return true;
    }

    public VkModel3D createModel3D(String modelFilepath) {
        int numDescriptorSets = gBufferNabor.getNumDescriptorSets();
        var descriptorSets = new ArrayList<Long>();
        for (int i = 0; i < numDescriptorSets; i++) {
            descriptorSets.add(gBufferNabor.getDescriptorSet(i));
        }

        var model = new VkModel3D(
                device,
                commandPool,
                graphicsQueue,
                descriptorSets,
                gBufferNabor.getSetCount(),
                modelFilepath);
        components.add(model);

        return model;
    }

    public VkModel3D duplicateModel3D(VkModel3D srcModel) {
        var model = new VkModel3D(device, commandPool, graphicsQueue, srcModel);
        components.add(model);

        return model;
    }
}
