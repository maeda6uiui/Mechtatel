package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.component.*;
import com.github.maeda6uiui.mechtatel.core.component.gui.*;
import com.github.maeda6uiui.mechtatel.core.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.physics.*;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.sound.Sound3D;
import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import org.joml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Base class of the Mechtatel engine
 *
 * @author maeda6uiui
 */
public class Mechtatel implements IMechtatel {
    private final Logger logger = LoggerFactory.getLogger(Mechtatel.class);

    private MttInstance instance;

    public Mechtatel(MttSettings settings) {
        logger.info(settings.toString());

        try {
            instance = new MttInstance(this, settings);
            logger.info("MttInstance successfully created");

            logger.info("Start running the main loop");
            instance.run();

            logger.info("Start cleaning up Mechtatel");
            instance.cleanup();

            System.exit(0);
        } catch (Exception e) {
            logger.error("Fatal error", e);
        }
    }

    @Override
    public void init() {
        logger.info("Mechtatel initialized");
    }

    @Override
    public void dispose() {
        logger.info("Mechtatel disposed");
    }

    @Override
    public void reshape(int width, int height) {
        logger.trace("Framebuffer size changed: (width, height)=({},{})", width, height);
    }

    @Override
    public void update() {

    }

    public void closeWindow() {
        instance.closeWindow();
    }

    public int getWindowWidth() {
        return instance.getWindowWidth();
    }

    public int getWindowHeight() {
        return instance.getWindowHeight();
    }

    public int getKeyboardPressingCount(String key) {
        return instance.getKeyboardPressingCount(key);
    }

    public int getKeyboardReleasingCount(String key) {
        return instance.getKeyboardReleasingCount(key);
    }

    public int getMousePressingCount(String key) {
        return instance.getMousePressingCount(key);
    }

    public int getMouseReleasingCount(String key) {
        return instance.getMouseReleasingCount(key);
    }

    public int getCursorPosX() {
        return instance.getCursorPosX();
    }

    public int getCursorPosY() {
        return instance.getCursorPosY();
    }

    /**
     * Fixes the cursor to (0,0).
     */
    public void fixCursor() {
        instance.setCursorPos(0, 0);
        instance.setFixCursorFlag(true);
    }

    public void unfixCursor() {
        instance.setFixCursorFlag(false);
    }

    public int setCursorMode(String cursorMode) {
        return instance.setCursorMode(cursorMode);
    }

    //=== Methods relating to components ===
    public void sortComponents() {
        instance.sortComponents();
    }

    public Model3D createModel3D(String screenName, String modelFilepath) throws IOException {
        return instance.createModel3D(screenName, modelFilepath);
    }

    public Model3D duplicateModel3D(Model3D srcModel) {
        return instance.duplicateModel3D(srcModel);
    }

    public Line3D createLine3D(Vertex3D v1, Vertex3D v2) {
        return instance.createLine3D(v1, v2);
    }

    public Line3D createLine3D(Vector3fc p1, Vector4fc color1, Vector3fc p2, Vector4fc color2) {
        var v1 = new Vertex3D(p1, color1);
        var v2 = new Vertex3D(p2, color2);
        return instance.createLine3D(v1, v2);
    }

    public Line3D createLine3D(Vector3fc p1, Vector3fc p2, Vector4fc color) {
        var v1 = new Vertex3D(p1, color);
        var v2 = new Vertex3D(p2, color);
        return instance.createLine3D(v1, v2);
    }

    public Line3DSet createLine3DSet() {
        return instance.createLine3DSet();
    }

