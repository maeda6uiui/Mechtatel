package com.github.maeda6uiui.mechtatel.core.fseffect;

import java.net.URL;

/**
 * Info of full-screen effect nabor
 *
 * @author maeda6uiui
 */
public class FullScreenEffectNaborInfo {
    private URL vertShaderResource;
    private URL fragShaderResource;

    public FullScreenEffectNaborInfo(URL vertShaderResource, URL fragShaderResource) {
        this.vertShaderResource = vertShaderResource;
        this.fragShaderResource = fragShaderResource;
    }

    public URL getVertShaderResource() {
        return vertShaderResource;
    }

    public URL getFragShaderResource() {
        return fragShaderResource;
    }
}
