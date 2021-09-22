package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.component.Model3D;
import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

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

    private Camera camera;
    private ParallelLight parallelLight;

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

        camera = new Camera();
        parallelLight = new ParallelLight();

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
                vulkanInstance.draw(camera, parallelLight);

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

    public Camera getCamera() {
        return camera;
    }

    public ParallelLight getParallelLight() {
        return parallelLight;
    }

    //=== Methods relating to components ===
    public Model3D createModel3D(String modelFilepath) {
        var model = new Model3D(vulkanInstance, modelFilepath);
        return model;
    }
}
