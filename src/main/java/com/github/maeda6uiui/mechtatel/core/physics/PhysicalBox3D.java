package com.github.maeda6uiui.mechtatel.core.physics;

import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

/**
 * Box 3D
 *
 * @author maeda
 */
public class PhysicalBox3D extends PhysicalObject3D {
    private BoxCollisionShape createShape(float xHalfExtent, float yHalfExtent, float zHalfExtent) {
        return new BoxCollisionShape(xHalfExtent, yHalfExtent, zHalfExtent);
    }

    public PhysicalBox3D(float halfExtent, float mass) {
        var shape = this.createShape(halfExtent, halfExtent, halfExtent);
        var body = new PhysicsRigidBody(shape, mass);
        PhysicalBox3D.getPhysicsSpace().addCollisionObject(body);
        body.setPhysicsLocation(new Vector3f(0.0f, 0.0f, 0.0f));

        this.setShape(shape);
        this.setBody(body);
    }

    public PhysicalBox3D(float xHalfExtent, float yHalfExtent, float zHalfExtent, float mass) {
        var shape = this.createShape(xHalfExtent, yHalfExtent, zHalfExtent);
        var body = new PhysicsRigidBody(shape, mass);
        PhysicalBox3D.getPhysicsSpace().addCollisionObject(body);
        body.setPhysicsLocation(new Vector3f(0.0f, 0.0f, 0.0f));

        this.setShape(shape);
        this.setBody(body);
    }
}
