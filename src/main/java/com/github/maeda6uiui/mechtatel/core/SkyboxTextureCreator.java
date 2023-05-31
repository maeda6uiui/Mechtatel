package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.component.MttModel3D;
import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;

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
            String textureDirname,
            String textureExtension,
            boolean generateMipmaps) {
        String texNxFilepath = Paths.get(textureDirname, String.format("nx.%s", textureExtension)).toString();
        String texNyFilepath = Paths.get(textureDirname, String.format("ny.%s", textureExtension)).toString();
        String texNzFilepath = Paths.get(textureDirname, String.format("nz.%s", textureExtension)).toString();
        String texPxFilepath = Paths.get(textureDirname, String.format("px.%s", textureExtension)).toString();
        String texPyFilepath = Paths.get(textureDirname, String.format("py.%s", textureExtension)).toString();
        String texPzFilepath = Paths.get(textureDirname, String.format("pz.%s", textureExtension)).toString();

        texNx = mtt.createTexture(screenName, texNxFilepath, generateMipmaps);
        texNy = mtt.createTexture(screenName, texNyFilepath, generateMipmaps);
        texNz = mtt.createTexture(screenName, texNzFilepath, generateMipmaps);
        texPx = mtt.createTexture(screenName, texPxFilepath, generateMipmaps);
        texPy = mtt.createTexture(screenName, texPyFilepath, generateMipmaps);
        texPz = mtt.createTexture(screenName, texPzFilepath, generateMipmaps);
    }

    public void apply(MttModel3D model) {
        model.replaceTexture(0, texPx);
        model.replaceTexture(1, texNz);
        model.replaceTexture(2, texNx);
        model.replaceTexture(3, texPz);
        model.replaceTexture(4, texPy);
        model.replaceTexture(5, texNy);
    }
}
