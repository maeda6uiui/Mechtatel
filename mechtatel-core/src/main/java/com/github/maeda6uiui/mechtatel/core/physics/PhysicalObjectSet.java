package com.github.maeda6uiui.mechtatel.core.physics;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of physical objects
 *
 * @author maeda6uiui
 */
public class PhysicalObjectSet {
    private List<PhysicalObject> physicalObjects;

    public PhysicalObjectSet() {
        physicalObjects = new ArrayList<>();
    }

    public void cleanup() {
        physicalObjects.forEach(PhysicalObject::cleanup);
    }

    public void add(PhysicalObject physicalObject) {
        physicalObjects.add(physicalObject);
    }

    public boolean remove(PhysicalObject physicalObject) {
        boolean objectExists = false;

        if (physicalObjects.contains(physicalObject)) {
            physicalObject.cleanup();
            physicalObjects.remove(physicalObject);
            objectExists = true;
        }

        return objectExists;
    }

    public List<PhysicalObject> getPhysicalObjects() {
        return new ArrayList<>(physicalObjects);
    }
}
