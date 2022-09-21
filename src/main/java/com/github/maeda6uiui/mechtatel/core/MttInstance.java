package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.component.*;
import com.github.maeda6uiui.mechtatel.core.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.Keyboard;
import com.github.maeda6uiui.mechtatel.core.input.mouse.Mouse;
import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.ParallelLightNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.PointLightNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.SpotlightNabor;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_2_BIT;

/**
 * Provides abstraction of the low-level operations
 *
 * @author maeda
 */
class MttInstance {
    private IMechtatel mtt;
    private long window;
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

    private void framebufferResizeCallback(long window, int width, int height) {
        mtt.reshape(width, height);

        if (vulkanInstance != null) {
            vulkanInstance.recreateSwapchain();
        }

        camera.setAspect((float) width / (float) height);
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

        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public void closeWindow() {
        glfwSetWindowShouldClose(window, true);
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
}
