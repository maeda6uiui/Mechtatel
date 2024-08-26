package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.operation.TextureOperation;
import com.github.maeda6uiui.mechtatel.core.operation.TextureOperationParameters;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttTexturedQuad2D;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class TextureOperationTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(TextureOperationTest.class);

    public TextureOperationTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        TextureOperationTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private List<MttScreen> interimScreens;
    private List<MttTexturedQuad2D> interimTexturedQuads;

    private MttScreen finalScreen;
    private MttTexturedQuad2D texturedQuad;
    private TextureOperation opAdd;

    private Random random;

    @Override
    public void onCreate(MttWindow window) {
        interimScreens = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            MttScreen interimScreen = window.createScreen(new MttScreen.MttScreenCreateInfo());
            interimScreens.add(interimScreen);
        }

        finalScreen = window.createScreen(new MttScreen.MttScreenCreateInfo());

        try {
            texturedQuad = finalScreen.createTexturedQuad2D(
                    Paths.get("./Mechtatel/Standard/Texture/checker.png"),
                    new Vector2f(-1.0f, -1.0f),
                    new Vector2f(1.0f, 1.0f),
                    0.0f
            );

            interimTexturedQuads = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                MttScreen interimScreen = interimScreens.get(i);

                String textureFilepath = switch (i) {
                    case 0 -> "./Mechtatel/Standard/Texture/checker_r.png";
                    case 1 -> "./Mechtatel/Standard/Texture/checker_g.png";
                    default -> "./Mechtatel/Standard/Texture/checker_b.png";
                };
                MttTexturedQuad2D interimTexturedQuad = interimScreen.createTexturedQuad2D(
                        Paths.get(textureFilepath),
                        new Vector2f(-1.0f, -1.0f),
                        new Vector2f(1.0f, 1.0f),
                        0.0f
                );
                interimTexturedQuads.add(interimTexturedQuad);
            }
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        random = new Random();

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
        final float TRANSLATION_SCALE = 0.01f;
        Supplier<Integer> getSign = () -> random.nextInt() % 2 == 0 ? 1 : -1;
        interimTexturedQuads.forEach(v -> {
            var signs = new int[2];
            for (int i = 0; i < 2; i++) {
                signs[i] = getSign.get();
            }

            var translations = new float[2];
            for (int i = 0; i < 2; i++) {
                translations[i] = random.nextFloat() * TRANSLATION_SCALE * signs[i];
            }

            v.translate(new Vector3f(translations[0], translations[1], 0.0f));
        });

        interimScreens.forEach(MttScreen::draw);
        opAdd.run();
        finalScreen.draw();
        window.present(finalScreen);
    }

    private void createTextureOperation() {
        if (opAdd != null) {
            opAdd.cleanup();
        }

        var interimTextures = new ArrayList<MttTexture>();
        interimScreens.forEach(v -> interimTextures.add(v.texturize(ScreenImageType.COLOR, finalScreen)));

        var texOpParams = new TextureOperationParameters();
        texOpParams.setOperationType(TextureOperationParameters.OperationType.ADD);
        texOpParams.fillFactors(3, new Vector4f(1.0f));

        opAdd = finalScreen.createTextureOperation(interimTextures, true);
        opAdd.setParameters(texOpParams);
        texturedQuad.replaceTexture(opAdd.getResultTexture());
    }
}
