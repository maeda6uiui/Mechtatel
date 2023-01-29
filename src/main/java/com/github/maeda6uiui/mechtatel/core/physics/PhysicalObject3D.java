package com.github.maeda6uiui.mechtatel.core.physics;

import com.github.maeda6uiui.mechtatel.core.component.Component3D;
import com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

/**
 * Base class for physical objects
 *
 * @author maeda6uiui
 */
public class PhysicalObject3D {
    private static PhysicsSpace physicsSpace;
    private CollisionShape shape;
    private PhysicsRigidBody body;

    private Component3D component;
    private Matrix4f mat;

    static {
        physicsSpace = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
        physicsSpace.setGravity(new com.jme3.math.Vector3f(0.0f, -9.8f, 0.0f));
    }

    public static PhysicsSpace getPhysicsSpace() {
        return physicsSpace;
    }

    public static void setGravity(Vector3fc gravity) {
        physicsSpace.setGravity(new com.jme3.math.Vector3f(gravity.x(), gravity.y(), gravity.z()));
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
            var translation = ClassConversionUtils.convertJMEVector3fToJOMLVector3f(bodyLocation);

            var bodyRotMat = new com.jme3.math.Matrix3f();
            body.getPhysicsRotationMatrix(bodyRotMat);
            var rotMat = ClassConversionUtils.convertJMEMatrix3fToJOMLMatrix4f(bodyRotMat);

            mat = rotMat.translate(translation);
        }
        if (component != null) {
            component.setMat(mat);
        }
    }
}
