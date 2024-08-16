package com.github.maeda6uiui.mechtatel.core.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanImplCommon;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttLine;

/**
 * Line
 *
 * @author maeda6uiui
 */
public class MttLine extends MttComponent {
    private VkMttLine vkLine;

    public MttLine(IMttVulkanImplCommon vulkanImplCommon, IMttScreenForMttComponent screen, MttPrimitiveVertex v1, MttPrimitiveVertex v2) {
        super(
                screen,
                new MttComponentCreateInfo()
                        .setVisible(true)
                        .setTwoDComponent(false)
                        .setCastShadow(false)
                        .setDrawOrder(0)
        );

        var dq = vulkanImplCommon.getDeviceAndQueues();
        vkLine = new VkMttLine(
                this,
                dq.device(),
                vulkanImplCommon.getCommandPool(),
                dq.graphicsQueue(),
                screen.getVulkanScreen(),
                v1, v2
        );
        this.associateVulkanComponents(vkLine);
    }
}
