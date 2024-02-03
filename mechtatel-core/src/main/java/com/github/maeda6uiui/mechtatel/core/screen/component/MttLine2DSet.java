package com.github.maeda6uiui.mechtatel.core.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttLineSet;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector4fc;

/**
 * Set of 2D lines
 *
 * @author maeda6uiui
 */
public class MttLine2DSet extends MttComponent {
    private VkMttLineSet vkLineSet;

    public MttLine2DSet(MttVulkanImpl vulkanImpl, IMttScreenForMttComponent screen) {
        super(
                screen,
                new MttComponentCreateInfo()
                        .setVisible(true)
                        .setTwoDComponent(true)
                        .setCastShadow(false)
                        .setDrawOrder(0)
        );

        var dq = vulkanImpl.getDeviceAndQueues();
        vkLineSet = new VkMttLineSet(
                this,
                dq.device(),
                vulkanImpl.getCommandPool(),
                dq.graphicsQueue(),
                screen.getVulkanScreen()
        );
        this.associateVulkanComponents(vkLineSet);
    }

    public MttLine2DSet add(MttVertex2D p1, MttVertex2D p2, float z) {
        var v1 = new MttVertex(new Vector3f(p1.pos.x(), p1.pos.y(), z), p1.color);
        var v2 = new MttVertex(new Vector3f(p2.pos.x(), p2.pos.y(), z), p2.color);
        vkLineSet.add(v1, v2);

        return this;
    }

    public MttLine2DSet add(Vector2fc p1, Vector4fc color1, Vector2fc p2, Vector4fc color2, float z) {
        var v1 = new MttVertex(new Vector3f(p1.x(), p1.y(), z), color1);
        var v2 = new MttVertex(new Vector3f(p2.x(), p2.y(), z), color2);
        vkLineSet.add(v1, v2);

        return this;
    }

    public MttLine2DSet add(Vector2fc p1, Vector2fc p2, Vector4fc color, float z) {
        var v1 = new MttVertex(new Vector3f(p1.x(), p1.y(), z), color);
        var v2 = new MttVertex(new Vector3f(p2.x(), p2.y(), z), color);
        vkLineSet.add(v1, v2);

        return this;
    }

    public void clear(boolean doCleanup) {
        vkLineSet.clear(doCleanup);
    }

    public void createBuffer() {
        vkLineSet.createBuffer();
    }
}
