package com.github.maeda6uiui.mechtatel.core.component;

import org.joml.Matrix4fc;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of components
 *
 * @author maeda6uiui
 */
public class MttComponentSet<T extends MttComponent> {
    private List<T> components;

    public MttComponentSet() {
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

    public MttComponentSet translate(Vector3fc v) {
        components.forEach(c -> c.translate(v));
        return this;
    }

    public MttComponentSet rotX(float ang) {
        components.forEach(c -> c.rotX(ang));
        return this;
    }

    public MttComponentSet rotY(float ang) {
        components.forEach(c -> c.rotY(ang));
        return this;
    }

    public MttComponentSet rotZ(float ang) {
        components.forEach(c -> c.rotZ(ang));
        return this;
    }

    public MttComponentSet rot(float ang, Vector3fc axis) {
        components.forEach(c -> c.rot(ang, axis));
        return this;
    }

    public MttComponentSet rescale(Vector3fc scale) {
        components.forEach(c -> c.rescale(scale));
        return this;
    }

    public void reset() {
        components.forEach(c -> c.reset());
    }

    public void cleanup() {
        components.forEach(c -> c.cleanup());
    }
}
