package com.github.maeda6uiui.mechtatel.core.component.gui;


import com.github.maeda6uiui.mechtatel.core.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.component.Quad2D;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;

import java.awt.*;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJavaColorToJOMLVector4f;

/**
 * Button
 *
 * @author maeda6uiui
 */
public class MttButton extends MttGuiComponent {
    private MttFont mttNonSelectedFont;
    private Quad2D nonSelectedFrame;
    private MttFont mttSelectedFont;
    private Quad2D selectedFrame;

    public MttButton(MttVulkanInstance vulkanInstance, MttButtonSettings settings) {
        super(vulkanInstance);

        var nonSelectedFont = new Font(
                settings.fontName,
                settings.nonSelectedFontStyle,
                settings.fontSize
        );
        mttNonSelectedFont = new MttFont(
                vulkanInstance,
                nonSelectedFont,
                true,
                settings.nonSelectedTextColor,
                settings.nonSelectedText
        );
        nonSelectedFrame = new Quad2D(
                vulkanInstance,
                settings.topLeft,
                settings.bottomRight,
                0.0f,
                convertJavaColorToJOMLVector4f(settings.nonSelectedFrameColor)
        );

        var selectedFont = new Font(
                settings.fontName,
                settings.selectedFontStyle,
                settings.fontSize
        );
        mttSelectedFont = new MttFont(
                vulkanInstance,
                selectedFont,
                true,
                settings.selectedTextColor,
                settings.selectedText
        );
        selectedFrame = new Quad2D(
                vulkanInstance,
                settings.topLeft,
                settings.bottomRight,
                0.0f,
                convertJavaColorToJOMLVector4f(settings.selectedFrameColor)
        );
    }
}
