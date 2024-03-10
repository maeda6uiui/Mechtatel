package com.github.maeda6uiui.mechtatel.core.screen;

import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.SamplerAddressMode;
import com.github.maeda6uiui.mechtatel.core.SamplerFilterMode;
import com.github.maeda6uiui.mechtatel.core.SamplerMipmapMode;
import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.nabor.CustomizableNaborInfo;
import com.github.maeda6uiui.mechtatel.core.operation.BiTextureOperation;
import com.github.maeda6uiui.mechtatel.core.postprocessing.blur.SimpleBlurInfo;
import com.github.maeda6uiui.mechtatel.core.postprocessing.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.screen.animation.AnimationInfo;
import com.github.maeda6uiui.mechtatel.core.screen.animation.MttAnimation;
import com.github.maeda6uiui.mechtatel.core.screen.component.*;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.ParallelLightNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.PointLightNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.SpotlightNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.texture.VkMttTexture;
import imgui.ImFontAtlas;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.internal.ImGuiContext;
import imgui.type.ImInt;
import jakarta.validation.constraints.NotNull;
import org.joml.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Screen
 *
 * @author maeda6uiui
 */
public class MttScreen implements IMttScreenForMttComponent, IMttScreenForMttTexture {
    public static class MttScreenCreateInfo {
        public int depthImageWidth;
        public int depthImageHeight;
        public int screenWidth;
        public int screenHeight;
        public SamplerFilterMode samplerFilter;
        public SamplerMipmapMode samplerMipmapMode;
        public SamplerAddressMode samplerAddressMode;
        public boolean shouldChangeExtentOnRecreate;
        public boolean useShadowMapping;
        public Map<String, CustomizableNaborInfo> customizableNaborInfos;
        public List<String> ppNaborNames;

        public MttScreenCreateInfo() {
            depthImageWidth = 2048;
            depthImageHeight = 2048;
            screenWidth = -1;
            screenHeight = -1;
            samplerFilter = SamplerFilterMode.NEAREST;
            samplerMipmapMode = SamplerMipmapMode.NEAREST;
            samplerAddressMode = SamplerAddressMode.REPEAT;
            shouldChangeExtentOnRecreate = true;
            useShadowMapping = false;
            customizableNaborInfos = new HashMap<>();
            ppNaborNames = new ArrayList<>();
        }

        public MttScreenCreateInfo setDepthImageWidth(int depthImageWidth) {
            this.depthImageWidth = depthImageWidth;
            return this;
        }

        public MttScreenCreateInfo setDepthImageHeight(int depthImageHeight) {
            this.depthImageHeight = depthImageHeight;
            return this;
        }

        public MttScreenCreateInfo setScreenWidth(int screenWidth) {
            this.screenWidth = screenWidth;
            return this;
        }

        public MttScreenCreateInfo setScreenHeight(int screenHeight) {
            this.screenHeight = screenHeight;
            return this;
        }

        public MttScreenCreateInfo setSamplerFilter(SamplerFilterMode samplerFilter) {
            this.samplerFilter = samplerFilter;
            return this;
        }

        public MttScreenCreateInfo setSamplerMipmapMode(SamplerMipmapMode samplerMipmapMode) {
            this.samplerMipmapMode = samplerMipmapMode;
            return this;
        }

        public MttScreenCreateInfo setSamplerAddressMode(SamplerAddressMode samplerAddressMode) {
            this.samplerAddressMode = samplerAddressMode;
            return this;
        }

        public MttScreenCreateInfo setShouldChangeExtentOnRecreate(boolean shouldChangeExtentOnRecreate) {
            this.shouldChangeExtentOnRecreate = shouldChangeExtentOnRecreate;
            return this;
        }

        public MttScreenCreateInfo setUseShadowMapping(boolean useShadowMapping) {
            this.useShadowMapping = useShadowMapping;
            return this;
        }

        public MttScreenCreateInfo setCustomizableNaborInfos(Map<String, CustomizableNaborInfo> customizableNaborInfos) {
            this.customizableNaborInfos = customizableNaborInfos;
            return this;
        }

