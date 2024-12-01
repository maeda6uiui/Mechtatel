package com.github.maeda6uiui.mechtatel.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Shader settings<br>
 * Shader settings must be updated before they are loaded by nabors.
 * Changes to these settings does not have any effects after nabors are created.
 *
 * @author maeda6uiui
 */
public class MttShaderSettings {
    private static final Logger logger = LoggerFactory.getLogger(MttShaderSettings.class);

    public static class VertShaderInfo {
        public String filepath;
        public String className;

        public VertShaderInfo() {
            filepath = "";
            className = MttShaderSettings.class.getName();
        }
    }

    public static class FragShaderInfo {
        public String filepath;
        public String className;

        public FragShaderInfo() {
            filepath = "";
            className = MttShaderSettings.class.getName();
        }
    }

    public static class ShaderInfo {
        public VertShaderInfo vert;
        public FragShaderInfo frag;

        public ShaderInfo() {
            vert = new VertShaderInfo();
            frag = new FragShaderInfo();
        }
    }

    public static class TextureOperationShaderInfo {
        public ShaderInfo main;

        public TextureOperationShaderInfo() {
            main = new ShaderInfo();
            main.vert.filepath = "/Standard/Shader/TextureOperation/texture_operation.vert";
            main.frag.filepath = "/Standard/Shader/TextureOperation/texture_operation.frag";
        }
    }

    public static class GBufferShaderInfo {
        public ShaderInfo albedo;
        public ShaderInfo properties;

        public GBufferShaderInfo() {
            albedo = new ShaderInfo();
            albedo.vert.filepath = "/Standard/Shader/GBuffer/albedo.vert";
            albedo.frag.filepath = "/Standard/Shader/GBuffer/albedo.frag";

            properties = new ShaderInfo();
            properties.vert.filepath = "/Standard/Shader/GBuffer/properties.vert";
            properties.frag.filepath = "/Standard/Shader/GBuffer/properties.frag";
        }
    }

    public static class MergeScenesShaderInfo {
        public ShaderInfo main;

        public MergeScenesShaderInfo() {
            main = new ShaderInfo();
            main.vert.filepath = "/Standard/Shader/MergeScenes/merge_scenes.vert";
            main.frag.filepath = "/Standard/Shader/MergeScenes/merge_scenes.frag";
        }
    }

    public static class PostProcessingShaderInfo {
        public ShaderInfo fog;
        public ShaderInfo parallelLight;
        public ShaderInfo pointLight;
        public ShaderInfo simpleBlur;
        public ShaderInfo spotlight;

        public PostProcessingShaderInfo() {
            fog = new ShaderInfo();
            fog.vert.filepath = "/Standard/Shader/PostProcessing/post_processing.vert";
            fog.frag.filepath = "/Standard/Shader/PostProcessing/fog.frag";

            parallelLight = new ShaderInfo();
            parallelLight.vert.filepath = "/Standard/Shader/PostProcessing/post_processing.vert";
            parallelLight.frag.filepath = "/Standard/Shader/PostProcessing/parallel_light.frag";

            pointLight = new ShaderInfo();
            pointLight.vert.filepath = "/Standard/Shader/PostProcessing/post_processing.vert";
            pointLight.frag.filepath = "/Standard/Shader/PostProcessing/point_light.frag";

            simpleBlur = new ShaderInfo();
            simpleBlur.vert.filepath = "/Standard/Shader/PostProcessing/post_processing.vert";
            simpleBlur.frag.filepath = "/Standard/Shader/PostProcessing/simple_blur.frag";

            spotlight = new ShaderInfo();
            spotlight.vert.filepath = "/Standard/Shader/PostProcessing/post_processing.vert";
            spotlight.frag.filepath = "/Standard/Shader/PostProcessing/spotlight.frag";
        }
    }

    public static class FullScreenEffectShaderInfo {
        public ShaderInfo gaussianBlur;

