package com.github.maeda6uiui.mechtatel.core.physics;

import com.github.maeda6uiui.mechtatel.core.component.MttComponent3D;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJMEMatrix3fToJOMLMatrix4f;
import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJMEVector3fToJOMLVector3f;

/**
 * Base class for physical objects
 *
 * @author maeda6uiui
 */
public class PhysicalObject3D {
    private static PhysicsSpace physicsSpace;
    private CollisionShape shape;
    private PhysicsRigidBody body;

    private MttComponent3D component;
    private Matrix4f transRotMat;

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
        transRotMat = new Matrix4f().identity();
    }

    public void cleanup() {
        if (body != null) {
            physicsSpace.removeCollisionObject(body);
        }
        if (component != null) {
            component.cleanup();
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

    public void setComponent(MttComponent3D component) {
        this.component = component;
    }

    public void updateObject() {
        if (body != null) {
            var bodyLocation = body.getPhysicsLocation(null);
            var translation = convertJMEVector3fToJOMLVector3f(bodyLocation);
            var translationMat = new Matrix4f().translate(translation);

            var bodyRotMat = body.getPhysicsRotationMatrix(null);
            var rotMat = convertJMEMatrix3fToJOMLMatrix4f(bodyRotMat);

            transRotMat = translationMat.mul(rotMat);
        }
        if (component != null) {
            var transRotScaleMat = transRotMat.mul(new Matrix4f().scale(component.getScale()));
            component.setMat(transRotScaleMat);
        }
    }
}
