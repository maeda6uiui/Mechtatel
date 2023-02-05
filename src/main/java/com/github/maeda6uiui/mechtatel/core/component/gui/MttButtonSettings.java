package com.github.maeda6uiui.mechtatel.core.component.gui;

import org.joml.Vector2f;

import java.awt.*;

/**
 * Settings for button
 *
 * @author maeda6uiui
 */
public class MttButtonSettings {
    public String fontName;
    public int fontSize;
    public Vector2f topLeft;
    public Vector2f bottomRight;
    public float z;
    public int nonSelectedFontStyle;
    public String nonSelectedText;
    public Color nonSelectedTextColor;
    public int selectedFontStyle;
    public String selectedText;
    public Color selectedTextColor;

    public MttButtonSettings() {
        fontName = Font.SERIF;
        fontSize = 60;
        topLeft = new Vector2f(-0.9f, -0.9f);
        bottomRight = new Vector2f(-0.55f, -0.8f);
        z = 0.0f;
        nonSelectedFontStyle = Font.PLAIN;
        nonSelectedText = "Not selected!";
        nonSelectedTextColor = Color.GREEN;
        selectedFontStyle = Font.PLAIN;
        selectedText = "Selected!";
        selectedTextColor = new Color(
                255 - nonSelectedTextColor.getRed(),
                255 - nonSelectedTextColor.getGreen(),
                255 - nonSelectedTextColor.getBlue());
    }
}
