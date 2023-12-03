package com.github.maeda6uiui.mechtatel.core.vulkan.screen;

import com.github.maeda6uiui.mechtatel.core.postprocessing.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.shadow.Pass1Info;
import com.github.maeda6uiui.mechtatel.core.shadow.Pass2Info;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowInfo;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.MergeScenesNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.PostProcessingNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.shadow.ShadowMappingNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow.Pass1InfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow.Pass2InfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow.ShadowInfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.CommandBufferUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.ImageUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Runs ShadowMappingNabor
 *
 * @author maeda6uiui
 */
public class ShadowMappingNaborRunner {
    private static void runShadowMappingPass1(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            PostProcessingNabor shadowMappingNabor,
            Pass1Info pass1Info,
            List<VkMttComponent> components) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long pass1InfoUBOMemory = shadowMappingNabor.getUniformBufferMemory(0, 0);
            var pass1InfoUBO = new Pass1InfoUBO(pass1Info);
            pass1InfoUBO.update(device, pass1InfoUBOMemory);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(shadowMappingNabor.getRenderPass(0));
            renderPassInfo.framebuffer(shadowMappingNabor.getFramebuffer(0, 0));
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(shadowMappingNabor.getExtent(0));
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
            clearValues.get(0).depthStencil().set(1.0f, 0);
            renderPassInfo.pClearValues(clearValues);

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(
                        commandBuffer,
                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                        shadowMappingNabor.getGraphicsPipeline(0, 0));

                vkCmdBindDescriptorSets(
                        commandBuffer,
                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                        shadowMappingNabor.getPipelineLayout(0, 0),
                        0,
                        shadowMappingNabor.pDescriptorSets(0),
                        null);