    public Line3DSet createAxesLine3DSet(float length) {
        Line3DSet axes = instance.createLine3DSet();

        axes.add(new Vector3f(-length, 0.0f, 0.0f), new Vector3f(length, 0.0f, 0.0f), new Vector4f(1.0f, 0.0f, 0.0f, 1.0f));
        axes.add(new Vector3f(0.0f, -length, 0.0f), new Vector3f(0.0f, length, 0.0f), new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
        axes.add(new Vector3f(0.0f, 0.0f, -length), new Vector3f(0.0f, 0.0f, length), new Vector4f(0.0f, 0.0f, 1.0f, 1.0f));
        axes.createBuffer();

        return axes;
    }

    public Line3DSet createPositiveAxesLine3DSet(float length) {
        Line3DSet axes = instance.createLine3DSet();

        axes.add(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(length, 0.0f, 0.0f), new Vector4f(1.0f, 0.0f, 0.0f, 1.0f));
        axes.add(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, length, 0.0f), new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
        axes.add(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, length), new Vector4f(0.0f, 0.0f, 1.0f, 1.0f));
        axes.createBuffer();

        return axes;
    }

    public Sphere3D createSphere3D(Vector3fc center, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        return instance.createSphere3D(center, radius, numVDivs, numHDivs, color);
    }

    public Capsule3D createCapsule3D(Vector3fc center, float length, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        return instance.createCapsule3D(center, length, radius, numVDivs, numHDivs, color);
    }

    public Line2D createLine2D(Vertex2D p1, Vertex2D p2, float z) {
        return instance.createLine2D(p1, p2, z);
    }

    public Line2DSet createLine2DSet() {
        return instance.createLine2DSet();
    }

    public FilledQuad3D createFilledQuad3D(Vertex3D v1, Vertex3D v2, Vertex3D v3, Vertex3D v4) {
        return instance.createFilledQuad3D(v1, v2, v3, v4);
    }

    public FilledQuad3D createFilledQuad3D(Vector3fc p1, Vector3fc p2, Vector3fc p3, Vector3fc p4, Vector4fc color) {
        var v1 = new Vertex3D(p1, color);
        var v2 = new Vertex3D(p2, color);
        var v3 = new Vertex3D(p3, color);
        var v4 = new Vertex3D(p4, color);

        return instance.createFilledQuad3D(v1, v2, v3, v4);
    }

    public FilledQuad2D createFilledQuad2D(Vertex2D p1, Vertex2D p2, Vertex2D p3, Vertex2D p4, float z) {
        return instance.createFilledQuad2D(p1, p2, p3, p4, z);
    }

    public FilledQuad2D createFilledQuad2D(Vector2fc p1, Vector2fc p2, Vector2fc p3, Vector2fc p4, float z, Vector4fc color) {
        return instance.createFilledQuad2D(p1, p2, p3, p4, z, color);
    }

    public FilledQuad2D createFilledQuad2D(Vector2fc topLeft, Vector2fc bottomRight, float z, Vector4fc color) {
        return instance.createFilledQuad2D(topLeft, bottomRight, z, color);
    }

    public TexturedQuad3D createTexturedQuad3D(
            String screenName,
            String textureFilepath,
            boolean generateMipmaps,
            Vertex3DUV v1,
            Vertex3DUV v2,
            Vertex3DUV v3,
            Vertex3DUV v4) {
        return instance.createTexturedQuad3D(screenName, textureFilepath, generateMipmaps, v1, v2, v3, v4);
    }

    public TexturedQuad3D createTexturedQuad3D(
            MttTexture texture,
            Vertex3DUV v1,
            Vertex3DUV v2,
            Vertex3DUV v3,
            Vertex3DUV v4) {
        return instance.createTexturedQuad3D(texture, v1, v2, v3, v4);
    }

    public TexturedQuad3D duplicateTexturedQuad3D(
            String screenName,
            TexturedQuad3D srcQuad,
            Vertex3DUV v1,
            Vertex3DUV v2,
            Vertex3DUV v3,
            Vertex3DUV v4) {
        return instance.duplicateTexturedQuad3D(screenName, srcQuad, v1, v2, v3, v4);
    }

    public TexturedQuad2D createTexturedQuad2D(
            String screenName, String textureFilepath, Vertex2DUV p1, Vertex2DUV p2, Vertex2DUV p3, Vertex2DUV p4, float z) {
        return instance.createTexturedQuad2D(screenName, textureFilepath, p1, p2, p3, p4, z);
    }

