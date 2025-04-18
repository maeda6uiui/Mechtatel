package com.github.maeda6uiui.mechtatel.core.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanImplCommon;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttLine;
import org.joml.Vector3f;

/**
 * 2D line
 *
 * @author maeda6uiui
 */
public class MttLine2D extends MttComponent {
    private VkMttLine vkLine;

    public MttLine2D(IMttVulkanImplCommon vulkanImplCommon, IMttScreenForMttComponent screen, MttVertex2D p1, MttVertex2D p2, float z) {
        super(
                screen,
                new MttComponentCreateInfo()
                        .setVisible(true)
                        .setTwoDComponent(true)
                        .setCastShadow(false)
                        .setDrawOrder(0)
        );

        var dq = vulkanImplCommon.getDeviceAndQueues();

        var v1 = new MttPrimitiveVertex(new Vector3f(p1.pos.x(), p1.pos.y(), z), p1.color);
        var v2 = new MttPrimitiveVertex(new Vector3f(p2.pos.x(), p2.pos.y(), z), p2.color);
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