        public MttScreenCreateInfo setPpNaborNames(List<String> ppNaborNames) {
            this.ppNaborNames = ppNaborNames;
            return this;
        }
    }

    private MttVulkanImpl vulkanImpl;
    private VkMttScreen screen;

    private ImGuiContext imguiContext;

    private Vector4f backgroundColor;
    private Camera camera;
    private Fog fog;
    private List<ParallelLight> parallelLights;
    private Vector3f parallelLightAmbientColor;
    private List<PointLight> pointLights;
    private Vector3f pointLightAmbientColor;
    private List<Spotlight> spotlights;
    private Vector3f spotlightAmbientColor;
    private ShadowMappingSettings shadowMappingSettings;
    private SimpleBlurInfo simpleBlurInfo;

    private boolean shouldAutoUpdateCameraAspect;

    private List<MttComponent> components;
    private List<BiTextureOperation> biTextureOperations;
    private List<MttTexture> textures;

    private Map<String, MttAnimation> animations;

    public MttScreen(MttVulkanImpl vulkanImpl, ImGuiContext imguiContext, MttScreenCreateInfo createInfo) {
        var dq = vulkanImpl.getDeviceAndQueues();
        screen = new VkMttScreen(
                dq.device(),
                vulkanImpl.getCommandPool(),
                dq.graphicsQueue(),
                vulkanImpl.getDepthImageFormat(),
                createInfo.depthImageWidth,
                createInfo.depthImageHeight,
                vulkanImpl.getDepthImageAspect(),
                vulkanImpl.getSwapchainImageFormat(),
                vulkanImpl.getAlbedoMSAASamples(),
                VkScreenCreationUtils.getISamplerFilter(createInfo.samplerFilter),
                VkScreenCreationUtils.getISamplerMipmapMode(createInfo.samplerMipmapMode),
                VkScreenCreationUtils.getISamplerAddressMode(createInfo.samplerAddressMode),
                VkScreenCreationUtils.createExtent(
                        vulkanImpl.getSwapchainExtent(),
                        createInfo.screenWidth,
                        createInfo.screenHeight
                ),
                createInfo.shouldChangeExtentOnRecreate,
                createInfo.useShadowMapping,
                createInfo.customizableNaborInfos,
                createInfo.ppNaborNames
        );

        shouldAutoUpdateCameraAspect = true;

        this.vulkanImpl = vulkanImpl;
        this.imguiContext = imguiContext;

        backgroundColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        parallelLightAmbientColor = new Vector3f(0.5f, 0.5f, 0.5f);
        pointLightAmbientColor = new Vector3f(0.5f, 0.5f, 0.5f);
        spotlightAmbientColor = new Vector3f(0.5f, 0.5f, 0.5f);
        shadowMappingSettings = new ShadowMappingSettings();
        simpleBlurInfo = new SimpleBlurInfo();

        camera = new Camera();
        fog = new Fog();
        parallelLights = new ArrayList<>();
        pointLights = new ArrayList<>();
        spotlights = new ArrayList<>();

        components = new ArrayList<>();
        biTextureOperations = new ArrayList<>();
        textures = new ArrayList<>();
        animations = new HashMap<>();
    }

    public void cleanup() {
        components.forEach(MttComponent::cleanup);
        biTextureOperations.forEach(BiTextureOperation::cleanup);
        textures.forEach(MttTexture::cleanup);
        screen.cleanup();
    }

    public void draw() {
        var vkComponents = new ArrayList<VkMttComponent>();
        components.forEach(v -> vkComponents.addAll(v.getVulkanComponents()));

        vulkanImpl.draw(
                screen,
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
                vkComponents
        );
    }

    public void recreate() {
        if (vulkanImpl != null) {
            screen.recreate(vulkanImpl.getSwapchainImageFormat(), vulkanImpl.getSwapchainExtent());
        }
    }

