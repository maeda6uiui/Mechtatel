package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.DrawPath;
import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.ScreenCreator;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.MttModel3D;
import com.github.maeda6uiui.mechtatel.core.physics.PhysicalObjectSet3D;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.Random;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJOMLVector3fToJMEVector3f;

public class PhysicalObjectSample extends Mechtatel {
    public PhysicalObjectSample(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings settings;
        try {
            settings = new MttSettings("./Mechtatel/Setting/settings.json");
        } catch (IOException e) {
            System.out.println("Failed to load setting file. Use default settings");
            settings = new MttSettings();
        }

        new PhysicalObjectSample(settings);
    }

    private MttScreen mainScreen;

    private MttModel3D plane;
    private MttModel3D srcCube;
    private PhysicalObjectSet3D physicalObjects;

    private FreeCamera camera;

    private Random random;
    private int screenshotCount;

    @Override
    public void init() {
        var mainScreenCreator = new ScreenCreator(this, "main");
        mainScreenCreator.addPostProcessingNabor("spotlight");
        mainScreenCreator.addPostProcessingNabor("fog");
        mainScreenCreator.setUseShadowMapping(true);
        mainScreen = mainScreenCreator.create();

        mainScreen.getFog().setStart(10.0f);
        mainScreen.getFog().setEnd(20.0f);

        mainScreen.getShadowMappingSettings().setBiasCoefficient(0.003f);

        mainScreen.setSpotlightAmbientColor(new Vector3f(0.0f, 0.0f, 0.0f));

        var spotlightR = mainScreen.createSpotlight();
        spotlightR.setPosition(new Vector3f(10.0f, 10.0f, 10.0f));
        spotlightR.setDirection(new Vector3f(-1.0f, -1.0f, -1.0f));
        spotlightR.setDiffuseColor(new Vector3f(1.0f, 0.0f, 0.0f));

        var spotlightG = mainScreen.createSpotlight();
        spotlightG.setPosition(new Vector3f(10.0f, 10.0f, -10.0f));
        spotlightG.setDirection(new Vector3f(-1.0f, -1.0f, 1.0f));
        spotlightG.setDiffuseColor(new Vector3f(0.0f, 1.0f, 0.0f));

        var spotlightB = mainScreen.createSpotlight();
        spotlightB.setPosition(new Vector3f(-10.0f, 10.0f, 0.0f));
        spotlightB.setDirection(new Vector3f(1.0f, -1.0f, 0.0f));
        spotlightB.setDiffuseColor(new Vector3f(0.0f, 0.0f, 1.0f));

        var drawPath = new DrawPath(this);
        drawPath.addToScreenDrawOrder("main");
        drawPath.setPresentScreenName("main");
        drawPath.apply();

        try {
            plane = this.createModel3D("main", "./Mechtatel/Standard/Model/Plane/plane.obj");

            srcCube = this.createModel3D("main", "./Mechtatel/Standard/Model/Cube/cube.obj");
            srcCube.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        var physicalPlane = this.createPhysicalMesh3D(plane, 0.0f);
        physicalPlane.getBody().setRestitution(0.7f);
        physicalPlane.getBody().setFriction(0.5f);

        physicalObjects = new PhysicalObjectSet3D();

        camera = new FreeCamera(mainScreen.getCamera());

        random = new Random();
        screenshotCount = 0;
    }

    @Override
    public void update() {
        camera.translate(
                this.getKeyboardPressingCount("W"),
                this.getKeyboardPressingCount("S"),
                this.getKeyboardPressingCount("A"),
                this.getKeyboardPressingCount("D")
        );
        camera.rotate(
                this.getKeyboardPressingCount("UP"),
                this.getKeyboardPressingCount("DOWN"),
                this.getKeyboardPressingCount("LEFT"),
                this.getKeyboardPressingCount("RIGHT")
        );

        float x = random.nextFloat();
        float z = random.nextFloat();
        int signX = (random.nextInt() % 2 == 0) ? 1 : -1;
        int signZ = (random.nextInt() % 2 == 0) ? 1 : -1;
        x *= signX;
        z *= signZ;

        if (this.getKeyboardPressingCount("1") == 1) {
            var dupCube = this.duplicateModel3D(srcCube);
            dupCube.rescale(new Vector3f(0.5f, 0.5f, 0.5f));

            var physicalCube = this.createPhysicalBox3D(0.5f, 1.0f);
            physicalCube.setComponent(dupCube);
            physicalCube.getBody().setRestitution(0.7f);
            physicalCube.getBody().setFriction(0.5f);

            physicalCube.getBody().setPhysicsLocation(
                    convertJOMLVector3fToJMEVector3f(new Vector3f(x, 10.0f, z)));

            physicalObjects.add(physicalCube);
        }

        if (this.getKeyboardPressingCount("C") == 1) {
            physicalObjects.cleanup();
        }
    }

    @Override
    public void postPresent() {
        if (this.getKeyboardPressingCount("F1") == 1) {
            try {
                this.saveScreenshot(
                        "main",
                        "bgra",
                        String.format("screenshot_%d.jpg", screenshotCount));
                screenshotCount++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
