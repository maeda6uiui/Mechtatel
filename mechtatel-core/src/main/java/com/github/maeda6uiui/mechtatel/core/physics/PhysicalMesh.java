package com.github.maeda6uiui.mechtatel.core.physics;

import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.util.ModelLoader;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.collision.shapes.infos.IndexedMesh;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;

/**
 * Physical mesh
 * Note that collision detection between two meshes doesn't work.
 *
 * @author maeda6uiui
 */
public class PhysicalMesh extends PhysicalObject {
    public PhysicalMesh(MttModel model, float mass) {
        var indexedMeshes = new ArrayList<IndexedMesh>();

        ModelLoader.Model innerModel = model.getModel();
        for (var mesh : innerModel.meshes.values()) {
            int numPositions = mesh.vertices.size();
            var positions = new Vector3f[numPositions];
            for (int i = 0; i < numPositions; i++) {
                Vector3fc pos = mesh.vertices.get(i).pos;
                var position = new Vector3f(pos.x(), pos.y(), pos.z());
                positions[i] = position;
            }

            int numIndices = mesh.indices.size();
            var indices = new int[numIndices];
            for (int i = 0; i < numIndices; i++) {
                indices[i] = mesh.indices.get(i);
            }

            var indexedMesh = new IndexedMesh(positions, indices);
            indexedMeshes.add(indexedMesh);
        }

        var shape = new MeshCollisionShape(true, indexedMeshes);
        var body = new PhysicsRigidBody(shape, mass);
        body.setPhysicsLocation(new Vector3f(0.0f, 0.0f, 0.0f));

        this.setShape(shape);
        this.setBody(body);
    }
}
