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
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
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
    private long window;
    private int width;
    private int height;
    private String title;

    private Keyboard keyboard;
    private Mouse mouse;
    private boolean fixCursorFlag;

    private MttVulkanInstance vulkanInstance;

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

        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create a window");
        }

        this.width = width;
        this.height = height;
        this.title = title;

        keyboard = new Keyboard();
        mouse = new Mouse();
        fixCursorFlag = false;

        glfwSetFramebufferSizeCallback(window, this::framebufferResizeCallback);
        glfwSetKeyCallback(window, this::keyCallback);
        glfwSetMouseButtonCallback(window, this::mouseButtonCallback);
        glfwSetCursorPosCallback(window, this::cursorPositionCallback);

        vulkanInstance = new MttVulkanInstance(window, settings.vulkanSettings);

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

        mtt.registerWindow(this);
    }

    public void update(double elapsedTime) {
        keyboard.update();
        mouse.update();

        if (mustRecreate) {
            vulkanInstance.recreateResourcesOnResize(window);
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

        animations.forEach((k, v) -> v.update());

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
            vulkanInstance.runTextureOperations(textureOperationName);
            mtt.postTextureOperation(this, textureOperationName);
        }
        for (var screenName : deferredScreenDrawOrder) {
            mtt.preDeferredDraw(this, screenName);

            MttScreen screen = screens.get(screenName);
            screen.draw();

            mtt.postDeferredDraw(this, screenName);
        }

        mtt.prePresent(this);
        vulkanInstance.presentToFrontScreen(presentScreenName);
        mtt.postPresent(this);
    }

    public void cleanup() {
        mtt.dispose(this);
        vulkanInstance.cleanup();
        physicalObjects.forEach(PhysicalObject::cleanup);
        sounds3D.forEach(MttSound::cleanup);

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
    }

    public void closeWindow() {
        glfwSetWindowShouldClose(window, true);
    }

    public void showWindow() {
        glfwShowWindow(window);
    }

    public void hideWindow() {
        glfwHideWindow(window);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(window);
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
        glfwSetCursorPos(window, x, y);
    }

    public void fixCursor() {
        fixCursorFlag = true;
    }

    public void unfixCursor() {
        fixCursorFlag = false;
    }

    public void setCursorMode(CursorMode cursorMode) {
        switch (cursorMode) {
            case NORMAL -> glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            case DISABLED -> glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            case HIDDEN -> glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
            default -> throw new IllegalArgumentException("Unsupported cursor mode specified: " + cursorMode);
        }
    }

    public void sortComponents() {
        vulkanInstance.sortComponents();
    }

    public MttModel createModel(String screenName, @NotNull URL modelResource) throws URISyntaxException, IOException {
        var model = new MttModel(vulkanInstance, screenName, modelResource.toURI());
        return model;
    }

    public MttModel duplicateModel(MttModel srcModel) {
        var model = new MttModel(vulkanInstance, srcModel);
        return model;
    }

    public MttLine createLine(MttVertex3D v1, MttVertex3D v2) {
        var line = new MttLine(vulkanInstance, v1, v2);
        return line;
    }

    public MttLineSet createLineSet() {
        var lineSet = new MttLineSet(vulkanInstance);
        return lineSet;
    }

    public MttSphere createSphere(Vector3fc center, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        var sphere = new MttSphere(vulkanInstance, center, radius, numVDivs, numHDivs, color);
        return sphere;
    }

    public MttCapsule createCapsule(Vector3fc center, float length, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        var capsule = new MttCapsule(vulkanInstance, center, length, radius, numVDivs, numHDivs, color);
        return capsule;
    }

    public MttLine2D createLine2D(MttVertex2D p1, MttVertex2D p2, float z) {
        var line = new MttLine2D(vulkanInstance, p1, p2, z);
        return line;
    }

    public MttLine2DSet createLine2DSet() {
        var lineSet = new MttLine2DSet(vulkanInstance);
        return lineSet;
    }

    public MttQuad createQuad(MttVertex3D v1, MttVertex3D v2, MttVertex3D v3, MttVertex3D v4, boolean fill) {
        var quad = new MttQuad(vulkanInstance, v1, v2, v3, v4, fill);
        return quad;
    }

    public MttQuad2D createQuad2D(MttVertex2D p1, MttVertex2D p2, MttVertex2D p3, MttVertex2D p4, float z, boolean fill) {
        var quad = new MttQuad2D(vulkanInstance, p1, p2, p3, p4, z, fill);
        return quad;
    }

    public MttQuad2D createQuad2D(
            Vector2fc p1, Vector2fc p2, Vector2fc p3, Vector2fc p4,
            float z, boolean fill, Vector4fc color) {
        var quad = new MttQuad2D(vulkanInstance, p1, p2, p3, p4, z, fill, color);
        return quad;
    }

    public MttQuad2D createQuad2D(Vector2fc topLeft, Vector2fc bottomRight, float z, boolean fill, Vector4fc color) {
        var quad = new MttQuad2D(vulkanInstance, topLeft, bottomRight, z, fill, color);
        return quad;
    }

    public MttTexturedQuad createTexturedQuad(
            String screenName,
            @NotNull URL textureResource,
            boolean generateMipmaps,
            MttVertex3DUV v1,
            MttVertex3DUV v2,
            MttVertex3DUV v3,
            MttVertex3DUV v4) throws URISyntaxException, FileNotFoundException {
        var texturedQuad = new MttTexturedQuad(
                vulkanInstance,
                screenName,
                textureResource.toURI(),
                generateMipmaps,
                v1,
                v2,
                v3,
                v4);
        return texturedQuad;
    }

    public MttTexturedQuad createTexturedQuad(
            String screenName,
            MttTexture texture,
            MttVertex3DUV v1,
            MttVertex3DUV v2,
            MttVertex3DUV v3,
            MttVertex3DUV v4) {
        var texturedQuad = new MttTexturedQuad(
                vulkanInstance,
                screenName,
                texture,
                v1,
                v2,
                v3,
                v4);
        return texturedQuad;
    }

    public MttTexturedQuad duplicateTexturedQuad(
            MttTexturedQuad srcQuad,
            MttVertex3DUV v1,
            MttVertex3DUV v2,
            MttVertex3DUV v3,
            MttVertex3DUV v4) {
        var texturedQuad = new MttTexturedQuad(
                vulkanInstance,
                srcQuad,
                v1,
                v2,
                v3,
                v4);
        return texturedQuad;
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            String screenName, @NotNull URL textureResource,
            MttVertex2DUV p1, MttVertex2DUV p2, MttVertex2DUV p3, MttVertex2DUV p4, float z)
            throws URISyntaxException, FileNotFoundException {
        var texturedQuad = new MttTexturedQuad2D(
                vulkanInstance, screenName, textureResource.toURI(), false, p1, p2, p3, p4, z);
        return texturedQuad;
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            String screenName, MttTexture texture,
            MttVertex2DUV p1, MttVertex2DUV p2, MttVertex2DUV p3, MttVertex2DUV p4, float z) {
        var texturedQuad = new MttTexturedQuad2D(vulkanInstance, screenName, texture, p1, p2, p3, p4, z);
        return texturedQuad;
    }

    public MttTexturedQuad2D duplicateTexturedQuad2D(
            MttTexturedQuad2D srcQuad, MttVertex2DUV p1, MttVertex2DUV p2, MttVertex2DUV p3, MttVertex2DUV p4, float z) {
        var texturedQuad = new MttTexturedQuad2D(vulkanInstance, srcQuad, p1, p2, p3, p4, z);
        return texturedQuad;
    }

    public MttTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(
            String screenName, @NotNull URL textureResource) throws URISyntaxException, FileNotFoundException {
        var texturedQuadSet = new MttTexturedQuad2DSingleTextureSet(
                vulkanInstance, screenName, textureResource.toURI());
        return texturedQuadSet;
    }

    public MttTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(String screenName, MttTexture texture) {
        var texturedQuadSet = new MttTexturedQuad2DSingleTextureSet(vulkanInstance, screenName, texture);
        return texturedQuadSet;
    }

    public MttBox createBox(float xHalfExtent, float yHalfExtent, float zHalfExtent, Vector4fc color) {
        var box = new MttBox(vulkanInstance, xHalfExtent, yHalfExtent, zHalfExtent, color);
        return box;
    }

    public MttBox createBox(float halfExtent, Vector4fc color) {
        return this.createBox(halfExtent, halfExtent, halfExtent, color);
    }

    public MttFont createFont(
            String screenName, Font font, boolean antiAlias, Color fontColor, String requiredChars) {
        var mttFont = new MttFont(vulkanInstance, screenName, font, antiAlias, fontColor, requiredChars);
        return mttFont;
    }

    public MttButton createButton(MttButton.MttButtonCreateInfo createInfo) {
        var mttButton = new MttButton(vulkanInstance, createInfo);
        guiComponents.add(mttButton);

        return mttButton;
    }

    public MttCheckbox createCheckbox(MttCheckbox.MttCheckboxCreateInfo createInfo) {
        var mttCheckbox = new MttCheckbox(vulkanInstance, createInfo);
        guiComponents.add(mttCheckbox);

        return mttCheckbox;
    }

    public MttVerticalScrollbar createVerticalScrollbar(MttVerticalScrollbar.MttVerticalScrollbarCreateInfo createInfo) {
        var mttScrollbar = new MttVerticalScrollbar(vulkanInstance, createInfo);
        guiComponents.add(mttScrollbar);

        return mttScrollbar;
    }

    public MttHorizontalScrollbar createHorizontalScrollbar(
            MttHorizontalScrollbar.MttHorizontalScrollbarCreateInfo createInfo) {
        var mttScrollbar = new MttHorizontalScrollbar(vulkanInstance, createInfo);
        guiComponents.add(mttScrollbar);

        return mttScrollbar;
    }

    public MttListbox createListbox(MttListbox.MttListboxCreateInfo createInfo) {
        var mttListbox = new MttListbox(vulkanInstance, createInfo);
        guiComponents.add(mttListbox);

        return mttListbox;
    }

    public MttLabel createLabel(MttLabel.MttLabelCreateInfo createInfo) {
        var mttLabel = new MttLabel(vulkanInstance, createInfo);
        guiComponents.add(mttLabel);

        return mttLabel;
    }

    public MttTextbox createTextbox(MttTextbox.MttTextboxCreateInfo createInfo) {
        var mttTextbox = new MttTextbox(vulkanInstance, createInfo);
        guiComponents.add(mttTextbox);

        return mttTextbox;
    }

    public MttTextarea createTextarea(MttTextarea.MttTextareaCreateInfo createInfo) {
        var mttTextarea = new MttTextarea(vulkanInstance, createInfo);
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
        } else {
            return false;
        }
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
        } else {
            return false;
        }
    }

    public MttTexture createTexture(
            String screenName, @NotNull URL textureResource, boolean generateMipmaps)
            throws URISyntaxException, FileNotFoundException {
        var texture = new MttTexture(vulkanInstance, screenName, textureResource.toURI(), generateMipmaps);
        return texture;
    }

    public MttTexture texturizeColorOfScreen(String srcScreenName, String dstScreenName) {
        var texture = new MttTexture(vulkanInstance, srcScreenName, dstScreenName, "color");
        return texture;
    }

    public MttTexture texturizeDepthOfScreen(String srcScreenName, String dstScreenName) {
        var texture = new MttTexture(vulkanInstance, srcScreenName, dstScreenName, "depth");
        return texture;
    }

    public void saveScreenshot(String screenName, String srcImageFormat, String outputFilepath) throws IOException {
        vulkanInstance.saveScreenshot(screenName, srcImageFormat, outputFilepath);
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
                vulkanInstance,
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
            glfwGetFramebufferSize(window, width, height);

            screen.getCamera().getPerspectiveCameraInfo().aspect = (float) width.get(0) / (float) height.get(0);
        }

        screens.put(screenName, screen);

        return screen;
    }

    public boolean removeScreen(String screenName) {
        screenDrawOrder.remove(screenName);
        screens.remove(screenName);

        return vulkanInstance.removeScreen(screenName);
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
        VkMttTexture vulkanTexture = vulkanInstance.createTextureOperation(
                operationName,
                firstColorTexture.getVulkanTexture(),
                secondColorTexture.getVulkanTexture(),
                firstDepthTexture.getVulkanTexture(),
                secondDepthTexture.getVulkanTexture(),
                dstScreenName,
                parameters);
        var texture = new MttTexture(vulkanInstance, vulkanTexture);

        return texture;
    }

    public boolean updateTextureOperationParameters(String operationName, TextureOperationParameters parameters) {
        return vulkanInstance.updateTextureOperationParameters(operationName, parameters);
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
        var animation = new MttAnimation(vulkanInstance, screenName, animationInfo);
        animations.put(tag, animation);

        return animation;
    }

    public MttAnimation createAnimation(String tag, AnimationInfo animationInfo, Map<String, MttModel> srcModels) {
        var animation = new MttAnimation(vulkanInstance, animationInfo, srcModels);
        animations.put(tag, animation);

        return animation;
    }
}
