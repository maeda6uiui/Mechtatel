package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.shadow;

import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.shadow.Pass1Info;
import com.github.maeda6uiui.mechtatel.core.shadow.Pass2Info;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowInfo;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.BufferCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.GBufferNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.PostProcessingNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow.Pass1InfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow.Pass2InfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow.ShadowInfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.CommandBufferUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.DepthResourceUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.ImageUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Runs ShadowMappingNabor
 *
 * @author maeda
 */
public class ShadowMappingNaborRunner {
    private static void runShadowMappingPass1(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            PostProcessingNabor shadowMappingNabor,
            Pass1Info pass1Info,
            List<VkComponent> components) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long pass1InfoUBOMemory = shadowMappingNabor.getUniformBufferMemory(0, 0);
            var pass1InfoUBO = new Pass1InfoUBO(pass1Info);
            pass1InfoUBO.update(device, pass1InfoUBOMemory);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(shadowMappingNabor.getRenderPass(0));
            renderPassInfo.framebuffer(shadowMappingNabor.getFramebuffer(0, 0));
            VkRect2D renderArea = VkRect2D.callocStack(stack);
            renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
            renderArea.extent(shadowMappingNabor.getExtent());
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.callocStack(2, stack);
            clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
            clearValues.get(1).depthStencil().set(1.0f, 0);
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
                    ByteBuffer matBuffer = stack.calloc(1 * 16 * Float.BYTES);
                    component.getMat().get(matBuffer);

                    vkCmdPushConstants(
                            commandBuffer,
                            shadowMappingNabor.getPipelineLayout(0, 0),
                            VK_SHADER_STAGE_VERTEX_BIT,
                            0,
                            matBuffer);

