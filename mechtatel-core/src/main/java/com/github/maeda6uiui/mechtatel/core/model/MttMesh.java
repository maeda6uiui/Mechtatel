package com.github.maeda6uiui.mechtatel.core.model;

import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertexUV;

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

    public final List<MttVertexUV> vertices;
    public final List<Integer> indices;

    public MttMesh() {
        materialIndex = -1;

        vertices = new ArrayList<>();
        indices = new ArrayList<>();
    }
}
