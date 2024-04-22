package com.github.maeda6uiui.mechtatel.core.model;

import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model data
 *
 * @author maeda6uiui
 */
public class MttModelData {
    public record AnimatedFrame(Matrix4f[] boneMatrices) {
    }

    public record Animation(String name, double duration, List<AnimatedFrame> frames) {
    }

    public final Map<Integer, MttMaterial> materials;
    public final Map<Integer, MttMesh> meshes;
    public List<Animation> animationList;

    public MttModelData(int numMaterials, int numMeshes) {
        materials = new HashMap<>();
        for (int i = 0; i < numMaterials; i++) {
            materials.put(i, new MttMaterial());
        }

        meshes = new HashMap<>();
        for (int i = 0; i < numMeshes; i++) {
            meshes.put(i, new MttMesh());
        }
    }
}
