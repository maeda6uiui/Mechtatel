package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.sound.Sound2D;
import com.goxr3plus.streamplayer.stream.StreamPlayerException;

import java.io.IOException;


public class MyMechtatel extends Mechtatel {
    public MyMechtatel(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings settings;
        //Load settings from a JSON file
        try {
            settings = new MttSettings("./Mechtatel/Setting/settings.json");
        }
        //If the program fails to load the JSON file, then use the default settings
        catch (IOException e) {
            System.out.println("Failed to load setting file. Use default settings");
            settings = new MttSettings();
        }

        new MyMechtatel(settings);
    }

    private Sound2D sound;

    @Override
    public void init() {
        try {
            sound = new Sound2D("./Mechtatel/Sound/no_9.mp3");
        } catch (StreamPlayerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        sound.stop();
    }

    @Override
    public void reshape(int width, int height) {

    }

    @Override
    public void update() {
        if (this.getKeyboardPressingCount("ENTER") == 1) {
            try {
                sound.play();
            } catch (StreamPlayerException e) {
                e.printStackTrace();
            }
        } else if (this.getKeyboardPressingCount("P") == 1) {
            sound.pause();
        } else if (this.getKeyboardPressingCount("R") == 1) {
            sound.resume();
        } else if (this.getKeyboardPressingCount("S") == 1) {
            sound.stop();
        }
    }
}
