package com.github.maeda6uiui.mechtatel.audio;

import java.io.FileNotFoundException;

public class TestAudioSeek {
    public static void main(String[] args) {
        MttAudio sound;
        try {
            sound = new MttAudio("./Data/Op23.mp3");
        } catch (FileNotFoundException e) {
            System.err.println(e);
            return;
        }

        sound.seek(10000);
        System.out.printf("Current position: %f s\n", sound.getPos() / 1000.0);

        sound.play();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }
}
