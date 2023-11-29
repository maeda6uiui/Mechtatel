package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Interface to {@link MttWindow} providing access to required methods for skybox texture creation
 *
 * @author maeda6uiui
 */
public interface IMttWindowForSkyboxTextureCreator {
    MttTexture createTexture(String screenName, URL textureResource, boolean generateMipmaps)
            throws URISyntaxException, FileNotFoundException;
}
