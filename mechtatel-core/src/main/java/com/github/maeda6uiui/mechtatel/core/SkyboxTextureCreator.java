package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

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
            URL textureDirUrl,
            String textureExtension,
            boolean generateMipmaps) throws URISyntaxException, IOException {
        Path textureDir = Paths.get(textureDirUrl.toURI());
        URL texNxResource = textureDir.resolve(String.format("nx.%s", textureExtension)).toUri().toURL();
        URL texNyResource = textureDir.resolve(String.format("ny.%s", textureExtension)).toUri().toURL();
        URL texNzResource = textureDir.resolve(String.format("nz.%s", textureExtension)).toUri().toURL();
        URL texPxResource = textureDir.resolve(String.format("px.%s", textureExtension)).toUri().toURL();
        URL texPyResource = textureDir.resolve(String.format("py.%s", textureExtension)).toUri().toURL();
        URL texPzResource = textureDir.resolve(String.format("pz.%s", textureExtension)).toUri().toURL();

        texNx = screen.createTexture(texNxResource, generateMipmaps);
        texNy = screen.createTexture(texNyResource, generateMipmaps);
        texNz = screen.createTexture(texNzResource, generateMipmaps);
        texPx = screen.createTexture(texPxResource, generateMipmaps);
        texPy = screen.createTexture(texPyResource, generateMipmaps);
        texPz = screen.createTexture(texPzResource, generateMipmaps);
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
