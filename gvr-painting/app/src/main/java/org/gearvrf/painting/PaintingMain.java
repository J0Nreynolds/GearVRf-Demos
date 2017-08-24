/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.painting;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRTexture;
import org.gearvrf.ISensorEvents;
import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRGUISceneObject;
import org.gearvrf.scene_objects.GVRGearControllerSceneObject;
import org.gearvrf.scene_objects.view.GVRFrameLayout;


public class PaintingMain extends GVRMain {
    private static final String TAG = PaintingMain.class.getSimpleName();

    private PaintableSurface paintingSceneObject;
    private GVRContext context;
    private GVRFrameLayout frameLayout;
    private GVRActivity activity;

    private static final float DEPTH = -1.5f;


    private GVRScene mainScene;
    private GVRSceneObject cursor, gui;

    public PaintingMain(PaintingActivity activity, GVRFrameLayout frameLayout) {
        this.frameLayout = frameLayout;
        this.activity = activity;
    }

    @Override
    public void onInit(final GVRContext gvrContext) throws Throwable {
        context = gvrContext;
        mainScene = context.getMainScene();

        gui = new GVRGUISceneObject(context, frameLayout, 3f, 45f);
        gui.getTransform().setPosition(-2,0,-3);
        gui.getTransform().setRotationByAxis(45, 0, 1, 0);
        mainScene.addSceneObject(gui);

        paintingSceneObject = new PaintableSurface(context);
        paintingSceneObject.getTransform().setPosition(2,0,-3);
        paintingSceneObject.getTransform().setRotationByAxis(-45, 0, 1, 0);
        mainScene.addSceneObject(paintingSceneObject);

        // set up the input manager for the main scene
        GVRInputManager inputManager = gvrContext.getInputManager();
        inputManager.addCursorControllerListener(cursorControllerListener);
        for (GVRCursorController cursor : inputManager.getCursorControllers()) {
            cursorControllerListener.onCursorControllerAdded(cursor);
        }
    }

    private CursorControllerListener cursorControllerListener = new CursorControllerListener() {
        @Override
        public void onCursorControllerAdded(GVRCursorController gvrCursorController) {
            if (gvrCursorController.getControllerType() == GVRControllerType.CONTROLLER) {
                android.util.Log.d(TAG, "Got the orientation remote controller");

                GVRGearControllerSceneObject controller = new GVRGearControllerSceneObject(context);
                GVRTexture cursorTexture = context.getAssetLoader().loadTexture(new GVRAndroidResource(context, R.raw.cursor));
                GVRSceneObject cursor = new GVRSceneObject(context,
                        context.createQuad(0.25f, 0.25f),
                        cursorTexture);
                cursor.getRenderData().setDepthTest(false);
                cursor.getRenderData().setRenderingOrder(10000);
                cursor.setName("Cursor");
                controller.setRayDepth(2.5f);
                controller.setCursor(cursor);
                controller.setName("GEARController");
                //controller.disableRay();
                controller.setCursorController(gvrCursorController);
                controller.enableSurfaceProjection();
                //controller.enableFastCursorProjection();
                ISensorEvents projectionListener = controller.getProjectionListener();

                paintingSceneObject.getEventReceiver().addListener(projectionListener);

                gvrCursorController.setNearDepth(DEPTH);
                gvrCursorController.setFarDepth(DEPTH);
            }
            else if (gvrCursorController.getControllerType() == GVRControllerType.GAZE) {
                cursor = new GVRSceneObject(context,
                        context.createQuad(0.1f, 0.1f),
                        context.getAssetLoader().loadTexture(new GVRAndroidResource(context, R.raw.cursor)));
                cursor.getTransform().setPosition(0.0f, 0.0f, DEPTH);
                mainScene.getMainCameraRig().addChildObject(cursor);
                cursor.getRenderData().setDepthTest(false);
                cursor.getRenderData().setRenderingOrder(100000);
                gvrCursorController.setPosition(0.0f, 0.0f, DEPTH);
                gvrCursorController.setNearDepth(DEPTH);
                gvrCursorController.setFarDepth(DEPTH);
            }
            else {
                //do nothing
            }
        }

        @Override
        public void onCursorControllerRemoved(GVRCursorController gvrCursorController) {
            if (gvrCursorController.getControllerType() == GVRControllerType.CONTROLLER) {
                android.util.Log.d(TAG, "Got the orientation remote controller");
            }
        }
    };


    void clearCanvas(){
        paintingSceneObject.clearCanvas();
    }


    void setBrushSize(int size){
        paintingSceneObject.setBrushSize(size);
    }

    void setColor(int red, int green, int blue){
        paintingSceneObject.setColor(red, green, blue);
    }

    @Override
    public void onStep() {
        // unused
    }
}
