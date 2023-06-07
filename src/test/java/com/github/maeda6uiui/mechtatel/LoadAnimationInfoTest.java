package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.animation.RawAnimationInfo;

import java.io.IOException;

public class LoadAnimationInfoTest {
    public static void main(String[] args) {
        try {
            RawAnimationInfo animInfo = RawAnimationInfo.load(
                    "./Mechtatel/Standard/Model/Cube/sample_animations.json");

            System.out.printf("name=%s\n", animInfo.name);
            System.out.println("models=");
            animInfo.models.forEach(m -> System.out.println(m.name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
