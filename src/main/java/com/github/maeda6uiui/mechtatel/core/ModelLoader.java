package com.github.maeda6uiui.mechtatel.core;

import org.joml.*;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.assimp.Assimp.*;

/**
 * Loads 3D models with Assimp
 *
 * @author maeda
 */
class ModelLoader {
    public static class Material {
        public String diffuseTexFilepath;

        public Vector4fc ambientColor;
        public Vector4fc diffuseColor;
        public Vector4fc specularColor;

        public Material() {
            ambientColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
            diffuseColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
            specularColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        }
    }

    public static class Mesh {
        public int materialIndex;

        public final List<Vertex3DUV> vertices;
        public final List<Integer> indices;

        public Mesh() {
            materialIndex = -1;

            vertices = new ArrayList<>();
            indices = new ArrayList<>();
        }
    }

    public static class Model {
        public final Map<Integer, Material> materials;
        public final Map<Integer, Mesh> meshes;

        public Model(int numMaterials, int numMeshes) {
            materials = new HashMap<>();
            for (int i = 0; i < numMaterials; i++) {
                materials.put(i, new Material());
            }

            meshes = new HashMap<>();
            for (int i = 0; i < numMeshes; i++) {
                meshes.put(i, new Mesh());
            }
        }
    }

    private static void processMaterial(AIMaterial aiMaterial, Material material) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            //Get the filepath of the diffuse texture
            AIString aiDiffuseTexFilepath = AIString.callocStack(stack);
            aiGetMaterialTexture(
                    aiMaterial,
                    aiTextureType_DIFFUSE,
                    0,
                    aiDiffuseTexFilepath,
                    (IntBuffer) null,
                    null,
                    null,
                    null,
                    null,
                    null);
            String diffuseTextFilepath = aiDiffuseTexFilepath.dataString();

            material.diffuseTexFilepath = diffuseTextFilepath;

            //Get the material colors
            AIColor4D color = AIColor4D.callocStack(stack);

            int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0, color);
            if (result == 0) {
                material.ambientColor = new Vector4f(color.r(), color.g(), color.b(), color.a());
            }

            result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color);
            if (result == 0) {
                material.diffuseColor = new Vector4f(color.r(), color.g(), color.b(), color.a());
            }

            result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, color);
            if (result == 0) {
                material.specularColor = new Vector4f(color.r(), color.g(), color.b(), color.a());
            }
        }
    }

    private static void processPositions(AIMesh mesh, List<Vector3fc> positions) {
        AIVector3D.Buffer vertices = Objects.requireNonNull(mesh.mVertices());

        for (int i = 0; i < vertices.capacity(); i++) {
            AIVector3D position = vertices.get(i);
            positions.add(new Vector3f(position.x(), position.y(), position.z()));
        }
    }

    private static void processTexCoords(AIMesh mesh, List<Vector2fc> texCoords) {
        AIVector3D.Buffer aiTexCoords = Objects.requireNonNull(mesh.mTextureCoords(0));

        for (int i = 0; i < aiTexCoords.capacity(); i++) {
            AIVector3D coords = aiTexCoords.get(i);
            texCoords.add(new Vector2f(coords.x(), coords.y()));
        }
    }

    private static void processIndices(AIMesh mesh, List<Integer> indices) {
        AIFace.Buffer aiFaces = Objects.requireNonNull(mesh.mFaces());

        for (int i = 0; i < mesh.mNumFaces(); i++) {
            AIFace face = aiFaces.get(i);
            IntBuffer pIndices = face.mIndices();

            for (int j = 0; j < pIndices.capacity(); j++) {
                indices.add(pIndices.get(j));
            }
        }
    }

    private static void processMesh(AIMesh aiMesh, Mesh mesh) {
        var positions = new ArrayList<Vector3fc>();
        var texCoords = new ArrayList<Vector2fc>();

        processPositions(aiMesh, positions);
        processTexCoords(aiMesh, texCoords);
        processIndices(aiMesh, mesh.indices);

        mesh.materialIndex = aiMesh.mMaterialIndex();

        //Create vertices
        int numVertices = positions.size();
        for (int i = 0; i < numVertices; i++) {
            var vertex = new Vertex3DUV(
                    positions.get(i),
                    new Vector3f(1.0f, 1.0f, 1.0f),
                    texCoords.get(i));
            mesh.vertices.add(vertex);
        }
    }

    public static Model loadModel(String modelFilepath) {
        try (AIScene scene = aiImportFile(modelFilepath, aiProcessPreset_TargetRealtime_Quality | aiProcess_FlipUVs)) {
            if (scene == null || scene.mRootNode() == null) {
                String errorStr = String.format("Could not load a model %s\n%s", modelFilepath, aiGetErrorString());
                throw new RuntimeException(errorStr);
            }

            int numMaterials = scene.mNumMaterials();
            int numMeshes = scene.mNumMeshes();

            PointerBuffer pMaterials = scene.mMaterials();
            PointerBuffer pMeshes = scene.mMeshes();

            var preModel = new Model(numMaterials, numMeshes);

            //Process materials
            for (int i = 0; i < numMaterials; i++) {
                AIMaterial aiMaterial = AIMaterial.create(pMaterials.get(i));
                processMaterial(aiMaterial, preModel.materials.get(i));
            }

            //Process meshes
            for (int i = 0; i < numMeshes; i++) {
                AIMesh aiMesh = AIMesh.create(pMeshes.get(i));
                processMesh(aiMesh, preModel.meshes.get(i));
            }

            return preModel;
        }
    }
}
