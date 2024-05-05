package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.model.MttAnimationData;
import com.github.maeda6uiui.mechtatel.core.model.MttModelData;
import com.github.maeda6uiui.mechtatel.core.model.helper.AnimationPlayMode;
import com.github.maeda6uiui.mechtatel.core.model.helper.AnimationUpdater;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class SkeletalAnimationTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(SkeletalAnimationTest.class);

    public SkeletalAnimationTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        SkeletalAnimationTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private FreeCamera camera;
    private MttModel animModel;
    private AnimationUpdater animUpdater;

    @Override
    public void onCreate(MttWindow window) {
        MttScreen defaultScreen = window.getDefaultScreen();
        defaultScreen.getCamera().setEye(new Vector3f(2.0f, 2.0f, 2.0f));
        camera = new FreeCamera(defaultScreen.getCamera());

        defaultScreen.createLineSet().addPositiveAxes(2.0f).createBuffer();

        try {
            animModel = defaultScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cz1/cz_1.dae"))
            );
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        //Attach first animation to the model
        MttModelData.Animation animation = animModel.getModelData().animationList.get(0);
        var animationData = new MttAnimationData(animation, 0, 0);
        animModel.setAnimationData(animationData);

        //Create helper class to update animation
        animUpdater = new AnimationUpdater(
                animationData,
                AnimationPlayMode.REPEAT,
                1.0,
                this.getSecondsPerFrame()
        );
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

        MttScreen defaultScreen = window.getDefaultScreen();
        defaultScreen.draw();
        window.present(defaultScreen);

        animUpdater.update();
    }
}
