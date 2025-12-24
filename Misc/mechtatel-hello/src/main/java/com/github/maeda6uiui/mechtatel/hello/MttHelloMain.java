package com.github.maeda6uiui.mechtatel.hello;

import com.github.maeda6uiui.mechtatel.core.MechtatelHeadless;
import com.github.maeda6uiui.mechtatel.core.MttHeadlessInstance;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

/**
 * Main class for MttHello
 *
 * @author maeda6uiui
 */
class MttHelloMain extends MechtatelHeadless {
    record MttHelloOptions(String renderingType, String imageOutputFilepath) {
    }

    private static final Logger logger = LoggerFactory.getLogger(MttHelloMain.class);

    private static MttHelloOptions options;
    private IMttHello mttHello;

    public MttHelloMain(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void setOptions(MttHelloOptions op) {
        options = op;
    }

    @Override
    public void onInit(MttHeadlessInstance instance) {
        switch (options.renderingType) {
            case "primitives" -> mttHello = new MttHelloPrimitives();
            case "imgui" -> mttHello = new MttHelloImGui();
            case "monochrome-image" -> mttHello = new MttMonochromeImage();
            default -> {
                logger.warn("Unknown rendering type specified ({}), fall back to 'primitives'", options.renderingType);
                mttHello = new MttHelloPrimitives();
            }
        }

        mttHello.onInit(instance);
    }

    @Override
    public void onUpdate(MttHeadlessInstance instance) {
        mttHello.onUpdate(instance, Paths.get(options.imageOutputFilepath));
    }
}

