package com.github.maeda6uiui.mechtatel.core.screen.component;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.nio.ByteBuffer;

/**
 * 3D vertex for primitive
 *
 * @author maeda6uiui
 */
public class MttPrimitiveVertex {
    public static final int SIZEOF = (3 + 4 + 3) * Float.BYTES;
    public static final int OFFSETOF_POS = 0;
    public static final int OFFSETOF_COLOR = 3 * Float.BYTES;
    public static final int OFFSETOF_NORMAL = (3 + 4) * Float.BYTES;

    public Vector3fc pos;
    public Vector4fc color;
    public Vector3fc normal;

    public void putToByteBuffer(ByteBuffer buffer) {
        for (int i = 0; i < 3; i++) {
            buffer.putFloat(pos.get(i));
        }
        for (int i = 0; i < 4; i++) {
            buffer.putFloat(color.get(i));
        }
        for (int i = 0; i < 3; i++) {
            buffer.putFloat(normal.get(i));
        }
    }

    public MttPrimitiveVertex(Vector3fc pos, Vector4fc color, Vector3fc normal) {
        this.pos = pos;
        this.color = color;
        this.normal = normal;
    }

    public MttPrimitiveVertex(Vector3fc pos, Vector4fc color) {
        this(pos, color, new Vector3f(0.0f, 1.0f, 0.0f));
    }

    public MttPrimitiveVertex() {
        this(
                new Vector3f(),
                new Vector4f(),
                new Vector3f(0.0f, 1.0f, 0.0f)
        );
    }
}
