package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttCapsule3D;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

/**
 * Capsule
 *
 * @author maeda6uiui
 */
public class MttCapsule3D extends MttComponent {
    private VkMttCapsule3D vkCapsule;

    public MttCapsule3D(
            MttVulkanInstance vulkanInstance,
            Vector3fc center,
            float length,
            float radius,
            int numVDivs,
            int numHDivs,
            Vector4fc color) {
        super(vulkanInstance);

        vkCapsule = vulkanInstance.createCapsule3D(center, length, radius, numVDivs, numHDivs, color);
        this.associateVulkanComponent(vkCapsule);
    }
}