    /**
     * Creates a texture for ImGui fonts.
     * ImGui context for this operation must be made current before calling this method.
     *
     * @return Texture for ImGui fonts
     */
    public MttTexture createImGuiFontTexture() {
        ImGuiIO io = ImGui.getIO();
        ImFontAtlas fontAtlas = io.getFonts();
        var fontTexWidth = new ImInt();
        var fontTexHeight = new ImInt();
        ByteBuffer fontBuffer = fontAtlas.getTexDataAsRGBA32(fontTexWidth, fontTexHeight);

        return new MttTexture(vulkanImpl, this, fontBuffer, fontTexWidth.get(), fontTexHeight.get());
    }

    /**
     * Creates a buffered image.
     * Note that in most cases depth image is not available and may lead to error.
     *
     * @param imageType   Underlying image type to create an image from
     * @param pixelFormat Pixel format of the underlying image
     * @return Buffered image
     */
    public BufferedImage createBufferedImage(ScreenImageType imageType, PixelFormat pixelFormat) {
        return screen.createBufferedImage(imageType, pixelFormat);
    }

    /**
     * Saves underlying pixels to an image file.
     * Note that in most cases depth image is not available and may lead to error.
     *
     * @param imageType   Underlying image type to create an image from
     * @param pixelFormat Pixel format of the underlying image
     * @param outputFile  Output file
     * @throws IOException If it fails to write to the file specified
     */
    public void save(
            ScreenImageType imageType,
            PixelFormat pixelFormat,
            Path outputFile) throws IOException {
        BufferedImage bufferedImage = this.createBufferedImage(imageType, pixelFormat);

        String[] splits = outputFile.toString().split(Pattern.quote("."));
        String formatName = splits[splits.length - 1];

        ImageIO.write(bufferedImage, formatName, outputFile.toFile());
    }

    @Override
    public VkMttScreen getVulkanScreen() {
        return screen;
    }

    public int getScreenWidth() {
        return screen.getScreenWidth();
    }

    public int getScreenHeight() {
        return screen.getScreenHeight();
    }

    public void setShouldAutoUpdateCameraAspect(boolean shouldAutoUpdateCameraAspect) {
        this.shouldAutoUpdateCameraAspect = shouldAutoUpdateCameraAspect;
    }

    public boolean shouldAutoUpdateCameraAspect() {
        return shouldAutoUpdateCameraAspect;
    }

    public Vector4f getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Vector4f backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void syncCamera(Camera camera) {
        this.camera.setEye(new Vector3f(camera.getEye()));
        this.camera.setCenter(new Vector3f(camera.getCenter()));
        this.camera.setUp(new Vector3f(camera.getUp()));
    }

    public Fog getFog() {
        return fog;
    }

    public void setFog(Fog fog) {
        this.fog = fog;
    }

    public Vector3f getParallelLightAmbientColor() {
        return parallelLightAmbientColor;
    }

    public Vector3f getPointLightAmbientColor() {
        return pointLightAmbientColor;
    }

    public void setPointLightAmbientColor(Vector3f pointLightAmbientColor) {
        this.pointLightAmbientColor = pointLightAmbientColor;
    }

    public Vector3f getSpotlightAmbientColor() {
        return spotlightAmbientColor;
    }

    public void setSpotlightAmbientColor(Vector3f spotlightAmbientColor) {
        this.spotlightAmbientColor = spotlightAmbientColor;
    }

    public ShadowMappingSettings getShadowMappingSettings() {
        return shadowMappingSettings;
    }

    public void setShadowMappingSettings(ShadowMappingSettings shadowMappingSettings) {
        this.shadowMappingSettings = shadowMappingSettings;
    }

    public SimpleBlurInfo getSimpleBlurInfo() {
        return simpleBlurInfo;
    }

    public void setSimpleBlurInfo(SimpleBlurInfo simpleBlurInfo) {
        this.simpleBlurInfo = simpleBlurInfo;
    }

    public int getNumParallelLights() {
        return parallelLights.size();
    }

    public ParallelLight getParallelLight(int index) {
        return parallelLights.get(index);
    }

