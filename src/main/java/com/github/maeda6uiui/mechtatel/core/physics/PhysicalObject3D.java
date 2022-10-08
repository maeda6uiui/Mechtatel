package com.github.maeda6uiui.mechtatel.core.physics;

import com.github.maeda6uiui.mechtatel.core.component.Component3D;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Base class for physical objects
 *
 * @author maeda
 */
public class PhysicalObject3D {
    private static PhysicsSpace physicsSpace;
    private CollisionShape shape;
    private PhysicsRigidBody body;

    private Component3D component;
    private Matrix4f mat;

    static {
        physicsSpace = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
    }

    public static PhysicsSpace getPhysicsSpace() {
        return physicsSpace;
    }

    public static void updatePhysicsSpace(float timeDelta, float scale) {
        float simulateSeconds = scale * timeDelta;
        physicsSpace.update(simulateSeconds);
    }

    public PhysicalObject3D() {
        mat = new Matrix4f().identity();
    }

    public void cleanup() {
        if (body != null) {
            physicsSpace.removeCollisionObject(body);
        }
    }

    protected void setShape(CollisionShape shape) {
        this.shape = shape;
    }

    protected void setBody(PhysicsRigidBody body) {
        this.body = body;
    }

    public PhysicsRigidBody getBody() {
        return body;
    }

    public void setComponent(Component3D component) {
        this.component = component;
    }

    public void updateObject() {
        if (body != null) {
            var bodyLocation = new com.jme3.math.Vector3f();
            body.getPhysicsLocation(bodyLocation);
            var translation = new Vector3f(bodyLocation.x, bodyLocation.y, bodyLocation.z);

            var bodyRotMat = new com.jme3.math.Matrix3f();
            body.getPhysicsRotationMatrix(bodyRotMat);

            var rotMat = new Matrix4f().identity();
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    rotMat.set(i, j, bodyRotMat.get(i, j));
                }
            }

            mat = rotMat.translate(translation);
        }
        if (component != null) {
            component.setMat(mat);
        }
    }
}
