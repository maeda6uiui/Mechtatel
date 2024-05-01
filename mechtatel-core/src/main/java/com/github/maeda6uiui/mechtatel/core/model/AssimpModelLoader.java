package com.github.maeda6uiui.mechtatel.core.model;

import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex;
import org.joml.*;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Math;
import java.net.URI;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.lwjgl.assimp.Assimp.*;

/**
 * Loads 3D models with Assimp
 *
 * @author maeda6uiui
 */
public class AssimpModelLoader {
    public static final int MAX_NUM_WEIGHTS = 4;    //Must be 4 or less
    public static final int MAX_NUM_BONES = 150;

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

    private static Matrix4f toMatrix(AIMatrix4x4 aiMatrix4x4) {
        var result = new Matrix4f();
        result.m00(aiMatrix4x4.a1());
        result.m10(aiMatrix4x4.a2());
        result.m20(aiMatrix4x4.a3());
        result.m30(aiMatrix4x4.a4());
        result.m01(aiMatrix4x4.b1());
        result.m11(aiMatrix4x4.b2());
        result.m21(aiMatrix4x4.b3());
        result.m31(aiMatrix4x4.b4());
        result.m02(aiMatrix4x4.c1());
        result.m12(aiMatrix4x4.c2());
        result.m22(aiMatrix4x4.c3());
        result.m32(aiMatrix4x4.c4());
        result.m03(aiMatrix4x4.d1());
        result.m13(aiMatrix4x4.d2());
        result.m23(aiMatrix4x4.d3());
        result.m33(aiMatrix4x4.d4());

        return result;
    }

    private static MttAnimMeshData processBones(AIMesh aiMesh, List<MttBone> boneList) {
        var boneIds = new ArrayList<Integer>();
        var weights = new ArrayList<Float>();
        var weightSet = new HashMap<Integer, List<MttVertexWeight>>();

        int numBones = aiMesh.mNumBones();
        PointerBuffer aiBones = aiMesh.mBones();
        for (int i = 0; i < numBones; i++) {
            AIBone aiBone = AIBone.create(aiBones.get(i));
            int id = boneList.size();
            var bone = new MttBone(id, aiBone.mName().dataString(), toMatrix(aiBone.mOffsetMatrix()));
            boneList.add(bone);

            int numWeights = aiBone.mNumWeights();
            AIVertexWeight.Buffer aiWeights = aiBone.mWeights();
            for (int j = 0; j < numWeights; j++) {
                AIVertexWeight aiWeight = aiWeights.get(j);
                var vw = new MttVertexWeight(bone.boneId(), aiWeight.mVertexId(), aiWeight.mWeight());

                List<MttVertexWeight> vertexWeightList = weightSet.computeIfAbsent(vw.vertexId(), v -> new ArrayList<>());
                vertexWeightList.add(vw);
            }
        }

        int numVertices = aiMesh.mNumVertices();
        for (int i = 0; i < numVertices; i++) {
            List<MttVertexWeight> vertexWeightList = weightSet.get(i);
            int size = vertexWeightList != null ? vertexWeightList.size() : 0;
            for (int j = 0; j < MAX_NUM_WEIGHTS; j++) {
                if (j < size) {
                    MttVertexWeight vw = vertexWeightList.get(j);
                    weights.add(vw.weight());
                    boneIds.add(vw.boneId());
                } else {
                    weights.add(0.0f);
                    boneIds.add(0);
                }
            }
        }

        return new MttAnimMeshData(weights, boneIds);
    }

    private static void processMesh(AIMesh aiMesh, MttMesh mesh, List<MttBone> boneList) {
        var positions = new ArrayList<Vector3fc>();
        var texCoords = new ArrayList<Vector2fc>();
        var normals = new ArrayList<Vector3fc>();

        processPositions(aiMesh, positions);
        processTexCoords(aiMesh, texCoords);
        processNormals(aiMesh, normals);
        processIndices(aiMesh, mesh.indices);

        mesh.materialIndex = aiMesh.mMaterialIndex();

        MttAnimMeshData animMeshData = processBones(aiMesh, boneList);

        //Create vertices
        int numVertices = positions.size();
        for (int i = 0; i < numVertices; i++) {
            var boneWeights = new Vector4f(0.0f);
            var boneIndices = new Vector4i(-1);
            for (int j = 0; j < Math.min(MAX_NUM_WEIGHTS, 4); j++) {
                boneWeights.setComponent(j, animMeshData.weights().get(i * MAX_NUM_WEIGHTS + j));
                boneIndices.setComponent(j, animMeshData.boneIds().get(i * MAX_NUM_WEIGHTS + j));
            }

            var vertex = new MttVertex(
                    positions.get(i),
                    new Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
                    texCoords.get(i),
                    normals.get(i),
                    boneWeights,
                    boneIndices
            );
            mesh.vertices.add(vertex);
        }
    }

