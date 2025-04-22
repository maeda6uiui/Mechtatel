package com.github.maeda6uiui.mechtatel.core.screen;

import com.github.maeda6uiui.mechtatel.core.SamplerAddressMode;
import com.github.maeda6uiui.mechtatel.core.SamplerFilterMode;
import com.github.maeda6uiui.mechtatel.core.SamplerMipmapMode;
import org.lwjgl.vulkan.VkExtent2D;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Utility methods for creating a Vulkan screen
 *
 * @author maeda6uiui
 */
public class VkScreenCreationUtils {
    public static VkExtent2D createExtent(VkExtent2D defaultExtent, int screenWidth, int screenHeight) {
        VkExtent2D extent = VkExtent2D.create();
        if (screenWidth < 0) {
            extent.width(defaultExtent.width());
        } else {
            extent.width(screenWidth);
        }
        if (screenHeight < 0) {
            extent.height(defaultExtent.height());
        } else {
            extent.height(screenHeight);
        }

        return extent;
    }

    public static int getISamplerFilter(SamplerFilterMode samplerFilter) {
        return switch (samplerFilter) {
            case NEAREST -> VK_FILTER_NEAREST;
            case LINEAR -> VK_FILTER_LINEAR;
        };
    }

    public static int getISamplerMipmapMode(SamplerMipmapMode samplerMipmapMode) {
        return switch (samplerMipmapMode) {
            case NEAREST -> VK_SAMPLER_MIPMAP_MODE_NEAREST;
            case LINEAR -> VK_SAMPLER_MIPMAP_MODE_LINEAR;
        };
    }

    public static int getISamplerAddressMode(SamplerAddressMode samplerAddressMode) {
        return switch (samplerAddressMode) {
            case REPEAT -> VK_SAMPLER_ADDRESS_MODE_REPEAT;
            case MIRRORED_REPEAT -> VK_SAMPLER_ADDRESS_MODE_MIRRORED_REPEAT;
            case CLAMP_TO_EDGE -> VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE;
            case CLAMP_TO_BORDER -> VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER;
        };
    }
}
