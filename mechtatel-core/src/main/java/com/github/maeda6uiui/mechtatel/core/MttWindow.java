package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.animation.AnimationInfo;
import com.github.maeda6uiui.mechtatel.core.animation.MttAnimation;
import com.github.maeda6uiui.mechtatel.core.component.*;
import com.github.maeda6uiui.mechtatel.core.component.gui.*;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.Keyboard;
import com.github.maeda6uiui.mechtatel.core.input.mouse.Mouse;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.sound.MttSound;
import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import jakarta.validation.constraints.NotNull;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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
public class MttWindow {
    private static final Logger logger = LoggerFactory.getLogger(MttWindow.class);

    private IMechtatelForMttWindow mtt;
    private MttVulkanImpl vulkanImpl;

    private long handle;
    private int width;
    private int height;
    private String title;

    private Keyboard keyboard;
    private Mouse mouse;
    private boolean fixCursorFlag;
    private boolean mustRecreate;
    private boolean validWindow;

    private List<MttScreen> screens;
    private List<MttGuiComponent> guiComponents;
    private Map<String, MttAnimation> animations;

    private List<MttSound> sounds3D;

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
        handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL) {
            throw new RuntimeException("Failed to create a window");
        }

        this.width = width;
        this.height = height;
        this.title = title;

        this.mtt = mtt;
        vulkanImpl = new MttVulkanImpl(handle, settings.vulkanSettings);

        keyboard = new Keyboard();
        mouse = new Mouse();
        fixCursorFlag = false;
        mustRecreate = false;
        validWindow = true;

        glfwSetFramebufferSizeCallback(handle, this::framebufferResizeCallback);
        glfwSetKeyCallback(handle, this::keyCallback);
        glfwSetMouseButtonCallback(handle, this::mouseButtonCallback);
        glfwSetCursorPosCallback(handle, this::cursorPositionCallback);

        MttScreen defaultScreen = new MttScreen(
                vulkanImpl,
                new MttScreen.MttScreenCreateInfo()
                        .setDepthImageWidth(2048)
                        .setDepthImageHeight(2048)
                        .setScreenWidth(-1)
                        .setScreenHeight(-1)
                        .setSamplerFilter(SamplerFilterMode.NEAREST)
                        .setSamplerMipmapMode(SamplerMipmapMode.NEAREST)
                        .setSamplerAddressMode(SamplerAddressMode.REPEAT)
                        .setShouldChangeExtentOnRecreate(true)
                        .setUseShadowMapping(false)
        );
        screens = new ArrayList<>();
        screens.add(defaultScreen);

        guiComponents = new ArrayList<>();
        animations = new HashMap<>();

        sounds3D = new ArrayList<>();

        logger.debug("Window ({}) successfully created", Long.toHexString(handle));

        mtt.init(this);
    }

    public void update() {
        keyboard.update();
        mouse.update();

        if (mustRecreate) {
            vulkanImpl.recreateResourcesOnResize(handle);
            screens.forEach(screen -> {
                if (screen.shouldAutoUpdateCameraAspect()) {
                    screen.getCamera().getPerspectiveCameraInfo().aspect = (float) width / (float) height;
                }
            });

            mustRecreate = false;
            logger.debug("Window ({}) recreated", Long.toHexString(handle));
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

        animations.values().forEach(MttAnimation::update);

        mtt.update(this);
    }

    public void draw(
            List<MttScreen> firstPhaseScreens,
            List<TextureOperation> textureOperations,
            List<MttScreen> secondPhaseScreens,
            MttScreen presentScreen) {
        firstPhaseScreens.forEach(screen -> {
            mtt.preDraw(this, screen);
            screen.draw();
            mtt.postDraw(this, screen);
        });
        textureOperations.forEach(op -> {
            mtt.preTextureOperation(this, op);
            op.run();
            mtt.postTextureOperation(this, op);
        });
        secondPhaseScreens.forEach(screen -> {
            mtt.preDeferredDraw(this, screen);
            screen.draw();
            mtt.postDeferredDraw(this, screen);
        });

        mtt.prePresent(this);
        vulkanImpl.presentToFrontScreen(presentScreen.getVulkanScreen());
        mtt.postPresent(this);
    }

    public void cleanup() {
        mtt.dispose(this);

        guiComponents.forEach(MttComponent::cleanup);
        screens.forEach(MttScreen::cleanup);
        vulkanImpl.cleanup();

        sounds3D.forEach(MttSound::cleanup);

        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);

        validWindow = false;
        logger.debug("Window ({}) cleaned up", Long.toHexString(handle));
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

    public MttModel createModel(MttScreen screen, @NotNull URL modelResource) throws URISyntaxException, IOException {
        return new MttModel(vulkanImpl, screen, modelResource.toURI());
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
            MttScreen screen,
            @NotNull URL textureResource,
            boolean generateMipmaps,
            MttVertexUV v1,
            MttVertexUV v2,
            MttVertexUV v3,
            MttVertexUV v4) throws URISyntaxException, FileNotFoundException {
        return new MttTexturedQuad(
                vulkanImpl,
                screen,
                textureResource.toURI(),
                generateMipmaps,
                v1, v2, v3, v4
        );
    }

    public MttTexturedQuad createTexturedQuad(
            MttTexture texture,
            MttVertexUV v1, MttVertexUV v2, MttVertexUV v3, MttVertexUV v4) {
        return new MttTexturedQuad(
                vulkanImpl, texture, v1, v2, v3, v4
        );
    }

    public MttTexturedQuad duplicateTexturedQuad(
            MttTexturedQuad srcQuad,
            MttVertexUV v1, MttVertexUV v2, MttVertexUV v3, MttVertexUV v4) {
        return new MttTexturedQuad(
                vulkanImpl, srcQuad, v1, v2, v3, v4
        );
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            MttScreen screen, @NotNull URL textureResource,
            MttVertex2DUV v1, MttVertex2DUV v2, MttVertex2DUV v3, MttVertex2DUV v4, float z)
            throws URISyntaxException, FileNotFoundException {
        return new MttTexturedQuad2D(
                vulkanImpl, screen, textureResource.toURI(), v1, v2, v3, v4, z);
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            MttScreen screen, @NotNull URL textureResource,
            Vector2fc topLeft, Vector2fc bottomRight, float z) throws URISyntaxException, FileNotFoundException {
        return new MttTexturedQuad2D(
                vulkanImpl, screen, textureResource.toURI(), topLeft, bottomRight, z);
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            MttTexture texture,
            MttVertex2DUV v1, MttVertex2DUV v2, MttVertex2DUV v3, MttVertex2DUV v4, float z) {
        return new MttTexturedQuad2D(vulkanImpl, texture, v1, v2, v3, v4, z);
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            MttTexture texture,
            Vector2fc topLeft, Vector2fc bottomRight, float z) {
        return new MttTexturedQuad2D(vulkanImpl, texture, topLeft, bottomRight, z);
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
            MttScreen screen, @NotNull URL textureResource) throws URISyntaxException, FileNotFoundException {
        return new MttTexturedQuad2DSingleTextureSet(
                vulkanImpl, screen, textureResource.toURI());
    }

    public MttTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(MttTexture texture) {
        return new MttTexturedQuad2DSingleTextureSet(vulkanImpl, texture);
    }

    public MttBox createBox(float xHalfExtent, float yHalfExtent, float zHalfExtent, Vector4fc color) {
        return new MttBox(vulkanImpl, xHalfExtent, yHalfExtent, zHalfExtent, color);
    }

    public MttBox createBox(float halfExtent, Vector4fc color) {
        return this.createBox(halfExtent, halfExtent, halfExtent, color);
    }

    public MttFont createFont(
            MttScreen screen, Font font, boolean antiAlias, Color fontColor, String requiredChars) {
        return new MttFont(vulkanImpl, screen, font, antiAlias, fontColor, requiredChars);
    }

    public MttButton createButton(MttScreen screen, MttButton.MttButtonCreateInfo createInfo) {
        var mttButton = new MttButton(vulkanImpl, screen, createInfo);
        guiComponents.add(mttButton);

        return mttButton;
    }

    public MttCheckBox createCheckBox(MttScreen screen, MttCheckBox.MttCheckBoxCreateInfo createInfo) {
        var mttCheckbox = new MttCheckBox(vulkanImpl, screen, createInfo);
        guiComponents.add(mttCheckbox);

        return mttCheckbox;
    }

    public MttVerticalScrollBar createVerticalScrollBar(MttVerticalScrollBar.MttVerticalScrollBarCreateInfo createInfo) {
        var mttScrollbar = new MttVerticalScrollBar(vulkanImpl, createInfo);
        guiComponents.add(mttScrollbar);

        return mttScrollbar;
    }

    public MttHorizontalScrollBar createHorizontalScrollBar(
            MttHorizontalScrollBar.MttHorizontalScrollBarCreateInfo createInfo) {
        var mttScrollbar = new MttHorizontalScrollBar(vulkanImpl, createInfo);
        guiComponents.add(mttScrollbar);

        return mttScrollbar;
    }

    public MttListBox createListBox(MttScreen screen, MttListBox.MttListBoxCreateInfo createInfo) {
        var mttListbox = new MttListBox(vulkanImpl, screen, createInfo);
        guiComponents.add(mttListbox);

        return mttListbox;
    }

    public MttLabel createLabel(MttScreen screen, MttLabel.MttLabelCreateInfo createInfo) {
        var mttLabel = new MttLabel(vulkanImpl, screen, createInfo);
        guiComponents.add(mttLabel);

        return mttLabel;
    }

    public MttTextField createTextField(MttScreen screen, MttTextField.MttTextFieldCreateInfo createInfo) {
        var mttTextbox = new MttTextField(vulkanImpl, screen, createInfo);
        guiComponents.add(mttTextbox);

        return mttTextbox;
    }

    public MttTextArea createTextArea(MttScreen screen, MttTextArea.MttTextAreaCreateInfo createInfo) {
        var mttTextarea = new MttTextArea(vulkanImpl, screen, createInfo);
        guiComponents.add(mttTextarea);

        return mttTextarea;
    }

    public boolean removeGuiComponent(MttGuiComponent guiComponent) {
        return guiComponents.remove(guiComponent);
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
            MttScreen screen, @NotNull URL textureResource, boolean generateMipmaps)
            throws URISyntaxException, FileNotFoundException {
        return new MttTexture(vulkanImpl, screen, textureResource.toURI(), generateMipmaps);
    }

    public MttAnimation createAnimation(String tag, MttScreen screen, AnimationInfo animationInfo) throws IOException {
        var animation = new MttAnimation(vulkanImpl, screen, animationInfo);
        animations.put(tag, animation);

        return animation;
    }

    public MttAnimation createAnimation(String tag, AnimationInfo animationInfo, Map<String, MttModel> srcModels) {
        var animation = new MttAnimation(vulkanImpl, animationInfo, srcModels);
        animations.put(tag, animation);

        return animation;
    }
}
