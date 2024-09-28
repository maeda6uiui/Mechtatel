package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.physics.MttDefaultPhysicsSpace;
import com.github.maeda6uiui.mechtatel.core.physics.MttPhysicsBox;
import com.github.maeda6uiui.mechtatel.core.physics.MttPhysicsMesh;
import com.github.maeda6uiui.mechtatel.core.physics.MttPhysicsObject;
import com.github.maeda6uiui.mechtatel.core.postprocessing.PostProcessingProperties;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttComponent;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PhysicsObjectTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(PhysicsObjectTest.class);

    public PhysicsObjectTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        PhysicsObjectTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttModel plane;
    private MttModel box;
    private List<MttPhysicsObject> physicsObjects;

    private MttScreen mainScreen;
    private FreeCamera camera;

    private Random random;

    @Override
    public void onCreate(MttWindow window) {
        mainScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setPostProcessingNaborNames(List.of("parallel_light"))
        );

        PostProcessingProperties ppProp = mainScreen.getPostProcessingProperties();
        ppProp.createParallelLight();

        try {
            plane = mainScreen.createModel(Paths.get("./Mechtatel/Standard/Model/Plane/plane.obj"));

            box = mainScreen.createModel(Paths.get("./Mechtatel/Standard/Model/Cube/cube.obj"));
            box.setVisible(false);
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        var phPlane = new MttPhysicsMesh(plane, 0.0f);
        phPlane.getBody().setRestitution(0.7f);
        phPlane.getBody().setFriction(0.5f);
        MttDefaultPhysicsSpace.get().ifPresent(v -> v.addCollisionObject(phPlane.getBody()));

        camera = new FreeCamera(mainScreen.getCamera());

        physicsObjects = new ArrayList<>();

        random = new Random();
    }

    @Override
    public void onUpdate(MttWindow window) {
        camera.translate(
                window.getKeyboardPressingCount(KeyCode.W),
                window.getKeyboardPressingCount(KeyCode.S),
                window.getKeyboardPressingCount(KeyCode.A),
                window.getKeyboardPressingCount(KeyCode.D)
        );
        camera.rotate(
                window.getKeyboardPressingCount(KeyCode.UP),
                window.getKeyboardPressingCount(KeyCode.DOWN),
                window.getKeyboardPressingCount(KeyCode.LEFT),
                window.getKeyboardPressingCount(KeyCode.RIGHT)
        );

        float x = random.nextFloat();
        float z = random.nextFloat();
        int signX = (random.nextInt() % 2 == 0) ? 1 : -1;
        int signZ = (random.nextInt() % 2 == 0) ? 1 : -1;
        x *= signX;
        z *= signZ;

        if (window.getKeyboardPressingCount(KeyCode.KEY_1) == 1) {
            var dupBox = mainScreen.duplicateModel(box);
            dupBox.rescale(new Vector3f(0.5f, 0.5f, 0.5f));

            var phBox = new MttPhysicsBox(0.5f, 1.0f);
            phBox.getBody().setRestitution(0.7f);
            phBox.getBody().setFriction(0.5f);
            phBox.setLocation(new Vector3f(x, 10.0f, z));
            phBox.addComponent(dupBox);
            MttDefaultPhysicsSpace.get().ifPresent(v -> v.addCollisionObject(phBox.getBody()));
            physicsObjects.add(phBox);
        }
        if (window.getKeyboardPressingCount(KeyCode.C) == 1) {
            for (var obj : physicsObjects) {
                MttDefaultPhysicsSpace.get().ifPresent(v -> v.removeCollisionObject(obj.getBody()));
                obj.getComponents().forEach(MttComponent::cleanup);
            }
            physicsObjects.clear();
        }

        physicsObjects.forEach(MttPhysicsObject::syncComponents);

        mainScreen.draw();
        window.present(mainScreen);
    }
}
