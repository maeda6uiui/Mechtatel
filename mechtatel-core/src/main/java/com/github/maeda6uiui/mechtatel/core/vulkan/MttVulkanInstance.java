package com.github.maeda6uiui.mechtatel.core.vulkan;

import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.InstanceCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.validation.ValidationLayers;
import org.lwjgl.vulkan.VkInstance;

import java.util.Optional;

import static org.lwjgl.vulkan.VK10.vkDestroyInstance;

/**
 * Retains Vulkan-related objects shared among the Mechtatel engine
 *
 * @author maeda6uiui
 */
public class MttVulkanInstance {
    private VkInstance vkInstance;
    private long debugMessenger;

    private boolean validationLayerEnabled;

    private static MttVulkanInstance instance;

    private MttVulkanInstance(MttSettings.VulkanSettings vulkanSettings) {
        vkInstance = InstanceCreator.createInstance(
                vulkanSettings.enableValidationLayer,
                vulkanSettings.appInfo
        );

        if (vulkanSettings.enableValidationLayer) {
            debugMessenger = ValidationLayers.setupDebugMessenger(vkInstance);
            validationLayerEnabled = true;
        } else {
            validationLayerEnabled = false;
        }
    }

    public void cleanup() {
        if (validationLayerEnabled) {
            ValidationLayers.destroyDebugUtilsMessengerEXT(vkInstance, debugMessenger, null);
        }

        vkDestroyInstance(vkInstance, null);
    }

    public VkInstance getVkInstance() {
        return vkInstance;
    }

    public static MttVulkanInstance create(MttSettings.VulkanSettings vulkanSettings, boolean forceRecreate) {
        if (!forceRecreate && instance != null) {
            return instance;
        }

        instance = new MttVulkanInstance(vulkanSettings);
        return instance;
    }

    public static Optional<MttVulkanInstance> get() {
        return Optional.ofNullable(instance);
    }
}
