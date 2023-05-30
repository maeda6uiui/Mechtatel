package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttLine3DSet;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

/**
 * Set of 3D lines
 *
 * @author maeda6uiui
 */
public class MttLine3DSet extends MttComponent3D {
    private VkMttLine3DSet vkLineSet;

    public MttLine3DSet(MttVulkanInstance vulkanInstance) {
        super(vulkanInstance);

        vkLineSet = vulkanInstance.createLine3DSet();
        this.associateVulkanComponent(vkLineSet);
    }

    public void add(MttVertex3D v1, MttVertex3D v2) {
        vkLineSet.add(v1, v2);
    }

    public void add(Vector3fc p1, Vector4fc color1, Vector3fc p2, Vector4fc color2) {
        vkLineSet.add(p1, color1, p2, color2);
    }

    public void add(Vector3fc p1, Vector3fc p2, Vector4fc color) {
        vkLineSet.add(p1, p2, color);
    }

    public void clear(boolean doCleanup) {
        vkLineSet.clear(doCleanup);
    }

    public void createBuffer() {
        vkLineSet.createBuffer();
    }
}
