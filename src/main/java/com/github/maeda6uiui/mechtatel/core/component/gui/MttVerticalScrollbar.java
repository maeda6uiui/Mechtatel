package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.FilledQuad2D;
import com.github.maeda6uiui.mechtatel.core.component.Quad2D;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.awt.*;

/**
 * Vertical scrollbar
 *
 * @author maeda6uiui
 */
public class MttVerticalScrollbar extends MttGuiComponent {
    private Quad2D frame;
    private FilledQuad2D grabFrame;

    private Vector2f grabTopLeft;
    private Vector2f grabBottomRight;
    private float prevFCursorY;
    private boolean grabbed;

    public MttVerticalScrollbar(
            MttVulkanInstance vulkanInstance,
            float x,
            float y,
            float width,
            float height,
            float grabHeight,
            Color frameColor,
            Color grabFrameColor) {
        super(vulkanInstance, x, y, width, height, "Scrollbar", Font.SERIF, Font.PLAIN, 50, Color.WHITE);
        this.getFont().setVisible(false);

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

        float fGrabFrameColorR = grabFrameColor.getRed() / 255.0f;
        float fGrabFrameColorG = grabFrameColor.getGreen() / 255.0f;
        float fGrabFrameColorB = grabFrameColor.getBlue() / 255.0f;
        grabFrame = new FilledQuad2D(
                vulkanInstance,
                new Vector2f(x, y),
                new Vector2f(x + width, y + grabHeight),
                0.01f,
                new Vector4f(fGrabFrameColorR, fGrabFrameColorG, fGrabFrameColorB, 1.0f)
        );

        grabTopLeft = new Vector2f(x, y);
        grabBottomRight = new Vector2f(x + width, y + grabHeight);

        prevFCursorY = 0.0f;
        grabbed = false;
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

        if ((grabTopLeft.x < fCursorX && fCursorX < grabBottomRight.x)
                && (grabTopLeft.y < fCursorY && fCursorY < grabBottomRight.y)) {
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
            if (grabTopLeft.y + diffFCursorY < frameY) {
                grabMoveAmount = frameY - grabTopLeft.y;
            } else if (grabBottomRight.y + diffFCursorY > frameY + frameHeight) {
                grabMoveAmount = frameY + frameHeight - grabBottomRight.y;
            } else {
                grabMoveAmount = diffFCursorY;
            }

            grabTopLeft.y += grabMoveAmount;
            grabBottomRight.y += grabMoveAmount;

            grabFrame.applyMat(new Matrix4f().translate(0.0f, grabMoveAmount, 0.0f));
        }

        prevFCursorY = fCursorY;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        frame.setVisible(visible);
        grabFrame.setVisible(visible);
    }
}
