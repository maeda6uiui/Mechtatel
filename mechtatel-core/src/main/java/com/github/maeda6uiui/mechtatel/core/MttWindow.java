package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.Keyboard;
import com.github.maeda6uiui.mechtatel.core.input.mouse.Mouse;
import com.github.maeda6uiui.mechtatel.core.input.mouse.MouseCode;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.animation.MttAnimation;
import com.github.maeda6uiui.mechtatel.core.sound.MttSound;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private String uniqueID;

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

    private MttScreen defaultScreen;
    private List<MttScreen> screens;

    private List<MttSound> sounds3D;

    private void framebufferResizeCallback(long window, int width, int height) {
        mtt.onReshape(this, width, height);

        this.width = width;
        this.height = height;
        //mustRecreate = true;
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
        uniqueID = UUID.randomUUID().toString();

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

        defaultScreen = new MttScreen(vulkanImpl, new MttScreen.MttScreenCreateInfo());
        screens = new ArrayList<>();
        screens.add(defaultScreen);

        sounds3D = new ArrayList<>();

        logger.debug("Window ({}) successfully created", Long.toHexString(handle));

        mtt.onCreate(this);
    }

    public void update() {
        keyboard.update();
        mouse.update();

        if (mustRecreate) {
            vulkanImpl.recreateResourcesOnResize(handle);
            screens.forEach(screen -> {
                screen.recreate();
                if (screen.shouldAutoUpdateCameraAspect()) {
                    screen.getCamera().getPerspectiveCameraInfo().aspect = (float) width / (float) height;
                }
            });

            mustRecreate = false;
            logger.debug("Window ({}) recreated", Long.toHexString(handle));

            mtt.onRecreate(this, width, height);
        }

        Map<KeyCode, Integer> keyboardPressingCounts = keyboard.getPressingCounts();
        screens.forEach(screen -> {
            screen.getGuiComponents().forEach(c -> {
                c.update(
                        this.getCursorPosX(),
                        this.getCursorPosY(),
                        this.getWidth(),
                        this.getHeight(),
                        this.getMousePressingCount(MouseCode.LEFT),
                        this.getMousePressingCount(MouseCode.MIDDLE),
                        this.getMousePressingCount(MouseCode.RIGHT),
                        keyboardPressingCounts
                );
            });
            screen.getAnimations().values().forEach(MttAnimation::update);

            screen.removeGarbageComponents();
            screen.removeGarbageTextures();
            screen.removeGarbageTextureOperations();
        });

        mtt.onUpdate(this);
    }

    public void present(MttScreen screen) {
        this.mustRecreate = vulkanImpl.presentToFrontScreen(screen.getVulkanScreen());
    }

    public void cleanup() {
        mtt.onDispose(this);

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

    public String getUniqueID() {
        return uniqueID;
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

    public int getKeyboardPressingCount(KeyCode keyCode) {
        return keyboard.getPressingCount(keyCode);
    }

    public int getKeyboardReleasingCount(KeyCode keyCode) {
        return keyboard.getReleasingCount(keyCode);
    }

    public int getMousePressingCount(MouseCode mouseCode) {
        return mouse.getPressingCount(mouseCode);
    }

    public int getMouseReleasingCount(MouseCode mouseCode) {
        return mouse.getReleasingCount(mouseCode);
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

    public MttScreen getDefaultScreen() {
        return defaultScreen;
    }

    public MttScreen createScreen(MttScreen.MttScreenCreateInfo createInfo) {
        var screen = new MttScreen(vulkanImpl, createInfo);
        screens.add(screen);

        return screen;
    }

    public boolean deleteScreen(MttScreen screen) {
        if (screen == defaultScreen) {
            logger.warn("You cannot delete default screen");
            return false;
        }

        screen.cleanup();
        return screens.remove(screen);
    }

    public void deleteAllScreens() {
        screens
                .stream()
                .filter(screen -> screen != defaultScreen)
                .forEach(MttScreen::cleanup);
        screens.clear();
        screens.add(defaultScreen);
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
}
