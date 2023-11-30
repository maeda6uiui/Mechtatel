package com.github.maeda6uiui.mechtatel.core.physics;

import com.github.maeda6uiui.mechtatel.core.component.MttComponent;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.*;

/**
 * Base class for physical objects
 *
 * @author maeda6uiui
 */
public class PhysicalObject {
    private CollisionShape shape;
    private PhysicsRigidBody body;

    private List<MttComponent> components;

    public PhysicalObject() {
        components = new ArrayList<>();
    }

    public void cleanup() {
        if (body != null) {
            PhysicalObjects.get().ifPresent(v -> v.removeCollisionObject(body));
        }
    }

    protected void setShape(CollisionShape shape) {
        this.shape = shape;
    }

    protected void setBody(PhysicsRigidBody body) {
        this.body = body;
        PhysicalObjects.get().ifPresent(v -> v.addCollisionObject(body));
    }

    public CollisionShape getShape() {
        return shape;
    }

    public PhysicsRigidBody getBody() {
        return body;
    }

    public void setLocation(Vector3fc location) {
        var loc = convertJOMLVector3fToJMEVector3f(location);
        body.setPhysicsLocation(loc);
    }

    public Vector3f getLocation() {
        var loc = body.getPhysicsLocation(null);
        return convertJMEVector3fToJOMLVector3f(loc);
    }

    public void setRotationMatrix(Matrix4fc rotationMatrix) {
        var rot = convertJOMLMatrix4fToJMEMatrix3f(rotationMatrix);
        body.setPhysicsRotation(rot);
    }

    public Matrix4f getRotationMatrix() {
        var rot = body.getPhysicsRotationMatrix(null);
        return convertJMEMatrix3fToJOMLMatrix4f(rot);
    }

    public void addComponent(MttComponent component) {
        components.add(component);
    }

    public boolean removeComponent(MttComponent component) {
        return components.remove(component);
    }

    public void removeAllComponents() {
        components.clear();
    }

    public void syncComponents() {
        Vector3f location = this.getLocation();
        Matrix4f rotationMat = this.getRotationMatrix();
        var transformMat = new Matrix4f().translate(location).mul(rotationMat);

        components.forEach(component -> component.setMat(transformMat));
    }
}
