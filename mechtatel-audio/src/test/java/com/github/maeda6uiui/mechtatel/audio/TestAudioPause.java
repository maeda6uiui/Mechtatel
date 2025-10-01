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
        MttAudio audio;
        try {
            audio = new MttAudio("./Mechtatel/Standard/Audio/op_8.mp3");
        } catch (FileNotFoundException e) {
            System.err.println(e);
            return;
        }

        audio.play();
        System.out.printf("isPaused: %b\n", audio.isPaused());
        sleep(10000);

        audio.pause();
        System.out.printf("isPaused: %b\n", audio.isPaused());
        sleep(3000);

        audio.play();
        System.out.printf("isPaused: %b\n", audio.isPaused());
        sleep(10000);

        audio.stop();
    }
}
