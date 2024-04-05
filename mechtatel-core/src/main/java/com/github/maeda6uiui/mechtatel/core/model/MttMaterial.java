package com.github.maeda6uiui.mechtatel.core.model;

import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.net.URI;

/**
 * Model material
 *
 * @author maeda6uiui
 */
public class MttMaterial {
    public URI diffuseTexResource;

    public Vector4fc ambientColor;
    public Vector4fc diffuseColor;
    public Vector4fc specularColor;

    public MttMaterial() {
        ambientColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        diffuseColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        specularColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
    }
}
