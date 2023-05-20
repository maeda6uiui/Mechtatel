package com.github.maeda6uiui.mechtatel.core.vulkan.screen;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.MergeScenesNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.PostProcessingNaborChain;
import org.joml.Vector3f;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkQueue;

import java.io.IOException;
import java.util.List;

/**
 * Scene
 *
 * @author maeda6uiui
 */
public class VkScreen {
    private PostProcessingNaborChain ppNaborChain;

    public VkScreen(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            int depthImageFormat,
            int depthImageWidth,
            int depthImageHeight,
            int depthImageAspect,
            int colorImageFormat,
            VkExtent2D extent,
            List<String> ppNaborNames) {
        ppNaborChain = new PostProcessingNaborChain(
                device,
                commandPool,
                graphicsQueue,
                depthImageFormat,
                depthImageWidth,
                depthImageHeight,
                depthImageAspect,
                colorImageFormat,
                extent,
                ppNaborNames
        );
    }

    public VkScreen() {

    }

    public void recreate(int colorImageFormat, VkExtent2D extent) {
        if (ppNaborChain != null) {
            ppNaborChain.recreate(colorImageFormat, extent);
        }
    }

    public void cleanup() {
        if (ppNaborChain != null) {
            ppNaborChain.cleanup();
        }
    }

    public void run(
            Camera camera,
            Fog fog,
            List<ParallelLight> parallelLights,
            Vector3f parallelLightAmbientColor,
            List<PointLight> pointLights,
            Vector3f pointLightAmbientColor,
            List<Spotlight> spotlights,
            Vector3f spotlightAmbientColor,
            ShadowMappingSettings shadowMappingSettings,
            MergeScenesNabor lastMergeNabor,
            List<VkComponent> components) {
        if (ppNaborChain != null) {
            ppNaborChain.run(
                    camera,
                    fog,
                    parallelLights,
                    parallelLightAmbientColor,
                    pointLights,
                    pointLightAmbientColor,
                    spotlights,
                    spotlightAmbientColor,
                    shadowMappingSettings,
                    lastMergeNabor,
                    components
            );
        }
    }

    public boolean isPassThroughScene() {
        return ppNaborChain == null;
    }

    public void transitionColorImageLayout() {
        if (ppNaborChain == null) {
            throw new RuntimeException("This operation is not allowed because this is a pass-through scene.");
        }

        ppNaborChain.transitionLastPPNaborColorImageLayout();
    }

    public long getColorImageView() {
        if (ppNaborChain == null) {
            throw new RuntimeException("This operation is not allowed because this is a pass-through scene.");
        }

        return ppNaborChain.getLastPPNaborColorImageView();
    }

    public void save(String srcImageFormat, String outputFilepath) throws IOException {
        ppNaborChain.save(srcImageFormat, outputFilepath);
    }
}
