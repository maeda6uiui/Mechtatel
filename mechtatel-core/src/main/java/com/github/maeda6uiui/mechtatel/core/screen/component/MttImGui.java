package com.github.maeda6uiui.mechtatel.core.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.IMttScreenForMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanImplCommon;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttImGui;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.texture.VkMttTexture;
import imgui.ImFontAtlas;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.internal.ImGuiContext;
import imgui.type.ImInt;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

/**
 * Component for ImGui rendering
 *
 * @author maeda6uiui
 */
public class MttImGui extends MttComponent {
    private ImGuiContext context;

    private VkMttTexture vkTexture;
    private VkMttImGui vkImGui;

    /**
     * Creates an instance of {@link MttImGui}.
     * This class does not change I/O settings (e.g. keyboard and mouse) for the ImGui context passed.
     * It only creates a texture for ImGui rendering.
     *
     * @param vulkanImplCommon Vulkan implementation
     * @param screen           Screen
     * @param context          ImGui context
     */
    public MttImGui(
            IMttVulkanImplCommon vulkanImplCommon,
            IMttScreenForMttComponent screen,
            ImGuiContext context) {
        super(
                screen,
                new MttComponentCreateInfo()
                        .setVisible(true)
                        .setTwoDComponent(true)
                        .setCastShadow(false)
                        .setDrawOrder(0)
        );

        this.context = context;

        //Get required resources related to Vulkan
        var dq = vulkanImplCommon.getDeviceAndQueues();

        //Make this ImGui context current
        ImGui.setCurrentContext(context);

        //Create a texture for ImGui
        ImFontAtlas fontAtlas = ImGui.getIO().getFonts();
        var width = new ImInt();
        var height = new ImInt();
        ByteBuffer buffer = fontAtlas.getTexDataAsRGBA32(width, height);

        vkTexture = new VkMttTexture(
                dq.device(),
                vulkanImplCommon.getCommandPool(),
                dq.graphicsQueue(),
                screen.getVulkanScreen(),
                buffer,
                width.get(),
                height.get(),
                false
        );
        this.associateVulkanTextures(vkTexture);

        //Create ImGui instance for rendering with Vulkan
        vkImGui = new VkMttImGui(
                this,
                dq.device(),
                vulkanImplCommon.getCommandPool(),
                dq.graphicsQueue(),
                screen.getVulkanScreen()
        );
        this.associateVulkanComponents(vkImGui);
    }

    /**
     * Returns ImGui context.
     *
     * @return ImGui context
     */
    public ImGuiContext getImGuiContext() {
        return context;
    }

    /**
     * Declares ImGui controls.
     * UI declaration has to be supplied every frame via this method.
     *
     * @param fImGuiDeclaration Runnable that declares ImGui controls
     */
    public void declare(Runnable fImGuiDeclaration) {
        ImGui.setCurrentContext(context);
        ImGui.newFrame();
        fImGuiDeclaration.run();
        ImGui.endFrame();
        ImGui.render();
        vkImGui.setDrawData(ImGui.getDrawData());

        //Transform this component to fit in the range of [-1, 1]
        ImGuiIO io = ImGui.getIO();

        float scaleX = 2.0f / io.getDisplaySizeX();
        float scaleY = 2.0f / io.getDisplaySizeY();

        var mat = new Matrix4f()
                .translate(new Vector3f(-1.0f, -1.0f, 0.0f))
                .scale(new Vector3f(scaleX, scaleY, 1.0f));
        this.setMat(mat);
    }
}
