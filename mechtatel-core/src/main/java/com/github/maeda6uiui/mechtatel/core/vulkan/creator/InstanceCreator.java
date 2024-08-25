package com.github.maeda6uiui.mechtatel.core.vulkan.creator;

import com.github.maeda6uiui.mechtatel.core.AppInfo;
import com.github.maeda6uiui.mechtatel.core.EngineInfo;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.PointerBufferUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.validation.ValidationLayers;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;

import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Creates Vulkan instances
 *
 * @author maeda6uiui
 */
public class InstanceCreator {
    private static PointerBuffer getRequiredExtensions(boolean enableValidationLayer) {
        PointerBuffer glfwExtensions = glfwGetRequiredInstanceExtensions();

        if (enableValidationLayer) {
            MemoryStack stack = MemoryStack.stackGet();

            PointerBuffer extensions = stack.mallocPointer(glfwExtensions.capacity() + 1);
            extensions.put(glfwExtensions);
            extensions.put(stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME));

            return extensions.rewind();
        }

        return glfwExtensions;
    }

    public static VkInstance createInstance(boolean enableValidationLayer) {
        if (enableValidationLayer && !ValidationLayers.checkValidationLayerSupport()) {
            throw new RuntimeException("Validation requested but not supported");
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkApplicationInfo vkAppInfo = VkApplicationInfo.calloc(stack);
            vkAppInfo.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO);
            vkAppInfo.pApplicationName(stack.UTF8Safe(AppInfo.NAME));
            vkAppInfo.applicationVersion(VK_MAKE_VERSION(
                    AppInfo.MAJOR_VERSION, AppInfo.MINOR_VERSION, AppInfo.PATCH_VERSION));
            vkAppInfo.pEngineName(stack.UTF8Safe(EngineInfo.NAME));
            vkAppInfo.engineVersion(VK_MAKE_VERSION(
                    EngineInfo.MAJOR_VERSION, EngineInfo.MINOR_VERSION, EngineInfo.PATCH_VERSION));
            vkAppInfo.apiVersion(VK_API_VERSION_1_0);

            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc(stack);
            createInfo.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
            createInfo.pApplicationInfo(vkAppInfo);
            createInfo.ppEnabledExtensionNames(getRequiredExtensions(enableValidationLayer));

            if (enableValidationLayer) {
                createInfo.ppEnabledLayerNames(PointerBufferUtils.asPointerBuffer(ValidationLayers.VALIDATION_LAYERS));

                VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
                ValidationLayers.populateDebugMessengerCreateInfo(debugCreateInfo);
                createInfo.pNext(debugCreateInfo.address());
            }

            PointerBuffer instancePtr = stack.mallocPointer(1);
            if (vkCreateInstance(createInfo, null, instancePtr) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a Vulkan instance");
            }

            var instance = new VkInstance(instancePtr.get(0), createInfo);
            return instance;
        }
    }
}
