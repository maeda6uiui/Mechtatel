package com.github.maeda6uiui.mechtatel.core.util.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model
 *
 * @author maeda6uiui
 */
public class Model {
    public final Map<Integer, Material> materials;
    public final Map<Integer, Mesh> meshes;

    public Model(int numMaterials, int numMeshes) {
        materials = new HashMap<>();
        for (int i = 0; i < numMaterials; i++) {
            materials.put(i, new Material());
        }

        meshes = new HashMap<>();
        for (int i = 0; i < numMeshes; i++) {
            meshes.put(i, new Mesh());
        }
    }
}
