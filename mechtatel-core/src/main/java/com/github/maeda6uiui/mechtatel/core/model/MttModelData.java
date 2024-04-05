package com.github.maeda6uiui.mechtatel.core.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model data
 *
 * @author maeda6uiui
 */
public class MttModelData {
    public final Map<Integer, MttMaterial> materials;
    public final Map<Integer, MttMesh> meshes;

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
