module com.github.maeda6uiui.mechtatel.core {
    requires java.desktop;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.github.dabasan.jxm.bd1;
    requires com.goxr3plus.streamplayer;
    requires Libbulletjme;
    requires transitive org.slf4j;
    requires transitive org.joml;
    requires org.lwjgl;
    requires org.lwjgl.glfw;
    requires org.lwjgl.vulkan;
    requires org.lwjgl.shaderc;
    requires org.lwjgl.stb;
    requires org.lwjgl.assimp;
    requires org.lwjgl.openal;
    requires jakarta.validation;

    exports com.github.maeda6uiui.mechtatel.core;
    exports com.github.maeda6uiui.mechtatel.core.screen.animation;
    exports com.github.maeda6uiui.mechtatel.core.camera;
    exports com.github.maeda6uiui.mechtatel.core.screen.component;
    exports com.github.maeda6uiui.mechtatel.core.screen.component.gui;
    exports com.github.maeda6uiui.mechtatel.core.nabor;
    exports com.github.maeda6uiui.mechtatel.core.physics;
    exports com.github.maeda6uiui.mechtatel.core.postprocessing.blur;
    exports com.github.maeda6uiui.mechtatel.core.postprocessing.fog;
    exports com.github.maeda6uiui.mechtatel.core.postprocessing.light;
    exports com.github.maeda6uiui.mechtatel.core.screen;
    exports com.github.maeda6uiui.mechtatel.core.shadow;
    exports com.github.maeda6uiui.mechtatel.core.sound;
    exports com.github.maeda6uiui.mechtatel.core.text;
    exports com.github.maeda6uiui.mechtatel.core.texture;
    exports com.github.maeda6uiui.mechtatel.core.util;
}