package com.github.maeda6uiui.mechtatel.core.util;

import com.github.maeda6uiui.mechtatel.core.component.Vertex3D;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for vertices
 *
 * @author maeda6uiui
 */
public class VertexUtils {
    public static List<Vertex3D> createSphereVertices(
            Vector3fc center,
            float radius,
            int numVDivs,
            int numHDivs,
            Vector4fc color) {
        var positions = new ArrayList<Vector3f>();

        float vAngle = (float) Math.PI / numVDivs;
        float hAngle = 2.0f * (float) Math.PI / numHDivs;

        for (int i = 0; i <= numVDivs; i++) {
            //North Pole
            if (i == 0) {
                positions.add(new Vector3f(0.0f, radius, 0.0f));
            }
            //South Pole
            else if (i == numVDivs) {
                positions.add(new Vector3f(0.0f, -radius, 0.0f));
            } else {
                float y = radius * (float) Math.sin(Math.PI / 2.0 - i * vAngle);
                float sliceRadius = radius * (float) Math.cos(Math.PI / 2.0 - i * vAngle);

                for (int j = 0; j < numHDivs; j++) {
                    float x = sliceRadius * (float) Math.cos(j * hAngle);
                    float z = sliceRadius * (float) Math.sin(j * hAngle);

                    positions.add(new Vector3f(x, y, z));
                }
            }
        }

        var vertices = new ArrayList<Vertex3D>();
        positions.forEach(pos -> {
            var vertex = new Vertex3D(pos.add(center), color);
            vertices.add(vertex);
        });

        return vertices;
    }

    public static List<Integer> createSphereIndices(int numVDivs, int numHDivs) {
        var indices = new ArrayList<Integer>();

        //Vertical lines
        for (int i = 1; i <= numHDivs; i++) {
            indices.add(0);
            indices.add(i);
        }
        for (int i = 0; i < numVDivs - 2; i++) {
            for (int j = 0; j < numHDivs; j++) {
                indices.add(1 + i * numHDivs + j);
                indices.add(1 + (i + 1) * numHDivs + j);
            }
        }
        for (int i = 0; i < numHDivs; i++) {
            indices.add(1 + (numVDivs - 2) * numHDivs + i);
            indices.add(1 + (numVDivs - 1) * numHDivs);
        }

        //Horizontal lines
        for (int i = 0; i < numVDivs - 1; i++) {
            for (int j = 0; j < numHDivs; j++) {
                indices.add(1 + i * numHDivs + j);
                indices.add(1 + i * numHDivs + (j + 1) % numHDivs);
            }
        }

        return indices;
    }

    public static List<Vertex3D> createCapsuleVertices(
            Vector3fc center,
            float length,
            float radius,
            int numVDivs,
            int numHDivs,
            Vector4fc color) {
        var positions = new ArrayList<Vector3f>();

        float halfLength = length / 2.0f;

        float vAngle = (float) Math.PI / numVDivs;
        float hAngle = 2.0f * (float) Math.PI / numHDivs;

        for (int i = 0; i <= numVDivs; i++) {
            //North Pole
            if (i == 0) {
                positions.add(new Vector3f(0.0f, halfLength + radius, 0.0f));
            }
            //South Pole
            else if (i == numVDivs) {
                positions.add(new Vector3f(0.0f, -halfLength - radius, 0.0f));
            } else {
                float y;
                if (i < numVDivs / 2) {
                    y = radius * (float) Math.sin(Math.PI / 2.0 - i * vAngle) + halfLength;
                } else {
                    y = radius * (float) Math.sin(Math.PI / 2.0 - i * vAngle) - halfLength;
                }
                float sliceRadius = radius * (float) Math.cos(Math.PI / 2.0 - i * vAngle);

                for (int j = 0; j < numHDivs; j++) {
                    float x = sliceRadius * (float) Math.cos(j * hAngle);
                    float z = sliceRadius * (float) Math.sin(j * hAngle);

                    positions.add(new Vector3f(x, y, z));
                }
            }
        }

        var transPositions = new ArrayList<Vector3f>();
        positions.forEach(pos -> {
            var transPosition = pos.add(center);
            transPositions.add(transPosition);
        });

        var vertices = new ArrayList<Vertex3D>();
        transPositions.forEach(pos -> {
            var vertex = new Vertex3D(pos, color);
            vertices.add(vertex);
        });

        return vertices;
    }

    public static List<Integer> createCapsuleIndices(int numVDivs, int numHDivs) {
        return createSphereIndices(numVDivs, numHDivs);
    }
}
