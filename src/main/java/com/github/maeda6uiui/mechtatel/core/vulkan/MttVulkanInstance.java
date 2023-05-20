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
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.MergeScenesNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.PresentNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.PrimitiveNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.gbuffer.GBufferNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.scene.VkScene;
import com.github.maeda6uiui.mechtatel.core.vulkan.swapchain.Swapchain;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.VkTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
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
import java.nio.ByteBuffer;
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
    private int depthImageWidth;
    private int depthImageHeight;

    private Swapchain swapchain;

    private PresentNabor presentNabor;
    private GBufferNabor gBufferNabor;
    private PrimitiveNabor primitiveNabor;
    private PrimitiveNabor primitiveFillNabor;
    private MergeScenesNabor mergeScenesNabor;
    private MergeScenesNabor mergeScenesFillNabor;

    private Map<String, VkScene> scenes;

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
        gBufferNabor.recreate(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent());
        primitiveNabor.recreate(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent());
        primitiveFillNabor.recreate(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent());
        mergeScenesNabor.recreate(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent());
        mergeScenesFillNabor.recreate(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent());

        for (var scene : scenes.values()) {
            scene.recreate(swapchain.getSwapchainImageFormat(), swapchain.getSwapchainExtent());
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
                this.albedoMsaaSamples,
                depthImageFormat,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT);
        gBufferNabor.compile(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent(),
                commandPool,
                graphicsQueue,
                1);

        //Create nabors for primitives
        primitiveNabor = new PrimitiveNabor(
                device,
                depthImageFormat,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                false);
        primitiveNabor.compile(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent(),
                commandPool,
                graphicsQueue,
                1);
        primitiveFillNabor = new PrimitiveNabor(
                device,
                depthImageFormat,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                true);
        primitiveFillNabor.compile(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent(),
                commandPool,
                graphicsQueue,
                1);

        //Create nabors to merge two scenes
        mergeScenesNabor = new MergeScenesNabor(
                device,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT);
        mergeScenesNabor.compile(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent(),
                commandPool,
                graphicsQueue,
                1);
        mergeScenesFillNabor = new MergeScenesNabor(
                device,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT);
        mergeScenesFillNabor.compile(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent(),
                commandPool,
                graphicsQueue,
                1);

        scenes = new HashMap<>();

        //Create sync objects
        inFlightFrames = SyncObjectsCreator.createSyncObjects(device, MAX_FRAMES_IN_FLIGHT);
        imagesInFlight = new HashMap<>(swapchain.getNumSwapchainImages());

        //Create an array list for components
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
        primitiveNabor.cleanup(false);
        primitiveFillNabor.cleanup(false);
        mergeScenesNabor.cleanup(false);
        mergeScenesFillNabor.cleanup(false);
        scenes.forEach((k, scene) -> scene.cleanup());

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

    public void addScene(String sceneName, List<String> ppNaborNames) {
        if (scenes.containsKey(sceneName)) {
            scenes.get(sceneName).cleanup();
        }

        VkScene scene;
        if (ppNaborNames == null) {
            scene = new VkScene();
        } else {
            scene = new VkScene(
                    device,
                    commandPool,
                    graphicsQueue,
                    depthImageFormat,
                    depthImageWidth,
                    depthImageHeight,
                    depthImageAspect,
                    swapchain.getSwapchainImageFormat(),
                    swapchain.getSwapchainExtent(),
                    ppNaborNames
            );
        }

        scenes.put(sceneName, scene);
    }

    public boolean removeScene(String sceneName) {
        if (scenes.containsKey(sceneName)) {
            scenes.get(sceneName).cleanup();
            scenes.remove(sceneName);

            return true;
        }

        return false;
    }

    public void removeAllScenes() {
        for (var scene : scenes.values()) {
            scene.cleanup();
        }

        scenes.clear();
    }

    private void runAlbedoNabor(VkCommandBuffer commandBuffer, Vector4f backgroundColor, Camera camera) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long cameraUBOMemory = gBufferNabor.getUniformBufferMemory(0, 0);
            var cameraUBO = new CameraUBO(camera);
            cameraUBO.update(device, cameraUBOMemory);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(gBufferNabor.getRenderPass(0));
            renderPassInfo.framebuffer(gBufferNabor.getFramebuffer(0, 0));
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(gBufferNabor.getExtent(0));
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
            clearValues.get(0).depthStencil().set(1.0f, 0);
            clearValues.get(1).color().float32(
                    stack.floats(
                            backgroundColor.x,
                            backgroundColor.y,
                            backgroundColor.z,
                            backgroundColor.w));
            renderPassInfo.pClearValues(clearValues);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, gBufferNabor.getGraphicsPipeline(0, 0));

                vkCmdBindDescriptorSets(
                        commandBuffer,
                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                        gBufferNabor.getPipelineLayout(0, 0),
                        0,
                        gBufferNabor.pDescriptorSets(0),
                        null);

                for (var component : components) {
                    if (component.getComponentType() == "gbuffer") {
                        ByteBuffer pcBuffer = stack.calloc(1 * 16 * Float.BYTES + 1 * Integer.BYTES);
                        component.getMat().get(pcBuffer);
                        if (component.isTwoDComponent()) {
                            pcBuffer.putInt(1 * 16 * Float.BYTES, 1);
                        } else {
                            pcBuffer.putInt(1 * 16 * Float.BYTES, 0);
                        }

                        vkCmdPushConstants(
                                commandBuffer,
                                gBufferNabor.getPipelineLayout(0, 0),
                                VK_SHADER_STAGE_VERTEX_BIT,
                                0,
                                pcBuffer);

                        component.draw(commandBuffer, gBufferNabor.getPipelineLayout(0, 0));
                    }
                }
            }
            vkCmdEndRenderPass(commandBuffer);
        }
    }

    private void runPropertiesNabor(VkCommandBuffer commandBuffer, Camera camera) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long cameraUBOMemory = gBufferNabor.getUniformBufferMemory(1, 0);
            var cameraUBO = new CameraUBO(camera);
            cameraUBO.update(device, cameraUBOMemory);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(gBufferNabor.getRenderPass(1));
            renderPassInfo.framebuffer(gBufferNabor.getFramebuffer(1, 0));
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(gBufferNabor.getExtent(1));
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(3, stack);
            clearValues.get(0).depthStencil().set(1.0f, 0);
            clearValues.get(1).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(2).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            renderPassInfo.pClearValues(clearValues);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, gBufferNabor.getGraphicsPipeline(1, 0));

                vkCmdBindDescriptorSets(
                        commandBuffer,
                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                        gBufferNabor.getPipelineLayout(1, 0),
                        0,
                        gBufferNabor.pDescriptorSets(1),
                        null);

                for (var component : components) {
                    if (component.getComponentType() == "gbuffer") {
                        ByteBuffer pcBuffer = stack.calloc(1 * 16 * Float.BYTES + 1 * Integer.BYTES);
                        component.getMat().get(pcBuffer);
                        if (component.isTwoDComponent()) {
                            pcBuffer.putInt(1 * 16 * Float.BYTES, 1);
                        } else {
                            pcBuffer.putInt(1 * 16 * Float.BYTES, 0);
                        }

                        vkCmdPushConstants(
                                commandBuffer,
                                gBufferNabor.getPipelineLayout(1, 0),
                                VK_SHADER_STAGE_VERTEX_BIT,
                                0,
                                pcBuffer);

                        component.transfer(commandBuffer);
                    }
                }
            }
            vkCmdEndRenderPass(commandBuffer);
        }
    }

    private void runGBufferNabor(Vector4f backgroundColor, Camera camera) {
        VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);
        this.runAlbedoNabor(commandBuffer, backgroundColor, camera);
        this.runPropertiesNabor(commandBuffer, camera);
        CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
    }

    private void runPrimitiveNabor(Vector4f backgroundColor, Camera camera) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long cameraUBOMemory = primitiveNabor.getUniformBufferMemory(0);
            var cameraUBO = new CameraUBO(camera);
            cameraUBO.update(device, cameraUBOMemory);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(primitiveNabor.getRenderPass());
            renderPassInfo.framebuffer(primitiveNabor.getFramebuffer(0));
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(primitiveNabor.getExtent());
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(4, stack);
            clearValues.get(0).depthStencil().set(1.0f, 0);
            clearValues.get(1).color().float32(
                    stack.floats(
                            backgroundColor.x,
                            backgroundColor.y,
                            backgroundColor.z,
                            backgroundColor.w));
            clearValues.get(2).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(3).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            renderPassInfo.pClearValues(clearValues);

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, primitiveNabor.getGraphicsPipeline(0));

                vkCmdBindDescriptorSets(
                        commandBuffer,
                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                        primitiveNabor.getPipelineLayout(0),
                        0,
                        primitiveNabor.pDescriptorSets(),
                        null);

                for (var component : components) {
                    if (component.getComponentType() == "primitive") {
                        ByteBuffer pcBuffer = stack.calloc(1 * 16 * Float.BYTES + 1 * 1 * Integer.BYTES);
                        component.getMat().get(pcBuffer);
                        if (component.isTwoDComponent()) {
                            pcBuffer.putInt(1 * 16 * Float.BYTES, 1);
                        } else {
                            pcBuffer.putInt(1 * 16 * Float.BYTES, 0);
                        }

                        vkCmdPushConstants(
                                commandBuffer,
                                primitiveNabor.getPipelineLayout(0),
                                VK_SHADER_STAGE_VERTEX_BIT,
                                0,
                                pcBuffer);

                        component.draw(commandBuffer, primitiveNabor.getPipelineLayout(0));
                    }
                }
            }
            vkCmdEndRenderPass(commandBuffer);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }

    private void runPrimitiveFillNabor(Vector4f backgroundColor, Camera camera) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long cameraUBOMemory = primitiveFillNabor.getUniformBufferMemory(0);
            var cameraUBO = new CameraUBO(camera);
            cameraUBO.update(device, cameraUBOMemory);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(primitiveFillNabor.getRenderPass());
            renderPassInfo.framebuffer(primitiveFillNabor.getFramebuffer(0));
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(primitiveFillNabor.getExtent());
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(4, stack);
            clearValues.get(0).depthStencil().set(1.0f, 0);
            clearValues.get(1).color().float32(
                    stack.floats(
                            backgroundColor.x,
                            backgroundColor.y,
                            backgroundColor.z,
                            backgroundColor.w));
            clearValues.get(2).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(3).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            renderPassInfo.pClearValues(clearValues);

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, primitiveFillNabor.getGraphicsPipeline(0));

                vkCmdBindDescriptorSets(
                        commandBuffer,
                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                        primitiveFillNabor.getPipelineLayout(0),
                        0,
                        primitiveFillNabor.pDescriptorSets(),
                        null);

                for (var component : components) {
                    if (component.getComponentType() == "primitive_fill") {
                        ByteBuffer pcBuffer = stack.calloc(1 * 16 * Float.BYTES + 1 * 1 * Integer.BYTES);
                        component.getMat().get(pcBuffer);
                        if (component.isTwoDComponent()) {
                            pcBuffer.putInt(1 * 16 * Float.BYTES, 1);
                        } else {
                            pcBuffer.putInt(1 * 16 * Float.BYTES, 0);
                        }

                        vkCmdPushConstants(
                                commandBuffer,
                                primitiveFillNabor.getPipelineLayout(0),
                                VK_SHADER_STAGE_VERTEX_BIT,
                                0,
                                pcBuffer);

                        component.draw(commandBuffer, primitiveFillNabor.getPipelineLayout(0));
                    }
                }
            }
            vkCmdEndRenderPass(commandBuffer);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }

    private void runMergeScenesNabor() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(mergeScenesNabor.getRenderPass());
            renderPassInfo.framebuffer(mergeScenesNabor.getFramebuffer(0));
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(mergeScenesNabor.getExtent());
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(4, stack);
            clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(1).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(2).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(3).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            renderPassInfo.pClearValues(clearValues);

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, mergeScenesNabor.getGraphicsPipeline(0));

                gBufferNabor.transitionAlbedoImage(commandPool, graphicsQueue);
                gBufferNabor.transitionDepthImage(commandPool, graphicsQueue);
                gBufferNabor.transitionPositionImage(commandPool, graphicsQueue);
                gBufferNabor.transitionNormalImage(commandPool, graphicsQueue);

                primitiveNabor.transitionAlbedoImage(commandPool, graphicsQueue);
                primitiveNabor.transitionDepthImage(commandPool, graphicsQueue);
                primitiveNabor.transitionPositionImage(commandPool, graphicsQueue);
                primitiveNabor.transitionNormalImage(commandPool, graphicsQueue);

                mergeScenesNabor.bindAlbedoImages(commandBuffer, gBufferNabor.getAlbedoImageView(), primitiveNabor.getAlbedoImageView());
                mergeScenesNabor.bindDepthImages(commandBuffer, gBufferNabor.getDepthImageView(), primitiveNabor.getDepthImageView());
                mergeScenesNabor.bindPositionImages(commandBuffer, gBufferNabor.getPositionImageView(), primitiveNabor.getPositionImageView());
                mergeScenesNabor.bindNormalImages(commandBuffer, gBufferNabor.getNormalImageView(), primitiveNabor.getNormalImageView());

                quadDrawer.draw(commandBuffer);
            }
            vkCmdEndRenderPass(commandBuffer);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }

    private void runMergeScenesFillNabor() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(mergeScenesFillNabor.getRenderPass());
            renderPassInfo.framebuffer(mergeScenesFillNabor.getFramebuffer(0));
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(mergeScenesFillNabor.getExtent());
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(4, stack);
            clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(1).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(2).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(3).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            renderPassInfo.pClearValues(clearValues);

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, mergeScenesFillNabor.getGraphicsPipeline(0));

                mergeScenesNabor.transitionAlbedoImage(commandPool, graphicsQueue);
                mergeScenesNabor.transitionDepthImage(commandPool, graphicsQueue);
                mergeScenesNabor.transitionPositionImage(commandPool, graphicsQueue);
                mergeScenesNabor.transitionNormalImage(commandPool, graphicsQueue);

                primitiveFillNabor.transitionAlbedoImage(commandPool, graphicsQueue);
                primitiveFillNabor.transitionDepthImage(commandPool, graphicsQueue);
                primitiveFillNabor.transitionPositionImage(commandPool, graphicsQueue);
                primitiveFillNabor.transitionNormalImage(commandPool, graphicsQueue);

                mergeScenesFillNabor.bindAlbedoImages(commandBuffer, mergeScenesNabor.getAlbedoImageView(), primitiveFillNabor.getAlbedoImageView());
                mergeScenesFillNabor.bindDepthImages(commandBuffer, mergeScenesNabor.getDepthImageView(), primitiveFillNabor.getDepthImageView());
                mergeScenesFillNabor.bindPositionImages(commandBuffer, mergeScenesNabor.getPositionImageView(), primitiveFillNabor.getPositionImageView());
                mergeScenesFillNabor.bindNormalImages(commandBuffer, mergeScenesNabor.getNormalImageView(), primitiveFillNabor.getNormalImageView());

                quadDrawer.draw(commandBuffer);
            }
            vkCmdEndRenderPass(commandBuffer);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }

    private void presentToFrontScreen(String sceneName) {
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

            VkScene scene = scenes.get(sceneName);

            long colorImageView;
            if (scene.isPassThroughScene()) {
                mergeScenesFillNabor.transitionAlbedoImage(commandPool, graphicsQueue);
                colorImageView = mergeScenesFillNabor.getAlbedoImageView();
            } else {
                scene.transitionColorImageLayout();
                colorImageView = scene.getColorImageView();
            }

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
        for (var scene : scenes.values()) {
            this.runGBufferNabor(backgroundColor, camera);
            this.runPrimitiveNabor(backgroundColor, camera);
            this.runPrimitiveFillNabor(backgroundColor, camera);
            this.runMergeScenesNabor();
            this.runMergeScenesFillNabor();
            scene.run(
                    camera,
                    fog,
                    parallelLights,
                    parallelLightAmbientColor,
                    pointLights,
                    pointLightAmbientColor,
                    spotlights,
                    spotlightAmbientColor,
                    shadowMappingSettings,
                    mergeScenesFillNabor,
                    components
            );
        }
    }

    public void present(String sceneName) {
        this.presentToFrontScreen(sceneName);
    }

    //=== Methods relating to components ===
    @Override
    public boolean removeComponent(VkComponent component) {
        return components.remove(component);
    }

    public void sortComponents() {
        Collections.sort(components);
    }

    public VkModel3D createModel3D(String modelFilepath) throws IOException {
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

    public VkTexturedQuad3D createTexturedQuad3D(String textureFilepath, boolean generateMipmaps, List<Vertex3DUV> vertices) {
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

    public VkTexturedQuad3D duplicateTexturedQuad3D(VkTexturedQuad3D srcQuad, List<Vertex3DUV> vertices) {
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

    public VkTexturedQuad2D createTexturedQuad2D(String textureFilepath, List<Vertex3DUV> vertices) {
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

    public VkTexturedQuad2D duplicateTexturedQuad2D(VkTexturedQuad2D srcQuad, List<Vertex3DUV> vertices) {
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

    public VkTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(String textureFilepath) {
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
            Font font, boolean antiAlias, Color fontColor, String requiredChars) {
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

    public void saveScreenshot(String sceneName, String srcImageFormat, String outputFilepath) throws IOException {
        VkScene scene = scenes.get(sceneName);

        if (scene.isPassThroughScene()) {
            mergeScenesFillNabor.save(commandPool, graphicsQueue, 0, srcImageFormat, outputFilepath);
        } else {
            scene.save(srcImageFormat, outputFilepath);
        }
    }
}
