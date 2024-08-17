package com.github.maeda6uiui.mechtatel.core.screen;

import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.SamplerAddressMode;
import com.github.maeda6uiui.mechtatel.core.SamplerFilterMode;
import com.github.maeda6uiui.mechtatel.core.SamplerMipmapMode;
import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.fseffect.FullScreenEffectNaborInfo;
import com.github.maeda6uiui.mechtatel.core.fseffect.FullScreenEffectProperties;
import com.github.maeda6uiui.mechtatel.core.operation.TextureOperation;
import com.github.maeda6uiui.mechtatel.core.postprocessing.CustomizablePostProcessingNaborInfo;
import com.github.maeda6uiui.mechtatel.core.postprocessing.PostProcessingProperties;
import com.github.maeda6uiui.mechtatel.core.screen.component.*;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanImplCommon;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImplHeadless;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.texture.VkMttTexture;
import imgui.ImFontAtlas;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.internal.ImGuiContext;
import imgui.type.ImInt;
import org.joml.*;
import org.lwjgl.vulkan.VkExtent2D;

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
        public List<String> ppNaborNames;
        public Map<String, CustomizablePostProcessingNaborInfo> customizablePPNaborInfos;
        public List<String> fseNaborNames;
        public Map<String, FullScreenEffectNaborInfo> fseNaborInfos;

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
            ppNaborNames = new ArrayList<>();
            customizablePPNaborInfos = new HashMap<>();
            fseNaborNames = new ArrayList<>();
            fseNaborInfos = new HashMap<>();
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

        public MttScreenCreateInfo setPostProcessingNaborNames(List<String> ppNaborNames) {
            this.ppNaborNames = ppNaborNames;
            return this;
        }

        public MttScreenCreateInfo setCustomizablePostProcessingNaborInfos(
                Map<String, CustomizablePostProcessingNaborInfo> customizablePPNaborInfos) {
            this.customizablePPNaborInfos = customizablePPNaborInfos;
            return this;
        }

        public MttScreenCreateInfo setFullScreenEffectNaborNames(List<String> fseNaborNames) {
            this.fseNaborNames = fseNaborNames;
            return this;
        }

        public MttScreenCreateInfo setFullScreenEffectNaborInfos(Map<String, FullScreenEffectNaborInfo> fseNaborInfos) {
            this.fseNaborInfos = fseNaborInfos;
            return this;
        }
    }

    private IMttVulkanImplCommon vulkanImplCommon;
    private VkMttScreen screen;

    private ImGuiContext imguiContext;

    private Vector4f backgroundColor;
    private Camera camera;

    private ShadowMappingSettings shadowMappingSettings;
    private PostProcessingProperties ppProperties;
    private FullScreenEffectProperties fseProperties;

    private boolean shouldAutoUpdateCameraAspect;

    private List<MttComponent> components;
    private List<TextureOperation> textureOperations;
    private List<MttTexture> textures;

    private void commonSetup() {
        camera = new Camera();
        backgroundColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

        shadowMappingSettings = new ShadowMappingSettings();
        ppProperties = new PostProcessingProperties();
        fseProperties = new FullScreenEffectProperties();

        shouldAutoUpdateCameraAspect = true;

        components = new ArrayList<>();
        textureOperations = new ArrayList<>();
        textures = new ArrayList<>();
    }

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
                vulkanImpl.getColorImageFormat(),
                vulkanImpl.getAlbedoMSAASamples(),
                VkScreenCreationUtils.getISamplerFilter(createInfo.samplerFilter),
                VkScreenCreationUtils.getISamplerMipmapMode(createInfo.samplerMipmapMode),
                VkScreenCreationUtils.getISamplerAddressMode(createInfo.samplerAddressMode),
                VkScreenCreationUtils.createExtent(
                        vulkanImpl.getExtent(),
                        createInfo.screenWidth,
                        createInfo.screenHeight
                ),
                createInfo.shouldChangeExtentOnRecreate,
                createInfo.useShadowMapping,
                createInfo.ppNaborNames,
                createInfo.customizablePPNaborInfos,
                createInfo.fseNaborNames,
                createInfo.fseNaborInfos
        );

        vulkanImplCommon = vulkanImpl;
        this.imguiContext = imguiContext;

        this.commonSetup();
    }

    public MttScreen(MttVulkanImplHeadless vulkanImplHeadless, ImGuiContext imguiContext, MttScreenCreateInfo createInfo) {
        if (createInfo.screenWidth <= 0 || createInfo.screenHeight <= 0) {
            throw new RuntimeException(
                    String.format(
                            "Screen size not allowed: (width,height)=(%d,%d)",
                            createInfo.screenWidth, createInfo.screenHeight
                    )
            );
        }

        VkExtent2D extent = VkExtent2D.create();
        extent.set(createInfo.screenWidth, createInfo.screenHeight);

        var dq = vulkanImplHeadless.getDeviceAndQueues();
        screen = new VkMttScreen(
                dq.device(),
                vulkanImplHeadless.getCommandPool(),
                dq.graphicsQueue(),
                vulkanImplHeadless.getDepthImageFormat(),
                createInfo.depthImageWidth,
                createInfo.depthImageHeight,
                vulkanImplHeadless.getDepthImageAspect(),
                vulkanImplHeadless.getColorImageFormat(),
                vulkanImplHeadless.getAlbedoMSAASamples(),
                VkScreenCreationUtils.getISamplerFilter(createInfo.samplerFilter),
                VkScreenCreationUtils.getISamplerMipmapMode(createInfo.samplerMipmapMode),
                VkScreenCreationUtils.getISamplerAddressMode(createInfo.samplerAddressMode),
                extent,
                createInfo.shouldChangeExtentOnRecreate,
                createInfo.useShadowMapping,
                createInfo.ppNaborNames,
                createInfo.customizablePPNaborInfos,
                createInfo.fseNaborNames,
                createInfo.fseNaborInfos
        );

        vulkanImplCommon = vulkanImplHeadless;
        this.imguiContext = imguiContext;

        this.commonSetup();
    }

    public void cleanup() {
        components.forEach(MttComponent::cleanup);
        textureOperations.forEach(TextureOperation::cleanup);
        textures.forEach(MttTexture::cleanup);
        screen.cleanup();
    }

    public void draw() {
        var vkComponents = new ArrayList<VkMttComponent>();
        components.forEach(v -> vkComponents.addAll(v.getVulkanComponents()));

        vulkanImplCommon.draw(
                screen,
                backgroundColor,
                camera,
                shadowMappingSettings,
                ppProperties,
                fseProperties,
                vkComponents
        );
    }

    public void recreate() {
        screen.recreate(vulkanImplCommon.getColorImageFormat(), vulkanImplCommon.getExtent());
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

        return new MttTexture(vulkanImplCommon, this, fontBuffer, fontTexWidth.get(), fontTexHeight.get());
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

    //Shadow mapping =====
    public ShadowMappingSettings getShadowMappingSettings() {
        return shadowMappingSettings;
    }

    public void setShadowMappingSettings(ShadowMappingSettings shadowMappingSettings) {
        this.shadowMappingSettings = shadowMappingSettings;
    }

    //Post-processing =====
    public PostProcessingProperties getPostProcessingProperties() {
        return ppProperties;
    }

    public void setPostProcessingProperties(PostProcessingProperties ppProperties) {
        this.ppProperties = ppProperties;
    }

    //Full-screen effect
    public FullScreenEffectProperties getFullScreenEffectProperties() {
        return fseProperties;
    }

    public void setFullScreenEffectProperties(FullScreenEffectProperties fseProperties) {
        this.fseProperties = fseProperties;
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

    public MttTexture createTexture(URL textureResource, boolean generateMipmaps)
            throws URISyntaxException, FileNotFoundException {
        return new MttTexture(vulkanImplCommon, this, textureResource.toURI(), generateMipmaps);
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
    public TextureOperation createTextureOperation(
            List<MttTexture> colorTextures,
            List<MttTexture> depthTextures,
            boolean textureCleanupDelegation) {
        var textureOperation = new TextureOperation(
                vulkanImplCommon,
                colorTextures,
                depthTextures,
                this,
                textureCleanupDelegation
        );
        textureOperations.add(textureOperation);

        return textureOperation;
    }

    public boolean deleteTextureOperation(TextureOperation textureOperation) {
        if (!textureOperations.contains(textureOperation)) {
            return false;
        }

        textureOperation.cleanup();
        return textureOperations.remove(textureOperation);
    }

    public void deleteAllTextureOperations() {
        textureOperations.forEach(TextureOperation::cleanup);
        textureOperations.clear();
    }

    public void removeGarbageTextureOperations() {
        textureOperations.removeIf(op -> !op.isValid());
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
        return new MttImGui(vulkanImplCommon, this, imguiContext);
    }

    public MttModel createModel(URL modelResource) throws URISyntaxException, IOException {
        return new MttModel(vulkanImplCommon, this, modelResource.toURI());
    }

    public MttModel duplicateModel(MttModel srcModel) {
        return new MttModel(vulkanImplCommon, this, srcModel);
    }

    public MttLine createLine(MttPrimitiveVertex v1, MttPrimitiveVertex v2) {
        return new MttLine(vulkanImplCommon, this, v1, v2);
    }

    public MttLineSet createLineSet() {
        return new MttLineSet(vulkanImplCommon, this);
    }

    public MttSphere createSphere(Vector3fc center, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        return new MttSphere(vulkanImplCommon, this, center, radius, numVDivs, numHDivs, color);
    }

    public MttCapsule createCapsule(
            Vector3fc center, float length, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        return new MttCapsule(vulkanImplCommon, this, center, length, radius, numVDivs, numHDivs, color);
    }

    public MttLine2D createLine2D(MttVertex2D p1, MttVertex2D p2, float z) {
        return new MttLine2D(vulkanImplCommon, this, p1, p2, z);
    }

    public MttLine2DSet createLine2DSet() {
        return new MttLine2DSet(vulkanImplCommon, this);
    }

    public MttQuad createQuad(MttPrimitiveVertex v1, MttPrimitiveVertex v2, MttPrimitiveVertex v3, MttPrimitiveVertex v4, boolean fill) {
        return new MttQuad(vulkanImplCommon, this, v1, v2, v3, v4, fill);
    }

    public MttQuad createQuad(Vector3fc p1, Vector3fc p2, Vector3fc p3, Vector3fc p4, boolean fill, Vector4fc color) {
        return new MttQuad(vulkanImplCommon, this, p1, p2, p3, p4, fill, color);
    }

    public MttQuad2D createQuad2D(
            MttVertex2D v1, MttVertex2D v2, MttVertex2D v3, MttVertex2D v4, float z, boolean fill) {
        return new MttQuad2D(vulkanImplCommon, this, v1, v2, v3, v4, z, fill);
    }

    public MttQuad2D createQuad2D(
            Vector2fc p1, Vector2fc p2, Vector2fc p3, Vector2fc p4, float z, boolean fill, Vector4fc color) {
        return new MttQuad2D(vulkanImplCommon, this, p1, p2, p3, p4, z, fill, color);
    }

    public MttQuad2D createQuad2D(Vector2fc topLeft, Vector2fc bottomRight, float z, boolean fill, Vector4fc color) {
        return new MttQuad2D(vulkanImplCommon, this, topLeft, bottomRight, z, fill, color);
    }

    public MttTexturedQuad createTexturedQuad(
            URL textureResource,
            boolean generateMipmaps,
            MttVertex v1,
            MttVertex v2,
            MttVertex v3,
            MttVertex v4) throws URISyntaxException, FileNotFoundException {
        return new MttTexturedQuad(
                vulkanImplCommon,
                this,
                textureResource.toURI(),
                generateMipmaps,
                v1, v2, v3, v4
        );
    }

    public MttTexturedQuad createTexturedQuad(
            MttTexture texture,
            MttVertex v1, MttVertex v2, MttVertex v3, MttVertex v4) {
        return new MttTexturedQuad(
                vulkanImplCommon, this, texture, v1, v2, v3, v4
        );
    }

    public MttTexturedQuad duplicateTexturedQuad(
            MttTexturedQuad srcQuad,
            MttVertex v1, MttVertex v2, MttVertex v3, MttVertex v4) {
        return new MttTexturedQuad(
                vulkanImplCommon, this, srcQuad, v1, v2, v3, v4
        );
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            URL textureResource,
            MttVertex2D v1, MttVertex2D v2, MttVertex2D v3, MttVertex2D v4, float z)
            throws URISyntaxException, FileNotFoundException {
        return new MttTexturedQuad2D(
                vulkanImplCommon, this, textureResource.toURI(), v1, v2, v3, v4, z);
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            URL textureResource,
            Vector2fc topLeft, Vector2fc bottomRight, float z) throws URISyntaxException, FileNotFoundException {
        return new MttTexturedQuad2D(
                vulkanImplCommon, this, textureResource.toURI(), topLeft, bottomRight, z);
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            MttTexture texture,
            MttVertex2D v1, MttVertex2D v2, MttVertex2D v3, MttVertex2D v4, float z) {
        return new MttTexturedQuad2D(vulkanImplCommon, this, texture, v1, v2, v3, v4, z);
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            MttTexture texture,
            Vector2fc topLeft, Vector2fc bottomRight, float z) {
        return new MttTexturedQuad2D(vulkanImplCommon, this, texture, topLeft, bottomRight, z);
    }

    public MttTexturedQuad2D duplicateTexturedQuad2D(
            MttTexturedQuad2D srcQuad,
            MttVertex2D v1, MttVertex2D v2, MttVertex2D v3, MttVertex2D v4, float z) {
        return new MttTexturedQuad2D(vulkanImplCommon, this, srcQuad, v1, v2, v3, v4, z);
    }

    public MttTexturedQuad2D duplicateTexturedQuad2D(
            MttTexturedQuad2D srcQuad, Vector2fc topLeft, Vector2fc bottomRight, float z) {
        return new MttTexturedQuad2D(vulkanImplCommon, this, srcQuad, topLeft, bottomRight, z);
    }

    public MttTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(URL textureResource)
            throws URISyntaxException, FileNotFoundException {
        return new MttTexturedQuad2DSingleTextureSet(vulkanImplCommon, this, textureResource.toURI());
    }

    public MttTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(MttTexture texture) {
        return new MttTexturedQuad2DSingleTextureSet(vulkanImplCommon, this, texture);
    }

    public MttBox createBox(float xHalfExtent, float yHalfExtent, float zHalfExtent, Vector4fc color) {
        return new MttBox(vulkanImplCommon, this, xHalfExtent, yHalfExtent, zHalfExtent, color);
    }

    public MttBox createBox(float halfExtent, Vector4fc color) {
        return new MttBox(vulkanImplCommon, this, halfExtent, color);
    }

    public MttFont createFont(Font font, boolean antiAlias, Color fontColor, String requiredChars) {
        return new MttFont(vulkanImplCommon, this, font, antiAlias, fontColor, requiredChars);
    }
}
