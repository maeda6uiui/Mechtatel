package com.github.maeda6uiui.mechtatel.core.physics;

import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

/**
 * Physics box
 *
 * @author maeda6uiui
 */
public class MttPhysicsBox extends MttPhysicsObject {
    public MttPhysicsBox(float xHalfExtent, float yHalfExtent, float zHalfExtent, float mass) {
        var shape = new BoxCollisionShape(xHalfExtent, yHalfExtent, zHalfExtent);
        var body = new PhysicsRigidBody(shape, mass);
        body.setPhysicsLocation(new Vector3f(0.0f, 0.0f, 0.0f));

        this.setShape(shape);
        this.setBody(body);
    }

    public MttPhysicsBox(float halfExtent, float mass) {
        this(halfExtent, halfExtent, halfExtent, mass);
    }
}
