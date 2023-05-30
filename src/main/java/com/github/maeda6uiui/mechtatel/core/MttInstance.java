package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.component.*;
import com.github.maeda6uiui.mechtatel.core.component.gui.*;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.Keyboard;
import com.github.maeda6uiui.mechtatel.core.input.mouse.Mouse;
import com.github.maeda6uiui.mechtatel.core.physics.*;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.sound.Sound3D;
import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.texture.TextureOperationParameters;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.VkMttTexture;
import com.jme3.system.NativeLibraryLoader;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_2_BIT;

/**
 * Provides abstraction of the low-level operations
 *
 * @author maeda6uiui
 */
class MttInstance {
    private IMechtatelForMttInstance mtt;
    private long window;
    private int windowWidth;
    private int windowHeight;
    private Keyboard keyboard;
    private Mouse mouse;
    private boolean fixCursorFlag;
    private MttVulkanInstance vulkanInstance;

    private int fps;

    private List<MttGuiComponent> guiComponents;

    private List<PhysicalObject3D> physicalObjects;
    private float physicsSimulationTimeScale;

    private Map<String, MttScreen> screens;
    private List<String> screenDrawOrder;
    private List<String> textureOperationOrder;
    private List<String> deferredScreenDrawOrder;
    private String presentScreenName;

    private List<Sound3D> sounds3D;

    private void framebufferResizeCallback(long window, int width, int height) {
        mtt.reshape(width, height);

        if (vulkanInstance != null) {
            vulkanInstance.recreateSwapchain();
        }

        for (var screen : screens.values()) {
            if (screen.shouldAutoUpdateCameraAspect()) {
                screen.getCamera().setAspect((float) width / (float) height);
            }
        }

        windowWidth = width;
        windowHeight = height;
    }

    private void keyCallback(long window, int key, int scancode, int action, int mods) {
        boolean pressingFlag = (action == GLFW_PRESS || action == GLFW_REPEAT) ? true : false;
        keyboard.setPressingFlag(key, pressingFlag);
    }

    private void mouseButtonCallback(long window, int button, int action, int mods) {
        boolean pressingFlag = (action == GLFW_PRESS) ? true : false;
        mouse.setPressingFlag(button, pressingFlag);
    }

    private void cursorPositionCallback(long window, double xpos, double ypos) {
        mouse.setCursorPos((int) xpos, (int) ypos);

        if (fixCursorFlag) {
            glfwSetCursorPos(window, 0, 0);
        }
    }

