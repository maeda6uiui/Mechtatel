package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.Quad2D;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Listbox
 *
 * @author maeda6uiui
 */
public class MttListbox extends MttGuiComponent {
    private static final float SCROLLBAR_FRAME_WIDTH_RATIO = 0.05f;

    private Quad2D frame;
    private Quad2D scrollbarFrame;
    private Quad2D scrollbarGrabFrame;

    private Vector2f scrollbarGrabTopLeft;
    private Vector2f scrollbarGrabBottomRight;
    private float prevFCursorY;
    private boolean grabbed;

    private List<MttButton> itemButtons;

    public MttListbox(
            MttVulkanInstance vulkanInstance,
            float x,
            float y,
            float width,
            float height,
            String fontName,
            int fontStyle,
            int fontSize,
            Color fontColor,
            Color frameColor,
            List<String> items,
            float itemHeight) {
        super(vulkanInstance, x, y, width, height, "Listbox", fontName, fontStyle, fontSize, fontColor);

        float fFrameColorR = frameColor.getRed() / 255.0f;
        float fFrameColorG = frameColor.getGreen() / 255.0f;
        float fFrameColorB = frameColor.getBlue() / 255.0f;
        frame = new Quad2D(
                vulkanInstance,
                new Vector2f(x, y),
                new Vector2f(x + width, y + height),
                0.0f,
                new Vector4f(fFrameColorR, fFrameColorG, fFrameColorB, 1.0f)
        );
        scrollbarFrame = new Quad2D(
                vulkanInstance,
                new Vector2f(x + width - width * SCROLLBAR_FRAME_WIDTH_RATIO, y),
                new Vector2f(x + width, y + height),
                0.0f,
                new Vector4f(fFrameColorR, fFrameColorG, fFrameColorB, 1.0f)
        );

        scrollbarGrabTopLeft = new Vector2f(x + width - width * SCROLLBAR_FRAME_WIDTH_RATIO, y);
        scrollbarGrabBottomRight = new Vector2f(x + width, y + height * 0.1f);
        scrollbarGrabFrame = new Quad2D(
                vulkanInstance,
                scrollbarGrabTopLeft,
                scrollbarGrabBottomRight,
                0.0f,
                new Vector4f(fFrameColorR, fFrameColorG, fFrameColorB, 1.0f)
        );

        this.getFont().setVisible(false);

        prevFCursorY = 0.0f;
        grabbed = false;

        itemButtons = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            var itemButton = new MttButton(
                    vulkanInstance, x, y + itemHeight * i, width - width * SCROLLBAR_FRAME_WIDTH_RATIO, itemHeight,
                    items.get(i), fontName, fontStyle, fontSize, fontColor, frameColor
            );
            itemButton.setFrameVisible(false);

            if (y + itemHeight * (i + 1) > y + height) {
                itemButton.setVisible(false);
            }

            itemButtons.add(itemButton);
        }
    }

    @Override
    public void update(
            int cursorX,
            int cursorY,
            int windowWidth,
            int windowHeight,
            int lButtonPressingCount,
            int mButtonPressingCount,
            int rButtonPressingCount) {
        super.update(
                cursorX, cursorY, windowWidth, windowHeight,
                lButtonPressingCount, mButtonPressingCount, rButtonPressingCount);

        float fCursorX = (float) cursorX / (float) windowWidth * 2.0f - 1.0f;
        float fCursorY = (float) cursorY / (float) windowHeight * 2.0f - 1.0f;

        if ((scrollbarGrabTopLeft.x < fCursorX && fCursorX < scrollbarGrabBottomRight.x)
                && (scrollbarGrabTopLeft.y < fCursorY && fCursorY < scrollbarGrabBottomRight.y)) {
            if (lButtonPressingCount > 0) {
                grabbed = true;
            }
        }
        if (lButtonPressingCount == 0) {
            grabbed = false;
        }

        if (grabbed) {
            float diffFCursorY = fCursorY - prevFCursorY;

            float grabMoveAmount;
            float frameY = this.getY();
            float frameHeight = this.getHeight();
            if (scrollbarGrabTopLeft.y + diffFCursorY < frameY) {
                grabMoveAmount = frameY - scrollbarGrabTopLeft.y;
            } else if (scrollbarGrabBottomRight.y + diffFCursorY > frameY + frameHeight) {
                grabMoveAmount = frameY + frameHeight - scrollbarGrabBottomRight.y;
            } else {
                grabMoveAmount = diffFCursorY;
            }

            scrollbarGrabTopLeft.y += grabMoveAmount;
            scrollbarGrabBottomRight.y += grabMoveAmount;

            scrollbarGrabFrame.applyMat(new Matrix4f().translate(0.0f, grabMoveAmount, 0.0f));
        }

        prevFCursorY = fCursorY;

        itemButtons.forEach(itemButton -> {
            itemButton.update(
                    cursorX, cursorY, windowWidth, windowHeight,
                    lButtonPressingCount, mButtonPressingCount, rButtonPressingCount);
            if (itemButton.isCursorOn()) {
                float bottomY = itemButton.getY() + itemButton.getHeight();
                float frameBottomY = this.getY() + this.getHeight();
                if (bottomY < frameBottomY) {
                    itemButton.setFrameVisible(true);
                }
            } else {
                itemButton.setFrameVisible(false);
            }
        });
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        frame.setVisible(visible);
        scrollbarFrame.setVisible(visible);
        scrollbarGrabFrame.setVisible(visible);
        itemButtons.forEach(itemButton -> itemButton.setVisible(visible));
    }
}