package com.github.maeda6uiui.mechtatel.core.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanInstanceForComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkComponent3D;
import org.joml.Vector3fc;

/**
 * Component 3D
 *
 * @author maeda
 */
public class Component3D extends Component {
    public Component3D(IMttVulkanInstanceForComponent vulkanInstance) {
        super(vulkanInstance);
    }

    public void translate(Vector3fc v) {
        VkComponent3D vkComponent = (VkComponent3D) this.getVulkanComponent();
        vkComponent.translate(v);
    }

    public void rotX(float ang) {
        VkComponent3D vkComponent = (VkComponent3D) this.getVulkanComponent();
        vkComponent.rotX(ang);
    }

    public void rotY(float ang) {
        VkComponent3D vkComponent = (VkComponent3D) this.getVulkanComponent();
        vkComponent.rotY(ang);
    }

    public void rotZ(float ang) {
        VkComponent3D vkComponent = (VkComponent3D) this.getVulkanComponent();
        vkComponent.rotZ(ang);
    }

    public void rot(float ang, Vector3fc axis) {
        VkComponent3D vkComponent = (VkComponent3D) this.getVulkanComponent();
        vkComponent.rot(ang, axis);
    }

    public void rescale(Vector3fc scale) {
        VkComponent3D vkComponent = (VkComponent3D) this.getVulkanComponent();
        vkComponent.rescale(scale);
    }
}
