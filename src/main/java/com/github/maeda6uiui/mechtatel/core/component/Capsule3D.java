package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkCapsule3D;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

/**
 * Capsule
 *
 * @author maeda6uiui
 */
public class Capsule3D extends Component3D {
    private VkCapsule3D vkCapsule;

    public Capsule3D(
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
