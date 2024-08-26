package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Utility class to create textures for a skybox
 *
 * @author maeda6uiui
 */
public class SkyboxTextureCreator {
    private MttTexture texNx;
    private MttTexture texNy;
    private MttTexture texNz;
    private MttTexture texPx;
    private MttTexture texPy;
    private MttTexture texPz;

    public SkyboxTextureCreator(
            MttScreen screen,
            Path textureDir,
            String textureExtension,
            boolean generateMipmaps) throws IOException {
        Path texNxFile = textureDir.resolve(String.format("nx.%s", textureExtension));
        Path texNyFile = textureDir.resolve(String.format("ny.%s", textureExtension));
        Path texNzFile = textureDir.resolve(String.format("nz.%s", textureExtension));
        Path texPxFile = textureDir.resolve(String.format("px.%s", textureExtension));
        Path texPyFile = textureDir.resolve(String.format("py.%s", textureExtension));
        Path texPzFile = textureDir.resolve(String.format("pz.%s", textureExtension));

        texNx = screen.createTexture(texNxFile, generateMipmaps);
        texNy = screen.createTexture(texNyFile, generateMipmaps);
        texNz = screen.createTexture(texNzFile, generateMipmaps);
        texPx = screen.createTexture(texPxFile, generateMipmaps);
        texPy = screen.createTexture(texPyFile, generateMipmaps);
        texPz = screen.createTexture(texPzFile, generateMipmaps);
    }

    public void apply(MttModel model) {
        model.replaceTexture(0, texPx);
        model.replaceTexture(1, texNz);
        model.replaceTexture(2, texNx);
        model.replaceTexture(3, texPz);
        model.replaceTexture(4, texPy);
        model.replaceTexture(5, texNy);
    }
}
