package com.github.maeda6uiui.mechtatel.core.camera;

import com.github.maeda6uiui.mechtatel.core.screen.component.MttComponent;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

/**
 * Camera that follows a target component
 *
 * @author maeda6uiui
 */
public class TrackingCamera {
    private Camera camera;

    private MttComponent trackedComponent;

    private float distance;
    private float horizontalAngle;
    private float verticalAngle;

    public TrackingCamera(Camera camera, MttComponent trackedComponent) {
        this.camera = camera;

        this.trackedComponent = trackedComponent;

        distance = 3.0f;
        horizontalAngle = 0.0f;
        verticalAngle = 0.0f;
    }

    public Camera getCamera() {
        return camera;
    }

    public MttComponent getTrackedComponent() {
        return trackedComponent;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public void setHorizontalAngle(float horizontalAngle) {
        this.horizontalAngle = horizontalAngle;
    }

    public void setVerticalAngle(float verticalAngle) {
        this.verticalAngle = verticalAngle;
    }

    public float getDistance() {
        return distance;
    }

    public float getHorizontalAngle() {
        return horizontalAngle;
    }

    public float getVerticalAngle() {
        return verticalAngle;
    }

    public void update() {
        Matrix4fc transformationMat = trackedComponent.getMat();
        var center = transformationMat.transformPosition(new Vector3f(0.0f));

        var xzAxis = new Vector3f(1.0f, 0.0f, 0.0f).rotateY(horizontalAngle);
        var rotAxis = new Vector3f(xzAxis).cross(new Vector3f(0.0f, 1.0f, 0.0f));
        var vecToCamera = xzAxis.rotateAxis(verticalAngle, rotAxis.x, rotAxis.y, rotAxis.z).mul(distance);
        var cameraPosition = new Vector3f(center).add(vecToCamera);

        camera.setEye(cameraPosition);
        camera.setCenter(center);
    }
}
