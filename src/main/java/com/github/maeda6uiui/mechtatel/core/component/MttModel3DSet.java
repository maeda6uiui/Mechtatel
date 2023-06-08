package com.github.maeda6uiui.mechtatel.core.component;

/**
 * Set of 3D models
 *
 * @author maeda6uiui
 */
public class MttModel3DSet extends MttComponent3DSet {
    @Override
    public void add(MttComponent component) {
        if (!(component instanceof MttModel3D)) {
            throw new IllegalArgumentException("Can only add 3D model");
        }

        super.add(component);
    }
}
