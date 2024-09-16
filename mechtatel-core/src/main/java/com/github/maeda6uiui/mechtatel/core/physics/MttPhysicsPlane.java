package com.github.maeda6uiui.mechtatel.core.physics;

import com.jme3.bullet.collision.shapes.PlaneCollisionShape;
import com.jme3.bullet.objects.PhysicsBody;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Plane;
import com.jme3.math.Vector3f;
import org.joml.Vector3fc;

/**
 * Physics plane
 *
 * @author maeda6uiui
 */
public class MttPhysicsPlane extends MttPhysicsObject {
    public MttPhysicsPlane(Vector3fc normal, float constant) {
        var plane = new Plane(new Vector3f(normal.x(), normal.y(), normal.z()), constant);
        var shape = new PlaneCollisionShape(plane);
        float mass = PhysicsBody.massForStatic;
        var body = new PhysicsRigidBody(shape, mass);

        this.setShape(shape);
        this.setBody(body);
    }
}
