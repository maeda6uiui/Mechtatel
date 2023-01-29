package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.vulkan.VkCommandBuffer;

/**
 * Component
 *
 * @author maeda6uiui
 */
public class VkComponent {
    private Matrix4f mat;
    private boolean visible;
    private String componentType;

    public VkComponent() {
        mat = new Matrix4f().identity();
        visible = true;
        componentType = "unknown";
    }

    public Matrix4fc getMat() {
        return mat;
    }

    protected Matrix4f getMatRef() {
        return mat;
    }

    public void setMat(Matrix4fc mat) {
        this.mat = new Matrix4f(mat);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    protected void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getComponentType() {
        return componentType;
    }

    public void applyMat(Matrix4fc right) {
        this.mat.mul(right);
    }

    public void reset() {
        this.mat.invert();
    }

    public void draw(VkCommandBuffer commandBuffer, long pipelineLayout) {

    }

    public void transfer(VkCommandBuffer commandBuffer) {

    }

    public void cleanup() {

    }
}
