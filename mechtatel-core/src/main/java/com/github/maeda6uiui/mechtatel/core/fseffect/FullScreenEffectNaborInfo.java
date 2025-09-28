package com.github.maeda6uiui.mechtatel.core.fseffect;

import java.net.URL;
import java.util.List;

/**
 * Info of full-screen effect nabor
 *
 * @author maeda6uiui
 */
public class FullScreenEffectNaborInfo {
    private List<URL> vertShaderResources;
    private List<URL> fragShaderResources;

    public FullScreenEffectNaborInfo(List<URL> vertShaderResources, List<URL> fragShaderResources) {
        this.vertShaderResources = vertShaderResources;
        this.fragShaderResources = fragShaderResources;
    }

    @Deprecated
    public FullScreenEffectNaborInfo(URL vertShaderResource, URL fragShaderResource) {
        this(List.of(vertShaderResource), List.of(fragShaderResource));
    }

    @Deprecated
    public URL getVertShaderResource() {
        return vertShaderResources.getFirst();
    }

    public List<URL> getVertShaderResources() {
        return vertShaderResources;
    }

    @Deprecated
    public URL getFragShaderResource() {
        return fragShaderResources.getFirst();
    }

    public List<URL> getFragShaderResources() {
        return fragShaderResources;
    }
}
