package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class MultipleWindowsTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(MultipleWindowsTest.class);

    public MultipleWindowsTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        MultipleWindowsTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    static class ModelProperties {
        public MttModel model;
        public Vector3f position;
        public Vector3f rotation;

        public ModelProperties() {
            position = new Vector3f();
            rotation = new Vector3f();
        }

        public void apply() {
            if (model == null) {
                return;
            }

            var mat = new Matrix4f().translate(position).rotateX(rotation.x).rotateY(rotation.y).rotateZ(rotation.z);
            model.setMat(mat);
        }
    }

    private static final float POSITION_UPDATE_DELTA = 0.01f;
    private static final float ROTATION_UPDATE_DELTA = 0.01f;

    private Map<String, ModelProperties> modelPropsMap;

    private void createResources(MttWindow window) throws URISyntaxException, IOException {
        MttScreen defaultScreen = window.getDefaultScreen();
        MttModel model = defaultScreen.createModel(
                Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj")));

        var random = new Random();
        var modelPosition = new Vector3f(4.0f, 0.0f, 0.0f);
        modelPosition.rotateY(random.nextFloat((float) Math.PI * 2.0f));

        var modelRotation = new Vector3f(
                random.nextFloat((float) Math.PI * 2.0f),
                random.nextFloat((float) Math.PI * 2.0f),
                random.nextFloat((float) Math.PI * 2.0f)
        );

        var modelProps = new ModelProperties();
        modelProps.model = model;
        modelProps.position = modelPosition;
        modelProps.rotation = modelRotation;
        modelProps.apply();
        modelPropsMap.put(window.getUniqueID(), modelProps);

        defaultScreen.createLineSet().addAxes(10.0f).createBuffer();
    }

    @Override
    public void onInit() {
        //Create windows
        MttSettings settings = MttSettings.get().orElse(new MttSettings());

        var windows = new ArrayList<MttWindow>();
        for (int i = 0; i < 3; i++) {
            var window = new MttWindow(this, settings, 640, 480, "Window " + i);
            windows.add(window);
        }
        windows.forEach(this::registerWindow);

        //Create resources
        modelPropsMap = new HashMap<>();
        try {
            this.createResources(this.getInitialWindow());
            for (var window : windows) {
                this.createResources(window);
            }
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            this.closeAllWindows();
        }
    }

    @Override
    public void onUpdate(MttWindow window) {
        modelPropsMap.values().forEach(modelProps -> {
            modelProps.position.rotateY(POSITION_UPDATE_DELTA);
            modelProps.rotation.add(new Vector3f(ROTATION_UPDATE_DELTA));
            modelProps.apply();
        });

        MttScreen defaultScreen = window.getDefaultScreen();
        defaultScreen.draw();
        window.present(defaultScreen);

        if (window.getKeyboardPressingCount(KeyCode.ESCAPE) == 1) {
            this.closeAllWindows();
        }
    }
}
