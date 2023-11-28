package com.github.maeda6uiui.mechtatel.core.vulkan.creator;

import com.github.maeda6uiui.mechtatel.core.vulkan.util.PointerBufferUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.QueueFamilyUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.SwapchainUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.validation.ValidationLayers;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Creates logical devices
 *
 * @author maeda6uiui
 */
public class LogicalDeviceCreator {
    public static class VkDeviceAndVkQueues {
        public VkDevice device;
        public VkQueue graphicsQueue;
        public VkQueue presentQueue;
    }

    public static VkDeviceAndVkQueues createLogicalDevice(
            VkPhysicalDevice physicalDevice,
            boolean enableValidationLayer,
            boolean useGraphicsQueueAsPresentQueue,
            long surface) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            QueueFamilyUtils.QueueFamilyIndices indices
                    = QueueFamilyUtils.findQueueFamilies(physicalDevice, surface);

            VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.calloc(2, stack);

            VkDeviceQueueCreateInfo graphicsQueueCreateInfo = queueCreateInfos.get(0);
            graphicsQueueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
            graphicsQueueCreateInfo.queueFamilyIndex(indices.graphicsFamily);
            graphicsQueueCreateInfo.pQueuePriorities(stack.floats(1.0f));

            VkDeviceQueueCreateInfo presentQueueCreateInfo = queueCreateInfos.get(1);
            presentQueueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
            if (useGraphicsQueueAsPresentQueue) {
                presentQueueCreateInfo.queueFamilyIndex(indices.graphicsFamily);
            } else {
                presentQueueCreateInfo.queueFamilyIndex(indices.presentFamily);
            }
            presentQueueCreateInfo.pQueuePriorities(stack.floats(1.0f));

            VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);
            deviceFeatures.samplerAnisotropy(true);
            deviceFeatures.sampleRateShading(true);

            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc(stack);
            createInfo.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
            createInfo.pQueueCreateInfos(queueCreateInfos);
            createInfo.pEnabledFeatures(deviceFeatures);
            createInfo.ppEnabledExtensionNames(PointerBufferUtils.asPointerBuffer(SwapchainUtils.DEVICE_EXTENSIONS));

            if (enableValidationLayer) {
                createInfo.ppEnabledLayerNames(PointerBufferUtils.asPointerBuffer(ValidationLayers.VALIDATION_LAYERS));
            }

            PointerBuffer pDevice = stack.pointers(VK_NULL_HANDLE);
            if (vkCreateDevice(physicalDevice, createInfo, null, pDevice) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a logical device");
            }

            var device = new VkDevice(pDevice.get(0), physicalDevice, createInfo);

            PointerBuffer pGraphicsQueue = stack.pointers(VK_NULL_HANDLE);
            vkGetDeviceQueue(device, indices.graphicsFamily, 0, pGraphicsQueue);
            var graphicsQueue = new VkQueue(pGraphicsQueue.get(0), device);

            PointerBuffer pPresentQueue = stack.pointers(VK_NULL_HANDLE);
            if (useGraphicsQueueAsPresentQueue) {
                vkGetDeviceQueue(device, indices.graphicsFamily, 0, pPresentQueue);
            } else {
                vkGetDeviceQueue(device, indices.presentFamily, 0, pPresentQueue);
            }
            var presentQueue = new VkQueue(pPresentQueue.get(0), device);

            var ret = new VkDeviceAndVkQueues();
            ret.device = device;
            ret.graphicsQueue = graphicsQueue;
            ret.presentQueue = presentQueue;

            return ret;
        }
    }
}
