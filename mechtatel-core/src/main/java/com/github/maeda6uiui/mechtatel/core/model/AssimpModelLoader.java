package com.github.maeda6uiui.mechtatel.core.model;

import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertexUV;
import org.joml.*;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.assimp.Assimp.*;

/**
 * Loads 3D models with Assimp
 *
 * @author maeda6uiui
 */
public class AssimpModelLoader {

    private static void processMaterial(
            AIMaterial aiMaterial, MttMaterial material, String modelDirname) throws IOException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            //Get the filepath of the diffuse texture
            AIString aiDiffuseTexFilename = AIString.calloc(stack);
            aiGetMaterialTexture(
                    aiMaterial,
                    aiTextureType_DIFFUSE,
                    0,
                    aiDiffuseTexFilename,
                    (IntBuffer) null,
                    null,
                    null,
                    null,
                    null,
                    null);
            String diffuseTexFilename = aiDiffuseTexFilename.dataString();

            Path diffuseTexFile = Paths.get(modelDirname, diffuseTexFilename);
            if (!Files.exists(diffuseTexFile)) {
                throw new FileNotFoundException("Texture file for the model does not exist: " + diffuseTexFile);
            }

            material.diffuseTexResource = diffuseTexFile.toUri();

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

    private static void processMesh(AIMesh aiMesh, MttMesh mesh) {
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
            var vertex = new MttVertexUV(
                    positions.get(i),
                    new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                    texCoords.get(i),
                    normals.get(i));
            mesh.vertices.add(vertex);
        }
    }

    public static MttModelData load(URI modelResource) throws IOException {
        Path modelFile = Paths.get(modelResource);
        try (AIScene scene = aiImportFile(
                modelFile.toString(),
                aiProcessPreset_TargetRealtime_Quality | aiProcess_FlipUVs)) {
            if (scene == null || scene.mRootNode() == null) {
                String errorStr = String.format(
                        "Could not load a model %s\n%s", modelResource.getPath(), aiGetErrorString());
                throw new IOException(errorStr);
            }

            int numMaterials = scene.mNumMaterials();
            int numMeshes = scene.mNumMeshes();

            PointerBuffer pMaterials = scene.mMaterials();
            PointerBuffer pMeshes = scene.mMeshes();

            var model = new MttModelData(numMaterials, numMeshes);

            //Process materials
            Path modelDir = modelFile.getParent();
            for (int i = 0; i < numMaterials; i++) {
                AIMaterial aiMaterial = AIMaterial.create(pMaterials.get(i));
                processMaterial(aiMaterial, model.materials.get(i), modelDir.toString());
            }

            //Process meshes
            for (int i = 0; i < numMeshes; i++) {
                AIMesh aiMesh = AIMesh.create(pMeshes.get(i));
                processMesh(aiMesh, model.meshes.get(i));
            }

            return model;
        }
    }
}
