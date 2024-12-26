package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.Keyboard;
import com.github.maeda6uiui.mechtatel.core.input.mouse.Mouse;
import com.github.maeda6uiui.mechtatel.core.input.mouse.MouseCode;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.sound.MttSound;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiKey;
import imgui.internal.ImGuiContext;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

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

    private static List<ImGuiContext> imguiContexts = new ArrayList<>();

    private String windowId;
    private IMechtatelWindowEventHandlers mtt;
    private MttVulkanImpl vulkanImpl;
    private ImGuiContext imguiContext;

    private long handle;
    private int width;
    private int height;
    private String title;

    private Keyboard keyboard;
    private Mouse mouse;
    private BiConsumer<Double, Double> scrollCallback;

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
        mustRecreate = true;
    }

    public MttWindow(IMechtatelWindowEventHandlers mtt, MttSettings settings) {
        this(
                mtt,
                settings,
                settings.windowSettings.width,
                settings.windowSettings.height,
                settings.windowSettings.title
        );
    }

    public MttWindow(
            IMechtatelWindowEventHandlers mtt,
            MttSettings settings,
            int width,
            int height,
            String title) {
        windowId = UUID.randomUUID().toString();

        //Get monitor if full-screen or windowed full-screen is requested
        boolean bSpecifyMonior = settings.windowSettings.fullScreen || settings.windowSettings.windowedFullScreen;
        long monitor = 0;
        int actualWidth = width;
        int actualHeight = height;
        if (bSpecifyMonior) {
            PointerBuffer pbMonitors = glfwGetMonitors();

            var monitors = new ArrayList<Long>();
            while (pbMonitors.hasRemaining()) {
                monitors.add(pbMonitors.get());
            }

            MemoryUtil.memFree(pbMonitors);

            int monitorIndex = settings.windowSettings.monitorIndex;
            if (monitorIndex == -1) {
                monitorIndex = monitors.size() - 1;
            }

            monitor = monitors.get(monitorIndex);
            logger.debug("{} monitors found, will use monitor {} ({})",
                    monitors.size(), monitorIndex, Long.toHexString(monitor));

            //Set window hints for windowed full-screen window
            if (settings.windowSettings.windowedFullScreen) {
                GLFWVidMode vidMode = glfwGetVideoMode(monitor);
                glfwWindowHint(GLFW_RED_BITS, vidMode.redBits());
                glfwWindowHint(GLFW_GREEN_BITS, vidMode.greenBits());
                glfwWindowHint(GLFW_BLUE_BITS, vidMode.blueBits());
                glfwWindowHint(GLFW_REFRESH_RATE, vidMode.refreshRate());

                actualWidth = vidMode.width();
                actualHeight = vidMode.height();
                logger.debug("Windowed full-screen window will be created with following window size: " +
                        "width={} height={}", actualWidth, actualHeight);
            }
        }

        //Create window =====
        if (bSpecifyMonior) {
            handle = glfwCreateWindow(actualWidth, actualHeight, title, monitor, NULL);
        } else {
            handle = glfwCreateWindow(actualWidth, actualHeight, title, NULL, NULL);
        }

        if (handle == NULL) {
            throw new RuntimeException("Failed to create a window");
        }
        //==========

        this.width = width;
        this.height = height;
        this.title = title;

        this.mtt = mtt;

        vulkanImpl = new MttVulkanImpl(handle);

        keyboard = new Keyboard();
        mouse = new Mouse();

        fixCursorFlag = false;
        mustRecreate = false;
        validWindow = true;

        glfwSetFramebufferSizeCallback(handle, this::framebufferResizeCallback);

        sounds3D = new ArrayList<>();

        //Set up ImGui =====
        imguiContext = ImGui.createContext();
        ImGui.setCurrentContext(imguiContext);
        imguiContexts.add(imguiContext);

        ImGuiIO io = ImGui.getIO();

        io.setIniFilename(null);
        io.setDisplaySize(width, height);

        Function<Integer, Integer> fConvGLFWKeyToImGuiKey = (glfwKey) -> switch (glfwKey) {
            case GLFW_KEY_TAB -> ImGuiKey.Tab;
            case GLFW_KEY_LEFT -> ImGuiKey.LeftArrow;
            case GLFW_KEY_RIGHT -> ImGuiKey.RightArrow;
            case GLFW_KEY_UP -> ImGuiKey.UpArrow;
            case GLFW_KEY_DOWN -> ImGuiKey.DownArrow;
            case GLFW_KEY_PAGE_UP -> ImGuiKey.PageUp;
            case GLFW_KEY_PAGE_DOWN -> ImGuiKey.PageDown;
            case GLFW_KEY_HOME -> ImGuiKey.Home;
            case GLFW_KEY_END -> ImGuiKey.End;
            case GLFW_KEY_INSERT -> ImGuiKey.Insert;
            case GLFW_KEY_DELETE -> ImGuiKey.Delete;
            case GLFW_KEY_BACKSPACE -> ImGuiKey.Backspace;
            case GLFW_KEY_SPACE -> ImGuiKey.Space;
            case GLFW_KEY_ENTER -> ImGuiKey.Enter;
            case GLFW_KEY_ESCAPE -> ImGuiKey.Escape;
            case GLFW_KEY_KP_ENTER -> ImGuiKey.KeyPadEnter;
            default -> ImGuiKey.None;
        };
        BiConsumer<Integer, Boolean> cSetSpecialKey = (glfwKey, value) -> {
            switch (glfwKey) {
                case GLFW_KEY_LEFT_CONTROL:
                case GLFW_KEY_RIGHT_CONTROL:
                    io.setKeyCtrl(value);
                    break;
                case GLFW_KEY_LEFT_SHIFT:
                case GLFW_KEY_RIGHT_SHIFT:
                    io.setKeyShift(value);
                    break;
                case GLFW_KEY_LEFT_ALT:
                case GLFW_KEY_RIGHT_ALT:
                    io.setKeyAlt(value);
                    break;
                case GLFW_KEY_LEFT_SUPER:
                case GLFW_KEY_RIGHT_SUPER:
                    io.setKeySuper(value);
                    break;
            }
        };

        glfwSetKeyCallback(handle, (handle, key, scancode, action, mods) -> {
            boolean pressingFlag = action == GLFW_PRESS || action == GLFW_REPEAT;
            keyboard.setPressingFlag(key, pressingFlag);

            if (action == GLFW_PRESS) {
                io.addKeyEvent(fConvGLFWKeyToImGuiKey.apply(key), true);
                cSetSpecialKey.accept(key, true);
            } else if (action == GLFW_RELEASE) {
                io.addKeyEvent(fConvGLFWKeyToImGuiKey.apply(key), false);
                cSetSpecialKey.accept(key, false);
            }
        });
        glfwSetMouseButtonCallback(handle, (handle, button, action, mods) -> {
            boolean pressingFlag = action == GLFW_PRESS;
            mouse.setPressingFlag(button, pressingFlag);

            if (action == GLFW_PRESS) {
                io.addMouseButtonEvent(button, true);
            } else if (action == GLFW_RELEASE) {
                io.addMouseButtonEvent(button, false);
            }
        });
        glfwSetCursorPosCallback(handle, (handle, xPos, yPos) -> {
            mouse.setCursorPos(xPos, yPos);
            if (fixCursorFlag) {
                glfwSetCursorPos(handle, 0, 0);
            }

            io.addMousePosEvent((float) xPos, (float) yPos);
        });
        glfwSetScrollCallback(handle, (handle, dx, dy) -> {
            if (scrollCallback != null) {
                scrollCallback.accept(dx, dy);
            }

            io.addMouseWheelEvent((float) dx, (float) dy);
        });
        glfwSetCharCallback(handle, (handle, c) -> {
            if (!io.getWantCaptureKeyboard()) {
                return;
            }
            io.addInputCharacter(c);
        });
        //==========

        defaultScreen = new MttScreen(vulkanImpl, imguiContext, new MttScreen.MttScreenCreateInfo());
        screens = new ArrayList<>();
        screens.add(defaultScreen);

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

            ImGui.setCurrentContext(imguiContext);
            ImGuiIO io = ImGui.getIO();
            io.setDisplaySize(width, height);

            mustRecreate = false;
            logger.debug("Window ({}) recreated", Long.toHexString(handle));

            mtt.onRecreate(this, width, height);
        }

        screens.forEach(screen -> {
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

    public static void destroyImGuiContexts() {
        imguiContexts.forEach(ImGui::destroyContext);
    }

    public Optional<MttVulkanImpl> getVulkanImpl() {
        return Optional.ofNullable(vulkanImpl);
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

    public String getWindowId() {
        return windowId;
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

    public double getCursorPosX() {
        return mouse.getCursorPosX();
    }

    public double getCursorPosY() {
        return mouse.getCursorPosY();
    }

    public void setCursorPos(double x, double y) {
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

    public void setScrollCallback(BiConsumer<Double, Double> scrollCallback) {
        this.scrollCallback = scrollCallback;
    }

    public boolean isValidWindow() {
        return validWindow;
    }

    public MttScreen getDefaultScreen() {
        return defaultScreen;
    }

    public MttScreen createScreen(MttScreen.MttScreenCreateInfo createInfo) {
        var screen = new MttScreen(vulkanImpl, imguiContext, createInfo);
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

    public MttSound createSound(Path soundFile, boolean loop, boolean relative) throws IOException {
        var sound = new MttSound(soundFile, loop, relative);
        sounds3D.add(sound);

        return sound;
    }

    public MttSound duplicateSound(MttSound srcSound, boolean loop, boolean relative) {
        var sound = new MttSound(srcSound, loop, relative);
        sounds3D.add(sound);

        return sound;
    }

    public boolean deleteSound(MttSound sound) {
        if (sounds3D.contains(sound)) {
            sound.cleanup();
            sounds3D.remove(sound);

            return true;
        }

        return false;
    }
}
