package com.github.maeda6uiui.mechtatel.core.physics;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Optional;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJOMLVector3fToJMEVector3f;

/**
 * Provides methods to manage physical objects
 *
 * @author maeda6uiui
 */
public class PhysicalObjects {
    private PhysicsSpace physicsSpace;
    private float physicsSimulationTimeScale;

    private static PhysicalObjects instance;

    private PhysicalObjects(PhysicsSpace.BroadphaseType broadphaseType) {
        physicsSpace = new PhysicsSpace(broadphaseType);
        physicsSpace.setGravity(convertJOMLVector3fToJMEVector3f(new Vector3f(0.0f, -9.8f, 0.0f)));
        physicsSimulationTimeScale = 1.0f;
    }

    public PhysicsSpace getPhysicsSpace() {
        return physicsSpace;
    }

    public void updatePhysicsSpace(float timeDelta) {
        float simulateSeconds = physicsSimulationTimeScale * timeDelta;
        physicsSpace.update(simulateSeconds);
    }

    public void addCollisionObject(PhysicsCollisionObject pco) {
        physicsSpace.addCollisionObject(pco);
    }

    public void removeCollisionObject(PhysicsCollisionObject pco) {
        physicsSpace.removeCollisionObject(pco);
    }

    public void setGravity(Vector3fc gravity) {
        physicsSpace.setGravity(convertJOMLVector3fToJMEVector3f(gravity));
    }

    public void setPhysicsSimulationTimeScale(float physicsSimulationTimeScale) {
        this.physicsSimulationTimeScale = physicsSimulationTimeScale;
    }

    public static void init(PhysicsSpace.BroadphaseType broadphaseType) {
        if (instance != null) {
            return;
        }

        instance = new PhysicalObjects(broadphaseType);
    }

    public static Optional<PhysicalObjects> get() {
        return Optional.ofNullable(instance);
    }
}
