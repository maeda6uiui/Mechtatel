package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import org.joml.Vector3fc;

/**
 * Component 3D
 *
 * @author maeda
 */
public class VkComponent3D extends VkComponent {
    private boolean castShadow;

    public VkComponent3D() {
        castShadow = true;
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
