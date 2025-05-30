package com.github.maeda6uiui.mechtatel.core.screen.component;

import com.github.maeda6uiui.mechtatel.core.model.AssimpModelLoader;
import com.github.maeda6uiui.mechtatel.core.model.JXMModelLoader;
import com.github.maeda6uiui.mechtatel.core.model.MttAnimationData;
import com.github.maeda6uiui.mechtatel.core.model.MttModelData;
import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanImplCommon;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttModel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Model
 *
 * @author maeda6uiui
 */
public class MttModel extends MttComponent implements IMttModelForVkMttModel {
    private Path modelFile;
    private VkMttModel vkModel;

    private MttModelData modelData;
    private MttAnimationData animationData;

    public MttModel(
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttComponent screen,
            Path modelFile) throws IOException {
        super(
                screen,
                new MttComponentCreateInfo()
                        .setVisible(true)
                        .setTwoDComponent(false)
                        .setCastShadow(true)
                        .setDrawOrder(0)
        );

        this.modelFile = modelFile;

        //Load model data
        String modelFilepath = modelFile.toString();
        if (modelFilepath.endsWith(".bd1") || modelFilepath.endsWith(".BD1")) {
            modelData = JXMModelLoader.load(modelFile);
        } else {
            modelData = AssimpModelLoader.load(modelFile);
        }

        //Create Vulkan component
        var dq = vulkanImplCommon.getDeviceAndQueues();
        vkModel = new VkMttModel(
                this, dq.device(),
                vulkanImplCommon.getCommandPool(),
                dq.graphicsQueue(),
                screen.getVulkanScreen(),
                this
        );
        this.associateVulkanComponents(vkModel);
    }

    public MttModel(IMttVulkanImplCommon vulkanImplCommon, IMttScreenForMttComponent screen, MttModel srcModel) {
        super(
                screen,
                new MttComponentCreateInfo()
                        .setVisible(true)
                        .setTwoDComponent(false)
                        .setCastShadow(true)
                        .setDrawOrder(0)
        );

        this.modelFile = srcModel.modelFile;

        var dq = vulkanImplCommon.getDeviceAndQueues();
        vkModel = new VkMttModel(
                this, dq.device(),
                vulkanImplCommon.getCommandPool(),
                dq.graphicsQueue(),
                srcModel.vkModel
        );
        this.associateVulkanComponents(vkModel);

        this.modelData = srcModel.modelData;
    }

    public Path getModelFile() {
        return modelFile;
    }

    @Override
    public MttModelData getModelData() {
        return modelData;
    }

    @Override
    public MttAnimationData getAnimationData() {
        return animationData;
    }

    public void setAnimationData(MttAnimationData animationData) {
        this.animationData = animationData;
    }

    public Set<Integer> getTextureIndices() {
        return vkModel.getTextureIndices();
    }

    public void replaceTexture(int index, MttTexture newTexture) {
        vkModel.replaceTexture(index, newTexture.getVulkanTexture());
    }

    public void setDrawMeshIndices(List<Integer> drawMeshIndices) {
        vkModel.setDrawMeshIndices(drawMeshIndices);
    }
}
