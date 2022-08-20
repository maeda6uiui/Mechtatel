package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkSphere3D;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

/**
 * Sphere
 *
 * @author maeda
 */
public class Sphere3D extends Component3D {
    private VkSphere3D vkSphere;

    public Sphere3D(
            MttVulkanInstance vulkanInstance,
            Vector3fc center,
            float radius,
            int numVDivs,
            int numHDivs,
            Vector4fc color) {
        super(vulkanInstance);

        vkSphere = vulkanInstance.createSphere3D(center, radius, numVDivs, numHDivs, color);
        this.associateVulkanComponent(vkSphere);
    }
}
