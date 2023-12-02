package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImpl;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttLineSet;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

/**
 * Set of lines
 *
 * @author maeda6uiui
 */
public class MttLineSet extends MttComponent {
    private VkMttLineSet vkLineSet;

    public MttLineSet(MttVulkanImpl vulkanImpl) {
        super(
                vulkanImpl,
                new MttComponentCreateInfo()
                        .setVisible(true)
                        .setTwoDComponent(true)
                        .setCastShadow(false)
                        .setDrawOrder(0)
        );

        vkLineSet = vulkanImpl.createLineSet();
        this.associateVulkanComponent(vkLineSet);
    }

    public MttLineSet add(MttVertex v1, MttVertex v2) {
        vkLineSet.add(v1, v2);
        return this;
    }

    public MttLineSet add(Vector3fc p1, Vector4fc color1, Vector3fc p2, Vector4fc color2) {
        vkLineSet.add(p1, color1, p2, color2);
        return this;
    }

    public MttLineSet add(Vector3fc p1, Vector3fc p2, Vector4fc color) {
        vkLineSet.add(p1, p2, color);
        return this;
    }

    public MttLineSet addAxes(float length) {
        return this
                .add(
                        new Vector3f(-length, 0.0f, 0.0f),
                        new Vector3f(length, 0.0f, 0.0f),
                        new Vector4f(1.0f, 0.0f, 0.0f, 1.0f)
                ).add(
                        new Vector3f(0.0f, -length, 0.0f),
                        new Vector3f(0.0f, length, 0.0f),
                        new Vector4f(0.0f, 1.0f, 0.0f, 1.0f)
                ).add(
                        new Vector3f(0.0f, 0.0f, -length),
                        new Vector3f(0.0f, 0.0f, length),
                        new Vector4f(0.0f, 0.0f, 1.0f, 1.0f)
                );
    }

    public MttLineSet addPositiveAxes(float length) {
        return this
                .add(
                        new Vector3f(0.0f, 0.0f, 0.0f),
                        new Vector3f(length, 0.0f, 0.0f),
                        new Vector4f(1.0f, 0.0f, 0.0f, 1.0f)
                ).add(
                        new Vector3f(0.0f, 0.0f, 0.0f),
                        new Vector3f(0.0f, length, 0.0f),
                        new Vector4f(0.0f, 1.0f, 0.0f, 1.0f)
                ).add(
                        new Vector3f(0.0f, 0.0f, 0.0f),
                        new Vector3f(0.0f, 0.0f, length),
                        new Vector4f(0.0f, 0.0f, 1.0f, 1.0f)
                );
    }

    public MttLineSet addNegativeAxes(float length) {
        return this
                .add(
                        new Vector3f(0.0f, 0.0f, 0.0f),
                        new Vector3f(-length, 0.0f, 0.0f),
                        new Vector4f(1.0f, 0.0f, 0.0f, 1.0f)
                ).add(
                        new Vector3f(0.0f, 0.0f, 0.0f),
                        new Vector3f(0.0f, -length, 0.0f),
                        new Vector4f(0.0f, 1.0f, 0.0f, 1.0f)
                ).add(
                        new Vector3f(0.0f, 0.0f, 0.0f),
                        new Vector3f(0.0f, 0.0f, -length),
                        new Vector4f(0.0f, 0.0f, 1.0f, 1.0f)
                );
    }

    public void clear(boolean doCleanup) {
        vkLineSet.clear(doCleanup);
    }

    public void createBuffer() {
        vkLineSet.createBuffer();
    }
}