                    component.transfer(commandBuffer, shadowMappingNabor.getPipelineLayout(0, 0));
                }
            }
            vkCmdEndRenderPass(commandBuffer);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }

    private static void copyShadowMappingPass1Images(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            PostProcessingNabor shadowMappingNabor,
            int depthImageFormat,
            int index) {
        VkExtent2D extent = shadowMappingNabor.getExtent();

        long shadowCoordsSrcImage = shadowMappingNabor.getImage(0, ShadowMappingNabor.SHADOW_COORDS_ATTACHMENT_INDEX);
        long shadowDepthSrcImage = shadowMappingNabor.getImage(0, ShadowMappingNabor.SHADOW_DEPTH_ATTACHMENT_INDEX);

        ImageUtils.transitionImageLayout(
                device,
                commandPool,
                graphicsQueue,
                shadowCoordsSrcImage,
                false,
                VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                1);
        ImageUtils.transitionImageLayout(
                device,
                commandPool,
                graphicsQueue,
                shadowDepthSrcImage,
                DepthResourceUtils.hasStencilComponent(depthImageFormat),
                VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
                VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                1);

        long shadowCoordsDstImage = shadowMappingNabor.getUserDefImage(index);
        long shadowDepthDstImage = shadowMappingNabor.getUserDefImage(index + ShadowMappingNabor.MAX_NUM_SHADOW_MAPS);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            //Copy shadow coords image
            VkImageCopy.Buffer imageCopyRegion = VkImageCopy.callocStack(1, stack);
            imageCopyRegion.srcSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            imageCopyRegion.srcSubresource().layerCount(1);
            imageCopyRegion.dstSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            imageCopyRegion.dstSubresource().layerCount(1);
            imageCopyRegion.extent(VkExtent3D.callocStack(stack).set(extent.width(), extent.height(), 1));

            vkCmdCopyImage(
                    commandBuffer,
                    shadowCoordsSrcImage,
                    VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    shadowCoordsDstImage,
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    imageCopyRegion);

            //Copy shadow depth image
            VkBufferImageCopy.Buffer bufferImageCopy = VkBufferImageCopy.callocStack(1, stack);
            bufferImageCopy.bufferOffset(0);
            bufferImageCopy.imageSubresource().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);
            bufferImageCopy.imageSubresource().layerCount(1);
            bufferImageCopy.imageExtent(VkExtent3D.callocStack(stack).set(extent.width(), extent.height(), 1));

            LongBuffer pBuffer = stack.mallocLong(1);
            LongBuffer pBufferMemory = stack.mallocLong(1);

            long bufferSize = extent.width() * extent.height() * Float.BYTES;

            BufferCreator.createBuffer(
                    device,
                    bufferSize,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pBuffer,
                    pBufferMemory);
            long buffer = pBuffer.get(0);
            long bufferMemory = pBufferMemory.get(0);

            vkCmdCopyImageToBuffer(
                    commandBuffer,
                    shadowDepthSrcImage,
                    VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    buffer,
                    bufferImageCopy);

            bufferImageCopy.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);

            vkCmdCopyBufferToImage(
                    commandBuffer,
                    buffer,
                    shadowDepthDstImage,
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    bufferImageCopy);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);

            vkDestroyBuffer(device, buffer, null);
            vkFreeMemory(device, bufferMemory, null);
        }
    }

    private static void runShadowMappingPass2(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            GBufferNabor gBufferNabor,
            PostProcessingNabor lastPPNabor,
            PostProcessingNabor shadowMappingNabor,
            int parallelLightShadowMapCount,
            int spotlightShadowMapCount,
            List<ParallelLight> parallelLights,
            List<Spotlight> spotlights,
            QuadDrawer quadDrawer) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long pass2InfoUBOMemory = shadowMappingNabor.getUniformBufferMemory(1, 0);
            var pass2Info = new Pass2Info();
            pass2Info.setNumShadowMaps(parallelLightShadowMapCount + spotlightShadowMapCount);
            var pass2InfoUBO = new Pass2InfoUBO(pass2Info);
            pass2InfoUBO.update(device, pass2InfoUBOMemory);

            long shadowInfosUBOMemory = shadowMappingNabor.getUniformBufferMemory(1, 1);
            for (int i = 0; i < parallelLightShadowMapCount; i++) {
                var shadowInfo = new ShadowInfo();
                ParallelLight parallelLight = parallelLights.get(i);

                shadowInfo.setProjectionType(ShadowInfo.PROJECTION_TYPE_ORTHOGRAPHIC);
                shadowInfo.setLightDirection(parallelLight.getDirection());

                var shadowInfoUBO = new ShadowInfoUBO(shadowInfo);
                shadowInfoUBO.update(device, shadowInfosUBOMemory, i);
            }
            for (int i = 0; i < spotlightShadowMapCount; i++) {
                var shadowInfo = new ShadowInfo();
                Spotlight spotlight = spotlights.get(i);

                shadowInfo.setProjectionType(ShadowInfo.PROJECTION_TYPE_PERSPECTIVE);
                shadowInfo.setLightDirection(spotlight.getDirection());

                var shadowInfoUBO = new ShadowInfoUBO(shadowInfo);
                shadowInfoUBO.update(device, shadowInfosUBOMemory, i + parallelLightShadowMapCount);
            }

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(shadowMappingNabor.getRenderPass(1));
            renderPassInfo.framebuffer(shadowMappingNabor.getFramebuffer(1, 0));
            VkRect2D renderArea = VkRect2D.callocStack(stack);
            renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
            renderArea.extent(shadowMappingNabor.getExtent());
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.callocStack(1, stack);
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
                if (lastPPNabor == null) {
                    gBufferNabor.transitionAlbedoImage(commandPool, graphicsQueue);
                    gBufferNabor.transitionDepthImage(commandPool, graphicsQueue);
                    gBufferNabor.transitionPositionImage(commandPool, graphicsQueue);
                    gBufferNabor.transitionNormalImage(commandPool, graphicsQueue);

                    shadowMappingNabor.bindImages(
                            commandBuffer,
                            1,
                            0,
                            gBufferNabor.getAlbedoImageView(),
                            gBufferNabor.getDepthImageView(),
                            gBufferNabor.getPositionImageView(),
                            gBufferNabor.getNormalImageView());
                } else {
                    lastPPNabor.transitionColorImageLayout(commandPool, graphicsQueue);

                    shadowMappingNabor.bindImages(
                            commandBuffer,
                            1,
                            0,
                            lastPPNabor.getColorImageView(),
                            gBufferNabor.getDepthImageView(),
                            gBufferNabor.getPositionImageView(),
                            gBufferNabor.getNormalImageView());
                }

                int numShadowMaps = parallelLightShadowMapCount + spotlightShadowMapCount;

                var shadowCoordsImageViews = new ArrayList<Long>();
                for (int i = 0; i < numShadowMaps; i++) {
                    shadowCoordsImageViews.add(shadowMappingNabor.getUserDefImageView(i));
                }

                var shadowDepthImageViews = new ArrayList<Long>();
                for (int i = 0; i < numShadowMaps; i++) {
                    shadowDepthImageViews.add(
                            shadowMappingNabor.getUserDefImageView(ShadowMappingNabor.MAX_NUM_SHADOW_MAPS + i));
                }

                shadowMappingNabor.bindImages(
                        commandBuffer,
                        1,
                        4,
                        shadowCoordsImageViews);
                shadowMappingNabor.bindImages(
                        commandBuffer,
                        1,
                        5,
                        shadowDepthImageViews);

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
            GBufferNabor gBufferNabor,
            PostProcessingNabor lastPPNabor,
            PostProcessingNabor shadowMappingNabor,
            List<ParallelLight> parallelLights,
            List<Spotlight> spotlights,
            List<VkComponent> components,
            QuadDrawer quadDrawer,
            int depthImageFormat) {
        //Pass 1
        int parallelLightShadowMapCount = 0;
        for (var parallelLight : parallelLights) {
            if (parallelLightShadowMapCount >= ShadowMappingNabor.MAX_NUM_SHADOW_MAPS) {
                break;
            }

            var pass1Info = new Pass1Info(parallelLight);
            runShadowMappingPass1(device, commandPool, graphicsQueue, shadowMappingNabor, pass1Info, components);
            copyShadowMappingPass1Images(
                    device,
                    commandPool,
                    graphicsQueue,
                    shadowMappingNabor,
                    depthImageFormat,
                    parallelLightShadowMapCount);

            parallelLightShadowMapCount++;
        }
        int spotlightShadowMapCount = 0;
        for (var spotlight : spotlights) {
            if (parallelLightShadowMapCount + spotlightShadowMapCount >= ShadowMappingNabor.MAX_NUM_SHADOW_MAPS) {
                break;
            }

            var pass1Info = new Pass1Info(spotlight);
            runShadowMappingPass1(device, commandPool, graphicsQueue, shadowMappingNabor, pass1Info, components);
            copyShadowMappingPass1Images(
                    device,
                    commandPool,
                    graphicsQueue,
                    shadowMappingNabor,
                    depthImageFormat,
                    ShadowMappingNabor.MAX_NUM_SHADOW_MAPS + spotlightShadowMapCount);

            spotlightShadowMapCount++;
        }

        //Pass 2
        runShadowMappingPass2(
                device,
                commandPool,
                graphicsQueue,
                gBufferNabor,
                lastPPNabor,
                shadowMappingNabor,
                parallelLightShadowMapCount,
                spotlightShadowMapCount,
                parallelLights,
                spotlights,
                quadDrawer);
    }
}