    public TexturedQuad2D createTexturedQuad2D(
            MttTexture texture, Vertex2DUV p1, Vertex2DUV p2, Vertex2DUV p3, Vertex2DUV p4, float z) {
        return instance.createTexturedQuad2D(texture, p1, p2, p3, p4, z);
    }

    public TexturedQuad2D createTexturedQuad2D(
            String screenName, String textureFilepath, Vector2fc topLeft, Vector2fc bottomRight, float z) {
        var p1 = new Vertex2DUV(topLeft, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 0.0f));
        var p2 = new Vertex2DUV(new Vector2f(topLeft.x(), bottomRight.y()), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 1.0f));
        var p3 = new Vertex2DUV(bottomRight, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 1.0f));
        var p4 = new Vertex2DUV(new Vector2f(bottomRight.x(), topLeft.y()), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 0.0f));

        return instance.createTexturedQuad2D(screenName, textureFilepath, p1, p2, p3, p4, z);
    }

    public TexturedQuad2D createTexturedQuad2D(MttTexture texture, Vector2fc topLeft, Vector2fc bottomRight, float z) {
        var p1 = new Vertex2DUV(topLeft, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 0.0f));
        var p2 = new Vertex2DUV(new Vector2f(topLeft.x(), bottomRight.y()), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 1.0f));
        var p3 = new Vertex2DUV(bottomRight, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 1.0f));
        var p4 = new Vertex2DUV(new Vector2f(bottomRight.x(), topLeft.y()), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 0.0f));

        return instance.createTexturedQuad2D(texture, p1, p2, p3, p4, z);
    }

    public TexturedQuad2D duplicateTexturedQuad2D(
            String screenName, TexturedQuad2D srcQuad, Vertex2DUV p1, Vertex2DUV p2, Vertex2DUV p3, Vertex2DUV p4, float z) {
        return instance.duplicateTexturedQuad2D(screenName, srcQuad, p1, p2, p3, p4, z);
    }

    public TexturedQuad2D duplicateTexturedQuad2D(
            String screenName, TexturedQuad2D srcQuad, Vector2fc topLeft, Vector2fc bottomRight, float z) {
        var p1 = new Vertex2DUV(topLeft, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 0.0f));
        var p2 = new Vertex2DUV(new Vector2f(topLeft.x(), bottomRight.y()), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 1.0f));
        var p3 = new Vertex2DUV(bottomRight, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 1.0f));
        var p4 = new Vertex2DUV(new Vector2f(bottomRight.x(), topLeft.y()), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 0.0f));
        return instance.duplicateTexturedQuad2D(screenName, srcQuad, p1, p2, p3, p4, z);
    }

    public TexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(String screenName, String textureFilepath) {
        return instance.createTexturedQuad2DSingleTextureSet(screenName, textureFilepath);
    }

    public TexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(MttTexture texture) {
        return instance.createTexturedQuad2DSingleTextureSet(texture);
    }

    public Quad2D createQuad2D(Vertex2D v1, Vertex2D v2, Vertex2D v3, Vertex2D v4, float z) {
        return instance.createQuad2D(v1, v2, v3, v4, z);
    }

    public Quad2D createQuad2D(Vector2fc p1, Vector2fc p2, Vector2fc p3, Vector2fc p4, float z, Vector4fc color) {
        return instance.createQuad2D(p1, p2, p3, p4, z, color);
    }

    public Quad2D createQuad2D(Vector2fc topLeft, Vector2fc bottomRight, float z, Vector4fc color) {
        return instance.createQuad2D(topLeft, bottomRight, z, color);
    }

    public Quad3D createQuad3D(Vertex3D v1, Vertex3D v2, Vertex3D v3, Vertex3D v4) {
        return instance.createQuad3D(v1, v2, v3, v4);
    }

    public Quad3D createQuad3D(Vector3fc p1, Vector3fc p2, Vector3fc p3, Vector3fc p4, Vector4fc color) {
        return instance.createQuad3D(p1, p2, p3, p4, color);
    }

    public Box3D createBox3D(float xHalfExtent, float yHalfExtent, float zHalfExtent, Vector4fc color) {
        return instance.createBox3D(xHalfExtent, yHalfExtent, zHalfExtent, color);
    }

    public Box3D createBox3D(float halfExtent, Vector4fc color) {
        return instance.createBox3D(halfExtent, color);
    }

    public MttFont createMttFont(String screenName, Font font, boolean antiAlias, Color fontColor, String requiredChars) {
        return instance.createMttFont(screenName, font, antiAlias, fontColor, requiredChars);
    }

    public MttButton createMttButton(
            float x,
            float y,
            float width,
            float height,
            String text,
            String fontName,
            int fontStyle,
            int fontSize,
            Color fontColor,
            Color frameColor) {
        return instance.createMttButton(x, y, width, height, text, fontName, fontStyle, fontSize, fontColor, frameColor);
    }

    public MttCheckbox createMttCheckbox(
            float x,
            float y,
            float width,
            float height,
            String text,
            String fontName,
            int fontStyle,
            int fontSize,
            Color fontColor,
            Color checkboxColor) {
        return instance.createMttCheckbox(
                x, y, width, height, text, fontName, fontStyle, fontSize, fontColor, checkboxColor);
    }

    public MttVerticalScrollbar createMttVerticalScrollbar(
            float x,
            float y,
            float width,
            float height,
            float grabHeight,
            Color frameColor,
            Color grabFrameColor) {
        return instance.createMttVerticalScrollbar(x, y, width, height, grabHeight, frameColor, grabFrameColor);
    }

    public MttHorizontalScrollbar createMttHorizontalScrollbar(
            float x,
            float y,
            float width,
            float height,
            float grabWidth,
            Color frameColor,
            Color grabFrameColor) {
        return instance.createMttHorizontalScrollbar(x, y, width, height, grabWidth, frameColor, grabFrameColor);
    }

    public MttListbox createMttListbox(
            float x,
            float y,
            float width,
            float height,
            float scrollbarWidth,
            float scrollbarGrabHeight,
            Color scrollbarFrameColor,
            Color scrollbarGrabColor,
            String nonSelectedFontName,
            int nonSelectedFontStyle,
            int nonSelectedFontSize,
            Color nonSelectedFontColor,
            Color frameColor,
            List<String> itemTexts,
            float itemHeight,
            String selectedFontName,
            int selectedFontStyle,
            int selectedFontSize,
            Color selectedFontColor) {
        return instance.createMttListbox(
                x, y, width, height,
                scrollbarWidth, scrollbarGrabHeight, scrollbarFrameColor, scrollbarGrabColor,
                nonSelectedFontName, nonSelectedFontStyle, nonSelectedFontSize, nonSelectedFontColor,
                frameColor, itemTexts, itemHeight,
                selectedFontName, selectedFontStyle, selectedFontSize, selectedFontColor);
    }

    public MttLabel createMttLabel(
            float x,
            float y,
            float width,
            float height,
            String requiredChars,
            String fontName,
            int fontStyle,
            int fontSize,
            Color fontColor,
            Color frameColor) {
        return instance.createMttLabel(
                x, y, width, height, requiredChars, fontName, fontStyle, fontSize, fontColor, frameColor);
    }

    public PhysicalPlane3D createPhysicalPlane3D(Vector3fc normal, float constant) {
        return instance.createPhysicalPlane3D(normal, constant);
    }

    public PhysicalPlane3D createPhysicalPlane3DWithComponent(
            Vector3fc p1, Vector3fc p2, Vector3fc p3, Vector3fc p4, Vector4fc color) {
        return instance.createPhysicalPlane3DWithComponent(p1, p2, p3, p4, color);
    }

    public PhysicalSphere3D createPhysicalSphere3D(float radius, float mass) {
        return instance.createPhysicalSphere3D(radius, mass);
    }

    public PhysicalSphere3D createPhysicalSphere3DWithComponent(
            float radius, float mass, int numVDivs, int numHDivs, Vector4fc color) {
        return instance.createPhysicalSphere3DWithComponent(radius, mass, numVDivs, numHDivs, color);
    }

    public PhysicalCapsule3D createPhysicalCapsule3D(float radius, float height, float mass) {
        return instance.createPhysicalCapsule3D(radius, height, mass);
    }

    public PhysicalCapsule3D createPhysicalCapsule3DWithComponent(float radius, float height, float mass, int numVDivs, int numHDivs, Vector4fc color) {
        return instance.createPhysicalCapsule3DWithComponent(radius, height, mass, numVDivs, numHDivs, color);
    }

    public PhysicalBox3D createPhysicalBox3D(float xHalfExtent, float yHalfExtent, float zHalfExtent, float mass) {
        return instance.createPhysicalBox3D(xHalfExtent, yHalfExtent, zHalfExtent, mass);
    }

    public PhysicalBox3D createPhysicalBox3D(float halfExtent, float mass) {
        return instance.createPhysicalBox3D(halfExtent, mass);
    }

    public PhysicalBox3D createPhysicalBox3DWithComponent(float xHalfExtent, float yHalfExtent, float zHalfExtent, float mass, Vector4fc color) {
        return instance.createPhysicalBox3DWithComponent(xHalfExtent, yHalfExtent, zHalfExtent, mass, color);
    }

    public PhysicalBox3D createPhysicalBox3DWithComponent(float halfExtent, float mass, Vector4fc color) {
        return instance.createPhysicalBox3DWithComponent(halfExtent, mass, color);
    }

    public PhysicalMesh3D createPhysicalMesh3D(Model3D model, float mass) {
        return instance.createPhysicalMesh3D(model, mass);
    }

    public boolean removePhysicalObject3D(PhysicalObject3D physicalObject) {
        return instance.removePhysicalObject3D(physicalObject);
    }

    public void setPhysicsSimulationTimeScale(float physicsSimulationTimeScale) {
        instance.setPhysicsSimulationTimeScale(physicsSimulationTimeScale);
    }

    public Sound3D createSound3D(String filepath, boolean loop, boolean relative) {
        Sound3D sound;
        try {
            sound = instance.createSound3D(filepath, loop, relative);
        } catch (IOException e) {
            throw new RuntimeException("Could not load a sound " + filepath);
        }

        return sound;
    }

    public Sound3D duplicateSound3D(Sound3D srcSound, boolean loop, boolean relative) {
        Sound3D sound = instance.duplicateSound3D(srcSound, loop, relative);
        return sound;
    }

    public boolean removeSound3D(Sound3D sound) {
        return instance.removeSound3D(sound);
    }

    public MttTexture createMttTexture(String screenName, String textureFilepath, boolean generateMipmaps) {
        return instance.createMttTexture(screenName, textureFilepath, generateMipmaps);
    }

    public MttTexture texturizeScreen(String srcScreenName, String dstScreenName) {
        return instance.texturizeScreen(srcScreenName, dstScreenName);
    }

    public void saveScreenshot(String screenName, String srcImageFormat, String outputFilepath) throws IOException {
        instance.saveScreenshot(screenName, srcImageFormat, outputFilepath);
    }

    public MttScreen createScreen(
            String screenName,
            int depthImageWidth,
            int depthImageHeight,
            int screenWidth,
            int screenHeight,
            boolean shouldChangeExtentOnRecreate,
            List<String> ppNaborNames) {
        return instance.createScreen(
                screenName,
                depthImageWidth,
                depthImageHeight,
                screenWidth,
                screenHeight,
                shouldChangeExtentOnRecreate,
                ppNaborNames
        );
    }

    public boolean removeScreen(String screenName) {
        return instance.removeScreen(screenName);
    }

    public void setScreenDrawOrder(List<String> screenDrawOrder) {
        instance.setScreenDrawOrder(screenDrawOrder);
    }

    public MttScreen getDefaultScreen(){
        return instance.getDefaultScreen();
    }

    public Map<String,MttScreen> getScreens(){
        return instance.getScreens();
    }
}
