package com.github.maeda6uiui.mechtatel.core.model;

import com.github.dabasan.jxm.bd1.BD1Buffer;
import com.github.dabasan.jxm.bd1.BD1Manipulator;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads 3D models with JXM
 *
 * @author maeda6uiui
 */
public class JXMModelLoader {
    public static MttModelData load(Path modelFile) throws IOException {
        var manipulator = new BD1Manipulator(modelFile.toFile());

        //Rescale the model so that 1 coord represents 1 meter
        final float RESCALE_FACTOR = 1.7f / 20.0f;
        manipulator.rescale(RESCALE_FACTOR, RESCALE_FACTOR, RESCALE_FACTOR).applyTransformation();

        //Invert the model along the Z-axis
        //so that it matches the right-handed coordinate system of OpenGL and Vulkan
        manipulator.invertZ();

        List<BD1Buffer> buffers = manipulator.getBuffers(false);

        Path modelDir = modelFile.getParent();

        var model = new MttModelData(buffers.size(), buffers.size());
        for (int i = 0; i < buffers.size(); i++) {
            BD1Buffer buffer = buffers.get(i);

            String textureFilename = manipulator.getTextureFilename(buffer.textureID);
            Path textureFile = modelDir.resolve(textureFilename);
            if (!Files.exists(textureFile)) {
                throw new FileNotFoundException("Texture file for the model does not exist: " + textureFile);
            }

            model.materials.get(i).diffuseTexFile = textureFile;
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

                var vertex = new MttVertex(position, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), texCoords, normal);
                model.meshes.get(i).vertices.add(vertex);
            }
        }

        return model;
    }
}
