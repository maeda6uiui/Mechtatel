package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.fseffect;

import com.github.maeda6uiui.mechtatel.core.MttShaderConfig;
import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.fseffect.FullScreenEffectNaborInfo;
import com.github.maeda6uiui.mechtatel.core.fseffect.FullScreenEffectProperties;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.fseffect.GaussianBlurInfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.fseffect.MonochromeEffectInfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.CommandBufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Chain of full-screen effect nabors
 *
 * @author maeda6uiui
 */
public class FullScreenEffectNaborChain {
    private VkDevice device;
    private long commandPool;
    private VkQueue graphicsQueue;

    private Map<String, FullScreenEffectNabor> fseNabors;
    private FullScreenEffectNabor lastFSENabor;

    private QuadDrawer quadDrawer;

    public FullScreenEffectNaborChain(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            int colorImageFormat,
            int samplerFilter,
            int samplerMipmapMode,
            int samplerAddressMode,
            VkExtent2D extent,
            List<String> naborNames,
            Map<String, FullScreenEffectNaborInfo> fseNaborInfos) {
        this.device = device;
        this.commandPool = commandPool;
        this.graphicsQueue = graphicsQueue;

        fseNabors = new LinkedHashMap<>();

        MttShaderConfig shaderConfig = MttShaderConfig
                .get()
                .orElse(MttShaderConfig.create());

        List<URL> gaussianBlurVertShaderResources = shaderConfig.fullScreenEffect.gaussianBlur.vertex.mustGetResourceURLs();
        List<URL> gaussianBlurFragShaderResources = shaderConfig.fullScreenEffect.gaussianBlur.fragment.mustGetResourceURLs();
        List<URL> monochromeEffectVertShaderResources = shaderConfig.fullScreenEffect.monochromeEffect.vertex.mustGetResourceURLs();
        List<URL> monochromeEffectFragShaderResources = shaderConfig.fullScreenEffect.monochromeEffect.fragment.mustGetResourceURLs();

        for (var naborName : naborNames) {
            FullScreenEffectNabor fseNabor;

            FullScreenEffectNaborInfo fseNaborInfo = fseNaborInfos
                    .entrySet()
                    .stream()
                    .filter(v -> v.getKey().equals(naborName))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            if (fseNaborInfo != null) {
                fseNabor = new FullScreenEffectNabor(
                        device,
                        fseNaborInfo.getVertShaderResources(),
                        fseNaborInfo.getFragShaderResources()
                );
            } else {
                fseNabor = switch (naborName) {
                    case "fse.gaussian_blur" ->
                            new GaussianBlurNabor(device, gaussianBlurVertShaderResources, gaussianBlurFragShaderResources);
                    case "fse.monochrome_effect" -> new MonochromeEffectNabor(
                            device, monochromeEffectVertShaderResources, monochromeEffectFragShaderResources);
                    default -> throw new IllegalArgumentException("Unknown nabor name specified: " + naborName);
                };
            }

            fseNabor.compile(
                    colorImageFormat,
                    samplerFilter,
                    samplerMipmapMode,
                    samplerAddressMode,
                    extent,
                    commandPool,
                    graphicsQueue,
                    1
            );

            fseNabors.put(naborName, fseNabor);
            lastFSENabor = fseNabor;
        }

        quadDrawer = new QuadDrawer(device, commandPool, graphicsQueue);
    }

    public void recreate(int imageFormat, VkExtent2D extent) {
        for (var fseNabor : fseNabors.values()) {
            fseNabor.recreate(imageFormat, extent);
        }
    }

    public void cleanup() {
        fseNabors.forEach((k, nabor) -> nabor.cleanup(false));
        quadDrawer.cleanup();
    }

    private void updateFSENaborUBOs(
            String naborName,
            FullScreenEffectNabor fseNabor,
            FullScreenEffectProperties fseProperties) {
        if (naborName.equals("fse.gaussian_blur")) {
            long gaussianBlurInfoUBOMemory = fseNabor.getUniformBufferMemory(0);
            var gaussianBlurInfoUBO = new GaussianBlurInfoUBO(fseProperties.gaussianBlurInfo);
            gaussianBlurInfoUBO.update(device, gaussianBlurInfoUBOMemory);
        } else if (naborName.equals("fse.monochrome_effect")) {
            long monochromeEffectInfoUBOMemory = fseNabor.getUniformBufferMemory(0);
            var monochromeEffectInfoUBO = new MonochromeEffectInfoUBO(fseProperties.monochromeEffectInfo);
            monochromeEffectInfoUBO.update(device, monochromeEffectInfoUBOMemory);
        }
    }

    public void run(FullScreenEffectProperties fseProperties, long baseColorImageView) {
        FullScreenEffectNabor previousFSENabor = null;
        for (var entry : fseNabors.entrySet()) {
            String naborName = entry.getKey();
            FullScreenEffectNabor fseNabor = entry.getValue();

            this.updateFSENaborUBOs(
                    naborName,
                    fseNabor,
                    fseProperties
            );

            try (MemoryStack stack = MemoryStack.stackPush()) {
                VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
                renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
                renderPassInfo.renderPass(fseNabor.getRenderPass());
                renderPassInfo.framebuffer(fseNabor.getFramebuffer(0));

                VkRect2D renderArea = VkRect2D.calloc(stack);
                renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
                renderArea.extent(fseNabor.getExtent());
                renderPassInfo.renderArea(renderArea);

                VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
                clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
                renderPassInfo.pClearValues(clearValues);

                VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

                vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
                {
                    vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, fseNabor.getGraphicsPipeline(0));

                    //First full-screen effect
                    if (previousFSENabor == null) {
                        fseNabor.bindImages(
                                commandBuffer,
                                1,
                                0,
                                List.of(baseColorImageView)
                        );
                    } else {
                        fseNabor.bindImages(
                                commandBuffer,
                                1,
                                0,
                                List.of(previousFSENabor.getColorImageView())
                        );
                    }

                    quadDrawer.draw(commandBuffer);
                }
                vkCmdEndRenderPass(commandBuffer);

                CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
            }

            fseNabor.transitionColorImageLayout(commandPool, graphicsQueue);
            previousFSENabor = fseNabor;
        }
    }

    public Map<String, List<Long>> getVertShaderModules() {
        var vertShaderModules = new HashMap<String, List<Long>>();

        for (var entry : fseNabors.entrySet()) {
            String naborName = entry.getKey();
            FullScreenEffectNabor fseNabor = entry.getValue();

            vertShaderModules.put(naborName, fseNabor.getVertShaderModules());
        }

        return vertShaderModules;
    }

    public Map<String, List<Long>> getFragShaderModules() {
        var fragShaderModules = new HashMap<String, List<Long>>();

        for (var entry : fseNabors.entrySet()) {
            String naborName = entry.getKey();
            FullScreenEffectNabor fseNabor = entry.getValue();

            fragShaderModules.put(naborName, fseNabor.getFragShaderModules());
        }

        return fragShaderModules;
    }

    public long getLastFSENaborColorImageView() {
        return lastFSENabor.getColorImageView();
    }

    public BufferedImage createBufferedImage(int imageIndex, PixelFormat pixelFormat) {
        return lastFSENabor.createBufferedImage(commandPool, graphicsQueue, imageIndex, pixelFormat);
    }
}
