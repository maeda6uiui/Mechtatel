package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import org.joml.Vector3f;
import org.joml.Vector4fc;

/**
 * Box
 *
 * @author maeda6uiui
 */
public class MttBox extends MttComponent {
    private MttLineSet lineSet;

    private void setupLineSet(float xHalfExtent, float yHalfExtent, float zHalfExtent, Vector4fc color) {
        //Top
        var vTop1 = new MttVertex3D(new Vector3f(-xHalfExtent, yHalfExtent, -zHalfExtent), color);
        var vTop2 = new MttVertex3D(new Vector3f(-xHalfExtent, yHalfExtent, zHalfExtent), color);
        var vTop3 = new MttVertex3D(new Vector3f(xHalfExtent, yHalfExtent, zHalfExtent), color);
        var vTop4 = new MttVertex3D(new Vector3f(xHalfExtent, yHalfExtent, -zHalfExtent), color);
        lineSet.add(vTop1, vTop2);
        lineSet.add(vTop2, vTop3);
        lineSet.add(vTop3, vTop4);
        lineSet.add(vTop4, vTop1);
        //Bottom
        var vBottom1 = new MttVertex3D(new Vector3f(-xHalfExtent, -yHalfExtent, -zHalfExtent), color);
        var vBottom2 = new MttVertex3D(new Vector3f(-xHalfExtent, -yHalfExtent, zHalfExtent), color);
        var vBottom3 = new MttVertex3D(new Vector3f(xHalfExtent, -yHalfExtent, zHalfExtent), color);
        var vBottom4 = new MttVertex3D(new Vector3f(xHalfExtent, -yHalfExtent, -zHalfExtent), color);
        lineSet.add(vBottom1, vBottom2);
        lineSet.add(vBottom2, vBottom3);
        lineSet.add(vBottom3, vBottom4);
        lineSet.add(vBottom4, vBottom1);
        //Vertical edges
        lineSet.add(vTop1, vBottom1);
        lineSet.add(vTop2, vBottom2);
        lineSet.add(vTop3, vBottom3);
        lineSet.add(vTop4, vBottom4);

        lineSet.createBuffer();
    }

    public MttBox(MttVulkanImpl vulkanImpl, float xHalfExtent, float yHalfExtent, float zHalfExtent, Vector4fc color) {
        super(vulkanImpl);

        lineSet = new MttLineSet(vulkanImpl);
        this.setupLineSet(xHalfExtent, yHalfExtent, zHalfExtent, color);
        this.associateVulkanComponent(lineSet.getVulkanComponent());
    }

    public MttBox(MttVulkanImpl vulkanImpl, float halfExtent, Vector4fc color) {
        this(vulkanImpl, halfExtent, halfExtent, halfExtent, color);
    }
}
