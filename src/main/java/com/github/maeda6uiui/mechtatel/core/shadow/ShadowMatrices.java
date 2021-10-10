package com.github.maeda6uiui.mechtatel.core.shadow;

import org.joml.Matrix4f;

/**
 * View and projection matrices of a light used in shadow mapping
 *
 * @author maeda
 */
public class ShadowMatrices {
    private Matrix4f view;
    private Matrix4f proj;

    public ShadowMatrices() {
        view = new Matrix4f();
        proj = new Matrix4f();
    }

    public Matrix4f getView() {
        return view;
    }

    public void setView(Matrix4f view) {
        this.view = view;
    }

    public Matrix4f getProj() {
        return proj;
    }

    public void setProj(Matrix4f proj) {
        this.proj = proj;
    }
}
