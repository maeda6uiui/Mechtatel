package com.github.maeda6uiui.mechtatel.core.component;

import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Set of 3D components
 *
 * @author maeda6uiui
 */
public class MttComponent3DSet extends MttComponentSet {
    private Vector3f position;
    private Vector3f scale;

    public MttComponent3DSet() {
        position = new Vector3f(0.0f, 0.0f, 0.0f);
        scale = new Vector3f(1.0f, 1.0f, 1.0f);
    }

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

        position.add(v);

        return this;
    }

    public MttComponent3DSet rotX(float ang) {
        this.getComponents().forEach(c -> {
            ((MttComponent3D) c).rotX(ang);
        });

        position.rotateX(ang);

        return this;
    }

    public MttComponent3DSet rotY(float ang) {
        this.getComponents().forEach(c -> {
            ((MttComponent3D) c).rotY(ang);
        });

        position.rotateY(ang);

        return this;
    }

    public MttComponent3DSet rotZ(float ang) {
        this.getComponents().forEach(c -> {
            ((MttComponent3D) c).rotZ(ang);
        });

        position.rotateZ(ang);

        return this;
    }

    public MttComponent3DSet rot(float ang, Vector3fc axis) {
        this.getComponents().forEach(c -> {
            ((MttComponent3D) c).rot(ang, axis);
        });

        position.rotateAxis(ang, axis.x(), axis.y(), axis.z());

        return this;
    }

    public MttComponent3DSet rescale(Vector3fc scale) {
        this.getComponents().forEach(c -> {
            ((MttComponent3D) c).rescale(scale);
        });

        this.scale.mul(scale);
        position.mul(scale);

        return this;
    }

    /**
     * Returns the position of this set.
     * Returned position is only valid if all translation is done via {@link #translate(Vector3fc)}.
     *
     * @return Position of this set
     */
    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    /**
     * Returns the scale of this set.
     * Returned scale is only valid if all rescaling is done via {@link #rescale(Vector3fc)}.
     *
     * @return Scale of this set
     */
    public Vector3f getScale() {
        return new Vector3f(scale);
    }
}
