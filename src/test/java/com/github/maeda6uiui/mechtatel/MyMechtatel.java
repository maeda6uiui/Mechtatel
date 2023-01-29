package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.Model3D;
import com.github.maeda6uiui.mechtatel.core.physics.PhysicalObject3D;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJOMLVector3fToJMEVector3f;

public class MyMechtatel extends Mechtatel {
    public MyMechtatel(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings settings;
        //Load settings from a JSON file
        try {
            settings = new MttSettings("./Mechtatel/Setting/settings.json");
        }
        //If the program fails to load the JSON file, then use the default settings
        catch (IOException e) {
            settings = new MttSettings();
        }

        new MyMechtatel(settings);
    }

    private FreeCamera camera;
    private Model3D cube;

    @Override
    public void init() {
        camera = new FreeCamera(this.getCamera());

        var naborNames = new ArrayList<String>();
        naborNames.add("spotlight");
        naborNames.add("shadow_mapping");
        this.createPostProcessingNabors(naborNames);
        
        this.setSpotlightAmbientColor(new Vector3f(0.0f, 0.0f, 0.0f));

        var spotlightR = this.createSpotlight();
        spotlightR.setDiffuseColor(new Vector3f(1.0f, 0.0f, 0.0f));
        spotlightR.setPosition(new Vector3f(-10.0f, 10.0f, -10.0f));
        spotlightR.setDirection(new Vector3f(-10.0f, 10.0f, -10.0f).mul(-1.0f));

        var spotlightG = this.createSpotlight();
        spotlightG.setDiffuseColor(new Vector3f(0.0f, 1.0f, 0.0f));
        spotlightG.setPosition(new Vector3f(10.0f, 10.0f, -10.0f));
        spotlightG.setDirection(new Vector3f(10.0f, 10.0f, -10.0f).mul(-1.0f));

        var ground = this.createModel3D("./Mechtatel/Model/Plane/plane.obj");
        var physicalGround = this.createPhysicalBox3D(10.0f, 0.01f, 10.0f, 0.0f);
        physicalGround.setComponent(ground);
        physicalGround.getBody().setRestitution(0.8f);
        physicalGround.getBody().setRollingFriction(0.5f);
        physicalGround.getBody().setSpinningFriction(0.5f);

        cube = this.createModel3D("./Mechtatel/Model/Cube/cube.obj");
        cube.setVisible(false);
    }

    @Override
    public void dispose() {

    }

    @Override
    public void reshape(int width, int height) {

    }

    @Override
    public void update() {
        int keyFront = this.getKeyboardPressingCount("W");
        int keyBack = this.getKeyboardPressingCount("S");
        int keyLeft = this.getKeyboardPressingCount("A");
        int keyRight = this.getKeyboardPressingCount("D");
        camera.translate(keyFront, keyBack, keyLeft, keyRight);

        int keyRotateTop = this.getKeyboardPressingCount("UP");
        int keyRotateBottom = this.getKeyboardPressingCount("DOWN");
        int keyRotateLeft = this.getKeyboardPressingCount("LEFT");
        int keyRotateRight = this.getKeyboardPressingCount("RIGHT");
        camera.rotate(keyRotateTop, keyRotateBottom, keyRotateLeft, keyRotateRight);

        if (this.getKeyboardPressingCount("ENTER") == 1) {
            var random = new Random();

            int xSign = random.nextInt() % 2 == 0 ? 1 : -1;
            int zSign = random.nextInt() % 2 == 0 ? 1 : -1;
            float x = random.nextFloat(0.2f) * xSign;
            float z = random.nextFloat(0.2f) * zSign;

            PhysicalObject3D physicalObject = null;
            if (this.getKeyboardPressingCount("1") > 0) {
                physicalObject = this.createPhysicalSphere3DWithComponent(
                        1.0f, 1.0f, 16, 16, new Vector4f(1.0f, 0.0f, 0.0f, 1.0f));
            }
            if (this.getKeyboardPressingCount("2") > 0) {
                physicalObject = this.createPhysicalCapsule3DWithComponent(
                        1.0f, 2.0f, 1.0f, 16, 16, new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
            }
            if (this.getKeyboardPressingCount("3") > 0) {
                physicalObject = this.createPhysicalBox3DWithComponent(
                        1.0f, 1.0f, new Vector4f(0.0f, 0.0f, 1.0f, 1.0f));
            }
            if (this.getKeyboardPressingCount("4") > 0) {
                var dupCube = this.duplicateModel3D(cube);
                physicalObject = this.createPhysicalBox3D(1.0f, 1.0f);
                physicalObject.setComponent(dupCube);
            }

            if (physicalObject != null) {
                physicalObject.getBody().setPhysicsLocation(convertJOMLVector3fToJMEVector3f(new Vector3f(x, 10.0f, z)));
                physicalObject.getBody().setRestitution(0.8f);
                physicalObject.getBody().setRollingFriction(0.5f);
                physicalObject.getBody().setSpinningFriction(0.5f);
            }
        }
    }
}
