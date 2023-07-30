package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.animation.AnimationInfo;
import com.github.maeda6uiui.mechtatel.core.animation.MttAnimation;
import com.github.maeda6uiui.mechtatel.core.component.*;
import com.github.maeda6uiui.mechtatel.core.component.gui.*;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.interpreter.KeyInterpreter;
import com.github.maeda6uiui.mechtatel.core.nabor.FlexibleNaborInfo;
import com.github.maeda6uiui.mechtatel.core.physics.*;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.sound.Sound3D;
import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.texture.TextureOperationParameters;
import org.joml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py4j.GatewayServer;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Base class of the Mechtatel engine
 *
 * @author maeda6uiui
 */
public class Mechtatel
        implements
        IMechtatelForMttInstance,
        IMechtatelForDrawPath,
        IMechtatelForScreenCreator,
        IMechtatelForSkyboxTextureCreator {
    private final Logger logger = LoggerFactory.getLogger(Mechtatel.class);

    private MttInstance instance;

    public Mechtatel(MttSettings settings) {
        logger.info(settings.toString());

        try {
            instance = new MttInstance(this, settings);
            logger.info("MttInstance successfully created");

            if (settings.systemSettings.gatewayServer.enabled) {
                var gatewayServer = new GatewayServer(this, settings.systemSettings.gatewayServer.port);
                gatewayServer.start();
                logger.info("Start gateway server");
            }

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

    @Override
    public void preDraw(String screenName) {

    }

    @Override
    public void postDraw(String screenName) {

    }

    @Override
    public void preTextureOperation(String operationName) {

    }

    @Override
    public void postTextureOperation(String operationName) {

    }

    @Override
    public void preDeferredDraw(String screenName) {

    }

    @Override
    public void postDeferredDraw(String screenName) {

    }

    @Override
    public void prePresent() {

    }

    @Override
    public void postPresent() {

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

    public int getFPS() {
        return instance.getFPS();
    }

    public float getSecondsPerFrame() {
        return instance.getSecondsPerFrame();
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

    public void setCursorPos(int x, int y) {
        instance.setCursorPos(x, y);
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

    public void setCursorMode(String cursorMode) {
        instance.setCursorMode(cursorMode);
    }

    public void sortComponents() {
        instance.sortComponents();
    }

    public MttModel createModel(String screenName, String modelFilepath) throws IOException {
        return instance.createModel(screenName, modelFilepath);
    }

    public MttModel duplicateModel(MttModel srcModel) {
        return instance.duplicateModel(srcModel);
    }

    public MttLine createLine(MttVertex3D v1, MttVertex3D v2) {
        return instance.createLine(v1, v2);
    }

    public MttLine createLine(Vector3fc p1, Vector4fc color1, Vector3fc p2, Vector4fc color2) {
        var v1 = new MttVertex3D(p1, color1);
        var v2 = new MttVertex3D(p2, color2);
        return instance.createLine(v1, v2);
    }

    public MttLine createLine(Vector3fc p1, Vector3fc p2, Vector4fc color) {
        var v1 = new MttVertex3D(p1, color);
        var v2 = new MttVertex3D(p2, color);
        return instance.createLine(v1, v2);
    }

    public MttLineSet createLineSet() {
        return instance.createLineSet();
    }

    public MttLineSet createAxesLineSet(float length) {
        MttLineSet axes = instance.createLineSet();

        axes.add(new Vector3f(-length, 0.0f, 0.0f), new Vector3f(length, 0.0f, 0.0f), new Vector4f(1.0f, 0.0f, 0.0f, 1.0f));
        axes.add(new Vector3f(0.0f, -length, 0.0f), new Vector3f(0.0f, length, 0.0f), new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
        axes.add(new Vector3f(0.0f, 0.0f, -length), new Vector3f(0.0f, 0.0f, length), new Vector4f(0.0f, 0.0f, 1.0f, 1.0f));
        axes.createBuffer();

        return axes;
    }

    public MttLineSet createPositiveAxesLineSet(float length) {
        MttLineSet axes = instance.createLineSet();

        axes.add(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(length, 0.0f, 0.0f), new Vector4f(1.0f, 0.0f, 0.0f, 1.0f));
        axes.add(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, length, 0.0f), new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
        axes.add(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, length), new Vector4f(0.0f, 0.0f, 1.0f, 1.0f));
        axes.createBuffer();

        return axes;
    }

    public MttSphere createSphere(Vector3fc center, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        return instance.createSphere(center, radius, numVDivs, numHDivs, color);
    }

    public MttCapsule createCapsule(Vector3fc center, float length, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        return instance.createCapsule(center, length, radius, numVDivs, numHDivs, color);
    }

    public MttLine2D createLine2D(MttVertex2D p1, MttVertex2D p2, float z) {
        return instance.createLine2D(p1, p2, z);
    }

    public MttLine2DSet createLine2DSet() {
        return instance.createLine2DSet();
    }

    public MttQuad createQuad(MttVertex3D v1, MttVertex3D v2, MttVertex3D v3, MttVertex3D v4, boolean fill) {
        return instance.createQuad(v1, v2, v3, v4, fill);
    }

    public MttQuad createQuad(Vector3fc p1, Vector3fc p2, Vector3fc p3, Vector3fc p4, boolean fill, Vector4fc color) {
        var v1 = new MttVertex3D(p1, color);
        var v2 = new MttVertex3D(p2, color);
        var v3 = new MttVertex3D(p3, color);
        var v4 = new MttVertex3D(p4, color);

        return instance.createQuad(v1, v2, v3, v4, fill);
    }

    public MttQuad2D createQuad2D(MttVertex2D p1, MttVertex2D p2, MttVertex2D p3, MttVertex2D p4, float z, boolean fill) {
        return instance.createQuad2D(p1, p2, p3, p4, z, fill);
    }

    public MttQuad2D createQuad2D(
            Vector2fc p1, Vector2fc p2, Vector2fc p3, Vector2fc p4,
            float z, boolean fill, Vector4fc color) {
        return instance.createQuad2D(p1, p2, p3, p4, z, fill, color);
    }

    public MttQuad2D createQuad2D(Vector2fc topLeft, Vector2fc bottomRight, float z, boolean fill, Vector4fc color) {
        return instance.createQuad2D(topLeft, bottomRight, z, fill, color);
    }

    public MttTexturedQuad createTexturedQuad(
            String screenName,
            String textureFilepath,
            boolean generateMipmaps,
            MttVertex3DUV v1,
            MttVertex3DUV v2,
            MttVertex3DUV v3,
            MttVertex3DUV v4) {
        return instance.createTexturedQuad(screenName, textureFilepath, generateMipmaps, v1, v2, v3, v4);
    }

    public MttTexturedQuad createTexturedQuad(
            String screenName,
            MttTexture texture,
            MttVertex3DUV v1,
            MttVertex3DUV v2,
            MttVertex3DUV v3,
            MttVertex3DUV v4) {
        return instance.createTexturedQuad(screenName, texture, v1, v2, v3, v4);
    }

    public MttTexturedQuad duplicateTexturedQuad(
            MttTexturedQuad srcQuad,
            MttVertex3DUV v1,
            MttVertex3DUV v2,
            MttVertex3DUV v3,
            MttVertex3DUV v4) {
        return instance.duplicateTexturedQuad(srcQuad, v1, v2, v3, v4);
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            String screenName,
            String textureFilepath,
            MttVertex2DUV p1,
            MttVertex2DUV p2,
            MttVertex2DUV p3,
            MttVertex2DUV p4,
            float z) {
        return instance.createTexturedQuad2D(screenName, textureFilepath, p1, p2, p3, p4, z);
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            String screenName, MttTexture texture, MttVertex2DUV p1, MttVertex2DUV p2, MttVertex2DUV p3, MttVertex2DUV p4, float z) {
        return instance.createTexturedQuad2D(screenName, texture, p1, p2, p3, p4, z);
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            String screenName, String textureFilepath, Vector2fc topLeft, Vector2fc bottomRight, float z) {
        var p1 = new MttVertex2DUV(topLeft, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 0.0f));
        var p2 = new MttVertex2DUV(new Vector2f(topLeft.x(), bottomRight.y()), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 1.0f));
        var p3 = new MttVertex2DUV(bottomRight, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 1.0f));
        var p4 = new MttVertex2DUV(new Vector2f(bottomRight.x(), topLeft.y()), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 0.0f));

        return instance.createTexturedQuad2D(screenName, textureFilepath, p1, p2, p3, p4, z);
    }

    public MttTexturedQuad2D createTexturedQuad2D(
            String screenName, MttTexture texture, Vector2fc topLeft, Vector2fc bottomRight, float z) {
        var p1 = new MttVertex2DUV(topLeft, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 0.0f));
        var p2 = new MttVertex2DUV(new Vector2f(topLeft.x(), bottomRight.y()), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 1.0f));
        var p3 = new MttVertex2DUV(bottomRight, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 1.0f));
        var p4 = new MttVertex2DUV(new Vector2f(bottomRight.x(), topLeft.y()), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 0.0f));

        return instance.createTexturedQuad2D(screenName, texture, p1, p2, p3, p4, z);
    }

    public MttTexturedQuad2D duplicateTexturedQuad2D(
            MttTexturedQuad2D srcQuad, MttVertex2DUV p1, MttVertex2DUV p2, MttVertex2DUV p3, MttVertex2DUV p4, float z) {
        return instance.duplicateTexturedQuad2D(srcQuad, p1, p2, p3, p4, z);
    }

    public MttTexturedQuad2D duplicateTexturedQuad2D(
            MttTexturedQuad2D srcQuad, Vector2fc topLeft, Vector2fc bottomRight, float z) {
        var p1 = new MttVertex2DUV(topLeft, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 0.0f));
        var p2 = new MttVertex2DUV(new Vector2f(topLeft.x(), bottomRight.y()), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 1.0f));
        var p3 = new MttVertex2DUV(bottomRight, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 1.0f));
        var p4 = new MttVertex2DUV(new Vector2f(bottomRight.x(), topLeft.y()), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 0.0f));
        return instance.duplicateTexturedQuad2D(srcQuad, p1, p2, p3, p4, z);
    }

    public MttTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(String screenName, String textureFilepath) {
        return instance.createTexturedQuad2DSingleTextureSet(screenName, textureFilepath);
    }

    public MttTexturedQuad2DSingleTextureSet createTexturedQuad2DSingleTextureSet(String screenName, MttTexture texture) {
        return instance.createTexturedQuad2DSingleTextureSet(screenName, texture);
    }

    public MttBox createBox(float xHalfExtent, float yHalfExtent, float zHalfExtent, Vector4fc color) {
        return instance.createBox(xHalfExtent, yHalfExtent, zHalfExtent, color);
    }

    public MttBox createBox(float halfExtent, Vector4fc color) {
        return instance.createBox(halfExtent, color);
    }

    public MttFont createFont(String screenName, Font font, boolean antiAlias, Color fontColor, String requiredChars) {
        return instance.createFont(screenName, font, antiAlias, fontColor, requiredChars);
    }

    public MttButton createButton(
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
        return instance.createButton(x, y, width, height, text, fontName, fontStyle, fontSize, fontColor, frameColor);
    }

    public MttCheckbox createCheckbox(
            float x,
            float y,
            float width,
            float height,
            float boxX,
            float boxY,
            float boxWidth,
            float boxHeight,
            float textX,
            float textY,
            String text,
            String fontName,
            int fontStyle,
            int fontSize,
            Color fontColor,
            Color checkboxColor) {
        return instance.createCheckbox(
                x, y, width, height,
                boxX, boxY, boxWidth, boxHeight, textX, textY,
                text, fontName, fontStyle, fontSize, fontColor, checkboxColor);
    }

    public MttVerticalScrollbar createVerticalScrollbar(
            float x,
            float y,
            float width,
            float height,
            float grabHeight,
            Color frameColor,
            Color grabFrameColor) {
        return instance.createVerticalScrollbar(x, y, width, height, grabHeight, frameColor, grabFrameColor);
    }

    public MttHorizontalScrollbar createHorizontalScrollbar(
            float x,
            float y,
            float width,
            float height,
            float grabWidth,
            Color frameColor,
            Color grabFrameColor) {
        return instance.createHorizontalScrollbar(x, y, width, height, grabWidth, frameColor, grabFrameColor);
    }

    public MttListbox createListbox(
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
        return instance.createListbox(
                x, y, width, height,
                scrollbarWidth, scrollbarGrabHeight, scrollbarFrameColor, scrollbarGrabColor,
                nonSelectedFontName, nonSelectedFontStyle, nonSelectedFontSize, nonSelectedFontColor,
                frameColor, itemTexts, itemHeight,
                selectedFontName, selectedFontStyle, selectedFontSize, selectedFontColor);
    }

    public MttLabel createLabel(
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
        return instance.createLabel(
                x, y, width, height, requiredChars, fontName, fontStyle, fontSize, fontColor, frameColor);
    }

    public MttTextbox createTextbox(
            float x,
            float y,
            float width,
            float height,
            float caretMarginX,
            float caretMarginY,
            String fontName,
            int fontStyle,
            int fontSize,
            Color fontColor,
            Color frameColor,
            Color caretColor,
            float caretBlinkInterval,
            float repeatDelay,
            KeyInterpreter keyInterpreter,
            String supportedCharacters) {
        return instance.createTextbox(
                x, y, width, height, caretMarginX, caretMarginY,
                fontName, fontStyle, fontSize, fontColor, frameColor, caretColor,
                caretBlinkInterval, repeatDelay, keyInterpreter, supportedCharacters);
    }

    public MttTextarea createTextarea(
            float x,
            float y,
            float width,
            float height,
            float caretLength,
            float caretMarginX,
            float caretMarginY,
            String fontName,
            int fontStyle,
            int fontSize,
            Color fontColor,
            Color frameColor,
            Color caretColor,
            float caretBlinkInterval,
            float repeatDelay,
            KeyInterpreter keyInterpreter,
            String supportedCharacters) {
        return instance.createTextarea(
                x, y, width, height, caretLength, caretMarginX, caretMarginY,
                fontName, fontStyle, fontSize, fontColor, frameColor, caretColor,
                caretBlinkInterval, repeatDelay, keyInterpreter, supportedCharacters);
    }

    public PhysicalPlane createPhysicalPlane(Vector3fc normal, float constant) {
        return instance.createPhysicalPlane(normal, constant);
    }

    public PhysicalSphere createPhysicalSphere(float radius, float mass) {
        return instance.createPhysicalSphere(radius, mass);
    }

    public PhysicalCapsule createPhysicalCapsule(float radius, float height, float mass) {
        return instance.createPhysicalCapsule(radius, height, mass);
    }

    public PhysicalBox createPhysicalBox(float xHalfExtent, float yHalfExtent, float zHalfExtent, float mass) {
        return instance.createPhysicalBox(xHalfExtent, yHalfExtent, zHalfExtent, mass);
    }

    public PhysicalBox createPhysicalBox(float halfExtent, float mass) {
        return instance.createPhysicalBox(halfExtent, mass);
    }

    public PhysicalMesh createPhysicalMesh(MttModel model, float mass) {
        return instance.createPhysicalMesh(model, mass);
    }

    public boolean removePhysicalObject(PhysicalObject physicalObject) {
        return instance.removePhysicalObject(physicalObject);
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

    @Override
    public MttTexture createTexture(String screenName, String textureFilepath, boolean generateMipmaps) {
        return instance.createTexture(screenName, textureFilepath, generateMipmaps);
    }

    public MttTexture texturizeColorOfScreen(String srcScreenName, String dstScreenName) {
        return instance.texturizeColorOfScreen(srcScreenName, dstScreenName);
    }

    public MttTexture texturizeDepthOfScreen(String srcScreenName, String dstScreenName) {
        return instance.texturizeDepthOfScreen(srcScreenName, dstScreenName);
    }

    public void saveScreenshot(String screenName, String srcImageFormat, String outputFilepath) throws IOException {
        instance.saveScreenshot(screenName, srcImageFormat, outputFilepath);
    }

    @Override
    public MttScreen createScreen(
            String screenName,
            int depthImageWidth,
            int depthImageHeight,
            int screenWidth,
            int screenHeight,
            String samplerFilter,
            String samplerMipmapMode,
            String samplerAddressMode,
            boolean shouldChangeExtentOnRecreate,
            boolean useShadowMapping,
            Map<String, FlexibleNaborInfo> flexibleNaborInfos,
            List<String> ppNaborNames) {
        return instance.createScreen(
                screenName,
                depthImageWidth,
                depthImageHeight,
                screenWidth,
                screenHeight,
                samplerFilter,
                samplerMipmapMode,
                samplerAddressMode,
                shouldChangeExtentOnRecreate,
                useShadowMapping,
                flexibleNaborInfos,
                ppNaborNames
        );
    }

    public boolean removeScreen(String screenName) {
        return instance.removeScreen(screenName);
    }

    @Override
    public void setScreenDrawOrder(List<String> screenDrawOrder) {
        instance.setScreenDrawOrder(screenDrawOrder);
    }

    public MttScreen getScreen(String screenName) {
        return instance.getScreen(screenName);
    }

    public Map<String, MttScreen> getScreens() {
        return instance.getScreens();
    }

    public MttTexture createTextureOperation(
            String operationName,
            MttTexture firstColorTexture,
            MttTexture secondColorTexture,
            MttTexture firstDepthTexture,
            MttTexture secondDepthTexture,
            String dstScreenName,
            TextureOperationParameters parameters) {
        return instance.createTextureOperation(
                operationName,
                firstColorTexture,
                secondColorTexture,
                firstDepthTexture,
                secondDepthTexture,
                dstScreenName,
                parameters
        );
    }

    @Override
    public void setTextureOperationOrder(List<String> textureOperationOrder) {
        instance.setTextureOperationOrder(textureOperationOrder);
    }

    @Override
    public void setDeferredScreenDrawOrder(List<String> deferredScreenDrawOrder) {
        instance.setDeferredScreenDrawOrder(deferredScreenDrawOrder);
    }

    @Override
    public void setPresentScreenName(String presentScreenName) {
        instance.setPresentScreenName(presentScreenName);
    }

    public MttAnimation createAnimation(String tag, String screenName, AnimationInfo animationInfo) throws IOException {
        return instance.createAnimation(tag, screenName, animationInfo);
    }

    public MttAnimation createAnimation(String tag, AnimationInfo animationInfo, Map<String, MttModel> srcModels) {
        return instance.createAnimation(tag, animationInfo, srcModels);
    }
}
