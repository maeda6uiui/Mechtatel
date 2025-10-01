package com.github.maeda6uiui.mechtatel.audio;

import java.io.FileNotFoundException;

public class TestAudioSeek {
    public static void main(String[] args) {
        MttAudio audio;
        try {
            audio = new MttAudio("./Mechtatel/Standard/Audio/op_8.mp3");
        } catch (FileNotFoundException e) {
            System.err.println(e);
            return;
        }

        audio.seek(10000);
        System.out.printf("Current position: %f s\n", audio.getPos() / 1000.0);

        audio.play();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.err.println(e);
        }
    }
}
