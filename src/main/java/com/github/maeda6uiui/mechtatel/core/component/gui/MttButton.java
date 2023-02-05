package com.github.maeda6uiui.mechtatel.core.component.gui;


import com.github.maeda6uiui.mechtatel.core.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;

import java.awt.*;

/**
 * Button
 *
 * @author maeda6uiui
 */
public class MttButton extends MttGuiComponent {
    private MttFont mttNonSelectedFont;
    private MttFont mttSelectedFont;
    private MttButtonSettings settings;

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
        mttNonSelectedFont.prepare(
                settings.nonSelectedText, settings.topLeft, settings.z, 0.001f, 0.001f, 0.0f);
        mttNonSelectedFont.createBuffers();

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
        mttSelectedFont.prepare(
                settings.selectedText, settings.topLeft, settings.z, 0.001f, 0.001f, 0.0f);
        mttSelectedFont.createBuffers();
        mttSelectedFont.setVisible(false);

        this.settings = settings;
    }

    @Override
    public void updateState() {
        float cursorX = this.getCursorPosX();
        float cursorY = this.getCursorPosY();

        //Selected
        if ((settings.topLeft.x <= cursorX && cursorX <= settings.bottomRight.x)
                && (settings.topLeft.y <= cursorY && cursorY <= settings.bottomRight.y)) {
            mttNonSelectedFont.setVisible(false);
            mttSelectedFont.setVisible(true);
        }
        //Not selected
        else {
            mttNonSelectedFont.setVisible(true);
            mttSelectedFont.setVisible(false);
        }
    }
}
