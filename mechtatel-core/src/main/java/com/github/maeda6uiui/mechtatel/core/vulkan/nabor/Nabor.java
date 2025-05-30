package com.github.maeda6uiui.mechtatel.core.vulkan.nabor;

import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.util.FilenameUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.cache.ShaderBuildCacheManager;
import com.github.maeda6uiui.mechtatel.core.vulkan.shader.SPIRVUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.shader.ShaderKind;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.BufferUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.CommandBufferUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.ImageUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Set of necessary objects for rendering with Vulkan
 *
 * @author maeda6uiui
 */
public abstract class Nabor {
    private VkDevice device;

    private int msaaSamples;
    private VkExtent2D extent;

    private URL vertShaderResource;
    private URL fragShaderResource;

    private boolean isContainer;
    private boolean isBorrowedShaderModules;

    private long dummyImage;
    private long dummyImageMemory;
    private long dummyImageView;

    private List<Long> textureSamplers;

    private List<Long> uniformBuffers;
    private List<Long> uniformBufferMemories;

    private long renderPass;
    private List<Long> descriptorSetLayouts;
    private List<Long> descriptorPools;
    private List<Long> descriptorSets;
    private List<Long> vertShaderModules;
    private List<Long> fragShaderModules;
    private List<Long> pipelineLayouts;
    private List<Long> graphicsPipelines;

    private List<Long> images;
    private List<Long> imageMemories;
    private List<Long> imageViews;
    private List<Long> framebuffers;

    private List<Long> userDefImages;
    private List<Long> userDefImageMemories;
    private List<Long> userDefImageViews;

    private int setCount;

    public Nabor(
            VkDevice device,
            int msaaSamples,
            boolean isContainer,
            URL vertShaderResource,
            URL fragShaderResource) {
        this.device = device;

        this.msaaSamples = msaaSamples;
        this.isContainer = isContainer;
        isBorrowedShaderModules = false;

        this.vertShaderResource = vertShaderResource;
        this.fragShaderResource = fragShaderResource;

        textureSamplers = new ArrayList<>();

        uniformBuffers = new ArrayList<>();
        uniformBufferMemories = new ArrayList<>();

        descriptorSetLayouts = new ArrayList<>();
        descriptorPools = new ArrayList<>();
        descriptorSets = new ArrayList<>();
        vertShaderModules = new ArrayList<>();
        fragShaderModules = new ArrayList<>();
        pipelineLayouts = new ArrayList<>();
        graphicsPipelines = new ArrayList<>();

        images = new ArrayList<>();
        imageMemories = new ArrayList<>();
        imageViews = new ArrayList<>();
        framebuffers = new ArrayList<>();

        userDefImages = new ArrayList<>();
        userDefImageMemories = new ArrayList<>();
        userDefImageViews = new ArrayList<>();
    }

    //===== Getters and setters =====
    protected VkDevice getDevice() {
        return device;
    }

    public int getMsaaSamples() {
        return msaaSamples;
    }

    public int getMsaaSamples(int naborIndex) {
        throw new UnsupportedOperationException();
    }

    public VkExtent2D getExtent() {
        return extent;
    }

    public VkExtent2D getExtent(int naborIndex) {
        throw new UnsupportedOperationException();
    }

    public URL getVertShaderResource() {
        return vertShaderResource;
    }

    public URL getFragShaderResource() {
        return fragShaderResource;
    }

    public boolean isContainer() {
        return isContainer;
    }

    public boolean isContainer(int naborIndex) {
        throw new UnsupportedOperationException();
    }

    protected long getDummyImage() {
        return dummyImage;
    }

    protected long getDummyImageMemory() {
        return dummyImageMemory;
    }

    public long getDummyImageView() {
        return dummyImageView;
    }

    protected List<Long> getTextureSamplers() {
        return textureSamplers;
    }

    public long getTextureSampler(int index) {
        return textureSamplers.get(index);
    }

    public long getTextureSampler(int naborIndex, int arrayIndex) {
        throw new UnsupportedOperationException();
    }

    protected List<Long> getUniformBuffers() {
        return uniformBuffers;
    }

    public long getUniformBuffer(int index) {
        return uniformBuffers.get(index);
    }

