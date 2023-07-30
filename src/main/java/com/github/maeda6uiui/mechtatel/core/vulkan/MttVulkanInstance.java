package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.component.MttVertex3D;
import com.github.maeda6uiui.mechtatel.core.component.MttVertex3DUV;
import com.github.maeda6uiui.mechtatel.core.nabor.FlexibleNaborInfo;
import com.github.maeda6uiui.mechtatel.core.postprocessing.blur.SimpleBlurInfo;
import com.github.maeda6uiui.mechtatel.core.postprocessing.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.texture.TextureOperationParameters;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.*;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.*;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.frame.Frame;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.PresentNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.TextureOperationNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.swapchain.Swapchain;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.VkMttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.TextureOperationParametersUBO;
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
public class MttVulkanInstance
        implements IMttVulkanInstanceForComponent, IMttVulkanInstanceForTexture, IMttVulkanInstanceForScreen {
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

    private TextureOperationNabor textureOperationNabor;
    private Map<String, TextureOperationNabor.TextureOperationInfo> textureOperationInfos;

    private PresentNabor presentNabor;

    private Map<String, VkMttScreen> screens;

    private long commandPool;

    private List<Frame> inFlightFrames;
    private Map<Integer, Frame> imagesInFlight;
    private int currentFrame;

    private List<VkMttComponent> components;
    private List<VkMttTexture> textures;

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
                    VK_FILTER_NEAREST,
                    VK_SAMPLER_MIPMAP_MODE_NEAREST,
                    VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER,
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

    private void recreateScreens() {
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

        textureOperationNabor = new TextureOperationNabor(device);
        textureOperationNabor.compile(
                swapchain.getSwapchainImageFormat(),
                VK_FILTER_NEAREST,
                VK_SAMPLER_MIPMAP_MODE_NEAREST,
                VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER,
                swapchain.getSwapchainExtent(),
                commandPool,
                graphicsQueue,
                1);

        textureOperationInfos = new HashMap<>();

        screens = new HashMap<>();

        inFlightFrames = SyncObjectsCreator.createSyncObjects(device, MAX_FRAMES_IN_FLIGHT);
        imagesInFlight = new HashMap<>(swapchain.getNumSwapchainImages());

        components = new ArrayList<>();
        textures = new ArrayList<>();

        quadDrawer = new QuadDrawer(device, commandPool, graphicsQueue);
    }

    public void cleanup() {
        quadDrawer.cleanup();

        components.forEach(component -> component.cleanup());
        textures.forEach(texture -> texture.cleanup());

        inFlightFrames.forEach(frame -> {
            vkDestroySemaphore(device, frame.renderFinishedSemaphore(), null);
            vkDestroySemaphore(device, frame.imageAvailableSemaphore(), null);
            vkDestroyFence(device, frame.fence(), null);
        });
        imagesInFlight.clear();

        swapchain.cleanup();
        textureOperationNabor.cleanup(false);
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

        textureOperationNabor.recreate(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent());
        textureOperationNabor.cleanupUserDefImages();
        textureOperationInfos.clear();

        this.recreateScreens();
    }

    public VkMttScreen createScreen(
            String screenName,
            int depthImageWidth,
            int depthImageHeight,
            int screenWidth,
            int screenHeight,
            String samplerFilter,
            String samplerMipmapMode,
            String samplerAddressMode,
            boolean shouldChangeExtentOnRecreate,
            boolean useShadowMapping,
            Map<String, FlexibleNaborInfo> flexibleNaborInfos,
            List<String> ppNaborNames) {
        VkExtent2D extent = VkExtent2D.create();
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

        int iSamplerFilter;
        if (samplerFilter.equals("nearest")) {
            iSamplerFilter = VK_FILTER_NEAREST;
        } else if (samplerFilter.equals("linear")) {
            iSamplerFilter = VK_FILTER_LINEAR;
        } else {
            throw new IllegalArgumentException("Unsupported sampler filter specified: " + samplerFilter);
        }

        int iSamplerMipmapMode;
        if (samplerMipmapMode.equals("nearest")) {
            iSamplerMipmapMode = VK_SAMPLER_MIPMAP_MODE_NEAREST;
        } else if (samplerMipmapMode.equals("linear")) {
            iSamplerMipmapMode = VK_SAMPLER_MIPMAP_MODE_LINEAR;
        } else {
            throw new IllegalArgumentException("Unsupported sampler mipmap mode specified: " + samplerMipmapMode);
        }

        int iSamplerAddressMode;
        if (samplerAddressMode.equals("repeat")) {
            iSamplerAddressMode = VK_SAMPLER_ADDRESS_MODE_REPEAT;
        } else if (samplerAddressMode.equals("mirrored_repeat")) {
            iSamplerAddressMode = VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT;
        } else if (samplerAddressMode.equals("clamp_to_edge")) {
            iSamplerAddressMode = VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE;
        } else if (samplerAddressMode.equals("clamp_to_border")) {
            iSamplerAddressMode = VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER;
        } else {
            throw new IllegalArgumentException("Unsupported sampler address mode specified: " + samplerAddressMode);
        }

        var screen = new VkMttScreen(
                device,
                commandPool,
                graphicsQueue,
                depthImageFormat,
                depthImageWidth,
                depthImageHeight,
                depthImageAspect,
                swapchain.getSwapchainImageFormat(),
                albedoMsaaSamples,
                iSamplerFilter,
                iSamplerMipmapMode,
                iSamplerAddressMode,
                extent,
                shouldChangeExtentOnRecreate,
                useShadowMapping,
                flexibleNaborInfos,
                ppNaborNames,
                screenName
        );
        screens.put(screenName, screen);

        return screen;
    }

    public boolean removeScreen(String screenName) {
        if (screens.containsKey(screenName)) {
            screens.remove(screenName);
            return true;
        }

        return false;
    }

    public void runTextureOperations(String operationName) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(textureOperationNabor.getRenderPass());
            renderPassInfo.framebuffer(textureOperationNabor.getFramebuffer(0));
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(textureOperationNabor.getExtent());
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
            clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
            renderPassInfo.pClearValues(clearValues);

            var textureOperationInfo = textureOperationInfos.get(operationName);

            long parametersUBOMemory = textureOperationNabor.getUniformBufferMemory(0);
            var parametersUBO = new TextureOperationParametersUBO(textureOperationInfo.parameters);
            parametersUBO.update(device, parametersUBOMemory);

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, textureOperationNabor.getGraphicsPipeline(0));

                textureOperationNabor.bindColorImages(
                        commandBuffer,
                        textureOperationInfo.srcColorImageViewA,
                        textureOperationInfo.srcColorImageViewB
                );
                textureOperationNabor.bindDepthImages(
                        commandBuffer,
                        textureOperationInfo.srcDepthImageViewA,
                        textureOperationInfo.srcDepthImageViewB
                );

                quadDrawer.draw(commandBuffer);
            }
            vkCmdEndRenderPass(commandBuffer);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);

            textureOperationNabor.copyColorImage(commandPool, graphicsQueue, textureOperationInfo.dstImage);
        }
    }

    public void presentToFrontScreen(String screenName) {
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

            VkMttScreen screen = screens.get(screenName);
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

    @Override
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
            SimpleBlurInfo simpleBlurInfo) {
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

    @Override
    public boolean removeComponent(VkMttComponent component) {
        return components.remove(component);
    }

    public void sortComponents() {
        Collections.sort(components);
    }

    public VkMttModel3D createModel3D(String screenName, String modelFilepath) throws IOException {
        VkMttScreen screen = screens.get(screenName);

        var model = new VkMttModel3D(
                device,
                commandPool,
                graphicsQueue,
                screen,
                modelFilepath);
        components.add(model);

        return model;
    }

    public VkMttModel3D duplicateModel3D(VkMttModel3D srcModel) {
        var model = new VkMttModel3D(device, commandPool, graphicsQueue, srcModel);
        components.add(model);

        return model;
    }

    public VkMttLine3D createLine3D(MttVertex3D v1, MttVertex3D v2) {
        var line = new VkMttLine3D(device, commandPool, graphicsQueue, v1, v2);
        components.add(line);

        return line;
    }

    public VkMttLine3DSet createLine3DSet() {
        var lineSet = new VkMttLine3DSet(device, commandPool, graphicsQueue);
        components.add(lineSet);

        return lineSet;
    }

    public VkMttSphere3D createSphere3D(
            Vector3fc center,
            float radius,
            int numVDivs,
            int numHDivs,
            Vector4fc color) {
        var sphere = new VkMttSphere3D(device, commandPool, graphicsQueue, center, radius, numVDivs, numHDivs, color);
        components.add(sphere);

        return sphere;
    }

    public VkMttCapsule3D createCapsule3D(
            Vector3fc center,
            float length,
            float radius,
            int numVDivs,
            int numHDivs,
            Vector4fc color) {
        var capsule = new VkMttCapsule3D(device, commandPool, graphicsQueue, center, length, radius, numVDivs, numHDivs, color);
        components.add(capsule);

        return capsule;
    }

    public VkMttLine2D createLine2D(MttVertex3D v1, MttVertex3D v2) {
        var line = new VkMttLine2D(device, commandPool, graphicsQueue, v1, v2);
        components.add(line);

        return line;
    }

    public VkMttLine2DSet createLine2DSet() {
        var lineSet = new VkMttLine2DSet(device, commandPool, graphicsQueue);
        components.add(lineSet);

        return lineSet;
    }

    public VkMttFilledQuad3D createFilledQuad3D(List<MttVertex3D> vertices) {
        var filledQuad = new VkMttFilledQuad3D(device, commandPool, graphicsQueue, vertices);
        components.add(filledQuad);

        return filledQuad;
    }

    public VkMttTexturedQuad3D createTexturedQuad3D(
            String screenName, String textureFilepath, boolean generateMipmaps, List<MttVertex3DUV> vertices) {
        VkMttScreen screen = screens.get(screenName);

        var texturedQuad = new VkMttTexturedQuad3D(
                device,
                commandPool,
                graphicsQueue,
                screen,
                textureFilepath,
                generateMipmaps,
                vertices);
        components.add(texturedQuad);

        return texturedQuad;
    }

    public VkMttTexturedQuad3D createTexturedQuad3D(String screenName, VkMttTexture texture, List<MttVertex3DUV> vertices) {
        var texturedQuad = new VkMttTexturedQuad3D(
                device,
                commandPool,
                graphicsQueue,
                screenName,
                texture,
                vertices);
        components.add(texturedQuad);

        return texturedQuad;
    }

    public VkMttTexturedQuad3D duplicateTexturedQuad3D(VkMttTexturedQuad3D srcQuad, List<MttVertex3DUV> vertices) {
        var texturedQuad = new VkMttTexturedQuad3D(
                device,
                commandPool,
                graphicsQueue,
                srcQuad,
                vertices);
        components.add(texturedQuad);

        return texturedQuad;
    }

    public VkMttTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(String screenName, VkMttTexture texture) {
        var texturedQuadSet = new VkMttTexturedQuad2DSingleTextureSet(
                device, commandPool, graphicsQueue, screenName, texture);
        components.add(texturedQuadSet);

        return texturedQuadSet;
    }

    public VkMttTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(String screenName, String textureFilepath) {
        VkMttScreen screen = screens.get(screenName);

        var texturedQuadSet = new VkMttTexturedQuad2DSingleTextureSet(
                device,
                commandPool,
                graphicsQueue,
                screen,
                textureFilepath);
        components.add(texturedQuadSet);

        return texturedQuadSet;
    }

    public VkMttFont createFont(
            String screenName, Font font, boolean antiAlias, Color fontColor, String requiredChars) {
        VkMttScreen screen = screens.get(screenName);

        var mttFont = new VkMttFont(
                device,
                commandPool,
                graphicsQueue,
                screen,
                font,
                antiAlias,
                fontColor,
                requiredChars);
        components.add(mttFont);

        return mttFont;
    }

    @Override
    public VkMttTexture createTexture(String screenName, String textureFilepath, boolean generateMipmaps) {
        VkMttScreen screen = screens.get(screenName);

        var texture = new VkMttTexture(
                device,
                commandPool,
                graphicsQueue,
                screen,
                textureFilepath,
                generateMipmaps
        );
        textures.add(texture);

        return texture;
    }

    @Override
    public VkMttTexture texturizeColorOfScreen(String srcScreenName, String dstScreenName) {
        VkMttScreen srcScreen = screens.get(srcScreenName);
        long imageView = srcScreen.getColorImageView();

        VkMttScreen dstScreen = screens.get(dstScreenName);
        var texture = new VkMttTexture(device, dstScreen, imageView);
        textures.add(texture);

        return texture;
    }

    @Override
    public VkMttTexture texturizeDepthOfScreen(String srcScreenName, String dstScreenName) {
        VkMttScreen srcScreen = screens.get(srcScreenName);
        long imageView = srcScreen.getDepthImageView();

        VkMttScreen dstScreen = screens.get(dstScreenName);
        var texture = new VkMttTexture(device, dstScreen, imageView);
        textures.add(texture);

        return texture;
    }

    @Override
    public boolean removeTexture(VkMttTexture texture) {
        return textures.remove(texture);
    }

    public void saveScreenshot(String screenName, String srcImageFormat, String outputFilepath) throws IOException {
        VkMttScreen screen = screens.get(screenName);
        screen.save(srcImageFormat, outputFilepath);
    }

    public VkMttTexture createTextureOperation(
            String operationName,
            VkMttTexture firstColorTexture,
            VkMttTexture secondColorTexture,
            VkMttTexture firstDepthTexture,
            VkMttTexture secondDepthTexture,
            String dstScreenName,
            TextureOperationParameters parameters) {
        long dstImage;
        long dstImageView;
        if (textureOperationInfos.containsKey(operationName)) {
            TextureOperationNabor.TextureOperationInfo textureOperationInfo = textureOperationInfos.get(operationName);
            dstImage = textureOperationInfo.dstImage;
            dstImageView = textureOperationInfo.dstImageView;
        } else {
            VkExtent2D extent = textureOperationNabor.getExtent();
            dstImage = textureOperationNabor.createUserDefImage(
                    extent.width(),
                    extent.height(),
                    1,
                    VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    swapchain.getSwapchainImageFormat(),
                    VK_IMAGE_ASPECT_COLOR_BIT);
            dstImageView = textureOperationNabor.lookUpUserDefImageView(dstImage);
        }

        var textureOperationInfo = new TextureOperationNabor.TextureOperationInfo(
                firstColorTexture.getTextureImageView(),
                secondColorTexture.getTextureImageView(),
                firstDepthTexture.getTextureImageView(),
                secondDepthTexture.getTextureImageView(),
                dstImage,
                dstImageView,
                parameters);
        textureOperationInfos.put(operationName, textureOperationInfo);

        VkMttScreen dstScreen = screens.get(dstScreenName);
        var dstTexture = new VkMttTexture(device, dstScreen, dstImageView);
        return dstTexture;
    }

    public boolean updateTextureOperationParameters(String operationName, TextureOperationParameters parameters) {
        if (textureOperationInfos.containsKey(operationName) == false) {
            return false;
        }

        var textureOperationInfo = textureOperationInfos.get(operationName);
        textureOperationInfo.parameters = parameters;
        textureOperationInfos.put(operationName, textureOperationInfo);

        return true;
    }
}
