package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.libsoundplayer.core.Sound;
import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

public class SoundPlayerTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(SoundPlayerTest.class);

    public SoundPlayerTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        SoundPlayerTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private Sound sound;

    @Override
    public void onCreate(MttWindow window) {
        try {
            sound = new Sound("./Mechtatel/Standard/Sound/Op24.mp3");
        } catch (FileNotFoundException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        sound.play();
    }

    @Override
    public void onUpdate(MttWindow window) {
        if (window.getKeyboardPressingCount(KeyCode.F1) == 1) {
            sound.play();
        } else if (window.getKeyboardPressingCount(KeyCode.F2) == 1) {
            sound.pause();
        }

        if (sound.isFinished()) {
            window.close();
        }
    }
}
