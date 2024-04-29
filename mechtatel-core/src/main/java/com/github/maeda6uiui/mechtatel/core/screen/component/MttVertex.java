package com.github.maeda6uiui.mechtatel.core.screen.component;

import org.joml.*;

import java.nio.ByteBuffer;

/**
 * 3D vertex
 *
 * @author maeda6uiui
 */
public class MttVertex {
    public static final int OFFSETOF_POS = 0;
    public static final int OFFSETOF_COLOR = OFFSETOF_POS + 3 * Float.BYTES;
    public static final int OFFSETOF_TEXCOORDS = OFFSETOF_COLOR + 4 * Float.BYTES;
    public static final int OFFSETOF_NORMAL = OFFSETOF_TEXCOORDS + 2 * Float.BYTES;
    public static final int OFFSETOF_BONE_WEIGHTS = OFFSETOF_NORMAL + 3 * Float.BYTES;
    public static final int OFFSETOF_BONE_INDICES = OFFSETOF_BONE_WEIGHTS + 4 * Float.BYTES;
    public static final int SIZEOF = OFFSETOF_BONE_INDICES + 4 * Integer.BYTES;

    public Vector3fc pos;
    public Vector4fc color;
    public Vector2fc texCoords;
    public Vector3fc normal;
    public Vector4fc boneWeights;
    public Vector4ic boneIndices;

    public void putToByteBuffer(ByteBuffer buffer) {
        for (int i = 0; i < 3; i++) {
            buffer.putFloat(pos.get(i));
        }
        for (int i = 0; i < 4; i++) {
            buffer.putFloat(color.get(i));
        }
        for (int i = 0; i < 2; i++) {
            buffer.putFloat(texCoords.get(i));
        }
        for (int i = 0; i < 3; i++) {
            buffer.putFloat(normal.get(i));
        }
        for (int i = 0; i < 4; i++) {
            buffer.putFloat(boneWeights.get(i));
        }
        for (int i = 0; i < 4; i++) {
            buffer.putInt(boneIndices.get(i));
        }
    }

    public MttVertex(
            Vector3fc pos,
            Vector4fc color,
            Vector2fc texCoords,
            Vector3fc normal,
            Vector4fc boneWeights,
            Vector4ic boneIndices) {
        this.pos = pos;
        this.color = color;
        this.texCoords = texCoords;
        this.normal = normal;
        this.boneWeights = boneWeights;
        this.boneIndices = boneIndices;
    }

    public MttVertex(Vector3fc pos, Vector4fc color, Vector2fc texCoords, Vector3fc normal) {
        this(pos, color, texCoords, normal, new Vector4f(), new Vector4i(-1));
    }

    public MttVertex(Vector3fc pos, Vector4fc color, Vector2fc texCoords) {
        this(pos, color, texCoords, new Vector3f(0.0f, 1.0f, 0.0f), new Vector4f(), new Vector4i(-1));
    }

    public MttVertex() {
        this(
                new Vector3f(),
                new Vector4f(),
                new Vector2f(),
                new Vector3f(0.0f, 1.0f, 0.0f),
                new Vector4f(),
                new Vector4i(-1)
        );
    }
}
