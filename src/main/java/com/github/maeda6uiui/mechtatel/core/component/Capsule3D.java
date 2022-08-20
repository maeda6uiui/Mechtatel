package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkCapsule3D;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

/**
 * Capsule
 *
 * @author maeda
 */
public class Capsule3D extends Component3D {
    private VkCapsule3D vkCapsule;

    public Capsule3D(
            MttVulkanInstance vulkanInstance,
            Vector3fc p1,
            Vector3fc p2,
            float radius,
            int numVDivs,
            int numHDivs,
            Vector4fc color) {
        super(vulkanInstance);

        vkCapsule = vulkanInstance.createCapsule3D(p1, p2, radius, numVDivs, numHDivs, color);
        this.associateVulkanComponent(vkCapsule);
    }
}
