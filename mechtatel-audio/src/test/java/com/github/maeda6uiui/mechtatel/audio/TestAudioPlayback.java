package com.github.maeda6uiui.mechtatel.audio;

import java.io.FileNotFoundException;

public class TestAudioPlayback {
    public static void main(String[] args) {
        MttAudio audio;
        try {
            audio = new MttAudio("./Mechtatel/Standard/Audio/op_8.mp3");
        } catch (FileNotFoundException e) {
            System.err.println(e);
            return;
        }

        audio.play();

        while (true) {
            if (audio.isFinished()) {
                break;
            }
            System.out.printf("%f s\n", audio.getPos() / 1000.0);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println(e);
                break;
            }
        }
    }
}
