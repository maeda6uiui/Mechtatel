package com.github.maeda6uiui.mechtatel.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Configuration for shaders
 *
 * @author maeda6uiui
 */
public class MttShaderConfig {
    public record ShaderInfo(String filepath, String location) {

    }

    public static class ShaderModuleInfo {
        public List<ShaderInfo> shaders;

        public ShaderModuleInfo() {
            shaders = new ArrayList<>();
        }

        public void addShader(String shaderFilepath, String shaderLocation) {
            var shader = new ShaderInfo(shaderFilepath, shaderLocation);
            shaders.add(shader);
        }
    }

    public static class ShaderModulesInfo {
        public Map<String, ShaderModuleInfo> modules;

        public ShaderModulesInfo() {
            modules = new HashMap<>();
        }

        public void addSingleShaderModule(String shaderFilepath, String shaderLocation) {
            var module = new ShaderModuleInfo();
            module.addShader(shaderFilepath, shaderLocation);
            modules.put("main", module);
        }

        public void addSingleShaderModule(String shaderFilepath) {
            this.addSingleShaderModule(shaderFilepath, ShaderModulesInfo.class.getName());
        }
    }

    public static class TextureOperationShaderInfo {
        public ShaderModulesInfo vertex;
        public ShaderModulesInfo fragment;

        public TextureOperationShaderInfo() {
            vertex = new ShaderModulesInfo();
            vertex.addSingleShaderModule("/Standard/Shader/TextureOperation/texture_operation.vert.glsl");
            fragment = new ShaderModulesInfo();
            fragment.addSingleShaderModule("/Standard/Shader/TextureOperation/texture_operation.frag.glsl");
        }
    }

    public static class GBufferShaderInfo {
        public static class AlbedoShaderInfo {
            public ShaderModulesInfo vertex;
            public ShaderModulesInfo fragment;

            public AlbedoShaderInfo() {
                vertex = new ShaderModulesInfo();
                vertex.addSingleShaderModule("/Standard/Shader/GBuffer/albedo.vert.glsl");
                fragment = new ShaderModulesInfo();
                fragment.addSingleShaderModule("/Standard/Shader/GBuffer/albedo.frag.glsl");
            }
        }

        public static class PropertiesShaderInfo {
            public ShaderModulesInfo vertex;
            public ShaderModulesInfo fragment;

            public PropertiesShaderInfo() {
                vertex = new ShaderModulesInfo();
                vertex.addSingleShaderModule("/Standard/Shader/GBuffer/properties.vert.glsl");
                fragment = new ShaderModulesInfo();
                fragment.addSingleShaderModule("/Standard/Shader/GBuffer/properties.frag.glsl");
            }
        }

        public AlbedoShaderInfo albedo;
        public PropertiesShaderInfo properties;

        public GBufferShaderInfo() {
            albedo = new AlbedoShaderInfo();
            properties = new PropertiesShaderInfo();
        }
    }

    public static class MergeScenesShaderInfo {
        public ShaderModulesInfo vertex;
        public ShaderModulesInfo fragment;

        public MergeScenesShaderInfo() {
            vertex = new ShaderModulesInfo();
            vertex.addSingleShaderModule("/Standard/Shader/MergeScenes/merge_scenes.vert.glsl");
            fragment = new ShaderModulesInfo();
            fragment.addSingleShaderModule("/Standard/Shader/MergeScenes/merge_scenes.frag.glsl");
        }
    }

    public static class PostProcessingShaderInfo {
        public static class FogShaderInfo {
            public ShaderModulesInfo vertex;
            public ShaderModulesInfo fragment;

            public FogShaderInfo() {
                vertex = new ShaderModulesInfo();
                vertex.addSingleShaderModule("/Standard/Shader/PostProcessing/post_processing.vert.glsl");
                fragment = new ShaderModulesInfo();
                fragment.addSingleShaderModule("/Standard/Shader/PostProcessing/fog.frag.glsl");
            }
        }

        public static class ParallelLightShaderInfo {
            public ShaderModulesInfo vertex;
            public ShaderModulesInfo fragment;

            public ParallelLightShaderInfo() {
                vertex = new ShaderModulesInfo();
                vertex.addSingleShaderModule("/Standard/Shader/PostProcessing/post_processing.vert.glsl");
                fragment = new ShaderModulesInfo();
                fragment.addSingleShaderModule("/Standard/Shader/PostProcessing/parallel_light.frag.glsl");
            }
        }

        public static class PointLightShaderInfo {
            public ShaderModulesInfo vertex;
            public ShaderModulesInfo fragment;

            public PointLightShaderInfo() {
                vertex = new ShaderModulesInfo();
                vertex.addSingleShaderModule("/Standard/Shader/PostProcessing/post_processing.vert.glsl");
                fragment = new ShaderModulesInfo();
                fragment.addSingleShaderModule("/Standard/Shader/PostProcessing/point_light.frag.glsl");
            }
        }

        public static class SimpleBlurShaderInfo {
            public ShaderModulesInfo vertex;
            public ShaderModulesInfo fragment;

            public SimpleBlurShaderInfo() {
                vertex = new ShaderModulesInfo();
                vertex.addSingleShaderModule("/Standard/Shader/PostProcessing/post_processing.vert.glsl");
                fragment = new ShaderModulesInfo();
                fragment.addSingleShaderModule("/Standard/Shader/PostProcessing/simple_blur.frag.glsl");
            }
        }

        public static class SpotlightShaderInfo {
            public ShaderModulesInfo vertex;
            public ShaderModulesInfo fragment;

