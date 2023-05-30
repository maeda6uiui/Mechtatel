package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

/**
 * 3D quadrangle
 *
 * @author maeda6uiui
 */
public class MttQuad3D extends MttComponent3D {
    private MttLine3DSet lineSet;

    private void setupLineSet(MttVertex3D v1, MttVertex3D v2, MttVertex3D v3, MttVertex3D v4) {
        lineSet.add(v1, v2);
        lineSet.add(v2, v3);
        lineSet.add(v3, v4);
        lineSet.add(v4, v1);
        lineSet.createBuffer();
    }

    public MttQuad3D(MttVulkanInstance vulkanInstance, MttVertex3D v1, MttVertex3D v2, MttVertex3D v3, MttVertex3D v4) {
        super(vulkanInstance);

        lineSet = new MttLine3DSet(vulkanInstance);
        this.setupLineSet(v1, v2, v3, v4);
        this.associateVulkanComponent(lineSet.getVulkanComponent());
    }

    public MttQuad3D(MttVulkanInstance vulkanInstance, Vector3fc p1, Vector3fc p2, Vector3fc p3, Vector3fc p4, Vector4fc color) {
        super(vulkanInstance);

        lineSet = new MttLine3DSet(vulkanInstance);
        var v1 = new MttVertex3D(p1, color);
        var v2 = new MttVertex3D(p2, color);
        var v3 = new MttVertex3D(p3, color);
        var v4 = new MttVertex3D(p4, color);
        this.setupLineSet(v1, v2, v3, v4);
        this.associateVulkanComponent(lineSet.getVulkanComponent());
    }
}
