package com.github.maeda6uiui.mechtatel.core.component;

import org.joml.Matrix4fc;

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

    public void reset() {
        components.forEach(c -> c.reset());
    }

    public void cleanup() {
        components.forEach(c -> c.cleanup());
    }
}
