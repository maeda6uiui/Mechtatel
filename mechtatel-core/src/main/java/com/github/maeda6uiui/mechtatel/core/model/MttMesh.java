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
    public int materialIndex;

    public final List<MttVertex> vertices;
    public final List<Integer> indices;

    public MttMesh() {
        materialIndex = -1;

        vertices = new ArrayList<>();
        indices = new ArrayList<>();
    }
}
