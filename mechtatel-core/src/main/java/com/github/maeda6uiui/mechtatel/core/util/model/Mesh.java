package com.github.maeda6uiui.mechtatel.core.util.model;

import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertexUV;

import java.util.ArrayList;
import java.util.List;

/**
 * Model mesh
 *
 * @author maeda6uiui
 */
public class Mesh {
    public int materialIndex;

    public final List<MttVertexUV> vertices;
    public final List<Integer> indices;

    public Mesh() {
        materialIndex = -1;

        vertices = new ArrayList<>();
        indices = new ArrayList<>();
    }
}
