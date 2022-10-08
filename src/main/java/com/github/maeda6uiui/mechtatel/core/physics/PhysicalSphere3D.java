package com.github.maeda6uiui.mechtatel.core.physics;

import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

/**
 * 3D sphere
 *
 * @author maeda
 */
public class PhysicalSphere3D extends PhysicalObject3D {
    public PhysicalSphere3D(float radius, float mass) {
        var shape = new SphereCollisionShape(radius);
        var body = new PhysicsRigidBody(shape, mass);
        PhysicalSphere3D.getPhysicsSpace().addCollisionObject(body);
        body.setPhysicsLocation(new Vector3f(0.0f, 0.0f, 0.0f));

        this.setShape(shape);
        this.setBody(body);
    }
}
