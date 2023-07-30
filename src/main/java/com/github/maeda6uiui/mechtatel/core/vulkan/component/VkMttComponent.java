package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
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
    private boolean castShadow;
    private int drawOrder;
    private String screenName;

    public VkMttComponent() {
        mat = new Matrix4f().identity();
        visible = true;
        componentType = "n/a";
        twoDComponent = false;
        castShadow = false;
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

    public Matrix4f getMat() {
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

    public void setTwoDComponent(boolean twoDComponent) {
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
        this.mat.identity();
    }

    public boolean isCastShadow() {
        return castShadow;
    }

    public void setCastShadow(boolean castShadow) {
        this.castShadow = castShadow;
    }

    public void translate(Vector3fc v) {
        this.getMat().translate(v);
    }

    public void rotX(float ang) {
        this.getMat().rotate(ang, 1.0f, 0.0f, 0.0f);
    }

    public void rotY(float ang) {
        this.getMat().rotate(ang, 0.0f, 1.0f, 0.0f);
    }

    public void rotZ(float ang) {
        this.getMat().rotate(ang, 0.0f, 0.0f, 1.0f);
    }

    public void rot(float ang, Vector3fc axis) {
        this.getMat().rotate(ang, axis);
    }

    public void rescale(Vector3fc scale) {
        this.getMat().scale(scale);
    }

    public void draw(VkCommandBuffer commandBuffer, long pipelineLayout) {

    }

    public void transfer(VkCommandBuffer commandBuffer) {

    }

    public void cleanup() {

    }
}
