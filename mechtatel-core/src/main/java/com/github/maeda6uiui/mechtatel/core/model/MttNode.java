package com.github.maeda6uiui.mechtatel.core.model;

import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Node for model animation
 *
 * @author maeda6uiui
 */
class MttNode {
    private final List<MttNode> children;
    private final String name;
    private final MttNode parent;
    private final Matrix4f nodeTransformation;

    public MttNode(String name, MttNode parent, Matrix4f nodeTransformation) {
        this.name = name;
        this.parent = parent;
        this.nodeTransformation = nodeTransformation;
        this.children = new ArrayList<>();
    }

    public void addChild(MttNode node) {
        this.children.add(node);
    }

    public List<MttNode> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public Matrix4f getNodeTransformation() {
        return nodeTransformation;
    }

    public MttNode getParent() {
        return parent;
    }
}
