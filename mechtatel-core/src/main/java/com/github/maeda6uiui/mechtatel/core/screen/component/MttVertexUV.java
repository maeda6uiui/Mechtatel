package com.github.maeda6uiui.mechtatel.core.screen.component;

import org.joml.*;

import java.nio.ByteBuffer;

/**
 * 3D vertex with UV
 *
 * @author maeda6uiui
 */
public class MttVertexUV {
    public static final int SIZEOF = (3 + 4 + 2 + 3) * Float.BYTES;
    public static final int OFFSETOF_POS = 0;
    public static final int OFFSETOF_COLOR = 3 * Float.BYTES;
    public static final int OFFSETOF_TEXCOORDS = (3 + 4) * Float.BYTES;
    public static final int OFFSETOF_NORMAL = (3 + 4 + 2) * Float.BYTES;

    public Vector3fc pos;
    public Vector4fc color;
    public Vector2fc texCoords;
    public Vector3fc normal;

    public void putToByteBuffer(ByteBuffer buffer) {
        buffer.putFloat(pos.x());
        buffer.putFloat(pos.y());
        buffer.putFloat(pos.z());

        buffer.putFloat(color.x());
        buffer.putFloat(color.y());
        buffer.putFloat(color.z());
        buffer.putFloat(color.w());

        buffer.putFloat(texCoords.x());
        buffer.putFloat(texCoords.y());

        buffer.putFloat(normal.x());
        buffer.putFloat(normal.y());
        buffer.putFloat(normal.z());
    }

    public MttVertexUV(Vector3fc pos, Vector4fc color, Vector2fc texCoords, Vector3fc normal) {
        this.pos = pos;
        this.color = color;
        this.texCoords = texCoords;
        this.normal = normal;
    }

    public MttVertexUV(Vector3fc pos, Vector4fc color, Vector2fc texCoords) {
        this.pos = pos;
        this.color = color;
        this.texCoords = texCoords;
        this.normal = new Vector3f(0.0f, 1.0f, 0.0f);
    }

    public MttVertexUV() {
        pos = new Vector3f();
        color = new Vector4f();
        texCoords = new Vector2f();
        normal = new Vector3f(0.0f, 1.0f, 0.0f);
    }
}
