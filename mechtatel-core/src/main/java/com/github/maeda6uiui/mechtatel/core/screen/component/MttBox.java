package com.github.maeda6uiui.mechtatel.core.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanImplCommon;
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
        var vTop1 = new MttPrimitiveVertex(new Vector3f(-xHalfExtent, yHalfExtent, -zHalfExtent), color);
        var vTop2 = new MttPrimitiveVertex(new Vector3f(-xHalfExtent, yHalfExtent, zHalfExtent), color);
        var vTop3 = new MttPrimitiveVertex(new Vector3f(xHalfExtent, yHalfExtent, zHalfExtent), color);
        var vTop4 = new MttPrimitiveVertex(new Vector3f(xHalfExtent, yHalfExtent, -zHalfExtent), color);
        lineSet
                .add(vTop1, vTop2)
                .add(vTop2, vTop3)
                .add(vTop3, vTop4)
                .add(vTop4, vTop1);
        //Bottom
        var vBottom1 = new MttPrimitiveVertex(new Vector3f(-xHalfExtent, -yHalfExtent, -zHalfExtent), color);
        var vBottom2 = new MttPrimitiveVertex(new Vector3f(-xHalfExtent, -yHalfExtent, zHalfExtent), color);
        var vBottom3 = new MttPrimitiveVertex(new Vector3f(xHalfExtent, -yHalfExtent, zHalfExtent), color);
        var vBottom4 = new MttPrimitiveVertex(new Vector3f(xHalfExtent, -yHalfExtent, -zHalfExtent), color);
        lineSet
                .add(vBottom1, vBottom2)
                .add(vBottom2, vBottom3)
                .add(vBottom3, vBottom4)
                .add(vBottom4, vBottom1);
        //Vertical edges
        lineSet
                .add(vTop1, vBottom1)
                .add(vTop2, vBottom2)
                .add(vTop3, vBottom3)
                .add(vTop4, vBottom4);

        lineSet.createBuffer();
    }

    public MttBox(
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttComponent screen,
            float xHalfExtent,
            float yHalfExtent,
            float zHalfExtent,
            Vector4fc color) {
        super(
                screen,
                new MttComponentCreateInfo()
                        .setVisible(true)
                        .setTwoDComponent(false)
                        .setCastShadow(false)
                        .setDrawOrder(0)
        );

        lineSet = new MttLineSet(vulkanImplCommon, screen);
        this.setupLineSet(xHalfExtent, yHalfExtent, zHalfExtent, color);
        this.associateVulkanComponents(lineSet.getVulkanComponents());
    }

    public MttBox(IMttVulkanImplCommon vulkanImplCommon, IMttScreenForMttComponent screen, float halfExtent, Vector4fc color) {
        this(vulkanImplCommon, screen, halfExtent, halfExtent, halfExtent, color);
    }
}
