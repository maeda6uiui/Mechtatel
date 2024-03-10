package com.github.maeda6uiui.mechtatel.core.vulkan.operation;

import com.github.maeda6uiui.mechtatel.core.operation.BiTextureOperationParameters;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.BiTextureOperationNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.TextureOperationInfo;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.texture.VkMttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.BiTextureOperationParametersUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.CommandBufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Vulkan implementation of texture operation that consumes two textures
 *
 * @author maeda6uiui
 */
public class VkBiTextureOperation {
    private VkDevice device;
    private long commandPool;
    private VkQueue graphicsQueue;
    private BiTextureOperationNabor biTextureOperationNabor;

    private VkMttTexture resultTexture;
    private TextureOperationInfo textureOperationInfo;

    private QuadDrawer quadDrawer;

    public VkBiTextureOperation(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            BiTextureOperationNabor biTextureOperationNabor,
            int imageFormat,
            List<VkMttTexture> colorTextures,
            List<VkMttTexture> depthTextures,
            VkMttScreen dstScreen) {
        this.device = device;
        this.commandPool = commandPool;
        this.graphicsQueue = graphicsQueue;
        this.biTextureOperationNabor = biTextureOperationNabor;

        VkExtent2D extent = biTextureOperationNabor.getExtent();
        long dstImage = biTextureOperationNabor.createUserDefImage(
                extent.width(),
                extent.height(),
                1,
                VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                imageFormat,
                VK_IMAGE_ASPECT_COLOR_BIT
        );
        long dstImageView = biTextureOperationNabor.lookUpUserDefImageView(dstImage);
        resultTexture = new VkMttTexture(device, dstScreen, dstImageView);

        var colorImageViews = new ArrayList<Long>();
        colorTextures.forEach(v -> colorImageViews.add(v.getTextureImageView()));

        var depthImageViews = new ArrayList<Long>();
        depthTextures.forEach(v -> depthImageViews.add(v.getTextureImageView()));

        textureOperationInfo = new TextureOperationInfo(
                colorImageViews,
                depthImageViews,
                dstImage,
                dstImageView
        );

        quadDrawer = new QuadDrawer(device, commandPool, graphicsQueue);
    }

    public void cleanup() {
        resultTexture.cleanup();
        quadDrawer.cleanup();
    }

    public VkMttTexture getResultTexture() {
        return resultTexture;
    }

    public void run(BiTextureOperationParameters parameters) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(biTextureOperationNabor.getRenderPass());
            renderPassInfo.framebuffer(biTextureOperationNabor.getFramebuffer(0));

            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(biTextureOperationNabor.getExtent());
            renderPassInfo.renderArea(renderArea);

            VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
            clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
            renderPassInfo.pClearValues(clearValues);

            long parametersUBOMemory = biTextureOperationNabor.getUniformBufferMemory(0);
            var parametersUBO = new BiTextureOperationParametersUBO(parameters);
            parametersUBO.update(device, parametersUBOMemory);

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(
                        commandBuffer,
                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                        biTextureOperationNabor.getGraphicsPipeline(0));

                biTextureOperationNabor.bindColorImages(commandBuffer, textureOperationInfo.colorImageViews());
                biTextureOperationNabor.bindDepthImages(commandBuffer, textureOperationInfo.depthImageViews());

                quadDrawer.draw(commandBuffer);
            }
            vkCmdEndRenderPass(commandBuffer);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);

            biTextureOperationNabor.copyColorImage(commandPool, graphicsQueue, textureOperationInfo.dstImage());
        }
    }
}
