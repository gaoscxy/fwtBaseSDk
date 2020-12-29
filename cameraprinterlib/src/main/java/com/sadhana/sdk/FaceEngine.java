package com.sadhana.sdk;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

public class FaceEngine {

    private static final String TAG = "FaceEngine";
    private long mEngine = 0;

    static {
        Log.i(TAG, "Will load libSaFaceVerify.so");
        System.loadLibrary("SaFaceVerify");
        Log.i(TAG, "Loaded libSaFaceVerify.so");
    }

    public native int initialize(AssetManager assetManager, String modelName, int uvcFd);

    public native void release();

    public native Face[] detectFaces(byte[] bgr, int width, int height, int pitch, int count);

    public native Face[] detectFacesWithBitmap(Bitmap bmp, int count);

    public native float verify(byte[] bgr1, int width1, int height1, int pitch1,
                               byte[] bgr2, int width2, int height2, int pitch2);

    public native float verifyWithBitmap(Bitmap bmp1, Bitmap bmp2);
}
