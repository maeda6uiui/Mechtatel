package com.github.maeda6uiui.mechtatel.core.fog;

import org.joml.Vector3f;

/**
 * Fog
 *
 * @author maeda
 */
public class Fog {
    private Vector3f color;
    private float start;
    private float end;

    public Fog() {
        color = new Vector3f(0.0f, 0.0f, 0.0f);
        start = 500.0f;
        end = 1000.0f;
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public float getStart() {
        return start;
    }

    public void setStart(float start) {
        this.start = start;
    }

    public float getEnd() {
        return end;
    }

    public void setEnd(float end) {
        this.end = end;
    }
}
