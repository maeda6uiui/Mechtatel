package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;

import java.io.IOException;
import java.net.URL;
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
            IMechtatelForSkyboxTextureCreator mtt,
            String screenName,
            URL textureDirectory,
            String textureExtension,
            boolean generateMipmaps) throws IOException {
        String textureDirname = textureDirectory.getFile();
        URL texNxResource = Paths.get(textureDirname, String.format("nx.%s", textureExtension)).toUri().toURL();
        URL texNyResource = Paths.get(textureDirname, String.format("ny.%s", textureExtension)).toUri().toURL();
        URL texNzResource = Paths.get(textureDirname, String.format("nz.%s", textureExtension)).toUri().toURL();
        URL texPxResource = Paths.get(textureDirname, String.format("px.%s", textureExtension)).toUri().toURL();
        URL texPyResource = Paths.get(textureDirname, String.format("py.%s", textureExtension)).toUri().toURL();
        URL texPzResource = Paths.get(textureDirname, String.format("pz.%s", textureExtension)).toUri().toURL();

        texNx = mtt.createTexture(screenName, texNxResource, generateMipmaps);
        texNy = mtt.createTexture(screenName, texNyResource, generateMipmaps);
        texNz = mtt.createTexture(screenName, texNzResource, generateMipmaps);
        texPx = mtt.createTexture(screenName, texPxResource, generateMipmaps);
        texPy = mtt.createTexture(screenName, texPyResource, generateMipmaps);
        texPz = mtt.createTexture(screenName, texPzResource, generateMipmaps);
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
