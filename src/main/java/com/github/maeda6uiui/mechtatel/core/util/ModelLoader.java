package com.github.maeda6uiui.mechtatel.core.util;

import com.github.dabasan.jxm.bd1.BD1Buffer;
import com.github.dabasan.jxm.bd1.BD1Manipulator;
import com.github.maeda6uiui.mechtatel.core.component.Vertex3DUV;
import org.joml.*;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.assimp.Assimp.*;

/**
 * Loads 3D models with Assimp
 *
 * @author maeda6uiui
 */
public class ModelLoader {
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
            AIString aiDiffuseTexFilepath = AIString.calloc(stack);
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
            AIColor4D color = AIColor4D.calloc(stack);

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

    private static void processNormals(AIMesh mesh, List<Vector3fc> normals) {
        AIVector3D.Buffer aiNormals = Objects.requireNonNull(mesh.mNormals());

        for (int i = 0; i < aiNormals.capacity(); i++) {
            AIVector3D normal = aiNormals.get(i);
            normals.add(new Vector3f(normal.x(), normal.y(), normal.z()));
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
        var normals = new ArrayList<Vector3fc>();

        processPositions(aiMesh, positions);
        processTexCoords(aiMesh, texCoords);
        processNormals(aiMesh, normals);
        processIndices(aiMesh, mesh.indices);

        mesh.materialIndex = aiMesh.mMaterialIndex();

        //Create vertices
        int numVertices = positions.size();
        for (int i = 0; i < numVertices; i++) {
            var vertex = new Vertex3DUV(
                    positions.get(i),
                    new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                    texCoords.get(i),
                    normals.get(i));
            mesh.vertices.add(vertex);
        }
    }

    private static Model loadModelWithJXM(String modelFilepath) throws IOException {
        var manipulator = new BD1Manipulator(modelFilepath);

        //Rescale the model so that 1 coord represents 1 meter
        final float RESCALE_FACTOR = 1.7f / 20.0f;
        manipulator.rescale(RESCALE_FACTOR, RESCALE_FACTOR, RESCALE_FACTOR);

        //Invert the model along the Z-axis
        //so that it matches the right-handed coordinate system of OpenGL and Vulkan
        manipulator.invertZ();

        List<BD1Buffer> buffers = manipulator.getBuffers(false);

        var model = new Model(buffers.size(), buffers.size());
        for (int i = 0; i < buffers.size(); i++) {
            BD1Buffer buffer = buffers.get(i);

            String textureFilename = manipulator.getTextureFilename(buffer.textureID);
            model.materials.get(i).diffuseTexFilepath = textureFilename;
            model.meshes.get(i).materialIndex = i;

            IntBuffer indexBuffer = buffer.indexBuffer;
            for (int j = 0; j < indexBuffer.capacity(); j++) {
                model.meshes.get(i).indices.add(indexBuffer.get(j));
            }

            FloatBuffer posBuffer = buffer.posBuffer;
            FloatBuffer normBuffer = buffer.normBuffer;
            FloatBuffer uvBuffer = buffer.uvBuffer;
            int numVertices = posBuffer.capacity() / 3;
            for (int j = 0; j < numVertices; j++) {
                var position = new Vector3f(
                        posBuffer.get(j * 3),
                        posBuffer.get(j * 3 + 1),
                        posBuffer.get(j * 3 + 2)
                );
                var normal = new Vector3f(
                        normBuffer.get(j * 3),
                        normBuffer.get(j * 3 + 1),
                        normBuffer.get(j * 3 + 2)
                );
                var texCoords = new Vector2f(
                        uvBuffer.get(j * 2),
                        uvBuffer.get(j * 2 + 1)
                );

                var vertex = new Vertex3DUV(position, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), texCoords, normal);
                model.meshes.get(i).vertices.add(vertex);
            }
        }

        return model;
    }

    private static Model loadModelWithAssimp(String modelFilepath) throws IOException {
        try (AIScene scene = aiImportFile(modelFilepath, aiProcessPreset_TargetRealtime_Quality | aiProcess_FlipUVs)) {
            if (scene == null || scene.mRootNode() == null) {
                String errorStr = String.format("Could not load a model %s\n%s", modelFilepath, aiGetErrorString());
                throw new IOException(errorStr);
            }

            int numMaterials = scene.mNumMaterials();
            int numMeshes = scene.mNumMeshes();

            PointerBuffer pMaterials = scene.mMaterials();
            PointerBuffer pMeshes = scene.mMeshes();

            var model = new Model(numMaterials, numMeshes);

            //Process materials
            for (int i = 0; i < numMaterials; i++) {
                AIMaterial aiMaterial = AIMaterial.create(pMaterials.get(i));
                processMaterial(aiMaterial, model.materials.get(i));
            }

            //Process meshes
            for (int i = 0; i < numMeshes; i++) {
                AIMesh aiMesh = AIMesh.create(pMeshes.get(i));
                processMesh(aiMesh, model.meshes.get(i));
            }

            return model;
        }
    }

    public static Model loadModel(String modelFilepath) throws IOException {
        if (modelFilepath.endsWith(".bd1") || modelFilepath.endsWith(".BD1")) {
            return loadModelWithJXM(modelFilepath);
        } else {
            return loadModelWithAssimp(modelFilepath);
        }
    }
}
