package com.github.maeda6uiui.mechtatel.core.vulkan.creator;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkInstance;

import java.nio.LongBuffer;

import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;

/**
 * Creates a window surface
 *
 * @author maeda
 */
public class SurfaceCreator {
    public static long createSurface(VkInstance instance, long window) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pSurface = stack.longs(VK_NULL_HANDLE);
            if (glfwCreateWindowSurface(instance, window, null, pSurface) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a window surface");
            }

            return pSurface.get(0);
        }
    }
}
