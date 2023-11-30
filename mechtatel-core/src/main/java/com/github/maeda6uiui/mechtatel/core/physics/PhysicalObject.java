package com.github.maeda6uiui.mechtatel.core.physics;

import com.github.maeda6uiui.mechtatel.core.component.MttComponent;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import org.joml.Matrix4f;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJMEMatrix3fToJOMLMatrix4f;
import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJMEVector3fToJOMLVector3f;

/**
 * Base class for physical objects
 *
 * @author maeda6uiui
 */
public class PhysicalObject {
    private CollisionShape shape;
    private PhysicsRigidBody body;

    private MttComponent component;
    private Matrix4f transRotMat;

    public PhysicalObject() {
        transRotMat = new Matrix4f().identity();
    }

    public void cleanup() {
        if (body != null) {
            PhysicalObjects.get().ifPresent(p -> p.removeCollisionObject(body));
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
        PhysicalObjects.get().ifPresent(p -> p.addCollisionObject(body));
    }

    public PhysicsRigidBody getBody() {
        return body;
    }

    public void setComponent(MttComponent component) {
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
