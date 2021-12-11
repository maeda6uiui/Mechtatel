package com.github.maeda6uiui.mechtatel.core.util;

import com.github.maeda6uiui.mechtatel.core.component.Vertex2D;
import com.github.maeda6uiui.mechtatel.core.component.Vertex2DUV;
import com.github.maeda6uiui.mechtatel.core.component.Vertex3D;
import com.github.maeda6uiui.mechtatel.core.component.Vertex3DUV;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Memcpy methods
 *
 * @author maeda
 */
public class MemcpyUtils {
    public static void memcpyVertex2D(ByteBuffer buffer, List<Vertex2D> vertices) {
        for (var vertex : vertices) {
            buffer.putFloat(vertex.pos.x());
            buffer.putFloat(vertex.pos.y());

            buffer.putFloat(vertex.color.x());
            buffer.putFloat(vertex.color.y());
            buffer.putFloat(vertex.color.z());
            buffer.putFloat(vertex.color.w());
        }

        buffer.rewind();
    }

    public static void memcpyVertex2DUV(ByteBuffer buffer, List<Vertex2DUV> vertices) {
        for (var vertex : vertices) {
            buffer.putFloat(vertex.pos.x());
            buffer.putFloat(vertex.pos.y());

            buffer.putFloat(vertex.color.x());
            buffer.putFloat(vertex.color.y());
            buffer.putFloat(vertex.color.z());
            buffer.putFloat(vertex.color.w());

            buffer.putFloat(vertex.texCoords.x());
            buffer.putFloat(vertex.texCoords.y());
        }

        buffer.rewind();
    }

    public static void memcpyVertex3D(ByteBuffer buffer, List<Vertex3D> vertices) {
        for (var vertex : vertices) {
            buffer.putFloat(vertex.pos.x());
            buffer.putFloat(vertex.pos.y());
            buffer.putFloat(vertex.pos.z());

            buffer.putFloat(vertex.color.x());
            buffer.putFloat(vertex.color.y());
            buffer.putFloat(vertex.color.z());
            buffer.putFloat(vertex.color.w());

            buffer.putFloat(vertex.normal.x());
            buffer.putFloat(vertex.normal.y());
            buffer.putFloat(vertex.normal.z());
        }

        buffer.rewind();
    }

    public static void memcpyVertex3DUV(ByteBuffer buffer, List<Vertex3DUV> vertices) {
        for (var vertex : vertices) {
            buffer.putFloat(vertex.pos.x());
            buffer.putFloat(vertex.pos.y());
            buffer.putFloat(vertex.pos.z());

            buffer.putFloat(vertex.color.x());
            buffer.putFloat(vertex.color.y());
            buffer.putFloat(vertex.color.z());
            buffer.putFloat(vertex.color.w());

            buffer.putFloat(vertex.texCoords.x());
            buffer.putFloat(vertex.texCoords.y());

            buffer.putFloat(vertex.normal.x());
            buffer.putFloat(vertex.normal.y());
            buffer.putFloat(vertex.normal.z());
        }

        buffer.rewind();
    }

    public static void memcpyIntegers(ByteBuffer buffer, List<Integer> indices) {
        for (var index : indices) {
            buffer.putInt(index);
        }

        buffer.rewind();
    }
}
