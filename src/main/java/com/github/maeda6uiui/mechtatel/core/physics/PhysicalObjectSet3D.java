package com.github.maeda6uiui.mechtatel.core.physics;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of physical objects
 *
 * @author maeda6uiui
 */
public class PhysicalObjectSet3D {
    private List<PhysicalObject3D> physicalObjects;

    public PhysicalObjectSet3D() {
        physicalObjects = new ArrayList<>();
    }

    public void cleanup() {
        physicalObjects.forEach(o -> o.cleanup());
    }

    public void add(PhysicalObject3D physicalObject) {
        physicalObjects.add(physicalObject);
    }

    public boolean remove(PhysicalObject3D physicalObject) {
        boolean objectExists = false;

        if (physicalObjects.contains(physicalObject)) {
            physicalObject.cleanup();
            physicalObjects.remove(physicalObject);
            objectExists = true;
        }

        return objectExists;
    }
}
