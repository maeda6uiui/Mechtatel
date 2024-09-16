package com.github.maeda6uiui.mechtatel.core.physics;

import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

/**
 * Physics sphere
 *
 * @author maeda6uiui
 */
public class MttPhysicsSphere extends MttPhysicsObject {
    public MttPhysicsSphere(float radius, float mass) {
        var shape = new SphereCollisionShape(radius);
        var body = new PhysicsRigidBody(shape, mass);
        body.setPhysicsLocation(new Vector3f(0.0f, 0.0f, 0.0f));

        this.setShape(shape);
        this.setBody(body);
    }
}
