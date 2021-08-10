package com.github.maeda6uiui.mechtatel.core;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Provides abstraction of the low-level operations
 *
 * @author maeda
 */
class MttInstance {
    private IMechtatel mtt;
    private long window;

    private MttVulkanInstance vulkanInstance;

    private void framebufferResizeCallback(long window, int width, int height) {
        mtt.reshape(width, height);
    }

    public MttInstance(IMechtatel mtt, int windowWidth, int windowHeight, String windowTitle, boolean windowResizable) {
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

        vulkanInstance = new MttVulkanInstance(true, window);

        this.mtt = mtt;
        mtt.init();
    }

    public void run() {
        while (!glfwWindowShouldClose(window)) {
            mtt.update();
            mtt.draw();

            vulkanInstance.draw();

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
