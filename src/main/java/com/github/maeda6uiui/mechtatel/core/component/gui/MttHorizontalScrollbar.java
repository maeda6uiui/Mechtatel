package com.github.maeda6uiui.mechtatel.core.component.gui;

import com.github.maeda6uiui.mechtatel.core.component.FilledQuad2D;
import com.github.maeda6uiui.mechtatel.core.component.Quad2D;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.awt.*;

import static com.github.maeda6uiui.mechtatel.core.util.ClassConversionUtils.convertJavaColorToJOMLVector4f;

/**
 * Horizontal scrollbar
 *
 * @author maeda6uiui
 */
public class MttHorizontalScrollbar extends MttGuiComponent {
    private Quad2D frame;
    private FilledQuad2D grabFrame;

    private Vector2f grabTopLeft;
    private Vector2f grabBottomRight;
    private float prevFCursorX;
    private boolean grabbed;

    public MttHorizontalScrollbar(
            MttVulkanInstance vulkanInstance,
            float x,
            float y,
            float width,
            float height,
            float grabWidth,
            Color frameColor,
            Color grabFrameColor) {
        super(vulkanInstance, x, y, width, height);

        frame = new Quad2D(
                vulkanInstance,
                new Vector2f(x, y),
                new Vector2f(x + width, y + height),
                0.0f,
                convertJavaColorToJOMLVector4f(frameColor)
        );
        grabFrame = new FilledQuad2D(
                vulkanInstance,
                new Vector2f(x, y),
                new Vector2f(x + grabWidth, y + height),
                0.01f,
                convertJavaColorToJOMLVector4f(grabFrameColor)
        );

        grabTopLeft = new Vector2f(x, y);
        grabBottomRight = new Vector2f(x + grabWidth, y + height);

        prevFCursorX = 0.0f;
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
            float diffFCursorX = fCursorX - prevFCursorX;

            float grabMoveAmount;
            float frameX = this.getX();
            float frameWidth = this.getWidth();
            if (grabTopLeft.x + diffFCursorX < frameX) {
                grabMoveAmount = frameX - grabTopLeft.x;
            } else if (grabBottomRight.x + diffFCursorX > frameX + frameWidth) {
                grabMoveAmount = frameX + frameWidth - grabBottomRight.x;
            } else {
                grabMoveAmount = diffFCursorX;
            }

            grabTopLeft.x += grabMoveAmount;
            grabBottomRight.x += grabMoveAmount;

            grabFrame.applyMat(new Matrix4f().translate(grabMoveAmount, 0.0f, 0.0f));
        }

        prevFCursorX = fCursorX;
    }

    @Override
    public void setVisible(boolean visible) {
        frame.setVisible(visible);
        grabFrame.setVisible(visible);
    }

    public float getScrollAmount() {
        float grabWidth = grabBottomRight.x - grabTopLeft.x;
        float grabWholeAmount = this.getWidth() - grabWidth;
        float scrollAmount = (grabTopLeft.x - this.getX()) / grabWholeAmount;

        return scrollAmount;
    }
}