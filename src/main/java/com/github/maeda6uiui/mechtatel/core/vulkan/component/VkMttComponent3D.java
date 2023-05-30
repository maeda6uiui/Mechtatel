package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import org.joml.Vector3fc;

/**
 * Component 3D
 *
 * @author maeda6uiui
 */
public class VkMttComponent3D extends VkMttComponent {
    private boolean castShadow;

    public VkMttComponent3D() {
        castShadow = true;
        this.setTwoDComponent(false);
    }

    public boolean isCastShadow() {
        return castShadow;
    }

    public void setCastShadow(boolean castShadow) {
        this.castShadow = castShadow;
    }

    public void translate(Vector3fc v) {
        this.getMatRef().translate(v);
    }

    public void rotX(float ang) {
        this.getMatRef().rotate(ang, 1.0f, 0.0f, 0.0f);
    }

    public void rotY(float ang) {
        this.getMatRef().rotate(ang, 0.0f, 1.0f, 0.0f);
    }

    public void rotZ(float ang) {
        this.getMatRef().rotate(ang, 0.0f, 0.0f, 1.0f);
    }

    public void rot(float ang, Vector3fc axis) {
        this.getMatRef().rotate(ang, axis);
    }

    public void rescale(Vector3fc scale) {
        this.getMatRef().scale(scale);
    }
}
