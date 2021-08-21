package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;

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

    private void framebufferResizeCallback(long window, int width, int height) {
        mtt.reshape(width, height);

        if (vulkanInstance != null) {
            vulkanInstance.recreateSwapchain();
        }
    }

    public MttInstance(
            IMechtatel mtt,
            int windowWidth,
            int windowHeight,
            String windowTitle,
            boolean windowResizable,
            int fps) {
        if (!glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        if (windowResizable) {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        } else {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        }

        window = glfwCreateWindow(windowWidth, windowHeight, windowTitle, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create a window");
        }

        glfwSetFramebufferSizeCallback(window, this::framebufferResizeCallback);

        vulkanInstance = new MttVulkanInstance(true, window, VK_SAMPLE_COUNT_2_BIT);

        this.fps = fps;

        this.mtt = mtt;
        mtt.init();
    }

    public void run() {
        double currentTime = 0.0;
        double lastTime = 0.0;
        double elapsedTime = 0.0;
        glfwSetTime(0.0);

        while (!glfwWindowShouldClose(window)) {
            currentTime = glfwGetTime();
            elapsedTime = currentTime - lastTime;

            if (elapsedTime >= 1.0 / fps) {
                mtt.update();
                mtt.draw();

                vulkanInstance.draw();

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
}
