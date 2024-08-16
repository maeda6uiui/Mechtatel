package com.github.maeda6uiui.mechtatel.core.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanImplCommon;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttSphere;
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
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttComponent screen,
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

        var dq = vulkanImplCommon.getDeviceAndQueues();
        vkSphere = new VkMttSphere(
                this,
                dq.device(),
                vulkanImplCommon.getCommandPool(),
                dq.graphicsQueue(),
                screen.getVulkanScreen(),
                center, radius, numVDivs, numHDivs, color
        );
        this.associateVulkanComponents(vkSphere);
    }
}
