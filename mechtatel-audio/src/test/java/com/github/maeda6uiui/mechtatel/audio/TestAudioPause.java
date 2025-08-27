package com.github.maeda6uiui.mechtatel.audio;

import java.io.FileNotFoundException;

public class TestAudioPause {
    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        MttAudio sound;
        try {
            sound = new MttAudio("./Mechtatel/Standard/Audio/op_8.mp3");
        } catch (FileNotFoundException e) {
            System.err.println(e);
            return;
        }

        sound.play();
        System.out.printf("isPaused: %b\n", sound.isPaused());
        sleep(10000);

        sound.pause();
        System.out.printf("isPaused: %b\n", sound.isPaused());
        sleep(3000);

        sound.play();
        System.out.printf("isPaused: %b\n", sound.isPaused());
        sleep(10000);

        sound.stop();
    }
}
