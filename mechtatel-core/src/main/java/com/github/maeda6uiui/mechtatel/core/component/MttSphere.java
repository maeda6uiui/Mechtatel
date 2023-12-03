package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
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
            MttScreen screen,
            Vector3fc center,
            float radius,
            int numVDivs,
            int numHDivs,
            Vector4fc color) {
        super(
                screen,
                new MttComponentCreateInfo()
                        .setVisible(true)
                        .setTwoDComponent(false)
                        .setCastShadow(false)
                        .setDrawOrder(0)
        );

        var dq = vulkanImpl.getDeviceAndQueues();
        vkSphere = new VkMttSphere(
                this, dq.device(), vulkanImpl.getCommandPool(), dq.graphicsQueue(),
                center, radius, numVDivs, numHDivs, color
        );
        this.associateVulkanComponent(vkSphere);
    }
}