    public long getUniformBuffer(int naborIndex, int arrayIndex) {
        throw new UnsupportedOperationException();
    }

    protected List<Long> getUniformBufferMemories() {
        return uniformBufferMemories;
    }

    public long getUniformBufferMemory(int index) {
        return uniformBufferMemories.get(index);
    }

    public long getUniformBufferMemory(int naborIndex, int arrayIndex) {
        throw new UnsupportedOperationException();
    }

    public long getRenderPass() {
        return renderPass;
    }

    public long getRenderPass(int naborIndex) {
        throw new UnsupportedOperationException();
    }

    protected void setRenderPass(long renderPass) {
        this.renderPass = renderPass;
    }

    protected List<Long> getDescriptorSetLayouts() {
        return descriptorSetLayouts;
    }

    public long getDescriptorSetLayout(int index) {
        return descriptorSetLayouts.get(index);
    }

    public long getDescriptorSetLayout(int naborIndex, int arrayIndex) {
        throw new UnsupportedOperationException();
    }

    protected LongBuffer pDescriptorSetLayouts() {
        LongBuffer pLayouts = MemoryStack.stackGet().mallocLong(descriptorSetLayouts.size());
        for (int i = 0; i < descriptorSetLayouts.size(); i++) {
            pLayouts.put(i, descriptorSetLayouts.get(i));
        }

        return pLayouts;
    }

    protected List<Long> getDescriptorPools() {
        return descriptorPools;
    }

    public long getDescriptorPool(int index) {
        return descriptorPools.get(index);
    }

    public long getDescriptorPool(int naborIndex, int arrayIndex) {
        throw new UnsupportedOperationException();
    }

    public int getNumDescriptorSets() {
        return descriptorSets.size();
    }

    public int getNumDescriptorSets(int naborIndex) {
        throw new UnsupportedOperationException();
    }

    protected List<Long> getDescriptorSets() {
        return descriptorSets;
    }

    public long getDescriptorSet(int index) {
        return descriptorSets.get(index);
    }

    public long getDescriptorSet(int naborIndex, int arrayIndex) {
        throw new UnsupportedOperationException();
    }

    public LongBuffer pDescriptorSets() {
        LongBuffer pSets = MemoryStack.stackGet().mallocLong(descriptorSets.size());
        for (int i = 0; i < descriptorSets.size(); i++) {
            pSets.put(i, descriptorSets.get(i));
        }

        return pSets;
    }

    public LongBuffer pDescriptorSets(int naborIndex) {
        throw new UnsupportedOperationException();
    }

    public List<Long> getVertShaderModules() {
        return new ArrayList<>(vertShaderModules);
    }

    public long getVertShaderModule(int index) {
        return vertShaderModules.get(index);
    }

    protected void addVertShaderModule(long vertShaderModule) {
        vertShaderModules.add(vertShaderModule);
    }

    public List<Long> getFragShaderModules() {
        return new ArrayList<>(fragShaderModules);
    }

    public long getFragShaderModule(int index) {
        return fragShaderModules.get(index);
    }

    protected void addFragShaderModule(long fragShaderModule) {
        fragShaderModules.add(fragShaderModule);
    }

    protected List<Long> getPipelineLayouts() {
        return pipelineLayouts;
    }

    public long getPipelineLayout(int index) {
        return pipelineLayouts.get(index);
    }

    public long getPipelineLayout(int naborIndex, int arrayIndex) {
        throw new UnsupportedOperationException();
    }

    protected List<Long> getGraphicsPipelines() {
        return graphicsPipelines;
    }

    public long getGraphicsPipeline(int index) {
        return graphicsPipelines.get(index);
    }

    public long getGraphicsPipeline(int naborIndex, int arrayIndex) {
        throw new UnsupportedOperationException();
    }

    protected List<Long> getImages() {
        return images;
    }

    public long getImage(int index) {
        return images.get(index);
    }

    public long getImage(int naborIndex, int arrayIndex) {
        throw new UnsupportedOperationException();
    }

    protected List<Long> getImageMemories() {
        return imageMemories;
    }

    public long getImageMemory(int index) {
        return imageMemories.get(index);
    }

    public long getImageMemory(int naborIndex, int arrayIndex) {
        throw new UnsupportedOperationException();
    }

