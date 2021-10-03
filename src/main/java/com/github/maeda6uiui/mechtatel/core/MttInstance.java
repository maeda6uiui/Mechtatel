package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.component.Model3D;
import com.github.maeda6uiui.mechtatel.core.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Vector3f;
import org.joml.Vector4f;
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
    private MttVulkanInstance vulkanInstance;

    private int fps;

    private Vector4f backgroundColor;
    private Vector3f parallelLightAmbientColor;
    private Vector3f spotlightAmbientColor;

    private Camera camera;
    private List<ParallelLight> parallelLights;
    private List<Spotlight> spotlights;
    private Fog fog;

    private void framebufferResizeCallback(long window, int width, int height) {
        mtt.reshape(width, height);

        if (vulkanInstance != null) {
            vulkanInstance.recreateSwapchain();
        }

        camera.setAspect((float) width / (float) height);
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

        glfwSetFramebufferSizeCallback(window, this::framebufferResizeCallback);

        vulkanInstance = new MttVulkanInstance(true, window, VK_SAMPLE_COUNT_2_BIT);

        this.fps = settings.systemSettings.fps;

        this.mtt = mtt;

        backgroundColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        parallelLightAmbientColor = new Vector3f(0.5f, 0.5f, 0.5f);
        spotlightAmbientColor = new Vector3f(0.5f, 0.5f, 0.5f);

        camera = new Camera();

        parallelLights = new ArrayList<>();
        parallelLights.add(new ParallelLight());

        spotlights = new ArrayList<>();

        fog = new Fog();

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
                mtt.update();
                vulkanInstance.draw(
                        backgroundColor,
                        camera,
                        parallelLights,
                        parallelLightAmbientColor,
                        spotlights,
                        spotlightAmbientColor,
                        fog);

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

    public Vector3f getSpotlightAmbientColor() {
        return spotlightAmbientColor;
    }

    public void setSpotlightAmbientColor(Vector3f spotlightAmbientColor) {
        this.spotlightAmbientColor = spotlightAmbientColor;
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
        var parallelLight = new ParallelLight();
        parallelLights.add(parallelLight);

        return parallelLight;
    }

    public boolean removeParallelLight(ParallelLight parallelLight) {
        if (parallelLights.get(0) == parallelLight) {
            throw new RuntimeException("Default light cannot be removed");
        }

        return parallelLights.remove(parallelLight);
    }

    public int getNumSpotlights() {
        return spotlights.size();
    }

    public Spotlight getSpotlight(int index) {
        return spotlights.get(index);
    }

    public Spotlight createSpotlight() {
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
}
