package com.github.maeda6uiui.mechtatel.core.model;

import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Model mesh
 *
 * @author maeda6uiui
 */
public class MttMesh {
    public static final int MAX_NUM_WEIGHTS = 4;

    public int materialIndex;

    public final List<MttVertex> vertices;
    public final List<Integer> indices;
    public final List<Integer> boneIndices;
    public final List<Float> weights;

    public MttMesh() {
        materialIndex = -1;

        vertices = new ArrayList<>();
        indices = new ArrayList<>();
        boneIndices = new ArrayList<>();
        weights = new ArrayList<>();
    }
}
