package com.github.maeda6uiui.mechtatel.core.component;

import org.joml.Vector3fc;

/**
 * Set of 3D components
 *
 * @author maeda6uiui
 */
public class MttComponent3DSet extends MttComponentSet {
    @Override
    public void add(MttComponent component) {
        if (!(component instanceof MttComponent3D)) {
            throw new IllegalArgumentException("Cannot add 2D component");
        }

        super.add(component);
    }

    public MttComponent3DSet translate(Vector3fc v) {
        this.getComponents().forEach(c -> {
            ((MttComponent3D) c).translate(v);
        });

        return this;
    }

    public MttComponent3DSet rotX(float ang) {
        this.getComponents().forEach(c -> {
            ((MttComponent3D) c).rotX(ang);
        });

        return this;
    }

    public MttComponent3DSet rotY(float ang) {
        this.getComponents().forEach(c -> {
            ((MttComponent3D) c).rotY(ang);
        });

        return this;
    }

    public MttComponent3DSet rotZ(float ang) {
        this.getComponents().forEach(c -> {
            ((MttComponent3D) c).rotZ(ang);
        });

        return this;
    }

    public MttComponent3DSet rot(float ang, Vector3fc axis) {
        this.getComponents().forEach(c -> {
            ((MttComponent3D) c).rot(ang, axis);
        });

        return this;
    }

    public MttComponent3DSet rescale(Vector3fc scale) {
        this.getComponents().forEach(c -> {
            ((MttComponent3D) c).rescale(scale);
        });

        return this;
    }
}
