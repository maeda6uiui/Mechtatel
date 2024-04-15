package com.github.maeda6uiui.mechtatel.core.model;

import java.util.List;

/**
 * Animation mesh data
 *
 * @author maeda6uiui
 */
public record MttAnimMeshData(List<Float> weights, List<Integer> boneIds) {
}
