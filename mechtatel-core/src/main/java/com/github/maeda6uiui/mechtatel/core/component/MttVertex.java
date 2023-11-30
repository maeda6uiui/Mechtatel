package com.github.maeda6uiui.mechtatel.core.component;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

/**
 * 3D vertex
 *
 * @author maeda6uiui
 */
public class MttVertex {
    public static final int SIZEOF = (3 + 4 + 3) * Float.BYTES;
    public static final int OFFSETOF_POS = 0;
    public static final int OFFSETOF_COLOR = 3 * Float.BYTES;
    public static final int OFFSETOF_NORMAL = (3 + 4) * Float.BYTES;

    public Vector3fc pos;
    public Vector4fc color;
    public Vector3fc normal;

    public MttVertex(Vector3fc pos, Vector4fc color, Vector3fc normal) {
        this.pos = pos;
        this.color = color;
        this.normal = normal;
    }

    public MttVertex(Vector3fc pos, Vector4fc color) {
        this.pos = pos;
        this.color = color;
        this.normal = new Vector3f(0.0f, 1.0f, 0.0f);
    }
}
