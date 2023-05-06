package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.IOException;

public class ScreenshotTest extends Mechtatel {
    public ScreenshotTest(MttSettings settings) {
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

        new ScreenshotTest(settings);
    }

    @Override
    public void init() {
        var sphere = this.createSphere3D(
                new Vector3f(0.0f, 0.0f, 0.0f),
                5.0f,
                16,
                16,
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)
        );
    }

    @Override
    public void dispose() {

    }

    @Override
    public void reshape(int width, int height) {

    }

    @Override
    public void update() {
        if (this.getKeyboardPressingCount("ENTER") == 1) {
            try {
                this.saveScreenshot("screenshot.jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
