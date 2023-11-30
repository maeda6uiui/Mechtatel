package com.github.maeda6uiui.mechtatel.core.physics;

import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.objects.PhysicsRigidBody;

/**
 * Base class for physical objects
 *
 * @author maeda6uiui
 */
public class PhysicalObject {
    private CollisionShape shape;
    private PhysicsRigidBody body;

    public PhysicalObject() {

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
}