    public ParallelLight createParallelLight() {
        if (parallelLights.size() >= ParallelLightNabor.MAX_NUM_LIGHTS) {
            String msg = String.format("Cannot create more than %d lights", ParallelLightNabor.MAX_NUM_LIGHTS);
            throw new RuntimeException(msg);
        }

        var parallelLight = new ParallelLight();
        parallelLights.add(parallelLight);

        return parallelLight;
    }

    public boolean removeParallelLight(ParallelLight parallelLight) {
        return parallelLights.remove(parallelLight);
    }

    public int getNumPointLights() {
        return pointLights.size();
    }

    public PointLight getPointLight(int index) {
        return pointLights.get(index);
    }

    public PointLight createPointLight() {
        if (pointLights.size() >= PointLightNabor.MAX_NUM_LIGHTS) {
            String msg = String.format("Cannot create more than %d lights", PointLightNabor.MAX_NUM_LIGHTS);
            throw new RuntimeException(msg);
        }

        var pointLight = new PointLight();
        pointLights.add(pointLight);

        return pointLight;
    }

    public boolean removePointLight(PointLight pointLight) {
        return pointLights.remove(pointLight);
    }

    public int getNumSpotlights() {
        return spotlights.size();
    }

    public Spotlight getSpotlight(int index) {
        return spotlights.get(index);
    }

    public Spotlight createSpotlight() {
        if (spotlights.size() >= SpotlightNabor.MAX_NUM_LIGHTS) {
            String msg = String.format("Cannot create more than %d lights", SpotlightNabor.MAX_NUM_LIGHTS);
            throw new RuntimeException(msg);
        }

        var spotlight = new Spotlight();
        spotlights.add(spotlight);

        return spotlight;
    }

    public boolean removeSpotlight(Spotlight spotlight) {
        return spotlights.remove(spotlight);
    }

    //Texture ==========
    @Override
    public void addTexture(MttTexture texture) {
        textures.add(texture);
    }

    public boolean deleteTexture(MttTexture texture) {
        if (!textures.contains(texture)) {
            return false;
        }

        texture.cleanup();
        return textures.remove(texture);
    }

    public void deleteAllTextures() {
        textures.forEach(MttTexture::cleanup);
        textures.clear();
    }

    public void removeGarbageTextures() {
        textures.removeIf(t -> !t.isValid());
    }

    public MttTexture texturize(ScreenImageType imageType, MttScreen dstScreen) {
        VkMttTexture texture = screen.texturize(imageType, dstScreen.getVulkanScreen());
        return new MttTexture(dstScreen, texture);
    }

    public MttTexture createTexture(@NotNull URL textureResource, boolean generateMipmaps)
            throws URISyntaxException, FileNotFoundException {
        return new MttTexture(vulkanImpl, this, textureResource.toURI(), generateMipmaps);
    }

    /**
     * Creates a texture operation that consumes two textures.
     * The list for depth textures can be empty
     * if the texture operation doesn't require them.
     *
     * @param colorTextures            List of color textures
     * @param depthTextures            List of depth textures
     * @param textureCleanupDelegation Whether to clean up textures when this texture operation is destroyed
     * @return Texture operation
     */
    public BiTextureOperation createBiTextureOperation(
            List<MttTexture> colorTextures,
            List<MttTexture> depthTextures,
            boolean textureCleanupDelegation) {
        var textureOperation = new BiTextureOperation(
                vulkanImpl,
                colorTextures,
                depthTextures,
                this,
                textureCleanupDelegation
        );
        biTextureOperations.add(textureOperation);

        return textureOperation;
    }

    public boolean deleteBiTextureOperation(BiTextureOperation biTextureOperation) {
        if (!biTextureOperations.contains(biTextureOperation)) {
            return false;
        }

        biTextureOperation.cleanup();
        return biTextureOperations.remove(biTextureOperation);
    }

    public void deleteAllBiTextureOperations() {
        biTextureOperations.forEach(BiTextureOperation::cleanup);
        biTextureOperations.clear();
    }

