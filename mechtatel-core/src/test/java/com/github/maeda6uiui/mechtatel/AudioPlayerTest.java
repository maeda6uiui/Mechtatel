package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.audio.AudioPlayer;
import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
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

    private AudioPlayer AudioPlayer;

    @Override
    public void onCreate(MttWindow window) {
        try {
            AudioPlayer = new AudioPlayer("./Mechtatel/Standard/AudioPlayer/Op24.mp3");
        } catch (FileNotFoundException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        AudioPlayer.play();
    }

    @Override
    public void onUpdate(MttWindow window) {
        if (window.getKeyboardPressingCount(KeyCode.F1) == 1) {
            AudioPlayer.play();
        } else if (window.getKeyboardPressingCount(KeyCode.F2) == 1) {
            AudioPlayer.pause();
        }

        if (AudioPlayer.isFinished()) {
            window.close();
        }
    }
}
