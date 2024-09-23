package com.github.maeda6uiui.mechtatel.core.camera;

import com.github.maeda6uiui.mechtatel.core.physics.MttPhysicsSphere;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttComponent;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

/**
 * Camera that follows a target component.
 * Its distance from the target component varies
 * if there is an obstacle between the target component and the camera.
 *
 * @author maeda6uiui
 */
public class VaryingDistanceTrackingCamera implements PhysicsCollisionListener {
    private Camera camera;

    private MttComponent trackedComponent;

    private float desiredDistance;
    private float minDistance;
    private float distanceDelta;
    private float cameraRadius;

    private float horizontalAngle;
    private float verticalAngle;

    private PhysicsSpace cameraPhysicsSpace;
    private boolean cameraCollides;

    public VaryingDistanceTrackingCamera(Camera camera, MttComponent trackedComponent) {
        this.camera = camera;

        this.trackedComponent = trackedComponent;

        desiredDistance = 3.0f;
        minDistance = 0.3f;
        distanceDelta = 0.3f;
        cameraRadius = 0.5f;

        horizontalAngle = 0.0f;
        verticalAngle = 0.0f;

        cameraPhysicsSpace = new PhysicsSpace(PhysicsSpace.BroadphaseType.DBVT);
        cameraCollides = false;
    }

    public Camera getCamera() {
        return camera;
    }

    public MttComponent getTrackedComponent() {
        return trackedComponent;
    }

    public float getDesiredDistance() {
        return desiredDistance;
    }

    public void setDesiredDistance(float desiredDistance) {
        this.desiredDistance = desiredDistance;
    }

    public float getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(float minDistance) {
        this.minDistance = minDistance;
    }

    public float getDistanceDelta() {
        return distanceDelta;
    }

    public void setDistanceDelta(float distanceDelta) {
        this.distanceDelta = distanceDelta;
    }

    public float getCameraRadius() {
        return cameraRadius;
    }

    public void setCameraRadius(float cameraRadius) {
        this.cameraRadius = cameraRadius;
    }

    public float getHorizontalAngle() {
        return horizontalAngle;
    }

    public void setHorizontalAngle(float horizontalAngle) {
        this.horizontalAngle = horizontalAngle;
    }

    public float getVerticalAngle() {
        return verticalAngle;
    }

    public void setVerticalAngle(float verticalAngle) {
        this.verticalAngle = verticalAngle;
    }

    public void addCollisionCheckObject(PhysicsCollisionObject pco) {
        cameraPhysicsSpace.addCollisionObject(pco);
    }

    public void removeCollisionCheckObject(PhysicsCollisionObject pco) {
        cameraPhysicsSpace.removeCollisionObject(pco);
    }

    @Override
    public void collision(PhysicsCollisionEvent event) {
        cameraCollides = true;
    }

    public void update() {
        Matrix4fc transformationMat = trackedComponent.getMat();
        var center = transformationMat.transformPosition(new Vector3f(0.0f));

        var xzAxis = new Vector3f(1.0f, 0.0f, 0.0f).rotateY(horizontalAngle);
        var rotAxis = new Vector3f(xzAxis).cross(new Vector3f(0.0f, 1.0f, 0.0f));

        float currentDistance = minDistance;
        while (true) {
            if (currentDistance > desiredDistance) {
                currentDistance = desiredDistance;
                break;
            }

            var vecToCamera = xzAxis.rotateAxis(verticalAngle, rotAxis.x, rotAxis.y, rotAxis.z).mul(currentDistance);
            var cameraPosition = new Vector3f(center).add(vecToCamera);

            var cameraSphere = new MttPhysicsSphere(cameraRadius, 0.0f);
            cameraSphere.setLocation(cameraPosition);

            cameraPhysicsSpace.contactTest(cameraSphere.getBody(), this);
            if (cameraCollides) {
                break;
            }

            currentDistance += distanceDelta;
        }

        var vecToCamera = xzAxis.rotateAxis(verticalAngle, rotAxis.x, rotAxis.y, rotAxis.z).mul(currentDistance);
        var cameraPosition = new Vector3f(center).add(vecToCamera);

        camera.setEye(cameraPosition);
        camera.setCenter(center);

        cameraCollides = false;
    }
}
