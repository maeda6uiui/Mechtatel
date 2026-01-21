package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.audio.MttAudio;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

public class AudioPlayerTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(AudioPlayerTest.class);

    public AudioPlayerTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        AudioPlayerTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttAudio audio;

    @Override
    public void onCreate(MttWindow window) {
        try {
            audio = new MttAudio("./Mechtatel/Standard/Audio/op_24.flac");
        } catch (FileNotFoundException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        audio.play();
    }

    @Override
    public void onUpdate(MttWindow window) {
        if (window.getKeyboardPressingCount(KeyCode.F1) == 1) {
            audio.play();
        } else if (window.getKeyboardPressingCount(KeyCode.F2) == 1) {
            audio.pause();
        }

        if (audio.isFinished()) {
            window.close();
        }
    }
}
