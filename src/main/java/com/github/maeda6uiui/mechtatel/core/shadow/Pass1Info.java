package com.github.maeda6uiui.mechtatel.core.shadow;

import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Pass 1 info
 *
 * @author maeda
 */
public class Pass1Info {
    private Matrix4f lightView;
    private Matrix4f lightProj;

    public Pass1Info() {
        lightView = new Matrix4f();
        lightProj = new Matrix4f();
    }

    public Pass1Info(ParallelLight light) {
        lightView = new Matrix4f().lookAt(light.getPosition(), light.getCenter(), new Vector3f(0.0f, 1.0f, 0.0f));
        lightProj = new Matrix4f().ortho(
                light.getOrthoLeft(),
                light.getOrthoRight(),
                light.getOrthoBottom(),
                light.getOrthoTop(),
                light.getzNear(),
                light.getzFar());
        lightProj.m11(lightProj.m11() * (-1.0f));
    }

    public Pass1Info(Spotlight light) {
        lightView = new Matrix4f().lookAt(light.getPosition(), light.getCenter(), new Vector3f(0.0f, 1.0f, 0.0f));
        lightProj = new Matrix4f().perspective(light.getFovY(), light.getAspect(), light.getzNear(), light.getzFar());
        lightProj.m11(lightProj.m11() * (-1.0f));
    }

    public Matrix4f getLightView() {
        return lightView;
    }

    public void setLightView(Matrix4f lightView) {
        this.lightView = lightView;
    }

    public Matrix4f getLightProj() {
        return lightProj;
    }

    public void setLightProj(Matrix4f lightProj) {
        this.lightProj = lightProj;
    }
}
