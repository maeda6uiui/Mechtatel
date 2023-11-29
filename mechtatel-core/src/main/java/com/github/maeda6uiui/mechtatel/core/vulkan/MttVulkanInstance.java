package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.*;
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
import java.net.URI;
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

    private long debugMessenger;

    private VkPhysicalDevice physicalDevice;

    private VkDevice device;
    private VkQueue graphicsQueue;

    private long surface;
    private VkQueue presentQueue;

    private int albedoMSAASamples;
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

    private MttSettings.VulkanSettings vulkanSettings;

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

    /**
     * Recreates resources that must be updated when framebuffer size has changed.
     * Call this method in the application's main loop to keep it in sync with Vulkan's procedure.
     * e.g. Set a flag to {@code true} in the callback invoked on framebuffer resize,
     * and then call this method in the main loop if the flag is set to {@code true}.
     *
     * @param window Window handle
     */
    public void recreateResourcesOnResize(long window) {
        vkDeviceWaitIdle(device);

        swapchain.cleanup();

        VkExtent2D framebufferSize = this.getFramebufferSize(window);
        swapchain = new Swapchain(device, surface, framebufferSize.width(), framebufferSize.height());

        presentNabor.recreate(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent());

        swapchain.createFramebuffers(presentNabor.getRenderPass());

        textureOperationNabor.recreate(
                swapchain.getSwapchainImageFormat(),
                swapchain.getSwapchainExtent());
        textureOperationNabor.cleanupUserDefImages();
        textureOperationInfos.clear();

        screens.forEach((name, screen) -> screen.recreate(
                swapchain.getSwapchainImageFormat(), swapchain.getSwapchainExtent()));

        imagesInFlight.clear();
    }

    private void loadShadercLib() {
        String shadercLibFilename;
        switch (PlatformInfo.PLATFORM) {
            case "windows" -> shadercLibFilename = "shaderc_shared.dll";
            case "linux" -> shadercLibFilename = "libshaderc_shared.so";
            case "macos" -> shadercLibFilename = "libshaderc_shared.dylib";
            default -> throw new RuntimeException("Unsupported platform: " + PlatformInfo.PLATFORM);
        }

        String shadercLibFilepath = Objects.requireNonNull(
                this.getClass().getResource("/Bin/" + shadercLibFilename)).getFile();
        System.load(shadercLibFilepath);
    }

    public MttVulkanInstance(long window, MttSettings.VulkanSettings vulkanSettings) {
        this.loadShadercLib();

        instance = InstanceCreator.createInstance(
                vulkanSettings.enableValidationLayer,
                vulkanSettings.appInfo
        );

        if (vulkanSettings.enableValidationLayer) {
            debugMessenger = ValidationLayers.setupDebugMessenger(instance);
        }

        surface = SurfaceCreator.createSurface(instance, window);

        physicalDevice = PhysicalDevicePicker.pickPhysicalDevice(instance, surface);

        LogicalDeviceCreator.VkDeviceAndVkQueues deviceAndQueues = LogicalDeviceCreator.createLogicalDevice(
                physicalDevice,
                vulkanSettings.enableValidationLayer,
                vulkanSettings.useGraphicsQueueAsPresentQueue,
                surface
        );
        device = deviceAndQueues.device;
        graphicsQueue = deviceAndQueues.graphicsQueue;
        presentQueue = deviceAndQueues.presentQueue;

        albedoMSAASamples = vulkanSettings.albedoMSAASamples < 0
                ? MultisamplingUtils.getMaxUsableSampleCount(device)
                : vulkanSettings.albedoMSAASamples;

        depthImageFormat = DepthResourceUtils.findDepthFormat(device);
        depthImageAspect = VK_IMAGE_ASPECT_DEPTH_BIT;

        boolean hasStencilComponent = DepthResourceUtils.hasStencilComponent(depthImageFormat);
        if (hasStencilComponent) {
            depthImageAspect |= VK_IMAGE_ASPECT_STENCIL_BIT;
        }

        commandPool = CommandPoolCreator.createCommandPool(device, surface);

        VkExtent2D framebufferSize = this.getFramebufferSize(window);
        swapchain = new Swapchain(device, surface, framebufferSize.width(), framebufferSize.height());

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

        swapchain.createFramebuffers(presentNabor.getRenderPass());

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

        this.vulkanSettings = vulkanSettings;
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

        if (vulkanSettings.enableValidationLayer) {
            ValidationLayers.destroyDebugUtilsMessengerEXT(instance, debugMessenger, null);
        }

        vkDestroySurfaceKHR(instance, surface, null);

        vkDestroyInstance(instance, null);
    }

    public VkMttScreen createScreen(
            String screenName,
            int depthImageWidth,
            int depthImageHeight,
            int screenWidth,
            int screenHeight,
            SamplerFilterMode samplerFilter,
            SamplerMipmapMode samplerMipmapMode,
            SamplerAddressMode samplerAddressMode,
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
        switch (samplerFilter) {
            case NEAREST -> iSamplerFilter = VK_FILTER_NEAREST;
            case LINEAR -> iSamplerFilter = VK_FILTER_LINEAR;
            default -> throw new IllegalArgumentException("Unsupported sampler filter specified: " + samplerFilter);
        }

        int iSamplerMipmapMode;
        switch (samplerMipmapMode) {
            case NEAREST -> iSamplerMipmapMode = VK_SAMPLER_MIPMAP_MODE_NEAREST;
            case LINEAR -> iSamplerMipmapMode = VK_SAMPLER_MIPMAP_MODE_LINEAR;
            default -> throw new IllegalArgumentException(
                    "Unsupported sampler mipmap mode specified: " + samplerMipmapMode);
        }

        int iSamplerAddressMode;
        switch (samplerAddressMode) {
            case REPEAT -> iSamplerAddressMode = VK_SAMPLER_ADDRESS_MODE_REPEAT;
            case MIRRORED_REPEAT -> iSamplerAddressMode = VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT;
            case CLAMP_TO_EDGE -> iSamplerAddressMode = VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE;
            case CLAMP_TO_BORDER -> iSamplerAddressMode = VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER;
            default -> throw new IllegalArgumentException(
                    "Unsupported sampler address mode specified: " + samplerAddressMode);
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
                albedoMSAASamples,
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
            thisFrame.present(
                    swapchain.getSwapchain(),
                    imagesInFlight,
                    commandBuffers,
                    graphicsQueue,
                    presentQueue);
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

    public VkMttModel createModel(String screenName, URI modelResource) throws IOException {
        VkMttScreen screen = screens.get(screenName);

        var model = new VkMttModel(
                device,
                commandPool,
                graphicsQueue,
                screen,
                modelResource);
        components.add(model);

        return model;
    }

    public VkMttModel duplicateModel(VkMttModel srcModel) {
        var model = new VkMttModel(device, commandPool, graphicsQueue, srcModel);
        components.add(model);

        return model;
    }

    public VkMttLine createLine(MttVertex3D v1, MttVertex3D v2) {
        var line = new VkMttLine(device, commandPool, graphicsQueue, v1, v2);
        components.add(line);

        return line;
    }

    public VkMttLineSet createLineSet() {
        var lineSet = new VkMttLineSet(device, commandPool, graphicsQueue);
        components.add(lineSet);

        return lineSet;
    }

    public VkMttSphere createSphere(
            Vector3fc center,
            float radius,
            int numVDivs,
            int numHDivs,
            Vector4fc color) {
        var sphere = new VkMttSphere(device, commandPool, graphicsQueue, center, radius, numVDivs, numHDivs, color);
        components.add(sphere);

        return sphere;
    }

    public VkMttCapsule createCapsule(
            Vector3fc center,
            float length,
            float radius,
            int numVDivs,
            int numHDivs,
            Vector4fc color) {
        var capsule = new VkMttCapsule(device, commandPool, graphicsQueue, center, length, radius, numVDivs, numHDivs, color);
        components.add(capsule);

        return capsule;
    }

    public VkMttQuad createQuad(List<MttVertex3D> vertices, boolean fill) {
        var filledQuad = new VkMttQuad(device, commandPool, graphicsQueue, vertices, fill);
        components.add(filledQuad);

        return filledQuad;
    }

    public VkMttTexturedQuad createTexturedQuad(
            String screenName, URI textureResource, boolean generateMipmaps, List<MttVertex3DUV> vertices) {
        VkMttScreen screen = screens.get(screenName);

        var texturedQuad = new VkMttTexturedQuad(
                device,
                commandPool,
                graphicsQueue,
                screen,
                textureResource,
                generateMipmaps,
                vertices);
        components.add(texturedQuad);

        return texturedQuad;
    }

    public VkMttTexturedQuad createTexturedQuad(String screenName, VkMttTexture texture, List<MttVertex3DUV> vertices) {
        var texturedQuad = new VkMttTexturedQuad(
                device,
                commandPool,
                graphicsQueue,
                screenName,
                texture,
                vertices);
        components.add(texturedQuad);

        return texturedQuad;
    }

    public VkMttTexturedQuad duplicateTexturedQuad(VkMttTexturedQuad srcQuad, List<MttVertex3DUV> vertices) {
        var texturedQuad = new VkMttTexturedQuad(
                device,
                commandPool,
                graphicsQueue,
                srcQuad,
                vertices);
        components.add(texturedQuad);

        return texturedQuad;
    }

    public VkMttTexturedQuadSingleTextureSet createTexturedQuadSingleTextureSet(String screenName, VkMttTexture texture) {
        var texturedQuadSet = new VkMttTexturedQuadSingleTextureSet(
                device, commandPool, graphicsQueue, screenName, texture);
        components.add(texturedQuadSet);

        return texturedQuadSet;
    }

    public VkMttTexturedQuadSingleTextureSet createTexturedQuadSingleTextureSet(
            String screenName, URI textureResource) {
        VkMttScreen screen = screens.get(screenName);

        var texturedQuadSet = new VkMttTexturedQuadSingleTextureSet(
                device,
                commandPool,
                graphicsQueue,
                screen,
                textureResource);
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
    public VkMttTexture createTexture(String screenName, URI textureResource, boolean generateMipmaps) {
        VkMttScreen screen = screens.get(screenName);

        var texture = new VkMttTexture(
                device,
                commandPool,
                graphicsQueue,
                screen,
                textureResource,
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
