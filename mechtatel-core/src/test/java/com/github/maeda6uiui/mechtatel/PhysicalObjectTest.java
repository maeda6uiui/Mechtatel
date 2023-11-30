package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.*;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.physics.PhysicalObjectSet;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Random;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJOMLVector3fToJMEVector3f;

public class PhysicalObjectTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(PhysicalObjectTest.class);

    public PhysicalObjectTest(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        PhysicalObjectTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen mainScreen;

    private MttModel plane;
    private MttModel srcCube;
    private PhysicalObjectSet physicalObjectSet;

    private FreeCamera camera;

    private Random random;
    private int screenshotCount;

    @Override
    public void init(MttWindow window) {
        var mainScreenCreator = new ScreenCreator(window, "main");
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

        var drawPath = new DrawPath(window);
        drawPath.addToScreenDrawOrder("main");
        drawPath.setPresentScreenName("main");
        drawPath.apply();

        try {
            plane = window.createModel(
                    "main",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Plane/plane.obj"))
            );

            srcCube = window.createModel(
                    "main",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj"))
            );
            srcCube.setVisible(false);
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        var physicalPlane = window.createPhysicalMesh(plane, 0.0f);
        physicalPlane.getBody().setRestitution(0.7f);
        physicalPlane.getBody().setFriction(0.5f);

        physicalObjectSet = new PhysicalObjectSet();

        camera = new FreeCamera(mainScreen.getCamera());

        random = new Random();
        screenshotCount = 0;
    }

    @Override
    public void update(MttWindow window) {
        camera.translate(
                window.getKeyboardPressingCount("W"),
                window.getKeyboardPressingCount("S"),
                window.getKeyboardPressingCount("A"),
                window.getKeyboardPressingCount("D")
        );
        camera.rotate(
                window.getKeyboardPressingCount("UP"),
                window.getKeyboardPressingCount("DOWN"),
                window.getKeyboardPressingCount("LEFT"),
                window.getKeyboardPressingCount("RIGHT")
        );

        float x = random.nextFloat();
        float z = random.nextFloat();
        int signX = (random.nextInt() % 2 == 0) ? 1 : -1;
        int signZ = (random.nextInt() % 2 == 0) ? 1 : -1;
        x *= signX;
        z *= signZ;

        if (window.getKeyboardPressingCount("1") == 1) {
            var dupCube = window.duplicateModel(srcCube);
            dupCube.rescale(new Vector3f(0.5f, 0.5f, 0.5f));

            var physicalCube = window.createPhysicalBox(0.5f, 1.0f);
            physicalCube.setComponent(dupCube);
            physicalCube.getBody().setRestitution(0.7f);
            physicalCube.getBody().setFriction(0.5f);

            physicalCube.getBody().setPhysicsLocation(
                    convertJOMLVector3fToJMEVector3f(new Vector3f(x, 10.0f, z)));

            physicalObjectSet.add(physicalCube);
        }

        if (window.getKeyboardPressingCount("C") == 1) {
            physicalObjectSet.cleanup();
        }
    }

    @Override
    public void postPresent(MttWindow window) {
        if (window.getKeyboardPressingCount("F1") == 1) {
            try {
                window.saveScreenshot(
                        "main",
                        "bgra",
                        String.format("screenshot_%d.jpg", screenshotCount));
                screenshotCount++;
            } catch (IOException e) {
                logger.error("Error", e);
                window.close();
            }
        }
    }
}
