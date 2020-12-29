package com.fx.device.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

public class CameraPreview extends SurfaceView implements Callback {
    private static final String TAG = "CameraPreview";
    SurfaceHolder mSurfaceHolder = this.getHolder();

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSurfaceHolder.setFormat(-2);
        this.mSurfaceHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d("CameraPreview", "surfaceCreated: ");
    }

    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d("CameraPreview", "surfaceChanged: ");
    }

    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

    public SurfaceHolder getSurfaceHolder() {
        return this.mSurfaceHolder;
    }
}
