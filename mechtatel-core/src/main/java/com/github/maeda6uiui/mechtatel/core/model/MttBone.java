package com.github.maeda6uiui.mechtatel.core.model;

import org.joml.Matrix4f;

/**
 * Model bone
 *
 * @author maeda6uiui
 */
record MttBone(int boneId, String boneName, Matrix4f offsetMatrix) {
}