    protected List<Long> getImageViews() {
        return imageViews;
    }

    public long getImageView(int index) {
        return imageViews.get(index);
    }

    public long getImageView(int naborIndex, int arrayIndex) {
        throw new UnsupportedOperationException();
    }

    protected List<Long> getUserDefImages() {
        return userDefImages;
    }

    public long getUserDefImage(int index) {
        return userDefImages.get(index);
    }

    public long getUserDefImage(int naborIndex, int arrayIndex) {
        throw new UnsupportedOperationException();
    }

    protected List<Long> getUserDefImageMemories() {
        return userDefImageMemories;
    }

    public long getUserDefImageMemory(int index) {
        return userDefImageMemories.get(index);
    }

    public long getUserDefImageMemory(int naborIndex, int arrayIndex) {
        throw new UnsupportedOperationException();
    }

    protected List<Long> getUserDefImageViews() {
        return userDefImageViews;
    }

    public long getUserDefImageView(int index) {
        return userDefImageViews.get(index);
    }

    public long getUserDefImageView(int naborIndex, int arrayIndex) {
        throw new UnsupportedOperationException();
    }

    public int getNumFramebuffers() {
        return framebuffers.size();
    }

    public int getNumFramebuffers(int naborIndex) {
        throw new UnsupportedOperationException();
    }

    public long getFramebuffer(int index) {
        return framebuffers.get(index);
    }

    public long getFramebuffer(int naborIndex, int arrayIndex) {
        throw new UnsupportedOperationException();
    }

    protected List<Long> getFramebuffers() {
        return framebuffers;
    }

    public int getSetCount() {
        return setCount;
    }

    public int getSetCount(int naborIndex) {
        throw new UnsupportedOperationException();
    }

    protected void setSetCount(int setCount) {
        this.setCount = setCount;
    }

