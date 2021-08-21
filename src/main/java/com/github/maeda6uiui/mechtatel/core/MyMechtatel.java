package com.github.maeda6uiui.mechtatel.core;

public class MyMechtatel extends Mechtatel {
    public MyMechtatel() {

    }

    @Override
    public void init() {
        System.out.println("init");
    }

    @Override
    public void dispose() {
        System.out.println("dispose");
    }

    @Override
    public void reshape(int width, int height) {
        /*
        String text = String.format("reshape: (%d, %d)", width, height);
        System.out.println(text);
         */
    }

    @Override
    public void update() {
        //System.out.println("update");
    }

    @Override
    public void draw() {
        //System.out.println("draw");
    }

    public static void main(String[] args) {
        new MyMechtatel();
    }
}