                for (var component : components) {
                    if (component.isTwoDComponent()) {
                        continue;
                    }
                    if (!component.shouldCastShadow()) {
                        continue;
                    }

                    ByteBuffer matBuffer = stack.calloc(1 * 16 * Float.BYTES);
                    component.getMat().get(matBuffer);

                    vkCmdPushConstants(
                            commandBuffer,
                            shadowMappingNabor.getPipelineLayout(0, 0),
                            VK_SHADER_STAGE_VERTEX_BIT,
                            0,
                            matBuffer);

                    component.transfer(commandBuffer);
                }
            }
            vkCmdEndRenderPass(commandBuffer);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }

    private static void copyDepthImage(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            PostProcessingNabor shadowMappingNabor,
            int shadowMapIndex,
            int depthImageAspect) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkExtent2D extent = shadowMappingNabor.getExtent(0);

            VkImageCopy.Buffer imageCopyRegion = VkImageCopy.calloc(1, stack);
            imageCopyRegion.srcSubresource().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);
            imageCopyRegion.srcSubresource().layerCount(1);
            imageCopyRegion.dstSubresource().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);
            imageCopyRegion.dstSubresource().layerCount(1);
            imageCopyRegion.extent(VkExtent3D.calloc(stack).set(extent.width(), extent.height(), 1));

            long depthSrcImage = shadowMappingNabor.getImage(0, 0);
            long depthDstImage = shadowMappingNabor.getUserDefImage(shadowMapIndex);

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            ImageUtils.transitionImageLayout(
                    commandBuffer,
                    depthSrcImage,
                    depthImageAspect,
                    VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
                    VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    1);
            ImageUtils.transitionImageLayout(
                    commandBuffer,
                    depthDstImage,
                    depthImageAspect,
                    VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    1);

            vkCmdCopyImage(
                    commandBuffer,
                    depthSrcImage,
                    VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    depthDstImage,
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    imageCopyRegion);

            ImageUtils.transitionImageLayout(
                    commandBuffer,
                    depthDstImage,
                    depthImageAspect,
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                    1);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }

    private static void runShadowMappingPass2(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            MergeScenesNabor lastMergeNabor,
            PostProcessingNabor previousPPNabor,
            PostProcessingNabor shadowMappingNabor,
            List<ParallelLight> shadowParallelLights,
            List<Spotlight> shadowSpotlights,
            ShadowMappingSettings settings,
            QuadDrawer quadDrawer) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long pass2InfoUBOMemory = shadowMappingNabor.getUniformBufferMemory(1, 0);
            var pass2Info = new Pass2Info(settings);
            pass2Info.setNumShadowMaps(shadowParallelLights.size() + shadowSpotlights.size());
            var pass2InfoUBO = new Pass2InfoUBO(pass2Info);
            pass2InfoUBO.update(device, pass2InfoUBOMemory);

            long shadowInfosUBOMemory = shadowMappingNabor.getUniformBufferMemory(1, 1);

            for (int i = 0; i < shadowParallelLights.size(); i++) {
                ParallelLight parallelLight = shadowParallelLights.get(i);

                var shadowInfo = new ShadowInfo(parallelLight);
                var shadowInfoUBO = new ShadowInfoUBO(shadowInfo);
                shadowInfoUBO.update(device, shadowInfosUBOMemory, i);
            }
            for (int i = 0; i < shadowSpotlights.size(); i++) {
                Spotlight spotlight = shadowSpotlights.get(i);

                var shadowInfo = new ShadowInfo(spotlight);
                var shadowInfoUBO = new ShadowInfoUBO(shadowInfo);
                shadowInfoUBO.update(device, shadowInfosUBOMemory, shadowParallelLights.size() + i);
            }

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(shadowMappingNabor.getRenderPass(1));
            renderPassInfo.framebuffer(shadowMappingNabor.getFramebuffer(1, 0));
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(shadowMappingNabor.getExtent());
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
            clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
            renderPassInfo.pClearValues(clearValues);

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(
                        commandBuffer,
                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                        shadowMappingNabor.getGraphicsPipeline(1, 0));

                //First post-processing
                if (previousPPNabor == null) {
                    shadowMappingNabor.bindImages(
                            commandBuffer,
                            1,
                            0,
                            lastMergeNabor.getAlbedoImageView(),
                            lastMergeNabor.getDepthImageView(),
                            lastMergeNabor.getPositionImageView(),
                            lastMergeNabor.getNormalImageView());
                } else {
                    previousPPNabor.transitionColorImageLayout(commandPool, graphicsQueue);

                    shadowMappingNabor.bindImages(
                            commandBuffer,
                            1,
                            0,
                            previousPPNabor.getColorImageView(),
                            lastMergeNabor.getDepthImageView(),
                            lastMergeNabor.getPositionImageView(),
                            lastMergeNabor.getNormalImageView());
                }

                int numShadowMaps = shadowParallelLights.size() + shadowSpotlights.size();
                var shadowDepthImageViews = new ArrayList<Long>();
                for (int i = 0; i < numShadowMaps; i++) {
                    shadowDepthImageViews.add(shadowMappingNabor.getUserDefImageView(i));
                }
                if (shadowDepthImageViews.size() != 0) {
                    shadowMappingNabor.bindImages(
                            commandBuffer,
                            1,
                            1,
                            4,
                            shadowDepthImageViews);
                }

                quadDrawer.draw(commandBuffer);
            }
            vkCmdEndRenderPass(commandBuffer);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }

    public static void runShadowMappingNabor(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            MergeScenesNabor lastMergeNabor,
            PostProcessingNabor previousPPNabor,
            PostProcessingNabor shadowMappingNabor,
            List<ParallelLight> parallelLights,
            List<Spotlight> spotlights,
            List<VkMttComponent> components,
            int depthImageAspect,
            ShadowMappingSettings settings,
            QuadDrawer quadDrawer) {
        //Pass 1
        var shadowParallelLights = new ArrayList<ParallelLight>();

        for (var parallelLight : parallelLights) {
            if (shadowParallelLights.size() >= ShadowMappingNabor.MAX_NUM_SHADOW_MAPS) {
                break;
            }
            if (!parallelLight.isCastShadow()) {
                continue;
            }

            var pass1Info = new Pass1Info(parallelLight);
            runShadowMappingPass1(device, commandPool, graphicsQueue, shadowMappingNabor, pass1Info, components);
            copyDepthImage(
                    device,
                    commandPool,
                    graphicsQueue,
                    shadowMappingNabor,
                    shadowParallelLights.size(),
                    depthImageAspect);

            shadowParallelLights.add(parallelLight);
        }

        var shadowSpotlights = new ArrayList<Spotlight>();

        for (var spotlight : spotlights) {
            if (shadowSpotlights.size() + shadowParallelLights.size() >= ShadowMappingNabor.MAX_NUM_SHADOW_MAPS) {
                break;
            }
            if (!spotlight.isCastShadow()) {
                continue;
            }

            var pass1Info = new Pass1Info(spotlight);
            runShadowMappingPass1(device, commandPool, graphicsQueue, shadowMappingNabor, pass1Info, components);
            copyDepthImage(
                    device,
                    commandPool,
                    graphicsQueue,
                    shadowMappingNabor,
                    shadowParallelLights.size() + shadowSpotlights.size(),
                    depthImageAspect);

            shadowSpotlights.add(spotlight);
        }

        //Pass 2
        runShadowMappingPass2(
                device,
                commandPool,
                graphicsQueue,
                lastMergeNabor,
                previousPPNabor,
                shadowMappingNabor,
                shadowParallelLights,
                shadowSpotlights,
                settings,
                quadDrawer);

        shadowMappingNabor.transitionColorImageLayout(commandPool, graphicsQueue);
    }
}
