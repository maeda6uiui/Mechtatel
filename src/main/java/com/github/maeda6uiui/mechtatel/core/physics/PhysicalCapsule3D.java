package com.github.maeda6uiui.mechtatel.core.physics;

import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

/**
 * 3D capsule
 *
 * @author maeda
 */
public class PhysicalCapsule3D extends PhysicalObject3D {
    public PhysicalCapsule3D(float radius, float height, float mass) {
        var shape = new CapsuleCollisionShape(radius, height);
        var body = new PhysicsRigidBody(shape, mass);
        PhysicalCapsule3D.getPhysicsSpace().addCollisionObject(body);
        body.setPhysicsLocation(new Vector3f(0.0f, 0.0f, 0.0f));
        
        this.setShape(shape);
        this.setBody(body);
    }
}
