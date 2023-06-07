package com.github.maeda6uiui.mechtatel.core.animation;

import com.github.maeda6uiui.mechtatel.core.component.MttModel3D;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

/**
 * Model animation
 *
 * @author maeda6uiui
 */
public class MttAnimation {
    private AnimationInfo animInfo;
    private Map<String, MttModel3D> models;

    private char[] getRotationApplyOrderCs(String rotationApplyOrder) {
        if (rotationApplyOrder.length() != 3) {
            throw new RuntimeException(
                    "Rotation apply order must be 3-letter representation such as xyz");
        }

        var cs = new char[3];
        for (int i = 0; i < 3; i++) {
            cs[i] = rotationApplyOrder.charAt(i);
            if (!(cs[i] == 'x' || cs[i] == 'y' || cs[i] == 'z')) {
                throw new RuntimeException(
                        "Element of rotation apply order must be one of x,y, and z");
            }
        }

        return cs;
    }

    public MttAnimation(AnimationInfo animInfo, Map<String, MttModel3D> models) {
        this.animInfo = animInfo;
        this.models = models;

        for (var entry : animInfo.getModels().entrySet()) {
            String modelName = entry.getKey();
            AnimationInfo.Model animModel = entry.getValue();

            MttModel3D model = models.get(modelName);
            for (var op : animModel.initialProperties.applyOrder) {
                if (op.equals("scale")) {
                    model.rescale(animModel.initialProperties.scale);
                } else if (op.equals("rotation")) {
                    Vector3f rotation = animModel.initialProperties.rotation;

                    char[] cs = this.getRotationApplyOrderCs(animModel.initialProperties.rotationApplyOrder);
                    for (char c : cs) {
                        if (c == 'x') {
                            model.rotX(rotation.x);
                        } else if (c == 'y') {
                            model.rotY(rotation.y);
                        } else if (c == 'z') {
                            model.rotZ(rotation.z);
                        }
                    }
                } else if (op.equals("position")) {
                    model.translate(animModel.initialProperties.position);
                } else {
                    throw new RuntimeException("Unsupported operation specified in applyOrder: " + op);
                }
            }
        }
    }

    public Map<String, MttModel3D> getModels() {
        return new HashMap<>(models);
    }
}
