package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkModel3D;

/**
 * 3D model
 *
 * @author maeda
 */
public class Model3D extends Component3D {
    private String modelFilepath;
    private VkModel3D vkModel;

    public Model3D(MttVulkanInstance vulkanInstance, String modelFilepath) {
        super(vulkanInstance);

        this.modelFilepath = modelFilepath;
        vkModel = vulkanInstance.createModel3D(modelFilepath);
        this.associateVulkanComponent(vkModel);
    }

    public Model3D(MttVulkanInstance vulkanInstance, Model3D srcModel) {
        super(vulkanInstance);

        this.modelFilepath = srcModel.modelFilepath;
        vkModel = vulkanInstance.duplicateModel3D(srcModel.vkModel);
        this.associateVulkanComponent(vkModel);
    }

    public String getModelFilepath() {
        return modelFilepath;
    }
}