    public void removeGarbageTextureOperations() {
        biTextureOperations.removeIf(op -> !op.isValid());
    }

    //Animation ==========
    public MttAnimation createAnimation(String tag, AnimationInfo animationInfo) throws IOException {
        var animation = new MttAnimation(vulkanImpl, this, animationInfo);
        animations.put(tag, animation);

        return animation;
    }

    public MttAnimation createAnimation(String tag, AnimationInfo animationInfo, Map<String, MttModel> srcModels) {
        var animation = new MttAnimation(vulkanImpl, this, animationInfo, srcModels);
        animations.put(tag, animation);

        return animation;
    }

    public Map<String, MttAnimation> getAnimations() {
        return animations;
    }

    public boolean deleteAnimation(String tag) {
        if (!animations.containsKey(tag)) {
            return false;
        }

        MttAnimation animation = animations.remove(tag);
        if (animation != null) {
            animation.cleanup();
            return true;
        }

        return false;
    }

    public void deleteAllAnimations() {
        animations.values().forEach(MttAnimation::cleanup);
        animations.clear();
    }

    //Components ==========
    @Override
    public void addComponent(MttComponent c) {
        components.add(c);
    }

    public boolean deleteComponent(MttComponent component) {
        if (!components.contains(component)) {
            return false;
        }

        component.cleanup();
        return components.remove(component);
    }

    public void removeGarbageComponents() {
        components.removeIf(c -> !c.isValid());
    }

    public void deleteAllComponents() {
        components.forEach(MttComponent::cleanup);
        components.clear();
    }

    public List<MttComponent> getComponents() {
        return components;
    }

    public MttImGui createImGui() {
        return new MttImGui(vulkanImpl, this, imguiContext);
    }

    public MttModel createModel(@NotNull URL modelResource) throws URISyntaxException, IOException {
        return new MttModel(vulkanImpl, this, modelResource.toURI());
    }

    public MttModel duplicateModel(MttModel srcModel) {
        return new MttModel(vulkanImpl, this, srcModel);
    }

    public MttLine createLine(MttVertex v1, MttVertex v2) {
        return new MttLine(vulkanImpl, this, v1, v2);
    }

    public MttLineSet createLineSet() {
        return new MttLineSet(vulkanImpl, this);
    }

    public MttSphere createSphere(Vector3fc center, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        return new MttSphere(vulkanImpl, this, center, radius, numVDivs, numHDivs, color);
    }

    public MttCapsule createCapsule(
            Vector3fc center, float length, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        return new MttCapsule(vulkanImpl, this, center, length, radius, numVDivs, numHDivs, color);
    }

    public MttLine2D createLine2D(MttVertex2D p1, MttVertex2D p2, float z) {
        return new MttLine2D(vulkanImpl, this, p1, p2, z);
    }

    public MttLine2DSet createLine2DSet() {
        return new MttLine2DSet(vulkanImpl, this);
    }

    public MttQuad createQuad(MttVertex v1, MttVertex v2, MttVertex v3, MttVertex v4, boolean fill) {
        return new MttQuad(vulkanImpl, this, v1, v2, v3, v4, fill);
    }

    public MttQuad createQuad(Vector3fc p1, Vector3fc p2, Vector3fc p3, Vector3fc p4, boolean fill, Vector4fc color) {
        return new MttQuad(vulkanImpl, this, p1, p2, p3, p4, fill, color);
    }

    public MttQuad2D createQuad2D(
            MttVertex2D v1, MttVertex2D v2, MttVertex2D v3, MttVertex2D v4, float z, boolean fill) {
        return new MttQuad2D(vulkanImpl, this, v1, v2, v3, v4, z, fill);
    }

    public MttQuad2D createQuad2D(
            Vector2fc p1, Vector2fc p2, Vector2fc p3, Vector2fc p4, float z, boolean fill, Vector4fc color) {
        return new MttQuad2D(vulkanImpl, this, p1, p2, p3, p4, z, fill, color);
    }

