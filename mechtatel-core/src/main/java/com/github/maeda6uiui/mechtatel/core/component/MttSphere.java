package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttSphere;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

/**
 * Sphere
 *
 * @author maeda6uiui
 */
public class MttSphere extends MttComponent {
    private VkMttSphere vkSphere;

    public MttSphere(
            MttVulkanImpl vulkanImpl,
            Vector3fc center,
            float radius,
            int numVDivs,
            int numHDivs,
            Vector4fc color) {
        super(
                vulkanImpl,
                new MttComponentCreateInfo()
                        .setVisible(true)
                        .setTwoDComponent(false)
                        .setCastShadow(false)
                        .setDrawOrder(0)
        );

        vkSphere = vulkanImpl.createSphere(center, radius, numVDivs, numHDivs, color);
        this.associateVulkanComponent(vkSphere);
    }
}
