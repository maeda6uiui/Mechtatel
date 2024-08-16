package com.github.maeda6uiui.mechtatel.core.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanImplCommon;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttQuad;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

import java.util.Arrays;
import java.util.List;

/**
 * Quadrangle
 *
 * @author maeda6uiui
 */
public class MttQuad extends MttComponent {
    private VkMttQuad vkQuad;

    private void setup(IMttVulkanImplCommon vulkanImplCommon, VkMttScreen vulkanScreen, List<MttPrimitiveVertex> vertices, boolean fill) {
        var dq = vulkanImplCommon.getDeviceAndQueues();
        vkQuad = new VkMttQuad(
                this,
                dq.device(),
                vulkanImplCommon.getCommandPool(),
                dq.graphicsQueue(),
                vulkanScreen,
                vertices,
                fill
        );
        this.associateVulkanComponents(vkQuad);
    }

    private static MttComponentCreateInfo generateCreateInfo(boolean fill) {
        return new MttComponentCreateInfo()
                .setVisible(true)
                .setTwoDComponent(false)
                .setCastShadow(fill)
                .setDrawOrder(0);
    }

    public MttQuad(
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttComponent screen,
            MttPrimitiveVertex v1,
            MttPrimitiveVertex v2,
            MttPrimitiveVertex v3,
            MttPrimitiveVertex v4,
            boolean fill) {
        super(screen, generateCreateInfo(fill));

        this.setup(vulkanImplCommon, screen.getVulkanScreen(), Arrays.asList(v1, v2, v3, v4), fill);
    }

    public MttQuad(
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttComponent screen,
            Vector3fc p1,
            Vector3fc p2,
            Vector3fc p3,
            Vector3fc p4,
            boolean fill,
            Vector4fc color) {
        super(screen, generateCreateInfo(fill));

        var v1 = new MttPrimitiveVertex(p1, color);
        var v2 = new MttPrimitiveVertex(p2, color);
        var v3 = new MttPrimitiveVertex(p3, color);
        var v4 = new MttPrimitiveVertex(p4, color);
        this.setup(vulkanImplCommon, screen.getVulkanScreen(), Arrays.asList(v1, v2, v3, v4), fill);
    }
}
