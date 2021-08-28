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
    }

    public String getModelFilepath() {
        return modelFilepath;
    }

    @Override
    public void cleanup() {
        this.getVulkanInstance().deleteComponent(vkModel);
    }
}
