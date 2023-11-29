package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttCapsule;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

/**
 * Capsule
 *
 * @author maeda6uiui
 */
public class MttCapsule extends MttComponent {
    private VkMttCapsule vkCapsule;

    public MttCapsule(
            MttVulkanImpl vulkanImpl,
            Vector3fc center,
            float length,
            float radius,
            int numVDivs,
            int numHDivs,
            Vector4fc color) {
        super(vulkanImpl);

        vkCapsule = vulkanImpl.createCapsule(center, length, radius, numVDivs, numHDivs, color);
        this.associateVulkanComponent(vkCapsule);
    }
}
