package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.component.*;
import com.github.maeda6uiui.mechtatel.core.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.Keyboard;
import com.github.maeda6uiui.mechtatel.core.input.mouse.Mouse;
import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.physics.*;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.sound.Sound3D;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.ParallelLightNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.PointLightNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.SpotlightNabor;
import com.jme3.system.NativeLibraryLoader;
import org.joml.*;
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
import java.util.List;

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
    private IMechtatel mtt;
    private long window;
    private int windowWidth;
    private int windowHeight;
    private Keyboard keyboard;
    private Mouse mouse;
    private boolean fixCursorFlag;
    private MttVulkanInstance vulkanInstance;

    private int fps;
    private Vector4f backgroundColor;
    private Vector3f parallelLightAmbientColor;
    private Vector3f pointLightAmbientColor;
    private Vector3f spotlightAmbientColor;
    private ShadowMappingSettings shadowMappingSettings;

    private Camera camera;
    private Fog fog;
    private List<ParallelLight> parallelLights;
    private List<PointLight> pointLights;
    private List<Spotlight> spotlights;

    private List<PhysicalObject3D> physicalObjects;
    private float physicsSimulationTimeScale;

    private List<Sound3D> sounds3D;

    private void framebufferResizeCallback(long window, int width, int height) {
        mtt.reshape(width, height);

        if (vulkanInstance != null) {
            vulkanInstance.recreateSwapchain();
        }

        camera.setAspect((float) width / (float) height);

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
            IMechtatel mtt,
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

        backgroundColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        parallelLightAmbientColor = new Vector3f(0.5f, 0.5f, 0.5f);
        pointLightAmbientColor = new Vector3f(0.5f, 0.5f, 0.5f);
        spotlightAmbientColor = new Vector3f(0.5f, 0.5f, 0.5f);
        shadowMappingSettings = new ShadowMappingSettings();

        camera = new Camera();
        fog = new Fog();
        parallelLights = new ArrayList<>();
        pointLights = new ArrayList<>();
        spotlights = new ArrayList<>();

        //Set initial aspect according to the framebuffer size
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.ints(0);
            IntBuffer height = stack.ints(0);
            glfwGetFramebufferSize(window, width, height);

            camera.setAspect((float) width.get(0) / (float) height.get(0));
        }

        physicalObjects = new ArrayList<>();
        physicsSimulationTimeScale = 1.0f;

        NativeLibraryLoader.loadLibbulletjme(
                settings.bulletSettings.dist,
                new File(settings.bulletSettings.dirname),
                settings.bulletSettings.buildType,
                settings.bulletSettings.flavor);

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
                physicalObjects.forEach(physicalObject -> {
                    physicalObject.updateObject();
                });
                PhysicalObject3D.updatePhysicsSpace((float) elapsedTime, physicsSimulationTimeScale);
                vulkanInstance.draw(
                        backgroundColor,
                        camera,
                        fog,
                        parallelLights,
                        parallelLightAmbientColor,
                        pointLights,
                        pointLightAmbientColor,
                        spotlights,
                        spotlightAmbientColor,
                        shadowMappingSettings);

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

    public void createPostProcessingNabors(List<String> naborNames) {
        vulkanInstance.createPostProcessingNabors(naborNames);
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

    public Vector4f getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Vector4f backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Vector3f getParallelLightAmbientColor() {
        return parallelLightAmbientColor;
    }

    public void setParallelLightAmbientColor(Vector3f parallelLightAmbientColor) {
        this.parallelLightAmbientColor = parallelLightAmbientColor;
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

    public Camera getCamera() {
        return camera;
    }

    public Fog getFog() {
        return fog;
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

    //=== Methods relating to components ===
    public Model3D createModel3D(String modelFilepath) {
        var model = new Model3D(vulkanInstance, modelFilepath);
        return model;
    }

    public Model3D duplicateModel3D(Model3D srcModel) {
        var model = new Model3D(vulkanInstance, srcModel);
        return model;
    }

    public Line3D createLine3D(Vertex3D v1, Vertex3D v2) {
        var line = new Line3D(vulkanInstance, v1, v2);
        return line;
    }

    public Line3DSet createLine3DSet() {
        var lineSet = new Line3DSet(vulkanInstance);
        return lineSet;
    }

    public Sphere3D createSphere3D(Vector3fc center, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        var sphere = new Sphere3D(vulkanInstance, center, radius, numVDivs, numHDivs, color);
        return sphere;
    }

    public Capsule3D createCapsule3D(Vector3fc center, float length, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        var capsule = new Capsule3D(vulkanInstance, center, length, radius, numVDivs, numHDivs, color);
        return capsule;
    }

    public Line2D createLine2D(Vertex2D p1, Vertex2D p2, float z) {
        var line = new Line2D(vulkanInstance, p1, p2, z);
        return line;
    }

    public Line2DSet createLine2DSet() {
        var lineSet = new Line2DSet(vulkanInstance);
        return lineSet;
    }

    public TexturedQuad3D createTexturedQuad3D(
            String textureFilepath,
            boolean generateMipmaps,
            Vertex3DUV v1,
            Vertex3DUV v2,
            Vertex3DUV v3,
            Vertex3DUV v4) {
        var texturedQuad = new TexturedQuad3D(
                vulkanInstance,
                textureFilepath,
                generateMipmaps,
                v1,
                v2,
                v3,
                v4);
        return texturedQuad;
    }

    public TexturedQuad3D duplicateTexturedQuad3D(
            TexturedQuad3D srcQuad,
            Vertex3DUV v1,
            Vertex3DUV v2,
            Vertex3DUV v3,
            Vertex3DUV v4) {
        var texturedQuad = new TexturedQuad3D(
                vulkanInstance,
                srcQuad,
                v1,
                v2,
                v3,
                v4);
        return texturedQuad;
    }

    public TexturedQuad2D createTexturedQuad2D(String textureFilepath, Vertex2DUV p1, Vertex2DUV p2, Vertex2DUV p3, Vertex2DUV p4, float z) {
        var texturedQuad = new TexturedQuad2D(vulkanInstance, textureFilepath, p1, p2, p3, p4, z);
        return texturedQuad;
    }

    public TexturedQuad2D duplicateTexturedQuad2D(TexturedQuad2D srcQuad, Vertex2DUV p1, Vertex2DUV p2, Vertex2DUV p3, Vertex2DUV p4, float z) {
        var texturedQuad = new TexturedQuad2D(vulkanInstance, srcQuad, p1, p2, p3, p4, z);
        return texturedQuad;
    }

    public TexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(String textureFilepath) {
        var texturedQuadSet = new TexturedQuad2DSingleTextureSet(vulkanInstance, textureFilepath);
        return texturedQuadSet;
    }

    public Quad2D createQuad2D(Vertex2D v1, Vertex2D v2, Vertex2D v3, Vertex2D v4, float z) {
        var quad = new Quad2D(vulkanInstance, v1, v2, v3, v4, z);
        return quad;
    }

    public Quad2D createQuad2D(Vector2fc p1, Vector2fc p2, Vector2fc p3, Vector2fc p4, float z, Vector4fc color) {
        var quad = new Quad2D(vulkanInstance, p1, p2, p3, p4, z, color);
        return quad;
    }

    public Quad2D createQuad2D(Vector2fc topLeft, Vector2fc bottomRight, float z, Vector4fc color) {
        var p1 = new Vector2f(topLeft);
        var p2 = new Vector2f(topLeft.x(), bottomRight.y());
        var p3 = new Vector2f(bottomRight);
        var p4 = new Vector2f(bottomRight.x(), topLeft.y());
        return this.createQuad2D(p1, p2, p3, p4, z, color);
    }

    public Quad3D createQuad3D(Vertex3D v1, Vertex3D v2, Vertex3D v3, Vertex3D v4) {
        var quad = new Quad3D(vulkanInstance, v1, v2, v3, v4);
        return quad;
    }

    public Quad3D createQuad3D(Vector3fc p1, Vector3fc p2, Vector3fc p3, Vector3fc p4, Vector4fc color) {
        var quad = new Quad3D(vulkanInstance, p1, p2, p3, p4, color);
        return quad;
    }

    public Box3D createBox3D(float xHalfExtent, float yHalfExtent, float zHalfExtent, Vector4fc color) {
        var box = new Box3D(vulkanInstance, xHalfExtent, yHalfExtent, zHalfExtent, color);
        return box;
    }

    public Box3D createBox3D(float halfExtent, Vector4fc color) {
        return this.createBox3D(halfExtent, halfExtent, halfExtent, color);
    }

    public MttFont createMttFont(Font font, boolean antiAlias, Color color, String requiredChars) {
        var mttFont = new MttFont(vulkanInstance, font, antiAlias, color, requiredChars);
        return mttFont;
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

        var quad = new Quad3D(vulkanInstance, p1, p2, p3, p4, color);
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

        var sphere = new Sphere3D(vulkanInstance, new Vector3f(0.0f, 0.0f, 0.0f), radius, numVDivs, numHDivs, color);
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

        var capsule = new Capsule3D(vulkanInstance, new Vector3f(0.0f, 0.0f, 0.0f), height, radius, numVDivs, numHDivs, color);
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

        var box = new Box3D(vulkanInstance, xHalfExtent, yHalfExtent, zHalfExtent, color);
        physicalBox.setComponent(box);

        return physicalBox;
    }

    public PhysicalBox3D createPhysicalBox3DWithComponent(float halfExtent, float mass, Vector4fc color) {
        return this.createPhysicalBox3DWithComponent(halfExtent, halfExtent, halfExtent, mass, color);
    }

    public PhysicalMesh3D createPhysicalMesh3D(Model3D model, float mass) {
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
}
