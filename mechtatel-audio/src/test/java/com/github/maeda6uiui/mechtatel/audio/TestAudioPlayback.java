package com.github.maeda6uiui.mechtatel.audio;

import java.io.FileNotFoundException;

public class TestAudioPlayback {
    public static void main(String[] args) {
        MttAudio sound;
        try {
            sound = new MttAudio("./Data/Op23.mp3");
        } catch (FileNotFoundException e) {
            System.err.println(e);
            return;
        }

        sound.play();

        while (true) {
            if (sound.isFinished()) {
                break;
            }
            System.out.printf("%f s\n", sound.getPos() / 1000.0);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println(e);
                break;
            }
        }
    }
}