    public MttQuad2D createQuad2D(Vector2fc topLeft, Vector2fc bottomRight, float z, boolean fill, Vector4fc color) {
        return new MttQuad2D(vulkanImpl, this, topLeft, bottomRight, z, fill, color);
    }

    public MttTexturedQuad createTexturedQuad(
            @NotNull URL textureResource,
            boolean generateMipmaps,
            MttVertexUV v1,
            MttVertexUV v2,
            MttVertexUV v3,
            MttVertexUV v4) throws URISyntaxException, FileNotFoundException {
        return new MttTexturedQuad(
                vulkanImpl,
                this,
                textureResource.toURI(),
                generateMipmaps,
                v1, v2, v3, v4
        );
    }

    public MttTexturedQuad createTexturedQuad(
            MttTexture texture,
            MttVertexUV v1, MttVertexUV v2, MttVertexUV v3, MttVertexUV v4) {
        return new MttTexturedQuad(
                vulkanImpl, this, texture, v1, v2, v3, v4
        );
    }

    public MttTexturedQuad duplicateTexturedQuad(
            MttTexturedQuad srcQuad,
            MttVertexUV v1, MttVertexUV v2, MttVertexUV v3, MttVertexUV v4) {
        return new MttTexturedQuad(
                vulkanImpl, this, srcQuad, v1, v2, v3, v4
        );
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            @NotNull URL textureResource,
            MttVertex2DUV v1, MttVertex2DUV v2, MttVertex2DUV v3, MttVertex2DUV v4, float z)
            throws URISyntaxException, FileNotFoundException {
        return new MttTexturedQuad2D(
                vulkanImpl, this, textureResource.toURI(), v1, v2, v3, v4, z);
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            @NotNull URL textureResource,
            Vector2fc topLeft, Vector2fc bottomRight, float z) throws URISyntaxException, FileNotFoundException {
        return new MttTexturedQuad2D(
                vulkanImpl, this, textureResource.toURI(), topLeft, bottomRight, z);
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            MttTexture texture,
            MttVertex2DUV v1, MttVertex2DUV v2, MttVertex2DUV v3, MttVertex2DUV v4, float z) {
        return new MttTexturedQuad2D(vulkanImpl, this, texture, v1, v2, v3, v4, z);
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            MttTexture texture,
            Vector2fc topLeft, Vector2fc bottomRight, float z) {
        return new MttTexturedQuad2D(vulkanImpl, this, texture, topLeft, bottomRight, z);
    }

    public MttTexturedQuad2D duplicateTexturedQuad2D(
            MttTexturedQuad2D srcQuad,
            MttVertex2DUV v1, MttVertex2DUV v2, MttVertex2DUV v3, MttVertex2DUV v4, float z) {
        return new MttTexturedQuad2D(vulkanImpl, this, srcQuad, v1, v2, v3, v4, z);
    }

    public MttTexturedQuad2D duplicateTexturedQuad2D(
            MttTexturedQuad2D srcQuad, Vector2fc topLeft, Vector2fc bottomRight, float z) {
        return new MttTexturedQuad2D(vulkanImpl, this, srcQuad, topLeft, bottomRight, z);
    }

    public MttTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(@NotNull URL textureResource)
            throws URISyntaxException, FileNotFoundException {
        return new MttTexturedQuad2DSingleTextureSet(vulkanImpl, this, textureResource.toURI());
    }

    public MttTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(MttTexture texture) {
        return new MttTexturedQuad2DSingleTextureSet(vulkanImpl, this, texture);
    }

    public MttBox createBox(float xHalfExtent, float yHalfExtent, float zHalfExtent, Vector4fc color) {
        return new MttBox(vulkanImpl, this, xHalfExtent, yHalfExtent, zHalfExtent, color);
    }

    public MttBox createBox(float halfExtent, Vector4fc color) {
        return new MttBox(vulkanImpl, this, halfExtent, color);
    }

    public MttFont createFont(Font font, boolean antiAlias, Color fontColor, String requiredChars) {
        return new MttFont(vulkanImpl, this, font, antiAlias, fontColor, requiredChars);
    }
}