            public SpotlightShaderInfo() {
                vertex = new ShaderModulesInfo();
                vertex.addSingleShaderModule("/Standard/Shader/PostProcessing/post_processing.vert.glsl");
                fragment = new ShaderModulesInfo();
                fragment.addSingleShaderModule("/Standard/Shader/PostProcessing/spotlight.frag.glsl");
            }
        }

        public FogShaderInfo fog;
        public ParallelLightShaderInfo parallelLight;
        public PointLightShaderInfo pointLight;
        public SimpleBlurShaderInfo simpleBlur;
        public SpotlightShaderInfo spotlight;

        public PostProcessingShaderInfo() {
            fog = new FogShaderInfo();
            parallelLight = new ParallelLightShaderInfo();
            pointLight = new PointLightShaderInfo();
            simpleBlur = new SimpleBlurShaderInfo();
            spotlight = new SpotlightShaderInfo();
        }
    }

    public static class FullScreenEffectShaderInfo {
        public static class GaussianBlurShaderInfo {
            public ShaderModulesInfo vertex;
            public ShaderModulesInfo fragment;

            public GaussianBlurShaderInfo() {
                vertex = new ShaderModulesInfo();
                vertex.addSingleShaderModule("/Standard/Shader/FullScreenEffect/full_screen_effect.vert.glsl");
                fragment = new ShaderModulesInfo();
                fragment.addSingleShaderModule("/Standard/Shader/FullScreenEffect/gaussian_blur.frag.glsl");
            }
        }

        public static class MonochromeEffectShaderInfo {
            public ShaderModulesInfo vertex;
            public ShaderModulesInfo fragment;

            public MonochromeEffectShaderInfo() {
                vertex = new ShaderModulesInfo();
                vertex.addSingleShaderModule("/Standard/Shader/FullScreenEffect/full_screen_effect.vert.slang");
                fragment = new ShaderModulesInfo();
                fragment.addSingleShaderModule("/Standard/Shader/FullScreenEffect/monochrome_effect.frag.slang");
            }
        }

        public GaussianBlurShaderInfo gaussianBlur;
        public MonochromeEffectShaderInfo monochromeEffect;

        public FullScreenEffectShaderInfo() {
            gaussianBlur = new GaussianBlurShaderInfo();
            monochromeEffect = new MonochromeEffectShaderInfo();
        }
    }

    public static class PresentShaderInfo {
        public ShaderModulesInfo vertex;
        public ShaderModulesInfo fragment;

        public PresentShaderInfo() {
            vertex = new ShaderModulesInfo();
            vertex.addSingleShaderModule("/Standard/Shader/Present/present.vert.glsl");
            fragment = new ShaderModulesInfo();
            fragment.addSingleShaderModule("/Standard/Shader/Present/present.frag.glsl");
        }
    }

    public static class PrimitiveShaderInfo {
        public ShaderModulesInfo vertex;
        public ShaderModulesInfo fragment;

        public PrimitiveShaderInfo() {
            vertex = new ShaderModulesInfo();
            vertex.addSingleShaderModule("/Standard/Shader/Primitive/primitive.vert.glsl");
            fragment = new ShaderModulesInfo();
            fragment.addSingleShaderModule("/Standard/Shader/Primitive/primitive.frag.glsl");
        }
    }

    public static class ShadowMappingShaderInfo {
        public static class Pass1ShaderInfo {
            public ShaderModulesInfo vertex;
            public ShaderModulesInfo fragment;

            public Pass1ShaderInfo() {
                vertex = new ShaderModulesInfo();
                vertex.addSingleShaderModule("/Standard/Shader/ShadowMapping/pass_1.vert.glsl");
                fragment = new ShaderModulesInfo();
                fragment.addSingleShaderModule("/Standard/Shader/ShadowMapping/pass_1.frag.glsl");
            }
        }

        public static class Pass2ShaderInfo {
            public ShaderModulesInfo vertex;
            public ShaderModulesInfo fragment;

            public Pass2ShaderInfo() {
                vertex = new ShaderModulesInfo();
                vertex.addSingleShaderModule("/Standard/Shader/ShadowMapping/pass_2.vert.glsl");
                fragment = new ShaderModulesInfo();
                fragment.addSingleShaderModule("/Standard/Shader/ShadowMapping/pass_2.frag.glsl");
            }
        }

        public Pass1ShaderInfo pass1;
        public Pass2ShaderInfo pass2;

        public ShadowMappingShaderInfo() {
            pass1 = new Pass1ShaderInfo();
            pass2 = new Pass2ShaderInfo();
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

    private static final Logger logger = LoggerFactory.getLogger(MttShaderConfig.class);
    private static MttShaderConfig instance;

    public MttShaderConfig() {
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
     * Loads shader config from a JSON file.
     * This method returns empty value if the file specified as {@code jsonFile} does not exist
     * or if it fails to load config from the file specified.
     *
     * @param jsonFile Path of the config file
     * @return Shader config
     */
    public static Optional<MttShaderConfig> load(Path jsonFile) {
        if (!Files.exists(jsonFile)) {
            logger.error("Shader config file ({}) was not found", jsonFile);
            return Optional.empty();
        }

        try {
            String json = Files.readString(jsonFile);
            instance = new ObjectMapper().readValue(json, MttShaderConfig.class);
            return Optional.of(instance);
        } catch (IOException e) {
            logger.error("Failed to load shader config file", e);
            return Optional.empty();
        }
    }

    /**
     * Creates a new instance of {@link MttShaderConfig}.
     *
     * @return Shader config
     */
    public static MttShaderConfig create() {
        instance = new MttShaderConfig();
        return instance;
    }

    /**
     * Returns currently retained shader config instance
     *
     * @return Shader config
     */
    public static Optional<MttShaderConfig> get() {
        return Optional.ofNullable(instance);
    }
}
