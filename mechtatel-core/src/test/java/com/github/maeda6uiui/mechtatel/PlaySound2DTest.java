package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.sound.MttSound2D;
import com.goxr3plus.streamplayer.stream.StreamPlayerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class PlaySound2DTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(PlaySound2DTest.class);

    public PlaySound2DTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        PlaySound2DTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttSound2D sound;

    @Override
    public void onInit(MttWindow initialWindow) {
        try {
            sound = new MttSound2D(
                    Objects.requireNonNull(
                            this.getClass().getResource("/Standard/Sound/no_1_stereo.ogg")
                    )
            );
            sound.play();
        } catch (StreamPlayerException e) {
            logger.error("Error", e);
            initialWindow.close();
        }
    }

    @Override
    public void onUpdate(MttWindow window) {
        if (window.getKeyboardPressingCount(KeyCode.KEY_1) == 1) {
            sound.pause();
        } else if (window.getKeyboardPressingCount(KeyCode.KEY_2) == 1) {
            sound.resume();
        } else if (window.getKeyboardPressingCount(KeyCode.KEY_3) == 1) {
            sound.stop();
        }
    }
}