        public FullScreenEffectShaderInfo() {
            gaussianBlur = new ShaderInfo();
            gaussianBlur.vert.filepath = "/Standard/Shader/FullScreenEffect/full_screen_effect.vert";
            gaussianBlur.frag.filepath = "/Standard/Shader/FullScreenEffect/gaussian_blur.frag";
        }
    }

    public static class PresentShaderInfo {
        public ShaderInfo main;

        public PresentShaderInfo() {
            main = new ShaderInfo();
            main.vert.filepath = "/Standard/Shader/Present/present.vert";
            main.frag.filepath = "/Standard/Shader/Present/present.frag";
        }
    }

    public static class PrimitiveShaderInfo {
        public ShaderInfo main;

        public PrimitiveShaderInfo() {
            main = new ShaderInfo();
            main.vert.filepath = "/Standard/Shader/Primitive/primitive.vert";
            main.frag.filepath = "/Standard/Shader/Primitive/primitive.frag";
        }
    }

    public static class ShadowMappingShaderInfo {
        public ShaderInfo pass1;
        public ShaderInfo pass2;

        public ShadowMappingShaderInfo() {
            pass1 = new ShaderInfo();
            pass1.vert.filepath = "/Standard/Shader/ShadowMapping/pass_1.vert";
            pass1.frag.filepath = "/Standard/Shader/ShadowMapping/pass_1.frag";

            pass2 = new ShaderInfo();
            pass2.vert.filepath = "/Standard/Shader/ShadowMapping/pass_2.vert";
            pass2.frag.filepath = "/Standard/Shader/ShadowMapping/pass_2.frag";
        }
    }

    public TextureOperationShaderInfo textureOperation;
    public GBufferShaderInfo gBuffer;
    public MergeScenesShaderInfo mergeScenes;
    public PostProcessingShaderInfo postProcessing;
    public PresentShaderInfo present;
    public PrimitiveShaderInfo primitive;
    public ShadowMappingShaderInfo shadowMapping;
    public FullScreenEffectShaderInfo fullScreenEffect;

    private static MttShaderSettings instance;

    public MttShaderSettings() {
        textureOperation = new TextureOperationShaderInfo();
        gBuffer = new GBufferShaderInfo();
        mergeScenes = new MergeScenesShaderInfo();
        postProcessing = new PostProcessingShaderInfo();
        present = new PresentShaderInfo();
        primitive = new PrimitiveShaderInfo();
        shadowMapping = new ShadowMappingShaderInfo();
        fullScreenEffect = new FullScreenEffectShaderInfo();
    }

    /**
     * Loads settings from a JSON file.
     * This method returns empty value if the file specified as {@code jsonFile} does not exist
     * or if it fails to load settings from the file specified.
     *
     * @param jsonFile Path of the setting file
     * @return Settings
     */
    public static Optional<MttShaderSettings> load(Path jsonFile) {
        if (!Files.exists(jsonFile)) {
            logger.error("Setting file ({}) was not found", jsonFile);
            return Optional.empty();
        }

        try {
            String json = Files.readString(jsonFile);
            instance = new ObjectMapper().readValue(json, MttShaderSettings.class);
            return Optional.of(instance);
        } catch (IOException e) {
            logger.error("Failed to load setting file", e);
            return Optional.empty();
        }
    }

    /**
     * Loads settings from a JSON file.
     *
     * @param jsonFilepath Filepath of the setting file
     * @return Settings
     * @see #load(String)
     */
    public static Optional<MttShaderSettings> load(String jsonFilepath) {
        return load(Paths.get(jsonFilepath));
    }

    /**
     * Creates a new instance of {@link MttShaderSettings}.
     *
     * @return Settings
     */
    public static MttShaderSettings create() {
        instance = new MttShaderSettings();
        return instance;
    }

    /**
     * Returns currently retained settings instance.
     *
     * @return Settings
     */
    public static Optional<MttShaderSettings> get() {
        return Optional.ofNullable(instance);
    }
}