    public MttInstance(
            IMechtatelForMttInstance mtt,
            MttSettings settings) {
        if (!glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        if (settings.windowSettings.resizable) {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        } else {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        }

        window = glfwCreateWindow(
                settings.windowSettings.width,
                settings.windowSettings.height,
                settings.windowSettings.title,
                NULL,
                NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create a window");
        }

        this.windowWidth = settings.windowSettings.width;
        this.windowHeight = settings.windowSettings.height;

        keyboard = new Keyboard();
        mouse = new Mouse();
        fixCursorFlag = false;

        glfwSetFramebufferSizeCallback(window, this::framebufferResizeCallback);
        glfwSetKeyCallback(window, this::keyCallback);
        glfwSetMouseButtonCallback(window, this::mouseButtonCallback);
        glfwSetCursorPosCallback(window, this::cursorPositionCallback);

        vulkanInstance = new MttVulkanInstance(
                true,
                window,
                VK_SAMPLE_COUNT_2_BIT);

        this.fps = settings.systemSettings.fps;

        this.mtt = mtt;

        guiComponents = new ArrayList<>();

        physicalObjects = new ArrayList<>();
        physicsSimulationTimeScale = 1.0f;

        NativeLibraryLoader.loadLibbulletjme(
                settings.bulletSettings.dist,
                new File(settings.bulletSettings.dirname),
                settings.bulletSettings.buildType,
                settings.bulletSettings.flavor);

        screens = new HashMap<>();
        MttScreen defaultScreen = this.createScreen(
                "default",
                2048,
                2048,
                -1,
                -1,
                true,
                null
        );
        screens.put("default", defaultScreen);

        screenDrawOrder = new ArrayList<>();
        screenDrawOrder.add("default");

        textureOperationOrder = new ArrayList<>();

        deferredScreenDrawOrder = new ArrayList<>();

        presentScreenName = "default";

        //Set up OpenAL
        long alcDevice = alcOpenDevice((ByteBuffer) null);
        if (alcDevice == 0) {
            throw new RuntimeException("Failed to open default OpenAL device");
        }
        ALCCapabilities deviceCaps = ALC.createCapabilities(alcDevice);

        long alcContext = alcCreateContext(alcDevice, (IntBuffer) null);
        if (alcContext == 0) {
            throw new RuntimeException("Failed to create OpenAL context");
        }

        alcMakeContextCurrent(alcContext);
        AL.createCapabilities(deviceCaps);

        sounds3D = new ArrayList<>();
    }

    public void run() {
        mtt.init();

        double currentTime = 0.0;
        double lastTime = 0.0;
        double elapsedTime = 0.0;
        glfwSetTime(0.0);

        while (!glfwWindowShouldClose(window)) {
            currentTime = glfwGetTime();
            elapsedTime = currentTime - lastTime;

            if (elapsedTime >= 1.0 / fps) {
                keyboard.update();
                mouse.update();
                mtt.update();
                guiComponents.forEach(guiComponent -> {
                    guiComponent.update(
                            this.getCursorPosX(),
                            this.getCursorPosY(),
                            this.getWindowWidth(),
                            this.getWindowHeight(),
                            this.getMousePressingCount("BUTTON_LEFT"),
                            this.getMousePressingCount("BUTTON_MIDDLE"),
                            this.getMousePressingCount("BUTTON_RIGHT")
                    );
                });
                physicalObjects.forEach(physicalObject -> {
                    physicalObject.updateObject();
                });
                PhysicalObject3D.updatePhysicsSpace((float) elapsedTime, physicsSimulationTimeScale);

                for (var screenName : screenDrawOrder) {
                    mtt.preDraw(screenName);

                    MttScreen screen = screens.get(screenName);
                    screen.draw();

                    mtt.postDraw(screenName);
                }
                for (var textureOperationName : textureOperationOrder) {
                    mtt.preTextureOperation(textureOperationName);
                    vulkanInstance.runTextureOperations(textureOperationName);
                    mtt.postTextureOperation(textureOperationName);
                }
                for (var screenName : deferredScreenDrawOrder) {
                    mtt.preDeferredDraw(screenName);

                    MttScreen screen = screens.get(screenName);
                    screen.draw();

                    mtt.postDeferredDraw(screenName);
                }

                mtt.prePresent();
                vulkanInstance.presentToFrontScreen(presentScreenName);
                mtt.postPresent();

                lastTime = glfwGetTime();
            }

            glfwPollEvents();
        }
    }

    public void cleanup() {
        mtt.dispose();
        vulkanInstance.cleanup();
        physicalObjects.forEach(physicalObject -> {
            physicalObject.cleanup();
        });
        sounds3D.forEach(sound -> {
            sound.cleanup();
        });

        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public void closeWindow() {
        glfwSetWindowShouldClose(window, true);
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
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

    public void setFixCursorFlag(boolean fixCursorFlag) {
        this.fixCursorFlag = fixCursorFlag;
    }

    public int setCursorMode(String cursorMode) {
        int ret = 0;

        switch (cursorMode) {
            case "normal":
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
                break;
            case "disabled":
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
                break;
            case "hidden":
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
                break;
            default:
                ret = -1;
                break;
        }

        return ret;
    }

    public void sortComponents() {
        vulkanInstance.sortComponents();
    }

    public MttModel3D createModel3D(String screenName, String modelFilepath) throws IOException {
        var model = new MttModel3D(vulkanInstance, screenName, modelFilepath);
        return model;
    }

    public MttModel3D duplicateModel3D(MttModel3D srcModel) {
        var model = new MttModel3D(vulkanInstance, srcModel);
        return model;
    }

    public MttLine3D createLine3D(MttVertex3D v1, MttVertex3D v2) {
        var line = new MttLine3D(vulkanInstance, v1, v2);
        return line;
    }

    public MttLine3DSet createLine3DSet() {
        var lineSet = new MttLine3DSet(vulkanInstance);
        return lineSet;
    }

    public MttSphere3D createSphere3D(Vector3fc center, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        var sphere = new MttSphere3D(vulkanInstance, center, radius, numVDivs, numHDivs, color);
        return sphere;
    }

    public MttCapsule3D createCapsule3D(Vector3fc center, float length, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        var capsule = new MttCapsule3D(vulkanInstance, center, length, radius, numVDivs, numHDivs, color);
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

    public MttFilledQuad3D createFilledQuad3D(MttVertex3D v1, MttVertex3D v2, MttVertex3D v3, MttVertex3D v4) {
        var filledQuad = new MttFilledQuad3D(vulkanInstance, v1, v2, v3, v4);
        return filledQuad;
    }

    public MttFilledQuad2D createFilledQuad2D(MttVertex2D p1, MttVertex2D p2, MttVertex2D p3, MttVertex2D p4, float z) {
        var filledQuad = new MttFilledQuad2D(vulkanInstance, p1, p2, p3, p4, z);
        return filledQuad;
    }

    public MttFilledQuad2D createFilledQuad2D(Vector2fc p1, Vector2fc p2, Vector2fc p3, Vector2fc p4, float z, Vector4fc color) {
        var filledQuad = new MttFilledQuad2D(vulkanInstance, p1, p2, p3, p4, z, color);
        return filledQuad;
    }

    public MttFilledQuad2D createFilledQuad2D(Vector2fc topLeft, Vector2fc bottomRight, float z, Vector4fc color) {
        var filledQuad = new MttFilledQuad2D(vulkanInstance, topLeft, bottomRight, z, color);
        return filledQuad;
    }

    public MttTexturedQuad3D createTexturedQuad3D(
            String screenName,
            String textureFilepath,
            boolean generateMipmaps,
            MttVertex3DUV v1,
            MttVertex3DUV v2,
            MttVertex3DUV v3,
            MttVertex3DUV v4) {
        var texturedQuad = new MttTexturedQuad3D(
                vulkanInstance,
                screenName,
                textureFilepath,
                generateMipmaps,
                v1,
                v2,
                v3,
                v4);
        return texturedQuad;
    }

    public MttTexturedQuad3D createTexturedQuad3D(
            String screenName,
            MttTexture texture,
            MttVertex3DUV v1,
            MttVertex3DUV v2,
            MttVertex3DUV v3,
            MttVertex3DUV v4) {
        var texturedQuad = new MttTexturedQuad3D(
                vulkanInstance,
                screenName,
                texture,
                v1,
                v2,
                v3,
                v4);
        return texturedQuad;
    }

    public MttTexturedQuad3D duplicateTexturedQuad3D(
            MttTexturedQuad3D srcQuad,
            MttVertex3DUV v1,
            MttVertex3DUV v2,
            MttVertex3DUV v3,
            MttVertex3DUV v4) {
        var texturedQuad = new MttTexturedQuad3D(
                vulkanInstance,
                srcQuad,
                v1,
                v2,
                v3,
                v4);
        return texturedQuad;
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            String screenName, String textureFilepath, MttVertex2DUV p1, MttVertex2DUV p2, MttVertex2DUV p3, MttVertex2DUV p4, float z) {
        var texturedQuad = new MttTexturedQuad2D(vulkanInstance, screenName, textureFilepath, p1, p2, p3, p4, z);
        return texturedQuad;
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            String screenName, MttTexture texture, MttVertex2DUV p1, MttVertex2DUV p2, MttVertex2DUV p3, MttVertex2DUV p4, float z) {
        var texturedQuad = new MttTexturedQuad2D(vulkanInstance, screenName, texture, p1, p2, p3, p4, z);
        return texturedQuad;
    }

    public MttTexturedQuad2D duplicateTexturedQuad2D(
            MttTexturedQuad2D srcQuad, MttVertex2DUV p1, MttVertex2DUV p2, MttVertex2DUV p3, MttVertex2DUV p4, float z) {
        var texturedQuad = new MttTexturedQuad2D(vulkanInstance, srcQuad, p1, p2, p3, p4, z);
        return texturedQuad;
    }

    public MttTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(String screenName, String textureFilepath) {
        var texturedQuadSet = new MttTexturedQuad2DSingleTextureSet(vulkanInstance, screenName, textureFilepath);
        return texturedQuadSet;
    }

    public MttTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(String screenName, MttTexture texture) {
        var texturedQuadSet = new MttTexturedQuad2DSingleTextureSet(vulkanInstance, screenName, texture);
        return texturedQuadSet;
    }

    public MttQuad2D createQuad2D(MttVertex2D v1, MttVertex2D v2, MttVertex2D v3, MttVertex2D v4, float z) {
        var quad = new MttQuad2D(vulkanInstance, v1, v2, v3, v4, z);
        return quad;
    }

    public MttQuad2D createQuad2D(Vector2fc p1, Vector2fc p2, Vector2fc p3, Vector2fc p4, float z, Vector4fc color) {
        var quad = new MttQuad2D(vulkanInstance, p1, p2, p3, p4, z, color);
        return quad;
    }

    public MttQuad2D createQuad2D(Vector2fc topLeft, Vector2fc bottomRight, float z, Vector4fc color) {
        var quad = new MttQuad2D(vulkanInstance, topLeft, bottomRight, z, color);
        return quad;
    }

    public MttQuad3D createQuad3D(MttVertex3D v1, MttVertex3D v2, MttVertex3D v3, MttVertex3D v4) {
        var quad = new MttQuad3D(vulkanInstance, v1, v2, v3, v4);
        return quad;
    }

    public MttQuad3D createQuad3D(Vector3fc p1, Vector3fc p2, Vector3fc p3, Vector3fc p4, Vector4fc color) {
        var quad = new MttQuad3D(vulkanInstance, p1, p2, p3, p4, color);
        return quad;
    }

    public MttBox3D createBox3D(float xHalfExtent, float yHalfExtent, float zHalfExtent, Vector4fc color) {
        var box = new MttBox3D(vulkanInstance, xHalfExtent, yHalfExtent, zHalfExtent, color);
        return box;
    }

    public MttBox3D createBox3D(float halfExtent, Vector4fc color) {
        return this.createBox3D(halfExtent, halfExtent, halfExtent, color);
    }

    public MttFont createFont(
            String screenName, Font font, boolean antiAlias, Color fontColor, String requiredChars) {
        var mttFont = new MttFont(vulkanInstance, screenName, font, antiAlias, fontColor, requiredChars);
        return mttFont;
    }

    public MttButton createButton(
            float x,
            float y,
            float width,
            float height,
            String text,
            String fontName,
            int fontStyle,
            int fontSize,
            Color fontColor,
            Color frameColor) {
        var mttButton = new MttButton(
                vulkanInstance, x, y, width, height, text, fontName, fontStyle, fontSize, fontColor, frameColor);
        guiComponents.add(mttButton);

        return mttButton;
    }

    public MttCheckbox createCheckbox(
            float x,
            float y,
            float width,
            float height,
            String text,
            String fontName,
            int fontStyle,
            int fontSize,
            Color fontColor,
            Color checkboxColor) {
        var mttCheckbox = new MttCheckbox(
                vulkanInstance, x, y, width, height, text, fontName, fontStyle, fontSize, fontColor, checkboxColor);
        guiComponents.add(mttCheckbox);

        return mttCheckbox;
    }

    public MttVerticalScrollbar createVerticalScrollbar(
            float x,
            float y,
            float width,
            float height,
            float grabHeight,
            Color frameColor,
            Color grabFrameColor) {
        var mttScrollbar = new MttVerticalScrollbar(
                vulkanInstance, x, y, width, height, grabHeight, frameColor, grabFrameColor);
        guiComponents.add(mttScrollbar);

        return mttScrollbar;
    }

    public MttHorizontalScrollbar createHorizontalScrollbar(
            float x,
            float y,
            float width,
            float height,
            float grabWidth,
            Color frameColor,
            Color grabFrameColor) {
        var mttScrollbar = new MttHorizontalScrollbar(
                vulkanInstance, x, y, width, height, grabWidth, frameColor, grabFrameColor);
        guiComponents.add(mttScrollbar);

        return mttScrollbar;
    }

    public MttListbox createListbox(
            float x,
            float y,
            float width,
            float height,
            float scrollbarWidth,
            float scrollbarGrabHeight,
            Color scrollbarFrameColor,
            Color scrollbarGrabColor,
            String nonSelectedFontName,
            int nonSelectedFontStyle,
            int nonSelectedFontSize,
            Color nonSelectedFontColor,
            Color frameColor,
            List<String> itemTexts,
            float itemHeight,
            String selectedFontName,
            int selectedFontStyle,
            int selectedFontSize,
            Color selectedFontColor) {
        var mttListbox = new MttListbox(
                vulkanInstance, x, y, width, height,
                scrollbarWidth, scrollbarGrabHeight, scrollbarFrameColor, scrollbarGrabColor,
                nonSelectedFontName, nonSelectedFontStyle, nonSelectedFontSize, nonSelectedFontColor,
                frameColor, itemTexts, itemHeight,
                selectedFontName, selectedFontStyle, selectedFontSize, selectedFontColor);
        guiComponents.add(mttListbox);

        return mttListbox;
    }

    public MttLabel createLabel(
            float x,
            float y,
            float width,
            float height,
            String requiredChars,
            String fontName,
            int fontStyle,
            int fontSize,
            Color fontColor,
            Color frameColor) {
        var mttLabel = new MttLabel(
                vulkanInstance, x, y, width, height, requiredChars, fontName, fontStyle, fontSize, fontColor, frameColor);
        guiComponents.add(mttLabel);

        return mttLabel;
    }

    public boolean removeGuiComponent(MttGuiComponent guiComponent) {
        return guiComponents.remove(guiComponent);
    }

    public PhysicalPlane3D createPhysicalPlane3D(Vector3fc normal, float constant) {
        var physicalPlane = new PhysicalPlane3D(normal, constant);
        physicalObjects.add(physicalPlane);

        return physicalPlane;
    }

    public PhysicalPlane3D createPhysicalPlane3DWithComponent(
            Vector3fc p1, Vector3fc p2, Vector3fc p3, Vector3fc p4, Vector4fc color) {
        var edge1 = new Vector3f();
        var edge2 = new Vector3f();
        p2.sub(p1, edge1);
        p4.sub(p1, edge2);

        var normal = edge1.cross(edge2).normalize();

        var center = new Vector3f();
        center.x = (p1.x() + p2.x() + p3.x() + p4.x()) / 4.0f;
        center.y = (p1.y() + p2.y() + p3.y() + p4.y()) / 4.0f;
        center.z = (p1.z() + p2.z() + p3.z() + p4.z()) / 4.0f;

        float constant = center.length();

        var physicalPlane = new PhysicalPlane3D(normal, constant);
        physicalObjects.add(physicalPlane);

        var quad = new MttQuad3D(vulkanInstance, p1, p2, p3, p4, color);
        physicalPlane.setComponent(quad);

        return physicalPlane;
    }

    public PhysicalSphere3D createPhysicalSphere3D(float radius, float mass) {
        var physicalSphere = new PhysicalSphere3D(radius, mass);
        physicalObjects.add(physicalSphere);

        return physicalSphere;
    }

    public PhysicalSphere3D createPhysicalSphere3DWithComponent(
            float radius, float mass, int numVDivs, int numHDivs, Vector4fc color) {
        var physicalSphere = new PhysicalSphere3D(radius, mass);
        physicalObjects.add(physicalSphere);

        var sphere = new MttSphere3D(vulkanInstance, new Vector3f(0.0f, 0.0f, 0.0f), radius, numVDivs, numHDivs, color);
        physicalSphere.setComponent(sphere);

        return physicalSphere;
    }

    public PhysicalCapsule3D createPhysicalCapsule3D(float radius, float height, float mass) {
        var physicalCapsule = new PhysicalCapsule3D(radius, height, mass);
        physicalObjects.add(physicalCapsule);

        return physicalCapsule;
    }

    public PhysicalCapsule3D createPhysicalCapsule3DWithComponent(float radius, float height, float mass, int numVDivs, int numHDivs, Vector4fc color) {
        var physicalCapsule = new PhysicalCapsule3D(radius, height, mass);
        physicalObjects.add(physicalCapsule);

        var capsule = new MttCapsule3D(vulkanInstance, new Vector3f(0.0f, 0.0f, 0.0f), height, radius, numVDivs, numHDivs, color);
        physicalCapsule.setComponent(capsule);

        return physicalCapsule;
    }

    public PhysicalBox3D createPhysicalBox3D(float xHalfExtent, float yHalfExtent, float zHalfExtent, float mass) {
        var physicalBox = new PhysicalBox3D(xHalfExtent, yHalfExtent, zHalfExtent, mass);
        physicalObjects.add(physicalBox);

        return physicalBox;
    }

    public PhysicalBox3D createPhysicalBox3D(float halfExtent, float mass) {
        return this.createPhysicalBox3D(halfExtent, halfExtent, halfExtent, mass);
    }

    public PhysicalBox3D createPhysicalBox3DWithComponent(float xHalfExtent, float yHalfExtent, float zHalfExtent, float mass, Vector4fc color) {
        var physicalBox = new PhysicalBox3D(xHalfExtent, yHalfExtent, zHalfExtent, mass);
        physicalObjects.add(physicalBox);

        var box = new MttBox3D(vulkanInstance, xHalfExtent, yHalfExtent, zHalfExtent, color);
        physicalBox.setComponent(box);

        return physicalBox;
    }

    public PhysicalBox3D createPhysicalBox3DWithComponent(float halfExtent, float mass, Vector4fc color) {
        return this.createPhysicalBox3DWithComponent(halfExtent, halfExtent, halfExtent, mass, color);
    }

    public PhysicalMesh3D createPhysicalMesh3D(MttModel3D model, float mass) {
        var physicalMesh = new PhysicalMesh3D(model, mass);
        physicalObjects.add(physicalMesh);

        return physicalMesh;
    }

    public boolean removePhysicalObject3D(PhysicalObject3D physicalObject) {
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

    public Sound3D createSound3D(String filepath, boolean loop, boolean relative) throws IOException {
        var sound = new Sound3D(filepath, loop, relative);
        sounds3D.add(sound);

        return sound;
    }

    public Sound3D duplicateSound3D(Sound3D srcSound, boolean loop, boolean relative) {
        var sound = new Sound3D(srcSound, loop, relative);
        sounds3D.add(sound);

        return sound;
    }

    public boolean removeSound3D(Sound3D sound) {
        if (sounds3D.contains(sound)) {
            sound.cleanup();
            sounds3D.remove(sound);
            return true;
        } else {
            return false;
        }
    }

    public MttTexture createTexture(String screenName, String textureFilepath, boolean generateMipmaps) {
        var texture = new MttTexture(vulkanInstance, screenName, textureFilepath, generateMipmaps);
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
            boolean shouldChangeExtentOnRecreate,
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
                shouldChangeExtentOnRecreate,
                ppNaborNames
        );

        //Set initial aspect according to the framebuffer size
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.ints(0);
            IntBuffer height = stack.ints(0);
            glfwGetFramebufferSize(window, width, height);

            screen.getCamera().setAspect((float) width.get(0) / (float) height.get(0));
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
}
