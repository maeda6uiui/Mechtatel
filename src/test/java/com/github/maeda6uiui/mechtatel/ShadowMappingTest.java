package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.Model3D;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ShadowMappingTest extends Mechtatel {
    public ShadowMappingTest(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings settings;
        try {
            settings = new MttSettings("./Mechtatel/Setting/settings.json");
        } catch (IOException e) {
            System.out.println("Failed to load setting file. Use default settings");
            settings = new MttSettings();
        }

        new ShadowMappingTest(settings);
    }

    private FreeCamera camera;
    private Model3D plane;
    private Model3D teapot;
    private List<Model3D> cubes;
    private List<Vector3f> cubePositions;
    private List<Float> cubeRotations;

    @Override
    public void init() {
        var ppNaborNames = new ArrayList<String>();
        ppNaborNames.add("parallel_light");
        ppNaborNames.add("fog");
        ppNaborNames.add("shadow_mapping");
        MttScreen mainScreen = this.createScreen(
                "main",
                2048,
                2048,
                -1,
                -1,
                true,
                ppNaborNames
        );

        var screenDrawOrder = new ArrayList<String>();
        screenDrawOrder.add("main");
        this.setScreenDrawOrder(screenDrawOrder);

        this.setPresentScreenName("main");

        camera = new FreeCamera(mainScreen.getCamera());

        cubes = new ArrayList<>();
        cubePositions = new ArrayList<>();
        cubeRotations = new ArrayList<>();
        try {
            plane = this.createModel3D("main", "./Mechtatel/Model/Plane/plane.obj");
            plane.rescale(new Vector3f(2.0f, 1.0f, 2.0f));

            teapot = this.createModel3D("main", "./Mechtatel/Model/Teapot/teapot.obj");
            teapot.rescale(new Vector3f(2.0f, 2.0f, 2.0f));

            var cube = this.createModel3D("main", "./Mechtatel/Model/Cube/cube.obj");
            cube.translate(new Vector3f(6.0f, 2.0f, 0.0f));
            cubes.add(cube);
            cubePositions.add(new Vector3f(6.0f, 2.0f, 0.0f));
            cubeRotations.add(0.0f);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mainScreen.createParallelLight();

        mainScreen.getFog().setStart(10.0f);
        mainScreen.getFog().setEnd(20.0f);

        var shadowMappingSettings = new ShadowMappingSettings();
        shadowMappingSettings.setBiasCoefficient(0.002f);
        mainScreen.setShadowMappingSettings(shadowMappingSettings);
    }

    @Override
    public void dispose() {

    }

    @Override
    public void reshape(int width, int height) {

    }

    @Override
    public void update() {
        camera.translate(
                this.getKeyboardPressingCount("W"),
                this.getKeyboardPressingCount("S"),
                this.getKeyboardPressingCount("A"),
                this.getKeyboardPressingCount("D")
        );
        camera.rotate(
                this.getKeyboardPressingCount("UP"),
                this.getKeyboardPressingCount("DOWN"),
                this.getKeyboardPressingCount("LEFT"),
                this.getKeyboardPressingCount("RIGHT")
        );

        if (this.getKeyboardPressingCount("ENTER") == 1) {
            var srcCube = cubes.get(0);
            var dupCube = this.duplicateModel3D(srcCube);

            dupCube.translate(new Vector3f(6.0f, 2.0f, 0.0f));
            cubes.add(dupCube);
            cubePositions.add(new Vector3f(6.0f, 2.0f, 0.0f));
            cubeRotations.add(0.0f);
        }

        for (int i = 0; i < cubes.size(); i++) {
            var cube = cubes.get(i);
            var cubePosition = cubePositions.get(i);
            var cubeRotation = cubeRotations.get(i);

            //Translation
            var posRotMat = new Matrix4f().rotate((float) Math.toRadians(0.5f), 0.0f, 1.0f, 0.0f);
            var newCubePosition = posRotMat.transformPosition(cubePosition);
            cubePositions.set(i, newCubePosition);

            var translateMat = new Matrix4f().translate(newCubePosition);

            //Rotation
            final float CUBE_ROT_ANGLE = (float) Math.toRadians(0.5f);
            var newCubeRotation = cubeRotation + CUBE_ROT_ANGLE;
            cubeRotations.set(i, newCubeRotation);

            var rotXMat = new Matrix4f().rotateX(newCubeRotation);
            var rotYMat = new Matrix4f().rotateY(newCubeRotation);
            var rotZMat = new Matrix4f().rotateZ(newCubeRotation);

            var mat = translateMat.mul(rotZMat).mul(rotYMat).mul(rotXMat);
            cube.setMat(mat);
        }
    }
}
