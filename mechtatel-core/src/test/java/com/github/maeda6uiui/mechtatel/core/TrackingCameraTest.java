package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.camera.TrackingCamera;
import com.github.maeda6uiui.mechtatel.core.input.mouse.MouseCode;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttSphere;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrackingCameraTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(TrackingCameraTest.class);

    public TrackingCameraTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        TrackingCameraTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttSphere sphere;
    private Vector3f spherePos;

    private TrackingCamera camera;
    private float horizontalAngle;
    private float verticalAngle;
    private float rotationDelta;
    private float minRotateV;
    private float maxRotateV;

    private double prevCursorPosX;
    private double prevCursorPosY;

    @Override
    public void onCreate(MttWindow window) {
        MttScreen defaultScreen = window.getDefaultScreen();

        defaultScreen.createLineSet().addPositiveAxes(10.0f).createBuffer();

        sphere = defaultScreen.createSphere(1.0f, 16, 16, new Vector4f(1.0f), false);
        spherePos = new Vector3f(5.0f, 0.0f, 0.0f);
        sphere.translate(spherePos);

        camera = new TrackingCamera(defaultScreen.getCamera(), sphere);
        camera.setDistance(5.0f);
        horizontalAngle = 0.0f;
        verticalAngle = 0.0f;
        rotationDelta = 0.01f;
        minRotateV = (float) Math.toRadians(-45.0f);
        maxRotateV = (float) Math.toRadians(80.0f);

        prevCursorPosX = window.getCursorPosX();
        prevCursorPosY = window.getCursorPosY();
    }

    @Override
    public void onUpdate(MttWindow window) {
        spherePos = new Matrix4f().rotateY(0.005f).transformPosition(spherePos);
        var transformMat = new Matrix4f().translate(spherePos);
        sphere.setMat(transformMat);

        double cursorPosX = window.getCursorPosX();
        double cursorPosY = window.getCursorPosY();

        if (window.getMousePressingCount(MouseCode.LEFT) > 0) {
            double diffX = cursorPosX - prevCursorPosX;
            double diffY = cursorPosY - prevCursorPosY;

            horizontalAngle -= (float) diffX * rotationDelta;
            if (horizontalAngle > Math.PI) {
                horizontalAngle -= (float) Math.PI * 2.0f;
            } else if (horizontalAngle < -Math.PI) {
                horizontalAngle += (float) Math.PI * 2.0f;
            }

            verticalAngle += (float) diffY * rotationDelta;
            if (verticalAngle < minRotateV) {
                verticalAngle = minRotateV;
            } else if (verticalAngle > maxRotateV) {
                verticalAngle = maxRotateV;
            }

            camera.setHorizontalAngle(horizontalAngle);
            camera.setVerticalAngle(verticalAngle);
        }

        camera.update();

        prevCursorPosX = cursorPosX;
        prevCursorPosY = cursorPosY;

        MttScreen defaultScreen = window.getDefaultScreen();
        defaultScreen.draw();
        window.present(defaultScreen);
    }
}
