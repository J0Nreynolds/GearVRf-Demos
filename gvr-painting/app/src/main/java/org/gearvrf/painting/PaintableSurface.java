package org.gearvrf.painting;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES30;
import android.view.MotionEvent;

import org.gearvrf.GVRBaseSensor;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTextureParameters;
import org.gearvrf.ISensorEvents;
import org.gearvrf.SensorEvent;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by j.reynolds on 8/9/2017.
 */

class PaintableSurface extends GVRSceneObject {
    private final static String TAG = PaintableSurface.class.getSimpleName();
    private final GVRBitmapTexture texture;
    private final byte[] pixels;
    private byte defaultR = (byte) 255;
    private byte defaultG = (byte) 255;
    private byte defaultB = (byte) 255;
    private byte defaultA = (byte) 255;
    private int drawR = 100;
    private int drawG = 100;
    private int drawB = 100;
    private int drawDiameter = 18;
    private final ByteBuffer buffer;
    private int size = 500;

    private boolean transparent = false;
    private GVRBitmapTexture tex;

    private static final int MAX_DRAW_DIAMETER = 50;

    public PaintableSurface(GVRContext gvrContext){
        super(gvrContext, GVRMesh.createCurvedMesh(gvrContext, 1,1, 60f, 3f));
        pixels = new byte[size*size*4];
        buffer = ByteBuffer.allocateDirect(size*size*4);
        for(int i = 0 ; i < size*size*4; i +=4){
            pixels[i] =   defaultR;
            pixels[i+1] = defaultG;
            pixels[i+2] = defaultB;
            pixels[i+3] = defaultA;
        }
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bmp.eraseColor(Color.argb(255,255,255,255)); //white canvas
        texture = new GVRBitmapTexture(gvrContext, bmp);
        GVRMaterial material = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Texture.ID);
        material.setMainTexture(texture);
        this.getRenderData().setMaterial(material);
        this.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        this.getRenderData().setAlphaToCoverage(true);
        this.attachCollider(new GVRMeshCollider(gvrContext, this.getRenderData().getMesh(), true));
        this.getEventReceiver().addListener(DrawListener);
        this.setSensor(new GVRBaseSensor(gvrContext));
    }

    private ISensorEvents DrawListener = new ISensorEvents() {
        @Override
        public void onSensorEvent(SensorEvent event) {
            List<MotionEvent> motionEvents = event.getCursorController().getMotionEvents();

            for (int cur = 0; cur < motionEvents.size(); cur++) {
                int x = (int) (size * event.getPickedObject().textureCoords[0]);
                int y = (int) (size * event.getPickedObject().textureCoords[1]);
                int dist = drawDiameter/2;
                for(int i = 0; i < drawDiameter; i++){
                    for(int j = 0; j < drawDiameter; j++) {
                        //Check if drawing outside of canvas bounds
                        if((y-drawDiameter/2+i) < 0 || (y-drawDiameter/2+i) >= size || (x-drawDiameter/2+j) < 0 || (x-drawDiameter/2+j) >= size){
                            continue;
                        }
                        //Otherwise calculate the index of the pixel to be drawn
                        int idx = size*(y-drawDiameter/2+i) + x-drawDiameter/2+j;
                        //Record changes in pixels array if pixel falls in brush circle
                        if (Math.sqrt(Math.pow(i - dist, 2) + Math.pow(j - dist, 2)) <= drawDiameter/2.0) {
                            pixels[4*idx    ] = (byte) drawR;
                            pixels[4*idx + 1] = (byte) drawG;
                            pixels[4*idx + 2] = (byte) drawB;
                            pixels[4*idx + 3] = (byte) 255;
                        }
                        //Send pixel records to the buffer
                        buffer.put(pixels[4*idx    ]);
                        buffer.put(pixels[4*idx + 1]);
                        buffer.put(pixels[4*idx + 2]);
                        buffer.put(pixels[4*idx + 3]);
                    }
                }
                texture.postBufferWithOffset( x - drawDiameter/2, y - drawDiameter/2, drawDiameter, drawDiameter, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buffer);
                buffer.position(0); //reset the buffer for the next draw
            }
        }
    };

    public void setBrushSize(int size){
        this.drawDiameter = size;
    }

    public void setColor(int red, int green, int blue){
        this.drawR = red;
        this.drawG = green;
        this.drawB = blue;
    }

    public void clearCanvas(){
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++) {
                int idx = size*i + j;
                pixels[4*idx    ] = defaultR;
                pixels[4*idx + 1] = defaultG;
                pixels[4*idx + 2] = defaultB;
                pixels[4*idx + 3] = (byte) 255;
                buffer.put(pixels[4*idx    ]);
                buffer.put(pixels[4*idx + 1]);
                buffer.put(pixels[4*idx + 2]);
                buffer.put(pixels[4*idx + 3]);
            }
        }
        texture.postBufferWithOffset(0, 0, size, size, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buffer);
        buffer.position(0);
    }

    private void makeTransparent(){
        //Make white pixels transparent
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++) {
                int idx = size*i + j;
                if(pixels[4*idx] == (byte) 255 && pixels[4*idx + 1] == (byte) 255 && pixels[4*idx + 2] == (byte) 255){
                    pixels[4*idx + 3] = (byte) 0;
                }
                buffer.put(pixels[4*idx    ]);
                buffer.put(pixels[4*idx + 1]);
                buffer.put(pixels[4*idx + 2]);
                buffer.put(pixels[4*idx + 3]);
            }
        }
        GVRTextureParameters params = new GVRTextureParameters(getGVRContext());
        params.setWidth(500);
        params.setHeight(500);
        params.setInternalFormat(GLES30.GL_RGBA);
        params.setFormat(GLES30.GL_RGBA);
        params.setType(GLES30.GL_UNSIGNED_BYTE);
        tex = new GVRBitmapTexture(getGVRContext(), params);
        tex.postBufferWithOffset(0, 0, size, size, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buffer);
        buffer.position(0);
        transparent = true;
    }

    public GVRSceneObject getTexturedPlane(){
        if(!transparent) makeTransparent();
        GVRSceneObject plane = new GVRSceneObject(getGVRContext(), getGVRContext().createQuad(2,2), tex);
        plane.getRenderData().getMaterial().setAmbientColor(0,0,0,1);
        plane.getRenderData().getMaterial().setSpecularColor(1f, 1f, 1f, 1);
        plane.getRenderData().getMaterial().setSpecularExponent(24);
        plane.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        plane.getRenderData().setAlphaToCoverage(true);
        return plane;
    }

}
