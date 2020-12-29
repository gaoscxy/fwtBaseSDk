package com.fx.basesdktest.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;

import com.fx.basesdktest.R;
import com.fx.device.camera.CameraInterface;
import com.fx.device.camera.Constants;
import com.fx.device.utils.DeviceConfig;
import com.fx.device.utils.CameraPrinterLog;
import com.fx.device.widget.CameraPreview;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 使用Android Camera API打开摄像头
 */
public class OpenCameraActivity extends Activity {

    private final String TAG = OpenCameraActivity.class.getSimpleName();

    private CameraPreview mSurfaceView;

    private Context mContext;

    private ExecutorService executor = Executors.newCachedThreadPool();

    private String mCameraFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null)
            mCameraFlag = intent.getStringExtra("cameraFlag");
        setContentView(R.layout.activity_camera_open);
        mContext = this;
        mSurfaceView = (CameraPreview) findViewById(R.id.surface);
        initCamera();
    }

    private void initCamera() {
        if (CameraInterface.getInstance() == null)
            CameraInterface.init(getApplicationContext());
        CameraInterface.getInstance().setICameraListener(new CameraInterface.ICameraListener() {
            @Override
            public void onCameraInit(DeviceConfig.DeivceType deivceType, boolean status, String msg) {
                Log.e(TAG, "onCameraInit >>>> device: " + deivceType.getName() + ", msg: " + msg);
                CameraPrinterLog.getInstance().collectDeviceLog(deivceType.getName(), msg);
            }

            @Override
            public void onCameraOpen(DeviceConfig.DeivceType deivceType, boolean status, String msg) {
                Log.e(TAG, "onCameraOpen >>>> device: " + deivceType.getName() + ", msg: " + msg);
                CameraPrinterLog.getInstance().collectDeviceLog(deivceType.getName(), msg);
            }
        });
        CameraInterface.getInstance().setCameraOccupied(false);
        CameraInterface.getInstance().cameraInit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            CameraInterface.getInstance().openCamera(mCameraFlag, mPreviewFrameCallback);
            CameraInterface.getInstance().setParameters(Constants.CAMERA_PREVIEW_SIZE_1280x720);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Thread startPreview = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                    CameraInterface.getInstance().startPreview(mSurfaceView.getSurfaceHolder());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        startPreview.start();
    }

    @Override
    protected void onStop() {
        try {
            CameraInterface.getInstance().CloseCamera();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private CameraInterface.onPreviewFrame mPreviewFrameCallback = new CameraInterface.onPreviewFrame() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
        }
    };
}
