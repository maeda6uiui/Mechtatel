package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.animation.AnimationInfo;

import java.io.IOException;

public class LoadAnimationInfoTest {
    public static void main(String[] args) {
        try {
            AnimationInfo animInfo = AnimationInfo.load("./Mechtatel/Standard/Model/Cube/anim_sample.json");

            System.out.printf("name=%s\n", animInfo.name);
            System.out.println("models=");
            animInfo.models.forEach(m -> System.out.println(m.name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
