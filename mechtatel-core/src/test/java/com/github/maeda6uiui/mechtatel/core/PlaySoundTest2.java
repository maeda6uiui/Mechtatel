package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.sound.MttSound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

public class PlaySoundTest2 extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(PlaySoundTest2.class);

    public PlaySoundTest2(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        PlaySoundTest2::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttSound sound;

    @Override
    public void onCreate(MttWindow window) {
        try {
            sound = window.createSound(
                    Paths.get("./Mechtatel/Standard/Audio/op_1.ogg"), false, false
            );
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        sound.getSoundSource().play();
    }

    @Override
    public void onUpdate(MttWindow window) {
        if (window.getKeyboardPressingCount(KeyCode.F1) == 1) {
            sound.getSoundSource().pause();
        } else if (window.getKeyboardPressingCount(KeyCode.F2) == 1) {
            sound.getSoundSource().play();
        } else if (window.getKeyboardPressingCount(KeyCode.F3) == 1) {
            sound.getSoundSource().stop();
        }
    }
}
