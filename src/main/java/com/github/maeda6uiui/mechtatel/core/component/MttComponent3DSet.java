package com.github.maeda6uiui.mechtatel.core.component;

import org.joml.Matrix4fc;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of 3D components
 *
 * @author maeda6uiui
 */
public class MttComponent3DSet<T extends MttComponent3D> {
    private List<T> components;

    public MttComponent3DSet() {
        components = new ArrayList<>();
    }

    protected List<T> getComponents() {
        return components;
    }

    public void add(T component) {
        components.add(component);
    }

    public boolean remove(T component) {
        return components.remove(component);
    }

    public void clear() {
        components.clear();
    }

    public void setMat(Matrix4fc mat) {
        components.forEach(c -> c.setMat(mat));
    }

    public void setVisible(boolean visible) {
        components.forEach(c -> c.setVisible(visible));
    }

    public void applyMat(Matrix4fc right) {
        components.forEach(c -> c.applyMat(right));
    }

    public void reset() {
        components.forEach(c -> c.reset());
    }

    public void cleanup() {
        components.forEach(c -> c.cleanup());
    }

    public MttComponent3DSet translate(Vector3fc v) {
        components.forEach(c -> c.translate(v));
        return this;
    }

    public MttComponent3DSet rotX(float ang) {
        components.forEach(c -> c.rotX(ang));
        return this;
    }

    public MttComponent3DSet rotY(float ang) {
        components.forEach(c -> c.rotY(ang));
        return this;
    }

    public MttComponent3DSet rotZ(float ang) {
        components.forEach(c -> c.rotZ(ang));
        return this;
    }

    public MttComponent3DSet rot(float ang, Vector3fc axis) {
        components.forEach(c -> c.rot(ang, axis));
        return this;
    }

    public MttComponent3DSet rescale(Vector3fc scale) {
        components.forEach(c -> c.rescale(scale));
        return this;
    }
}
