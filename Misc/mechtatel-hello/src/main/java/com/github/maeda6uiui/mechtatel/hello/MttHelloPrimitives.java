package com.github.maeda6uiui.mechtatel.hello;

import com.github.maeda6uiui.mechtatel.core.MttHeadlessInstance;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.postprocessing.PostProcessingProperties;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttCapsule;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttSphere;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Renders some primitives
 *
 * @author maeda6uiui
 */
class MttHelloPrimitives implements IMttHello {
    private static final Logger logger = LoggerFactory.getLogger(MttHelloPrimitives.class);

    private MttScreen mainScreen;

    @Override
    public void onInit(MttHeadlessInstance instance) {
        //Get settings
        MttSettings settings = MttSettings.get().orElse(new MttSettings());

        //Create a screen
        mainScreen = instance.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setPostProcessingNaborNames(List.of("pp.parallel_light"))
                        .setScreenWidth(settings.headlessSettings.width)
                        .setScreenHeight(settings.headlessSettings.height)
        );

        //Set up a parallel light
        PostProcessingProperties ppProps = mainScreen.getPostProcessingProperties();
        ppProps.createParallelLight();

        //Create primitives
        mainScreen.createLineSet().addPositiveAxes(10.0f).createBuffer();
        MttSphere sphere = mainScreen.createSphere(
                1.0f,
                16,
                16,
                new Vector4f(1.0f, 0.0f, 0.0f, 1.0f),
                false
        );
        sphere.translate(new Vector3f(0.0f, 0.0f, 3.0f));
        MttCapsule capsule = mainScreen.createCapsule(
                1.0f,
                1.0f,
                16,
                16,
                new Vector4f(0.0f, 1.0f, 0.0f, 1.0f),
                false
        );
        capsule.translate(new Vector3f(0.0f, 0.0f, 6.0f));
        MttSphere filledSphere = mainScreen.createSphere(
                1.0f,
                16,
                16,
                new Vector4f(0.0f, 0.0f, 1.0f, 1.0f),
                true
        );
        filledSphere.translate(new Vector3f(3.0f, 0.0f, 0.0f));
        MttCapsule filledCapsule = mainScreen.createCapsule(
                1.0f,
                1.0f,
                16,
                16,
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                true
        );
        filledCapsule.translate(new Vector3f(6.0f, 0.0f, 0.0f));

        //Set up a camera
        mainScreen.getCamera().setEye(new Vector3f(10.0f, 10.0f, 10.0f));
    }

    @Override
    public void onUpdate(MttHeadlessInstance instance, Path imageOutputPath) {
        mainScreen.draw();

        try {
            mainScreen.save(ScreenImageType.COLOR, PixelFormat.BGRA, imageOutputPath);
        } catch (IOException e) {
            logger.error("Error while saving image to disk", e);
        } finally {
            instance.close();
        }
    }
}
