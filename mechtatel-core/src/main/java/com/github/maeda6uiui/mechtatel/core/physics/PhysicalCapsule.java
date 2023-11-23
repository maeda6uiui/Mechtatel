package com.github.maeda6uiui.mechtatel.core.physics;

import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

/**
 * Physical capsule
 *
 * @author maeda6uiui
 */
public class PhysicalCapsule extends PhysicalObject {
    public PhysicalCapsule(float radius, float height, float mass) {
        var shape = new CapsuleCollisionShape(radius, height);
        var body = new PhysicsRigidBody(shape, mass);
        PhysicalCapsule.getPhysicsSpace().addCollisionObject(body);
        body.setPhysicsLocation(new Vector3f(0.0f, 0.0f, 0.0f));

        this.setShape(shape);
        this.setBody(body);
    }
}
