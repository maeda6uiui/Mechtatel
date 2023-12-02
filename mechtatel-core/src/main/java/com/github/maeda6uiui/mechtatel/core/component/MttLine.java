package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttLine;

/**
 * Line
 *
 * @author maeda6uiui
 */
public class MttLine extends MttComponent {
    private VkMttLine vkLine;

    public MttLine(MttVulkanImpl vulkanImpl, MttVertex v1, MttVertex v2) {
        super(
                new MttComponentCreateInfo()
                        .setVisible(true)
                        .setTwoDComponent(false)
                        .setCastShadow(false)
                        .setDrawOrder(0)
        );

        var dq = vulkanImpl.getDeviceAndQueues();
        vkLine = new VkMttLine(this, dq.device(), vulkanImpl.getCommandPool(), dq.graphicsQueue(), v1, v2);
        this.associateVulkanComponent(vkLine);
    }
}
