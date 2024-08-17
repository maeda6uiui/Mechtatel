package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.operation.BiTextureOperation;
import com.github.maeda6uiui.mechtatel.core.operation.BiTextureOperationParameters;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttTexturedQuad2D;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class BiTextureOperationAddTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(BiTextureOperationAddTest.class);

    public BiTextureOperationAddTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        BiTextureOperationAddTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen firstScreen;
    private MttScreen secondScreen;
    private MttScreen finalScreen;
    private MttTexturedQuad2D texturedQuad;
    private BiTextureOperation opAdd;
    private FreeCamera camera;

    @Override
    public void onCreate(MttWindow window) {
        firstScreen = window.createScreen(new MttScreen.MttScreenCreateInfo());
        firstScreen.getCamera().setEye(new Vector3f(2.0f, 2.0f, 2.0f));

        secondScreen = window.createScreen(new MttScreen.MttScreenCreateInfo());
        secondScreen.getCamera().setEye(new Vector3f(1.0f, 2.0f, 1.0f));

        finalScreen = window.createScreen(new MttScreen.MttScreenCreateInfo());

        try {
            texturedQuad = finalScreen.createTexturedQuad2D(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Texture/checker.png")),
                    new Vector2f(-1.0f, -1.0f),
                    new Vector2f(1.0f, 1.0f),
                    0.0f
            );

            firstScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Plane/plane.obj")));
            secondScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Teapot/teapot.obj")));
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        camera = new FreeCamera(firstScreen.getCamera());

        this.createTextureOperation();
    }

    @Override
    public void onRecreate(MttWindow window, int width, int height) {
        //Texture operations must be recreated on resource recreation accompanied by window resize,
        //as some resources such as underlying textures of a screen are destroyed and no longer valid.
        this.createTextureOperation();
    }

    @Override
    public void onUpdate(MttWindow window) {
        camera.translate(
                window.getKeyboardPressingCount(KeyCode.W),
                window.getKeyboardPressingCount(KeyCode.S),
                window.getKeyboardPressingCount(KeyCode.A),
                window.getKeyboardPressingCount(KeyCode.D)
        );
        camera.rotate(
                window.getKeyboardPressingCount(KeyCode.UP),
                window.getKeyboardPressingCount(KeyCode.DOWN),
                window.getKeyboardPressingCount(KeyCode.LEFT),
                window.getKeyboardPressingCount(KeyCode.RIGHT)
        );

        firstScreen.draw();
        secondScreen.draw();
        opAdd.run();
        finalScreen.draw();
        window.present(finalScreen);
    }

    private void createTextureOperation() {
        if (opAdd != null) {
            opAdd.cleanup();
        }

        MttTexture firstColorTexture = firstScreen.texturize(ScreenImageType.COLOR, finalScreen);
        MttTexture secondColorTexture = secondScreen.texturize(ScreenImageType.COLOR, finalScreen);

        var texOpParams = new BiTextureOperationParameters();
        texOpParams.setOperationType(BiTextureOperationParameters.OperationType.ADD);

        opAdd = finalScreen.createBiTextureOperation(
                Arrays.asList(firstColorTexture, secondColorTexture),
                new ArrayList<>(),
                true
        );
        opAdd.setParameters(texOpParams);
        texturedQuad.replaceTexture(opAdd.getResultTexture());
    }
}
