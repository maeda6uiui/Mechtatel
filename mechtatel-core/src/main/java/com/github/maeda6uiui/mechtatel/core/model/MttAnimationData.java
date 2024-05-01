package com.github.maeda6uiui.mechtatel.core.model;

/**
 * Animation data
 *
 * @author maeda6uiui
 */
public class MttAnimationData {
    private MttModelData.Animation currentAnimation;
    private int currentFrameIdx;

    private int startFrameOffset;
    private int endFrameOffset;

    public MttAnimationData(
            MttModelData.Animation currentAnimation,
            int startFrameOffset,
            int endFrameOffset) {
        this.currentAnimation = currentAnimation;
        currentFrameIdx = startFrameOffset;

        this.startFrameOffset = startFrameOffset;
        this.endFrameOffset = endFrameOffset;
    }

    public MttModelData.Animation getCurrentAnimation() {
        return currentAnimation;
    }

    public MttModelData.AnimatedFrame getCurrentFrame() {
        return currentAnimation.frames().get(currentFrameIdx);
    }

    public int getCurrentFrameIdx() {
        return currentFrameIdx;
    }

    public void nextFrame() {
        int nextFrame = currentFrameIdx + 1;
        int endFrameIdx = currentAnimation.frames().size() - 1 + endFrameOffset;
        if (nextFrame > endFrameIdx) {
            currentFrameIdx = startFrameOffset;
        } else {
            currentFrameIdx = nextFrame;
        }
    }

    public void setCurrentAnimation(MttModelData.Animation currentAnimation) {
        this.currentAnimation = currentAnimation;
        currentFrameIdx = startFrameOffset;
    }
}
