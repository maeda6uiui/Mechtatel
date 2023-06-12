package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;

import java.io.IOException;

public class CallFromPythonTest extends Mechtatel {
    public CallFromPythonTest(MttSettings settings) {
        super(settings);
    }

    public String greeting() {
        return "Hello, world!";
    }

    public static void main(String[] args) {
        MttSettings settings;
        try {
            settings = MttSettings.load("./Mechtatel/Setting/settings.json");
            settings.systemSettings.runGatewayServer = true;
        } catch (IOException e) {
            System.out.println("Failed to load setting file. Use default settings");
            settings = new MttSettings();
        }

        new CallFromPythonTest(settings);
    }
}
