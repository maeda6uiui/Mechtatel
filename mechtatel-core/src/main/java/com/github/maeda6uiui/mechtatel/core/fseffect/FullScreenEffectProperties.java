package com.github.maeda6uiui.mechtatel.core.fseffect;

/**
 * Info instances for full-screen effect
 *
 * @author maeda6uiui
 */
public class FullScreenEffectProperties {
    public GaussianBlurInfo gaussianBlurInfo;
    public MonochromeEffectInfo monochromeEffectInfo;

    public FullScreenEffectProperties() {
        gaussianBlurInfo = new GaussianBlurInfo(8, 4.0f, 0.25f);
        monochromeEffectInfo = new MonochromeEffectInfo();
    }
}
