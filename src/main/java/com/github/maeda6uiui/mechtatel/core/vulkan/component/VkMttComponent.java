package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.vulkan.VkCommandBuffer;

/**
 * Component
 *
 * @author maeda6uiui
 */
public class VkMttComponent implements Comparable<VkMttComponent> {
    private Matrix4f mat;
    private boolean visible;
    private String componentType;
    private boolean twoDComponent;
    private int drawOrder;
    private String screenName;

    public VkMttComponent() {
        mat = new Matrix4f().identity();
        visible = true;
        componentType = "n/a";
        twoDComponent = true;
        drawOrder = 0;
        screenName = "n/a";
    }

    @Override
    public int compareTo(VkMttComponent component) {
        if (this.drawOrder < component.drawOrder) {
            return -1;
        } else if (this.drawOrder > component.drawOrder) {
            return 1;
        } else {
            return 0;
        }
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

    public int getDrawOrder() {
        return drawOrder;
    }

    public void setDrawOrder(int drawOrder) {
        this.drawOrder = drawOrder;
    }

    public boolean isTwoDComponent() {
        return twoDComponent;
    }

    protected void setTwoDComponent(boolean twoDComponent) {
        this.twoDComponent = twoDComponent;
    }

    protected void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getScreenName() {
        return screenName;
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