package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.camera.VaryingDistanceTrackingCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.input.mouse.MouseCode;
import com.github.maeda6uiui.mechtatel.core.physics.MttPhysicsMesh;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttSphere;
import com.jme3.bullet.objects.PhysicsRigidBody;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;

public class VaryingDistanceTrackingCameraTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(VaryingDistanceTrackingCameraTest.class);

    public VaryingDistanceTrackingCameraTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        VaryingDistanceTrackingCameraTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttSphere sphere;
    private Vector3f spherePos;
    private float sphereTranslationSpeed;

    private VaryingDistanceTrackingCamera camera;
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

        sphere = defaultScreen.createSphere(
                new Vector3f(0.0f), 1.0f, 16, 16, new Vector4f(1.0f));
        spherePos = new Vector3f(5.0f, 1.0f, 5.0f);
        sphere.translate(spherePos);

        sphereTranslationSpeed = 0.05f;

        camera = new VaryingDistanceTrackingCamera(defaultScreen.getCamera(), sphere);
        camera.setDesiredDistance(5.0f);
        camera.setMinDistance(1.5f);
        camera.setDistanceDelta(0.1f);
        horizontalAngle = 0.0f;
        verticalAngle = (float) Math.toRadians(45.0f);
        rotationDelta = 0.01f;
        minRotateV = (float) Math.toRadians(-45.0f);
        maxRotateV = (float) Math.toRadians(80.0f);

        prevCursorPosX = window.getCursorPosX();
        prevCursorPosY = window.getCursorPosY();

        MttModel level;
        try {
            level = defaultScreen.createModel(Paths.get("./Mechtatel/Standard/Model/Playground/playground.bd1"));
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        var phLevel = new MttPhysicsMesh(level, 0.0f);
        camera.addCollisionCheckObject(phLevel.getBody());

        //Suppress INFO logs from PhysicsRigidBody
        PhysicsRigidBody.logger2.setLevel(Level.WARNING);
    }

    @Override
    public void onUpdate(MttWindow window) {
        //Move sphere
        Camera innerCamera = camera.getCamera();
        Vector3f cameraPos = innerCamera.getEye();
        Vector3f cameraTarget = innerCamera.getCenter();
        Vector3f cameraDirection = new Vector3f(cameraTarget).sub(cameraPos);
        cameraDirection.y = 0.0f;
        cameraDirection = cameraDirection.normalize();
        var rightVec = new Vector3f(cameraDirection).cross(new Vector3f(0.0f, 1.0f, 0.0f));

        var sphereTranslation = new Vector3f(0.0f);
        if (window.getKeyboardPressingCount(KeyCode.W) > 0) {
            sphereTranslation = sphereTranslation.add(cameraDirection);
        }
        if (window.getKeyboardPressingCount(KeyCode.S) > 0) {
            sphereTranslation = sphereTranslation.add(new Vector3f(cameraDirection).mul(-1.0f));
        }
        if (window.getKeyboardPressingCount(KeyCode.D) > 0) {
            sphereTranslation = sphereTranslation.add(rightVec);
        }
        if (window.getKeyboardPressingCount(KeyCode.A) > 0) {
            sphereTranslation = sphereTranslation.add(new Vector3f(rightVec).mul(-1.0f));
        }

        sphereTranslation = sphereTranslation.mul(sphereTranslationSpeed);

        spherePos = spherePos.add(sphereTranslation);
        var transformMat = new Matrix4f().translate(spherePos);
        sphere.setMat(transformMat);

        //Rotate camera
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

        //Draw
        MttScreen defaultScreen = window.getDefaultScreen();
        defaultScreen.draw();
        window.present(defaultScreen);
    }
}
