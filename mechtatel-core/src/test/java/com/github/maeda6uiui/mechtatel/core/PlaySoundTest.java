package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.sound.ISoundSource;
import com.github.maeda6uiui.mechtatel.core.sound.MttSound;
import com.github.maeda6uiui.mechtatel.core.sound.SoundListener;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

import static org.lwjgl.openal.AL10.AL_REFERENCE_DISTANCE;

public class PlaySoundTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(PlaySoundTest.class);

    public PlaySoundTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        PlaySoundTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttSound sound;
    private FreeCamera freeCamera;

    @Override
    public void onCreate(MttWindow window) {
        try {
            sound = window.createSound(
                    Paths.get("./Mechtatel/Standard/Sound/440hz_sine.ogg"),
                    true,
                    false
            );
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        ISoundSource soundSource = sound.getSoundSource();
        soundSource.setPosition(new Vector3f(0.0f));
        soundSource.setParameter(AL_REFERENCE_DISTANCE, 5.0f);

        MttScreen defaultScreen = window.getDefaultScreen();
        Camera camera = defaultScreen.getCamera();
        camera.setEye(new Vector3f(5.0f, 5.0f, 5.0f));
        freeCamera = new FreeCamera(camera);

        SoundListener.setPosition(camera.getEye());
        SoundListener.setOrientation(camera.getCenter(), camera.getUp());

        soundSource.play();

        defaultScreen.createSphere(1.0f, 16, 16, new Vector4f(1.0f), false);
        defaultScreen.createLineSet().addPositiveAxes(10.0f).createBuffer();
    }

    @Override
    public void onUpdate(MttWindow window) {
        freeCamera.translate(
                window.getKeyboardPressingCount(KeyCode.W),
                window.getKeyboardPressingCount(KeyCode.S),
                window.getKeyboardPressingCount(KeyCode.A),
                window.getKeyboardPressingCount(KeyCode.D)
        );
        freeCamera.rotate(
                window.getKeyboardPressingCount(KeyCode.UP),
                window.getKeyboardPressingCount(KeyCode.DOWN),
                window.getKeyboardPressingCount(KeyCode.LEFT),
                window.getKeyboardPressingCount(KeyCode.RIGHT)
        );

        MttScreen defaultScreen = window.getDefaultScreen();
        Camera camera = defaultScreen.getCamera();
        SoundListener.setPosition(camera.getEye());
        SoundListener.setOrientation(camera.getCenter(), camera.getUp());

        if (window.getKeyboardPressingCount(KeyCode.F1) == 1) {
            sound.getSoundSource().pause();
        } else if (window.getKeyboardPressingCount(KeyCode.F2) == 1) {
            sound.getSoundSource().play();
        } else if (window.getKeyboardPressingCount(KeyCode.F3) == 1) {
            sound.getSoundSource().stop();
        }

        defaultScreen.draw();
        window.present(defaultScreen);
    }
}
