package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.physics.PhysicalBox;
import com.github.maeda6uiui.mechtatel.core.physics.PhysicalMesh;
import com.github.maeda6uiui.mechtatel.core.physics.PhysicalObject;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import com.jme3.bullet.objects.PhysicsBody;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class PhysicalObjectTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(PhysicalObjectTest.class);

    public PhysicalObjectTest(MttSettings settings) {
        super(settings);
        this.run();
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
    private MttModel level;
    private MttModel box;
    private FreeCamera camera;

    private Random random;
    private int screenshotCount;

    private List<PhysicalObject> physicalObjects;

    @Override
    public void init(MttWindow window) {
        mainScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setUseShadowMapping(true)
                        .setPpNaborNames(Arrays.asList("spotlight", "fog"))
        );
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

        try {
            level = mainScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Plane/plane.obj"))
            );

            box = mainScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj"))
            );
            box.setVisible(false);
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        var phLevel = new PhysicalMesh(level, PhysicsBody.massForStatic);
        phLevel.getBody().setRestitution(0.7f);
        phLevel.getBody().setFriction(0.5f);

        camera = new FreeCamera(mainScreen.getCamera());

        random = new Random();
        screenshotCount = 0;

        physicalObjects = new ArrayList<>();
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
            var dupBox = mainScreen.duplicateModel(box);
            dupBox.rescale(new Vector3f(0.5f, 0.5f, 0.5f));

            var phBox = new PhysicalBox(0.5f, 1.0f);
            phBox.getBody().setRestitution(0.7f);
            phBox.getBody().setFriction(0.5f);
            phBox.setLocation(new Vector3f(x, 10.0f, z));
            phBox.addComponent(dupBox);
            physicalObjects.add(phBox);
        }

        if (window.getKeyboardPressingCount("C") == 1) {
            physicalObjects.forEach(PhysicalObject::cleanup);
            physicalObjects.clear();
        }
        physicalObjects.forEach(PhysicalObject::syncComponents);

        mainScreen.draw();
        window.present(mainScreen);

        if (window.getKeyboardPressingCount("F1") == 1) {
            try {
                mainScreen.save(
                        ScreenImageType.COLOR,
                        PixelFormat.BGRA,
                        Paths.get(String.format("screenshot_%d.png", screenshotCount))
                );
                screenshotCount++;
            } catch (IOException e) {
                logger.error("Error", e);
                window.close();
            }
        }
    }
}
