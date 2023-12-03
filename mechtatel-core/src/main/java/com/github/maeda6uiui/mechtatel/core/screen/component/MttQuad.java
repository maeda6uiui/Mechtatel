package com.github.maeda6uiui.mechtatel.core.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
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

    private void setup(MttVulkanImpl vulkanImpl, List<MttVertex> vertices, boolean fill) {
        var dq = vulkanImpl.getDeviceAndQueues();
        vkQuad = new VkMttQuad(
                this, dq.device(), vulkanImpl.getCommandPool(), dq.graphicsQueue(), vertices, fill);
        this.associateVulkanComponent(vkQuad);
    }

    private static MttComponentCreateInfo generateCreateInfo(boolean fill) {
        return new MttComponentCreateInfo()
                .setVisible(true)
                .setTwoDComponent(false)
                .setCastShadow(fill)
                .setDrawOrder(0);
    }

    public MttQuad(
            MttVulkanImpl vulkanImpl,
            IMttScreenForMttComponent screen,
            MttVertex v1,
            MttVertex v2,
            MttVertex v3,
            MttVertex v4,
            boolean fill) {
        super(screen, generateCreateInfo(fill));

        this.setup(vulkanImpl, Arrays.asList(v1, v2, v3, v4), fill);
    }

    public MttQuad(
            MttVulkanImpl vulkanImpl,
            IMttScreenForMttComponent screen,
            Vector3fc p1,
            Vector3fc p2,
            Vector3fc p3,
            Vector3fc p4,
            boolean fill,
            Vector4fc color) {
        super(screen, generateCreateInfo(fill));

        var v1 = new MttVertex(p1, color);
        var v2 = new MttVertex(p2, color);
        var v3 = new MttVertex(p3, color);
        var v4 = new MttVertex(p4, color);
        this.setup(vulkanImpl, Arrays.asList(v1, v2, v3, v4), fill);
    }
}