    //===== Methods executed in compile() and recreate() =====
    protected void createTextureSamplers(int filter, int mipmapMode, int addressMode) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.calloc(stack);
            samplerInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
            samplerInfo.magFilter(filter);
            samplerInfo.minFilter(filter);
            samplerInfo.addressModeU(addressMode);
            samplerInfo.addressModeV(addressMode);
            samplerInfo.addressModeW(addressMode);
            samplerInfo.anisotropyEnable(true);
            samplerInfo.maxAnisotropy(16.0f);
            samplerInfo.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK);
            samplerInfo.unnormalizedCoordinates(false);
            samplerInfo.compareEnable(false);
            samplerInfo.compareOp(VK_COMPARE_OP_ALWAYS);
            samplerInfo.mipmapMode(mipmapMode);
            samplerInfo.minLod(0.0f);
            samplerInfo.maxLod(10.0f);
            samplerInfo.mipLodBias(0.0f);

            LongBuffer pTextureSampler = stack.mallocLong(1);
            if (vkCreateSampler(device, samplerInfo, null, pTextureSampler) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a texture sampler");
            }

            long textureSampler = pTextureSampler.get(0);
            textureSamplers.add(textureSampler);
        }
    }

    protected void createUniformBuffers(int descriptorCount) {

    }

    protected void createRenderPass(int colorImageFormat) {

    }

    protected void createDescriptorSetLayouts() {

    }

    protected void createDescriptorPools(int descriptorCount) {

    }

    private void createDummyImage(long commandPool, VkQueue graphicsQueue) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            LongBuffer pImage = stack.mallocLong(1);
            LongBuffer pImageMemory = stack.mallocLong(1);
            LongBuffer pImageView = stack.mallocLong(1);

            //Color image
            ImageUtils.createImage(
                    device,
                    1,
                    1,
                    1,
                    1,
                    VK_FORMAT_R8_UNORM,
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_SAMPLED_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pImage,
                    pImageMemory);
            dummyImage = pImage.get(0);
            dummyImageMemory = pImageMemory.get(0);

            ImageUtils.transitionImageLayout(
                    device,
                    commandPool,
                    graphicsQueue,
                    dummyImage,
                    VK_IMAGE_ASPECT_COLOR_BIT,
                    VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                    1);

            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc(stack);
            viewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            viewInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
            viewInfo.image(dummyImage);
            viewInfo.format(VK_FORMAT_R8_UNORM);
            viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            viewInfo.subresourceRange().baseMipLevel(0);
            viewInfo.subresourceRange().levelCount(1);
            viewInfo.subresourceRange().baseArrayLayer(0);
            viewInfo.subresourceRange().layerCount(1);

            if (vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create an image view");
            }
            dummyImageView = pImageView.get(0);
        }
    }

    protected void createDescriptorSets(int descriptorCount, long commandPool, VkQueue graphicsQueue) {

    }

    protected void createGraphicsPipelines() {

    }

    protected void createImages(int colorImageFormat) {

    }

    protected void createFramebuffers() {
        if (imageViews.size() == 0) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();
            VkExtent2D extent = this.getExtent();

            long renderPass = this.getRenderPass();

            List<Long> imageViews = this.getImageViews();
            LongBuffer attachments = stack.mallocLong(imageViews.size());
            for (int i = 0; i < imageViews.size(); i++) {
                attachments.put(i, imageViews.get(i));
            }

            LongBuffer pFramebuffer = stack.mallocLong(1);

            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack);
            framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferInfo.renderPass(renderPass);
            framebufferInfo.width(extent.width());
            framebufferInfo.height(extent.height());
            framebufferInfo.layers(1);
            framebufferInfo.pAttachments(attachments);

            if (vkCreateFramebuffer(device, framebufferInfo, null, pFramebuffer) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a framebuffer");
            }

            this.getFramebuffers().add(pFramebuffer.get(0));
        }
    }

    //==========
    public void compile(
            int colorImageFormat,
            int samplerFilter,
            int samplerMipmapMode,
            int samplerAddressMode,
            VkExtent2D extent,
            long commandPool,
            VkQueue graphicsQueue,
            int descriptorCount) {
        this.extent = extent;

        if (isContainer) {
            return;
        }

        this.createTextureSamplers(samplerFilter, samplerMipmapMode, samplerAddressMode);
        this.createUniformBuffers(descriptorCount);
        this.createRenderPass(colorImageFormat);
        this.createDescriptorSetLayouts();
        this.createDescriptorPools(descriptorCount);
        this.createDummyImage(commandPool, graphicsQueue);
        this.createDescriptorSets(descriptorCount, commandPool, graphicsQueue);
        this.createGraphicsPipelines();
        this.createImages(colorImageFormat);
        this.createFramebuffers();
    }

    public void recreate(int colorImageFormat, VkExtent2D extent) {
        this.extent = extent;

        if (isContainer) {
            return;
        }

        this.cleanup(true);

        this.createRenderPass(colorImageFormat);
        this.createGraphicsPipelines();
        this.createImages(colorImageFormat);
        this.createFramebuffers();
    }

    private void cleanupContainer(boolean reserveForRecreation) {
        if (!reserveForRecreation) {
            this.cleanupUserDefImages();
        }
    }

    private void cleanupSelf(boolean reserveForRecreation) {
        graphicsPipelines.forEach(graphicsPipeline -> vkDestroyPipeline(device, graphicsPipeline, null));
        pipelineLayouts.forEach(pipelineLayout -> vkDestroyPipelineLayout(device, pipelineLayout, null));
        graphicsPipelines.clear();
        pipelineLayouts.clear();

        images.forEach(image -> vkDestroyImage(device, image, null));
        imageMemories.forEach(imageMemory -> vkFreeMemory(device, imageMemory, null));
        imageViews.forEach(imageView -> vkDestroyImageView(device, imageView, null));
        framebuffers.forEach(framebuffer -> vkDestroyFramebuffer(device, framebuffer, null));
        images.clear();
        imageMemories.clear();
        imageViews.clear();
        framebuffers.clear();

        if (!reserveForRecreation) {
            vkDestroyImage(device, dummyImage, null);
            vkFreeMemory(device, dummyImageMemory, null);
            vkDestroyImageView(device, dummyImageView, null);

            this.cleanupUserDefImages();

            if (!isBorrowedShaderModules) {
                vertShaderModules.forEach(vertShaderModule -> vkDestroyShaderModule(device, vertShaderModule, null));
                fragShaderModules.forEach(fragShaderModule -> vkDestroyShaderModule(device, fragShaderModule, null));
            }
            vertShaderModules.clear();
            fragShaderModules.clear();

            textureSamplers.forEach(textureSampler -> vkDestroySampler(device, textureSampler, null));
            textureSamplers.clear();

            uniformBuffers.forEach(uniformBuffer -> vkDestroyBuffer(device, uniformBuffer, null));
            uniformBufferMemories.forEach(uniformBufferMemory -> vkFreeMemory(device, uniformBufferMemory, null));
            uniformBuffers.clear();
            uniformBufferMemories.clear();

            descriptorSetLayouts.forEach(
                    descriptorSetLayout -> vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null));
            descriptorSetLayouts.clear();

            descriptorPools.forEach(descriptorPool -> vkDestroyDescriptorPool(device, descriptorPool, null));
            descriptorPools.clear();

            descriptorSets.clear();
        }

        vkDestroyRenderPass(device, renderPass, null);
    }

    public void cleanup(boolean reserveForRecreation) {
        if (isContainer) {
            this.cleanupContainer(reserveForRecreation);
        } else {
            this.cleanupSelf(reserveForRecreation);
        }
    }

    //==========
    protected long createShaderModule(VkDevice device, ByteBuffer spirvCode) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.calloc(stack);
            createInfo.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
            createInfo.pCode(spirvCode);

            LongBuffer pShaderModule = stack.mallocLong(1);
            if (vkCreateShaderModule(device, createInfo, null, pShaderModule) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a shader module");
            }

            return pShaderModule.get(0);
        }
    }

    private long createShaderModuleFromSPIRV(byte[] shaderContent) {
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(shaderContent.length);
        directBuffer.put(shaderContent);
        directBuffer.flip();

        return this.createShaderModule(device, directBuffer);
    }

    private long createShaderModuleFromGLSL(byte[] shaderContent, ShaderKind kind) {
        long shaderModule;
        try {
            var cacheMgr = new ShaderBuildCacheManager(shaderContent);
            byte[] buildCache = cacheMgr.retrieve();
            if (buildCache == null) {
                try (SPIRVUtils.SPIRV spirv = SPIRVUtils.compileShader(shaderContent, kind)) {
                    shaderModule = this.createShaderModule(device, spirv.bytecode());
                    cacheMgr.save(spirv.bytearray());
                }
            } else {
                shaderModule = this.createShaderModuleFromSPIRV(buildCache);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return shaderModule;
    }

    protected void setupShaderModules(boolean skipIfExists) {
        if (skipIfExists && !vertShaderModules.isEmpty()) {
            return;
        }
        if (vertShaderResource == null || fragShaderResource == null) {
            throw new RuntimeException("Shader resource cannot be null");
        }

        byte[] vertShaderContent;
        byte[] fragShaderContent;
        try (var bisVert = new BufferedInputStream(vertShaderResource.openStream());
             var bisFrag = new BufferedInputStream(fragShaderResource.openStream())) {
            vertShaderContent = bisVert.readAllBytes();
            fragShaderContent = bisFrag.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String vertShaderExtension = FilenameUtils.getFileExtension(vertShaderResource.getPath());
        String fragShaderExtension = FilenameUtils.getFileExtension(fragShaderResource.getPath());
        if (vertShaderExtension.isEmpty() || fragShaderExtension.isEmpty()) {
            throw new RuntimeException("Extension of shader file must not be empty");
        }

        long vertShaderModule = switch (vertShaderExtension) {
            case "spirv" -> this.createShaderModuleFromSPIRV(vertShaderContent);
            case "glsl" -> this.createShaderModuleFromGLSL(vertShaderContent, ShaderKind.VERTEX);
            default -> throw new RuntimeException("Cannot determine shader language from shader file extension");
        };
        long fragShaderModule = switch (fragShaderExtension) {
            case "spirv" -> this.createShaderModuleFromSPIRV(fragShaderContent);
            case "glsl" -> this.createShaderModuleFromGLSL(fragShaderContent, ShaderKind.FRAGMENT);
            default -> throw new RuntimeException("Cannot determine shader language from shader file extension");
        };

        this.addVertShaderModule(vertShaderModule);
        this.addFragShaderModule(fragShaderModule);
    }

    public long createUserDefImage(
            int width,
            int height,
            int msaaSamples,
            int usage,
            int memProperties,
            int format,
            int aspect) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pImage = stack.mallocLong(1);
            LongBuffer pImageMemory = stack.mallocLong(1);
            LongBuffer pImageView = stack.mallocLong(1);

            ImageUtils.createImage(
                    device,
                    width,
                    height,
                    1,
                    msaaSamples,
                    format,
                    VK_IMAGE_TILING_OPTIMAL,
                    usage,
                    memProperties,
                    pImage,
                    pImageMemory);

            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc(stack);
            viewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            viewInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
            viewInfo.image(pImage.get(0));
            viewInfo.format(format);
            viewInfo.subresourceRange().aspectMask(aspect);
            viewInfo.subresourceRange().baseMipLevel(0);
            viewInfo.subresourceRange().levelCount(1);
            viewInfo.subresourceRange().baseArrayLayer(0);
            viewInfo.subresourceRange().layerCount(1);

            if (vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create an image view");
            }

            userDefImages.add(pImage.get(0));
            userDefImageMemories.add(pImageMemory.get(0));
            userDefImageViews.add(pImageView.get(0));

            return pImage.get(0);
        }
    }

    public boolean removeUserDefImage(long userDefImage) {
        int imageIndex = -1;
        for (int i = 0; i < userDefImages.size(); i++) {
            if (userDefImages.get(i) == userDefImage) {
                imageIndex = i;
            }
        }
        if (imageIndex < 0) {
            return false;
        }

        long userDefImageMemory = userDefImageMemories.get(imageIndex);
        long userDefImageView = userDefImageViews.get(imageIndex);

        vkDestroyImage(device, userDefImage, null);
        vkFreeMemory(device, userDefImageMemory, null);
        vkDestroyImageView(device, userDefImageView, null);

        userDefImages.remove(imageIndex);
        userDefImageMemories.remove(imageIndex);
        userDefImageViews.remove(imageIndex);

        return true;
    }

    public long lookUpUserDefImageView(long userDefImage) {
        int imageIndex = -1;
        for (int i = 0; i < userDefImages.size(); i++) {
            if (userDefImages.get(i) == userDefImage) {
                imageIndex = i;
            }
        }
        if (imageIndex < 0) {
            throw new RuntimeException("User-defined image not found");
        }

        return userDefImageViews.get(imageIndex);
    }

    public void cleanupUserDefImages() {
        userDefImages.forEach(image -> vkDestroyImage(device, image, null));
        userDefImageMemories.forEach(imageMemory -> vkFreeMemory(device, imageMemory, null));
        userDefImageViews.forEach(imageView -> vkDestroyImageView(device, imageView, null));
        userDefImages.clear();
        userDefImageMemories.clear();
        userDefImageViews.clear();
    }

    /**
     * Binds an image.
     * Image binding will not be executed if the list of image views given is empty.
     *
     * @param commandBuffer Command buffer
     * @param dstSet        Index of destination set
     * @param dstBinding    Index of destination binding
     * @param imageViews    Image view
     */
    public void bindImages(VkCommandBuffer commandBuffer, int dstSet, int dstBinding, List<Long> imageViews) {
        if (imageViews.isEmpty()) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();
            int numImageViews = imageViews.size();

            VkDescriptorImageInfo.Buffer imageInfos = VkDescriptorImageInfo.calloc(numImageViews, stack);
            for (int i = 0; i < numImageViews; i++) {
                long imageView = imageViews.get(i);

                VkDescriptorImageInfo imageInfo = imageInfos.get(i);
                imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
                imageInfo.imageView(imageView);
            }

            VkWriteDescriptorSet.Buffer imageDescriptorWrite = VkWriteDescriptorSet.calloc(1, stack);
            imageDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            imageDescriptorWrite.dstBinding(dstBinding);
            imageDescriptorWrite.dstArrayElement(0);
            imageDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            imageDescriptorWrite.descriptorCount(numImageViews);
            imageDescriptorWrite.pImageInfo(imageInfos);

            long descriptorSet = this.getDescriptorSet(dstSet);
            imageDescriptorWrite.dstSet(descriptorSet);

            vkUpdateDescriptorSets(device, imageDescriptorWrite, null);

            vkCmdBindDescriptorSets(
                    commandBuffer,
                    VK_PIPELINE_BIND_POINT_GRAPHICS,
                    this.getPipelineLayout(0),
                    0,
                    this.pDescriptorSets(),
                    null);
        }
    }

    private void copyImageToBuffer(
            long commandPool,
            VkQueue graphicsQueue,
            long buffer,
            long image,
            int width,
            int height) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            VkBufferImageCopy.Buffer region = VkBufferImageCopy.calloc(1, stack);
            region.bufferOffset(0);
            region.bufferRowLength(0);
            region.bufferImageHeight(0);
            region.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            region.imageSubresource().mipLevel(0);
            region.imageSubresource().baseArrayLayer(0);
            region.imageSubresource().layerCount(1);
            region.imageOffset().set(0, 0, 0);
            region.imageExtent(VkExtent3D.calloc(stack).set(width, height, 1));

            vkCmdCopyImageToBuffer(commandBuffer, image, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, buffer, region);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }

    private void memcpy(ByteBuffer dst, ByteBuffer src, long size) {
        src.limit((int) size);
        dst.put(src);
        src.limit(src.capacity()).rewind();
    }

    private ByteBuffer createByteBufferFromImage(long commandPool, VkQueue graphicsQueue, int imageIndex) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long imageSize = extent.width() * extent.height() * 4;
            ByteBuffer pixels = ByteBuffer.allocate((int) imageSize);

            LongBuffer pStagingBuffer = stack.mallocLong(1);
            LongBuffer pStagingBufferMemory = stack.mallocLong(1);
            BufferUtils.createBuffer(
                    device,
                    imageSize,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                    pStagingBuffer,
                    pStagingBufferMemory);

            ImageUtils.transitionImageLayout(
                    device,
                    commandPool,
                    graphicsQueue,
                    images.get(imageIndex),
                    VK_IMAGE_ASPECT_COLOR_BIT,
                    VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                    VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    1);

            this.copyImageToBuffer(
                    commandPool, graphicsQueue, pStagingBuffer.get(0),
                    images.get(imageIndex), extent.width(), extent.height());

            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(device, pStagingBufferMemory.get(0), 0, imageSize, 0, data);
            {
                this.memcpy(pixels, data.getByteBuffer(0, (int) imageSize), imageSize);
            }
            vkUnmapMemory(device, pStagingBufferMemory.get(0));

            ImageUtils.transitionImageLayout(
                    device,
                    commandPool,
                    graphicsQueue,
                    images.get(imageIndex),
                    VK_IMAGE_ASPECT_COLOR_BIT,
                    VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                    1);

            vkDestroyBuffer(device, pStagingBuffer.get(0), null);
            vkFreeMemory(device, pStagingBufferMemory.get(0), null);

            pixels.flip();

            return pixels;
        }
    }

    public BufferedImage createBufferedImage(
            long commandPool,
            VkQueue graphicsQueue,
            int imageIndex,
            PixelFormat pixelFormat) {
        ByteBuffer pixels = this.createByteBufferFromImage(commandPool, graphicsQueue, imageIndex);

        var image = new BufferedImage(extent.width(), extent.height(), BufferedImage.TYPE_3BYTE_BGR);
        int pos = 0;
        for (int y = 0; y < extent.height(); y++) {
            for (int x = 0; x < extent.width(); x++) {
                int r = 0;
                int g = 0;
                int b = 0;
                int a = 0;
                if (pixelFormat == PixelFormat.RGBA) {
                    r = Byte.toUnsignedInt(pixels.get(pos));
                    g = Byte.toUnsignedInt(pixels.get(pos + 1));
                    b = Byte.toUnsignedInt(pixels.get(pos + 2));
                    a = Byte.toUnsignedInt(pixels.get(pos + 3));
                } else if (pixelFormat == PixelFormat.BGRA) {
                    b = Byte.toUnsignedInt(pixels.get(pos));
                    g = Byte.toUnsignedInt(pixels.get(pos + 1));
                    r = Byte.toUnsignedInt(pixels.get(pos + 2));
                    a = Byte.toUnsignedInt(pixels.get(pos + 3));
                }

                int rgba = (a << 24) | (r << 16) | (g << 8) | b;
                image.setRGB(x, y, rgba);

                pos += 4;
            }
        }

        return image;
    }
}