    private static MttNode buildNodesTree(AINode aiNode, MttNode parentNode) {
        String nodeName = aiNode.mName().dataString();
        var node = new MttNode(nodeName, parentNode, toMatrix(aiNode.mTransformation()));

        int numChildren = aiNode.mNumChildren();
        PointerBuffer aiChildren = aiNode.mChildren();
        for (int i = 0; i < numChildren; i++) {
            AINode aiChildNode = AINode.create(aiChildren.get(i));
            MttNode childNode = buildNodesTree(aiChildNode, node);
            node.addChild(childNode);
        }

        return node;
    }

    private static int calcAnimationMaxFrames(AIAnimation aiAnimation) {
        int maxFrames = 0;
        int numNodeAnims = aiAnimation.mNumChannels();
        PointerBuffer aiChannels = aiAnimation.mChannels();
        for (int i = 0; i < numNodeAnims; i++) {
            AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(i));
            int numFrames = Math.max(
                    Math.max(
                            aiNodeAnim.mNumPositionKeys(), aiNodeAnim.mNumScalingKeys()
                    ),
                    aiNodeAnim.mNumRotationKeys()
            );
            maxFrames = Math.max(maxFrames, numFrames);
        }

        return maxFrames;
    }

    private static AINodeAnim findAIAnimNode(AIAnimation aiAnimation, String nodeName) {
        AINodeAnim result = null;

        int numAnimNodes = aiAnimation.mNumChannels();
        PointerBuffer aiChannels = aiAnimation.mChannels();
        for (int i = 0; i < numAnimNodes; i++) {
            AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(i));
            if (nodeName.equals(aiNodeAnim.mNodeName().dataString())) {
                result = aiNodeAnim;
                break;
            }
        }

        return result;
    }

    private static Matrix4f buildNodeTransformationMatrix(AINodeAnim aiNodeAnim, int frame) {
        AIVectorKey.Buffer positionKeys = aiNodeAnim.mPositionKeys();
        AIVectorKey.Buffer scalingKeys = aiNodeAnim.mScalingKeys();
        AIQuatKey.Buffer rotationKeys = aiNodeAnim.mRotationKeys();

        AIVectorKey aiVecKey;
        AIQuatKey aiQuatKey;
        AIVector3D vec;

        var nodeTransform = new Matrix4f();
        int numPositions = aiNodeAnim.mNumPositionKeys();
        if (numPositions > 0) {
            aiVecKey = positionKeys.get(Math.min(numPositions - 1, frame));
            vec = aiVecKey.mValue();
            nodeTransform.translate(vec.x(), vec.y(), vec.z());
        }

        int numRotations = aiNodeAnim.mNumRotationKeys();
        if (numRotations > 0) {
            aiQuatKey = rotationKeys.get(Math.min(numRotations - 1, frame));
            AIQuaternion aiQuat = aiQuatKey.mValue();
            var quat = new Quaternionf(aiQuat.x(), aiQuat.y(), aiQuat.z(), aiQuat.w());
            nodeTransform.rotate(quat);
        }

        int numScalingKeys = aiNodeAnim.mNumScalingKeys();
        if (numScalingKeys > 0) {
            aiVecKey = scalingKeys.get(Math.min(numScalingKeys - 1, frame));
            vec = aiVecKey.mValue();
            nodeTransform.scale(vec.x(), vec.y(), vec.z());
        }

        return nodeTransform;
    }

    private static void buildFrameMatrices(
            AIAnimation aiAnimation,
            List<MttBone> boneList,
            MttModelData.AnimatedFrame animatedFrame,
            int frame,
            MttNode node,
            Matrix4f parentTransformation,
            Matrix4f globalInverseTransform) {
        String nodeName = node.getName();
        AINodeAnim aiNodeAnim = findAIAnimNode(aiAnimation, nodeName);
        Matrix4f nodeTransform = node.getNodeTransformation();
        if (aiNodeAnim != null) {
            nodeTransform = buildNodeTransformationMatrix(aiNodeAnim, frame);
        }
        var nodeGlobalTransform = new Matrix4f(parentTransformation).mul(nodeTransform);

        List<MttBone> affectedBones = boneList.stream().filter(b -> b.boneName().equals(nodeName)).toList();
        for (var bone : affectedBones) {
            var boneTransform = new Matrix4f(globalInverseTransform).mul(nodeGlobalTransform).mul(bone.offsetMatrix());
            animatedFrame.boneMatrices()[bone.boneId()] = boneTransform;
        }

        for (var childNode : node.getChildren()) {
            buildFrameMatrices(
                    aiAnimation,
                    boneList,
                    animatedFrame,
                    frame,
                    childNode,
                    nodeGlobalTransform,
                    globalInverseTransform
            );
        }
    }

    private static void processAnimations(
            AIScene aiScene,
            List<MttBone> boneList,
            MttNode rootNode,
            Matrix4f globalInverseTransformation,
            List<MttModelData.Animation> animationList) {
        int numAnimations = aiScene.mNumAnimations();
        PointerBuffer aiAnimations = aiScene.mAnimations();
        for (int i = 0; i < numAnimations; i++) {
            AIAnimation aiAnimation = AIAnimation.create(aiAnimations.get(i));
            int maxFrames = calcAnimationMaxFrames(aiAnimation);

            var frames = new ArrayList<MttModelData.AnimatedFrame>();
            var animation = new MttModelData.Animation(
                    aiAnimation.mName().dataString(),
                    aiAnimation.mDuration(),
                    frames
            );
            animationList.add(animation);

            for (int j = 0; j < maxFrames; j++) {
                var boneMatrices = new Matrix4f[MAX_NUM_BONES];
                Arrays.fill(boneMatrices, new Matrix4f().identity());
                var animatedFrame = new MttModelData.AnimatedFrame(boneMatrices);
                buildFrameMatrices(
                        aiAnimation,
                        boneList,
                        animatedFrame,
                        j,
                        rootNode,
                        rootNode.getNodeTransformation(),
                        globalInverseTransformation
                );
                frames.add(animatedFrame);
            }
        }
    }

    public static MttModelData load(URI modelResource) throws IOException {
        Path modelFile = Paths.get(modelResource);
        try (AIScene aiScene = aiImportFile(
                modelFile.toString(),
                aiProcessPreset_TargetRealtime_Quality | aiProcess_FlipUVs)
        ) {
            if (aiScene == null || aiScene.mRootNode() == null) {
                String errorStr = String.format(
                        "Could not load a model %s\n%s", modelResource.getPath(), aiGetErrorString());
                throw new IOException(errorStr);
            }

            int numMaterials = aiScene.mNumMaterials();
            int numMeshes = aiScene.mNumMeshes();

            PointerBuffer pMaterials = aiScene.mMaterials();
            PointerBuffer pMeshes = aiScene.mMeshes();

            var modelData = new MttModelData(numMaterials, numMeshes);

            //Process materials
            Path modelDir = modelFile.getParent();
            for (int i = 0; i < numMaterials; i++) {
                AIMaterial aiMaterial = AIMaterial.create(pMaterials.get(i));
                processMaterial(aiMaterial, modelData.materials.get(i), modelDir.toString());
            }

            //Process meshes
            var boneList = new ArrayList<MttBone>();
            for (int i = 0; i < numMeshes; i++) {
                AIMesh aiMesh = AIMesh.create(pMeshes.get(i));
                processMesh(aiMesh, modelData.meshes.get(i), boneList);
            }

            //Process animations
            int numAnimations = aiScene.mNumAnimations();
            if (numAnimations > 0) {
                MttNode rootNode = buildNodesTree(aiScene.mRootNode(), null);
                Matrix4f globalInverseTransformation = toMatrix(aiScene.mRootNode().mTransformation()).invert();
                processAnimations(
                        aiScene,
                        boneList,
                        rootNode,
                        globalInverseTransformation,
                        modelData.animationList
                );
            }

            return modelData;
        }
    }
}
