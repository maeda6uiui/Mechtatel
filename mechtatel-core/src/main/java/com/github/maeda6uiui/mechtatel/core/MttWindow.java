package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.animation.AnimationInfo;
import com.github.maeda6uiui.mechtatel.core.animation.MttAnimation;
import com.github.maeda6uiui.mechtatel.core.component.*;
import com.github.maeda6uiui.mechtatel.core.component.gui.*;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.Keyboard;
import com.github.maeda6uiui.mechtatel.core.input.mouse.Mouse;
import com.github.maeda6uiui.mechtatel.core.nabor.FlexibleNaborInfo;
import com.github.maeda6uiui.mechtatel.core.physics.*;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.sound.MttSound;
import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.texture.TextureOperationParameters;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.VkMttTexture;
import jakarta.validation.constraints.NotNull;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Window
 *
 * @author maeda6uiui
 */
public class MttWindow
        implements IMttWindowForDrawPath, IMttWindowForScreenCreator, IMttWindowForSkyboxTextureCreator {
    private IMechtatelForMttWindow mtt;

    private long handle;
    private int width;
    private int height;
    private String title;

    private Keyboard keyboard;
    private Mouse mouse;
    private boolean fixCursorFlag;

    private MttVulkanImpl vulkanImpl;

    private List<MttGuiComponent> guiComponents;

    private List<PhysicalObject> physicalObjects;
    private float physicsSimulationTimeScale;

    private Map<String, MttScreen> screens;
    private List<String> screenDrawOrder;
    private List<String> textureOperationOrder;
    private List<String> deferredScreenDrawOrder;
    private String presentScreenName;

    private Map<String, MttAnimation> animations;

    private List<MttSound> sounds3D;

    private boolean mustRecreate;
    private boolean validWindow;

    private void framebufferResizeCallback(long window, int width, int height) {
        mtt.reshape(this, width, height);

        this.width = width;
        this.height = height;
        mustRecreate = true;
    }

    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        boolean pressingFlag = action == GLFW_PRESS || action == GLFW_REPEAT;
        keyboard.setPressingFlag(key, pressingFlag);
    }

    private void mouseButtonCallback(long window, int button, int action, int mods) {
        boolean pressingFlag = action == GLFW_PRESS;
        mouse.setPressingFlag(button, pressingFlag);
    }

    private void cursorPositionCallback(long window, double xpos, double ypos) {
        mouse.setCursorPos((int) xpos, (int) ypos);

        if (fixCursorFlag) {
            glfwSetCursorPos(window, 0, 0);
        }
    }

    public MttWindow(IMechtatelForMttWindow mtt, MttSettings settings) {
        this(
                mtt,
                settings,
                settings.windowSettings.width,
                settings.windowSettings.height,
                settings.windowSettings.title
        );
    }

    public MttWindow(
            IMechtatelForMttWindow mtt,
            MttSettings settings,
            int width,
            int height,
            String title) {
        this.mtt = mtt;

        handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL) {
            throw new RuntimeException("Failed to create a window");
        }

        this.width = width;
        this.height = height;
        this.title = title;

        keyboard = new Keyboard();
        mouse = new Mouse();
        fixCursorFlag = false;

        glfwSetFramebufferSizeCallback(handle, this::framebufferResizeCallback);
        glfwSetKeyCallback(handle, this::keyCallback);
        glfwSetMouseButtonCallback(handle, this::mouseButtonCallback);
        glfwSetCursorPosCallback(handle, this::cursorPositionCallback);

        vulkanImpl = new MttVulkanImpl(handle, settings.vulkanSettings);

        guiComponents = new ArrayList<>();

        physicalObjects = new ArrayList<>();
        physicsSimulationTimeScale = 1.0f;

        screens = new HashMap<>();
        MttScreen defaultScreen = this.createScreen(
                "default",
                2048,
                2048,
                -1,
                -1,
                SamplerFilterMode.NEAREST,
                SamplerMipmapMode.NEAREST,
                SamplerAddressMode.REPEAT,
                true,
                false,
                null,
                null
        );
        screens.put("default", defaultScreen);
        screenDrawOrder = new ArrayList<>();
        screenDrawOrder.add("default");
        textureOperationOrder = new ArrayList<>();
        deferredScreenDrawOrder = new ArrayList<>();
        presentScreenName = "default";

        animations = new HashMap<>();

        sounds3D = new ArrayList<>();

        mustRecreate = false;
        validWindow = true;

        mtt.registerWindow(this);
        mtt.init(this);
    }

    public void update(double elapsedTime) {
        keyboard.update();
        mouse.update();

        if (mustRecreate) {
            vulkanImpl.recreateResourcesOnResize(handle);
            for (var screen : screens.values()) {
                if (screen.shouldAutoUpdateCameraAspect()) {
                    screen.getCamera().getPerspectiveCameraInfo().aspect
                            = (float) width / (float) height;
                }
            }

            mustRecreate = false;
        }

        Map<String, Integer> keyboardPressingCounts = keyboard.getPressingCounts();
        guiComponents.forEach(guiComponent -> {
            guiComponent.update(
                    this.getCursorPosX(),
                    this.getCursorPosY(),
                    this.getWidth(),
                    this.getHeight(),
                    this.getMousePressingCount("BUTTON_LEFT"),
                    this.getMousePressingCount("BUTTON_MIDDLE"),
                    this.getMousePressingCount("BUTTON_RIGHT"),
                    keyboardPressingCounts
            );
        });
        physicalObjects.forEach(PhysicalObject::updateObject);
        PhysicalObject.updatePhysicsSpace((float) elapsedTime, physicsSimulationTimeScale);

        animations.values().forEach(MttAnimation::update);

        mtt.update(this);
    }

    public void draw() {
        for (var screenName : screenDrawOrder) {
            mtt.preDraw(this, screenName);

            MttScreen screen = screens.get(screenName);
            screen.draw();

            mtt.postDraw(this, screenName);
        }
        for (var textureOperationName : textureOperationOrder) {
            mtt.preTextureOperation(this, textureOperationName);
            vulkanImpl.runTextureOperations(textureOperationName);
            mtt.postTextureOperation(this, textureOperationName);
        }
        for (var screenName : deferredScreenDrawOrder) {
            mtt.preDeferredDraw(this, screenName);

            MttScreen screen = screens.get(screenName);
            screen.draw();

            mtt.postDeferredDraw(this, screenName);
        }

        mtt.prePresent(this);
        vulkanImpl.presentToFrontScreen(presentScreenName);
        mtt.postPresent(this);
    }

    public void cleanup() {
        mtt.dispose(this);
        vulkanImpl.cleanup();
        physicalObjects.forEach(PhysicalObject::cleanup);
        sounds3D.forEach(MttSound::cleanup);

        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);

        validWindow = false;
    }

    public void close() {
        glfwSetWindowShouldClose(handle, true);
    }

    public void show() {
        glfwShowWindow(handle);
    }

    public void hide() {
        glfwHideWindow(handle);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getTitle() {
        return title;
    }

    public int getKeyboardPressingCount(String key) {
        return keyboard.getPressingCount(key);
    }

    public int getKeyboardReleasingCount(String key) {
        return keyboard.getReleasingCount(key);
    }

    public int getMousePressingCount(String key) {
        return mouse.getPressingCount(key);
    }

    public int getMouseReleasingCount(String key) {
        return mouse.getReleasingCount(key);
    }

    public int getCursorPosX() {
        return mouse.getCursorPosX();
    }

    public int getCursorPosY() {
        return mouse.getCursorPosY();
    }

    public void setCursorPos(int x, int y) {
        glfwSetCursorPos(handle, x, y);
    }

    public void fixCursor() {
        fixCursorFlag = true;
    }

    public void unfixCursor() {
        fixCursorFlag = false;
    }

    public void setCursorMode(CursorMode cursorMode) {
        switch (cursorMode) {
            case NORMAL -> glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            case DISABLED -> glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            case HIDDEN -> glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
            default -> throw new IllegalArgumentException("Unsupported cursor mode specified: " + cursorMode);
        }
    }

    public boolean isValidWindow() {
        return validWindow;
    }

    public void sortComponents() {
        vulkanImpl.sortComponents();
    }

    public MttModel createModel(String screenName, @NotNull URL modelResource) throws URISyntaxException, IOException {
        return new MttModel(vulkanImpl, screenName, modelResource.toURI());
    }

    public MttModel duplicateModel(MttModel srcModel) {
        return new MttModel(vulkanImpl, srcModel);
    }

    public MttLine createLine(MttVertex v1, MttVertex v2) {
        return new MttLine(vulkanImpl, v1, v2);
    }

    public MttLineSet createLineSet() {
        return new MttLineSet(vulkanImpl);
    }

    public MttSphere createSphere(Vector3fc center, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        return new MttSphere(vulkanImpl, center, radius, numVDivs, numHDivs, color);
    }

    public MttCapsule createCapsule(Vector3fc center, float length, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        return new MttCapsule(vulkanImpl, center, length, radius, numVDivs, numHDivs, color);
    }

    public MttLine2D createLine2D(MttVertex2D p1, MttVertex2D p2, float z) {
        return new MttLine2D(vulkanImpl, p1, p2, z);
    }

    public MttLine2DSet createLine2DSet() {
        return new MttLine2DSet(vulkanImpl);
    }

    public MttQuad createQuad(MttVertex v1, MttVertex v2, MttVertex v3, MttVertex v4, boolean fill) {
        return new MttQuad(vulkanImpl, v1, v2, v3, v4, fill);
    }

    public MttQuad createQuad(Vector3fc p1, Vector3fc p2, Vector3fc p3, Vector3fc p4, boolean fill, Vector4fc color) {
        return new MttQuad(vulkanImpl, p1, p2, p3, p4, fill, color);
    }

    public MttQuad2D createQuad2D(
            MttVertex2D v1, MttVertex2D v2, MttVertex2D v3, MttVertex2D v4, float z, boolean fill) {
        return new MttQuad2D(vulkanImpl, v1, v2, v3, v4, z, fill);
    }

    public MttQuad2D createQuad2D(
            Vector2fc p1, Vector2fc p2, Vector2fc p3, Vector2fc p4, float z, boolean fill, Vector4fc color) {
        return new MttQuad2D(vulkanImpl, p1, p2, p3, p4, z, fill, color);
    }

    public MttQuad2D createQuad2D(Vector2fc topLeft, Vector2fc bottomRight, float z, boolean fill, Vector4fc color) {
        return new MttQuad2D(vulkanImpl, topLeft, bottomRight, z, fill, color);
    }

    public MttTexturedQuad createTexturedQuad(
            String screenName,
            @NotNull URL textureResource,
            boolean generateMipmaps,
            MttVertexUV v1,
            MttVertexUV v2,
            MttVertexUV v3,
            MttVertexUV v4) throws URISyntaxException, FileNotFoundException {
        return new MttTexturedQuad(
                vulkanImpl,
                screenName,
                textureResource.toURI(),
                generateMipmaps,
                v1,
                v2,
                v3,
                v4
        );
    }

    public MttTexturedQuad createTexturedQuad(
            String screenName,
            MttTexture texture,
            MttVertexUV v1,
            MttVertexUV v2,
            MttVertexUV v3,
            MttVertexUV v4) {
        return new MttTexturedQuad(
                vulkanImpl,
                screenName,
                texture,
                v1,
                v2,
                v3,
                v4
        );
    }

    public MttTexturedQuad duplicateTexturedQuad(
            MttTexturedQuad srcQuad,
            MttVertexUV v1,
            MttVertexUV v2,
            MttVertexUV v3,
            MttVertexUV v4) {
        return new MttTexturedQuad(
                vulkanImpl,
                srcQuad,
                v1,
                v2,
                v3,
                v4
        );
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            String screenName, @NotNull URL textureResource,
            MttVertex2DUV v1, MttVertex2DUV v2, MttVertex2DUV v3, MttVertex2DUV v4, float z)
            throws URISyntaxException, FileNotFoundException {
        return new MttTexturedQuad2D(
                vulkanImpl, screenName, textureResource.toURI(), v1, v2, v3, v4, z);
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            String screenName, @NotNull URL textureResource,
            Vector2fc topLeft, Vector2fc bottomRight, float z) throws URISyntaxException, FileNotFoundException {
        return new MttTexturedQuad2D(
                vulkanImpl, screenName, textureResource.toURI(), topLeft, bottomRight, z);
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            String screenName, MttTexture texture,
            MttVertex2DUV v1, MttVertex2DUV v2, MttVertex2DUV v3, MttVertex2DUV v4, float z) {
        return new MttTexturedQuad2D(vulkanImpl, screenName, texture, v1, v2, v3, v4, z);
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            String screenName, MttTexture texture,
            Vector2fc topLeft, Vector2fc bottomRight, float z) {
        return new MttTexturedQuad2D(vulkanImpl, screenName, texture, topLeft, bottomRight, z);
    }

    public MttTexturedQuad2D duplicateTexturedQuad2D(
            MttTexturedQuad2D srcQuad,
            MttVertex2DUV v1, MttVertex2DUV v2, MttVertex2DUV v3, MttVertex2DUV v4, float z) {
        return new MttTexturedQuad2D(vulkanImpl, srcQuad, v1, v2, v3, v4, z);
    }

    public MttTexturedQuad2D duplicateTexturedQuad2D(
            MttTexturedQuad2D srcQuad, Vector2fc topLeft, Vector2fc bottomRight, float z) {
        return new MttTexturedQuad2D(vulkanImpl, srcQuad, topLeft, bottomRight, z);
    }

    public MttTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(
            String screenName, @NotNull URL textureResource) throws URISyntaxException, FileNotFoundException {
        return new MttTexturedQuad2DSingleTextureSet(
                vulkanImpl, screenName, textureResource.toURI());
    }

    public MttTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(String screenName, MttTexture texture) {
        return new MttTexturedQuad2DSingleTextureSet(vulkanImpl, screenName, texture);
    }

    public MttBox createBox(float xHalfExtent, float yHalfExtent, float zHalfExtent, Vector4fc color) {
        return new MttBox(vulkanImpl, xHalfExtent, yHalfExtent, zHalfExtent, color);
    }

    public MttBox createBox(float halfExtent, Vector4fc color) {
        return this.createBox(halfExtent, halfExtent, halfExtent, color);
    }

    public MttFont createFont(
            String screenName, Font font, boolean antiAlias, Color fontColor, String requiredChars) {
        return new MttFont(vulkanImpl, screenName, font, antiAlias, fontColor, requiredChars);
    }

    public MttButton createButton(MttButton.MttButtonCreateInfo createInfo) {
        var mttButton = new MttButton(vulkanImpl, createInfo);
        guiComponents.add(mttButton);

        return mttButton;
    }

    public MttCheckbox createCheckbox(MttCheckbox.MttCheckboxCreateInfo createInfo) {
        var mttCheckbox = new MttCheckbox(vulkanImpl, createInfo);
        guiComponents.add(mttCheckbox);

        return mttCheckbox;
    }

    public MttVerticalScrollbar createVerticalScrollbar(MttVerticalScrollbar.MttVerticalScrollbarCreateInfo createInfo) {
        var mttScrollbar = new MttVerticalScrollbar(vulkanImpl, createInfo);
        guiComponents.add(mttScrollbar);

        return mttScrollbar;
    }

    public MttHorizontalScrollbar createHorizontalScrollbar(
            MttHorizontalScrollbar.MttHorizontalScrollbarCreateInfo createInfo) {
        var mttScrollbar = new MttHorizontalScrollbar(vulkanImpl, createInfo);
        guiComponents.add(mttScrollbar);

        return mttScrollbar;
    }

    public MttListbox createListbox(MttListbox.MttListboxCreateInfo createInfo) {
        var mttListbox = new MttListbox(vulkanImpl, createInfo);
        guiComponents.add(mttListbox);

        return mttListbox;
    }

    public MttLabel createLabel(MttLabel.MttLabelCreateInfo createInfo) {
        var mttLabel = new MttLabel(vulkanImpl, createInfo);
        guiComponents.add(mttLabel);

        return mttLabel;
    }

    public MttTextbox createTextbox(MttTextbox.MttTextboxCreateInfo createInfo) {
        var mttTextbox = new MttTextbox(vulkanImpl, createInfo);
        guiComponents.add(mttTextbox);

        return mttTextbox;
    }

    public MttTextarea createTextarea(MttTextarea.MttTextareaCreateInfo createInfo) {
        var mttTextarea = new MttTextarea(vulkanImpl, createInfo);
        guiComponents.add(mttTextarea);

        return mttTextarea;
    }

    public boolean removeGuiComponent(MttGuiComponent guiComponent) {
        return guiComponents.remove(guiComponent);
    }

    public PhysicalPlane createPhysicalPlane(Vector3fc normal, float constant) {
        var physicalPlane = new PhysicalPlane(normal, constant);
        physicalObjects.add(physicalPlane);

        return physicalPlane;
    }

    public PhysicalSphere createPhysicalSphere(float radius, float mass) {
        var physicalSphere = new PhysicalSphere(radius, mass);
        physicalObjects.add(physicalSphere);

        return physicalSphere;
    }

    public PhysicalCapsule createPhysicalCapsule(float radius, float height, float mass) {
        var physicalCapsule = new PhysicalCapsule(radius, height, mass);
        physicalObjects.add(physicalCapsule);

        return physicalCapsule;
    }

    public PhysicalBox createPhysicalBox(float xHalfExtent, float yHalfExtent, float zHalfExtent, float mass) {
        var physicalBox = new PhysicalBox(xHalfExtent, yHalfExtent, zHalfExtent, mass);
        physicalObjects.add(physicalBox);

        return physicalBox;
    }

    public PhysicalBox createPhysicalBox(float halfExtent, float mass) {
        return this.createPhysicalBox(halfExtent, halfExtent, halfExtent, mass);
    }

    public PhysicalMesh createPhysicalMesh(MttModel model, float mass) {
        var physicalMesh = new PhysicalMesh(model, mass);
        physicalObjects.add(physicalMesh);

        return physicalMesh;
    }

    public boolean removePhysicalObject(PhysicalObject physicalObject) {
        if (physicalObjects.contains(physicalObject)) {
            physicalObject.cleanup();
            physicalObjects.remove(physicalObject);

            return true;
        }

        return false;
    }

    public void setPhysicsSimulationTimeScale(float physicsSimulationTimeScale) {
        this.physicsSimulationTimeScale = physicsSimulationTimeScale;
    }

    public MttSound createSound(@NotNull URL soundResource, boolean loop, boolean relative) throws URISyntaxException, IOException {
        var sound = new MttSound(soundResource.toURI(), loop, relative);
        sounds3D.add(sound);

        return sound;
    }

    public MttSound duplicateSound(MttSound srcSound, boolean loop, boolean relative) {
        var sound = new MttSound(srcSound, loop, relative);
        sounds3D.add(sound);

        return sound;
    }

    public boolean removeSound(MttSound sound) {
        if (sounds3D.contains(sound)) {
            sound.cleanup();
            sounds3D.remove(sound);

            return true;
        }

        return false;
    }

    public MttTexture createTexture(
            String screenName, @NotNull URL textureResource, boolean generateMipmaps)
            throws URISyntaxException, FileNotFoundException {
        return new MttTexture(vulkanImpl, screenName, textureResource.toURI(), generateMipmaps);
    }

    public MttTexture texturizeColorOfScreen(String srcScreenName, String dstScreenName) {
        return new MttTexture(vulkanImpl, srcScreenName, dstScreenName, "color");
    }

    public MttTexture texturizeDepthOfScreen(String srcScreenName, String dstScreenName) {
        return new MttTexture(vulkanImpl, srcScreenName, dstScreenName, "depth");
    }

    public void saveScreenshot(String screenName, String srcImageFormat, String outputFilepath) throws IOException {
        vulkanImpl.saveScreenshot(screenName, srcImageFormat, outputFilepath);
    }

    public MttScreen createScreen(
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
        if (screens.containsKey(screenName)) {
            screens.get(screenName).cleanup();
            screens.remove(screenName);
        }

        var screen = new MttScreen(
                vulkanImpl,
                screenName,
                depthImageWidth,
                depthImageHeight,
                screenWidth,
                screenHeight,
                samplerFilter,
                samplerMipmapMode,
                samplerAddressMode,
                shouldChangeExtentOnRecreate,
                useShadowMapping,
                flexibleNaborInfos,
                ppNaborNames
        );

        //Set initial aspect according to the framebuffer size
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.ints(0);
            IntBuffer height = stack.ints(0);
            glfwGetFramebufferSize(handle, width, height);

            screen.getCamera().getPerspectiveCameraInfo().aspect = (float) width.get(0) / (float) height.get(0);
        }

        screens.put(screenName, screen);

        return screen;
    }

    public boolean removeScreen(String screenName) {
        screenDrawOrder.remove(screenName);
        screens.remove(screenName);

        return vulkanImpl.removeScreen(screenName);
    }

    public MttScreen getScreen(String screenName) {
        return screens.get(screenName);
    }

    public Map<String, MttScreen> getScreens() {
        return new HashMap<>(screens);
    }

    public void setScreenDrawOrder(List<String> screenDrawOrder) {
        this.screenDrawOrder = screenDrawOrder;
    }

    public MttTexture createTextureOperation(
            String operationName,
            MttTexture firstColorTexture,
            MttTexture secondColorTexture,
            MttTexture firstDepthTexture,
            MttTexture secondDepthTexture,
            String dstScreenName,
            TextureOperationParameters parameters) {
        VkMttTexture vulkanTexture = vulkanImpl.createTextureOperation(
                operationName,
                firstColorTexture.getVulkanTexture(),
                secondColorTexture.getVulkanTexture(),
                firstDepthTexture.getVulkanTexture(),
                secondDepthTexture.getVulkanTexture(),
                dstScreenName,
                parameters);
        return new MttTexture(vulkanImpl, vulkanTexture);
    }

    public boolean updateTextureOperationParameters(String operationName, TextureOperationParameters parameters) {
        return vulkanImpl.updateTextureOperationParameters(operationName, parameters);
    }

    public void setTextureOperationOrder(List<String> textureOperationOrder) {
        this.textureOperationOrder = textureOperationOrder;
    }

    public void setDeferredScreenDrawOrder(List<String> deferredScreenDrawOrder) {
        this.deferredScreenDrawOrder = deferredScreenDrawOrder;
    }

    public void setPresentScreenName(String presentScreenName) {
        this.presentScreenName = presentScreenName;
    }

    public MttAnimation createAnimation(String tag, String screenName, AnimationInfo animationInfo) throws IOException {
        var animation = new MttAnimation(vulkanImpl, screenName, animationInfo);
        animations.put(tag, animation);

        return animation;
    }

    public MttAnimation createAnimation(String tag, AnimationInfo animationInfo, Map<String, MttModel> srcModels) {
        var animation = new MttAnimation(vulkanImpl, animationInfo, srcModels);
        animations.put(tag, animation);

        return animation;
    }
}
