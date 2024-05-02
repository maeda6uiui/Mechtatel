package com.github.maeda6uiui.mechtatel.core.model.helper;

import com.github.maeda6uiui.mechtatel.core.model.MttAnimationData;
import com.github.maeda6uiui.mechtatel.core.model.MttModelData;

/**
 * Helper class to update model animation
 *
 * @author maeda6uiui
 */
public class AnimationUpdater {
    private MttAnimationData animationData;
    private AnimationPlayMode playMode;
    private int numAnimationFrames;
    private double animationSecondsPerFrame;
    private double engineSecondsPerFrame;

    private double animationAccumTime;
    private int repeatCount;

    /**
     * @param animationData         Animation data
     * @param playMode              Play mode for the animation
     * @param engineSecondsPerFrame Seconds per frame of the Mechtatel engine
     */
    public AnimationUpdater(
            MttAnimationData animationData,
            AnimationPlayMode playMode,
            double engineSecondsPerFrame) {
        this.animationData = animationData;
        this.playMode = playMode;
        this.engineSecondsPerFrame = engineSecondsPerFrame;

        MttModelData.Animation currentAnimation = animationData.getCurrentAnimation();
        numAnimationFrames = currentAnimation.frames().size();
        double animationDuration = currentAnimation.duration();
        double animationDurationSeconds = animationDuration / 1000.0;
        animationSecondsPerFrame = animationDurationSeconds / numAnimationFrames;

        int animationStartFrameOffset = animationData.getStartFrameOffset();
        animationAccumTime = animationSecondsPerFrame * animationStartFrameOffset;

        repeatCount = 0;
    }

    public void update() {
        int currentFrameIdx = animationData.getCurrentFrameIdx();
        int animationStartFrameOffset = animationData.getStartFrameOffset();

        if (playMode == AnimationPlayMode.ONCE && repeatCount > 0) {
            return;
        }

        boolean shouldResetAccumTime = false;
        if (animationAccumTime >= animationSecondsPerFrame * (currentFrameIdx + 1)) {
            animationData.nextFrame();

            if (animationData.getCurrentFrameIdx() == animationStartFrameOffset) {
                shouldResetAccumTime = true;
            }
        }

        if (shouldResetAccumTime) {
            animationAccumTime = animationSecondsPerFrame * animationStartFrameOffset;
            repeatCount++;
        } else {
            animationAccumTime += engineSecondsPerFrame;
        }
    }

    public int getRepeatCount() {
        return repeatCount;
    }
}
