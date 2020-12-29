package com.fx.basesdktest.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.fx.basesdktest.R;
import com.fx.device.DeviceDAQConfig;
import com.fx.device.DropMenuPop;
import com.fx.device.listener.ICameraListener;
import com.fx.device.utils.CameraPrinterLog;
import com.fx.device.utils.UvcCameraUtil;
import com.serenegiant.helper.BitmapHelper;
import com.serenegiant.helper.UvcCameraHelper;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.widget.CameraViewInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 高拍仪示例
 */
public class HighCameraActivity extends Activity implements View.OnClickListener, CameraDialog.CameraDialogParent {

    private static final String TAG = "HighCameraActivity";

    private CameraViewInterface mCameraView;

    private Button mBtnSelect;
    private Button btnUpdate;
    private ImageView mIvFirst, mIvSecond;

    private Bitmap bitmapL, bitmapR;
    private EditText mEtResulution;
    private SeekBar mSbBrightness, mSbContrast, mSbGamma, mSbTemp, mSbSaturation, mSbHue, mSbSharpness, mSbBacklight;

    private int rotateInt = 0;

    private DropMenuPop menuPop;

    private int OPEM_CAMERA_PID = 26249;  //高拍仪摄像头Pid

    String mFaceModelDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.jpg";
    String mFaceModelDir1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test1.jpg";

    private UvcCameraUtil mUvcCameraUtil;
    private UvcCameraHelper mUvcCameraHelper;

    private final int MSG_GET_PICTURE = 1;
    private final int MSG_GET_PICTURE_SECOND = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_camera_uvc);
        initView();
        initCamera();
    }

    private void initCamera() {
        UvcCameraUtil.init(HighCameraActivity.this);
        mUvcCameraUtil = UvcCameraUtil.getInstance();
        mUvcCameraUtil.setCameraListener(iCameraListener);
        mUvcCameraUtil.initCameraHelper(HighCameraActivity.this, HighCameraActivity.this, mCameraView, 1, 1,
                DeviceDAQConfig.DEVICE_HIGH_CAMERA, "");
        mUvcCameraHelper = mUvcCameraUtil.getUvcCameraHelper();
    }

    private void initView() {
        mCameraView = (CameraViewInterface) findViewById(R.id.texture);
        mIvFirst = (ImageView) findViewById(R.id.first_iv);
        mIvSecond = (ImageView) findViewById(R.id.second_iv);

        mBtnSelect = (Button) findViewById(R.id.btn_device);
        Button btnFirst = (Button) findViewById(R.id.btn_getPicture);
        Button btnSecond = (Button) findViewById(R.id.btn_getPicture_second);
        Button btnRotate = (Button) findViewById(R.id.btn_rotate);
        Button btnSace = (Button) findViewById(R.id.btn_save);
        Button btnMerge = (Button) findViewById(R.id.btn_merge);
        Button btnFind = (Button) findViewById(R.id.btn_find);
        btnUpdate = (Button) findViewById(R.id.btn_update);
        Button btnPid = (Button) findViewById(R.id.btn_device_pid);
        Button btnFragment = (Button) findViewById(R.id.btn_fragment);
        mEtResulution = (EditText) findViewById(R.id.pic_resulution);
        btnFragment.setOnClickListener(this);
        btnPid.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
        btnSace.setOnClickListener(this);
        btnMerge.setOnClickListener(this);
        mBtnSelect.setOnClickListener(this);
        btnFirst.setOnClickListener(this);
        btnSecond.setOnClickListener(this);
        btnRotate.setOnClickListener(this);
        btnFind.setOnClickListener(this);

        initPopupwindow();

        initSb();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUvcCameraUtil.workOnStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        if (mUvcCameraHelper != null && !mUvcCameraHelper.getUvcCameraHandler().isOpened()) {
            Log.d(TAG, "onResume: camera");
            mUvcCameraHelper.getUsbMonitor().register();
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        mUvcCameraUtil.workOnStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        mUvcCameraUtil.workOnDestroy(DeviceDAQConfig.DEVICE_HIGH_CAMERA);
    }

    private void initSb() {
        mSbBrightness = (SeekBar) findViewById(R.id.seekbar_brightness);
        mSbBrightness.setOnSeekBarChangeListener(new MySeekBarChangeListener(UVCCamera.PU_BRIGHTNESS));

        mSbContrast = (SeekBar) findViewById(R.id.seekbar_contrast);
        mSbContrast.setOnSeekBarChangeListener(new MySeekBarChangeListener(UVCCamera.PU_CONTRAST));

        mSbGamma = (SeekBar) findViewById(R.id.seekbar_gamma);
        mSbGamma.setOnSeekBarChangeListener(new MySeekBarChangeListener(UVCCamera.PU_GAMMA));

        mSbTemp = (SeekBar) findViewById(R.id.seekbar_wb_temp);
        mSbTemp.setOnSeekBarChangeListener(new MySeekBarChangeListener(UVCCamera.PU_WB_TEMP));

        mSbSaturation = (SeekBar) findViewById(R.id.seekbar_satura);
        mSbSaturation.setOnSeekBarChangeListener(new MySeekBarChangeListener(UVCCamera.PU_SATURATION));

        mSbHue = (SeekBar) findViewById(R.id.seekbar_hue);
        mSbHue.setOnSeekBarChangeListener(new MySeekBarChangeListener(UVCCamera.PU_HUE));

        mSbSharpness = (SeekBar) findViewById(R.id.seekbar_sharpness);
        mSbSharpness.setOnSeekBarChangeListener(new MySeekBarChangeListener(UVCCamera.PU_SHARPNESS));

        mSbBacklight = (SeekBar) findViewById(R.id.seekbar_backlight);
        mSbBacklight.setOnSeekBarChangeListener(new MySeekBarChangeListener(UVCCamera.PU_BACKLIGHT));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_device: {
                mUvcCameraUtil.showOrDismissCameraList();
            }
            break;

            case R.id.btn_getPicture: {
                bitmapL = mUvcCameraUtil.takePhoto();
                if (TextUtils.isEmpty(mEtResulution.getText().toString())) {
                    bitmapL = mUvcCameraUtil.takePhoto();
                } else {
                    String width = mEtResulution.getText().toString().trim().split("x")[0];
                    String height = mEtResulution.getText().toString().trim().split("x")[1];

                    if (!TextUtils.isEmpty(width) && !TextUtils.isEmpty(height)) {
                        bitmapL = mUvcCameraUtil.takePhoto();
                    }
                }

                if (bitmapL != null) {
                    sendMessage(MSG_GET_PICTURE);
                }
            }
            break;

            case R.id.btn_getPicture_second: {
                bitmapR = mUvcCameraUtil.takePhoto();
//                if(TextUtils.isEmpty(mEtResulution.getText().toString())){
//                    bitmapR = mUvcCameraHelper.takePhoto();
//                }else{
//                    String width = mEtResulution.getText().toString().trim().split("x")[0];
//                    String height = mEtResulution.getText().toString().trim().split("x")[1];
//
//                    if(!TextUtils.isEmpty(width) && !TextUtils.isEmpty(height)){
//                        bitmapR = mUvcCameraHelper.takePhoto();
//                    }
//                }
                if (bitmapR != null) {
                    sendMessage(MSG_GET_PICTURE_SECOND);
                }
            }
            break;

            case R.id.btn_rotate: {
                int a = rotateInt % 4;
                switch (a) {
                    case 0: {
                        mUvcCameraUtil.setRotate(90);
                    }
                    break;

                    case 1: {
                        mUvcCameraUtil.setRotate(180);
                    }
                    break;

                    case 2: {
                        mUvcCameraUtil.setRotate(270);
                    }
                    break;

                    case 3: {
                        mUvcCameraUtil.setRotate(0);
                    }
                    break;
                }
                rotateInt++;
            }
            break;

            case R.id.btn_save: {
                if (bitmapL != null) {
                    BitmapHelper.saveBmpToFile(bitmapL, mFaceModelDir, Bitmap.CompressFormat.JPEG, 100);
                    //bitmapL = ThumbnailUtils.extractThumbnail(bitmapL, 640, 480);
                    BitmapHelper.saveBmpToFile(bitmapR, mFaceModelDir1, Bitmap.CompressFormat.JPEG, 100);
                    //saveBmpToFile(bitmapR, mFaceModelDir1, Bitmap.CompressFormat.JPEG);
                    Toast.makeText(HighCameraActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(HighCameraActivity.this, "请先采集照片", Toast.LENGTH_SHORT).show();
                }


            }
            break;

            case R.id.btn_merge: {

                if (bitmapL == null || bitmapR == null) {
                    return;
                }

                bitmapR = BitmapHelper.mergeBitmap_TB(bitmapL, bitmapR, false);
                sendMessage(MSG_GET_PICTURE_SECOND);
            }
            break;

            case R.id.btn_find: {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = mUvcCameraUtil.takePhoto();

                        bitmapL = BitmapHelper.autoCutBitmap(bitmap);

                        sendMessage(MSG_GET_PICTURE);
                    }
                }).start();


            }
            break;

            case R.id.btn_update: {
                List<String> strList = mUvcCameraUtil.getSupportPreviewSizesStr();
                if (strList != null && strList.size() > 0) {
                    menuPop.updateList(strList);
                    menuPop.showAsDropDown(mIvFirst);
                }
            }
            break;

            case R.id.btn_device_pid: {
                try {
                    mUvcCameraUtil.openCamera(OPEM_CAMERA_PID, DeviceDAQConfig.DEVICE_HIGH_CAMERA);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;

            case R.id.btn_fragment: {
                if (mUvcCameraHelper != null && mUvcCameraHelper.getUvcCameraHandler().isPreviewing()) {
                    mUvcCameraHelper.getUvcCameraHandler().close();
                }
                Intent intent = new Intent(HighCameraActivity.this, UvcActivity.class);
                startActivity(intent);
            }
            break;

        }
    }

    @Override
    public USBMonitor getUSBMonitor() {
        return mUvcCameraHelper.getUsbMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {

    }

    private void sendMessage(int type) {
        Message message = new Message();
        message.what = type;
        mHandler.sendMessage(message);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_GET_PICTURE: {
                    mIvFirst.setImageBitmap(bitmapL);
                }
                break;

                case MSG_GET_PICTURE_SECOND: {
                    mIvSecond.setImageBitmap(bitmapR);
                }
                break;
            }
        }
    };

    private void initPopupwindow() {
        menuPop = new DropMenuPop(this);
        menuPop.setmListener(new DropMenuPop.onInfoItemSelectedListener() {
            @Override
            public void onItemClick(String str) {
                String[] datas = str.split("x");
                if (datas.length == 2) {
                    Log.d(TAG, "onItemClick: width" + datas[0] + "x" + datas[1]);
                    mUvcCameraUtil.updateResolution(Integer.parseInt(datas[0]), Integer.parseInt(datas[1]));
                }
            }
        });
    }

    private class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        private int type;

        public MySeekBarChangeListener(int type) {
            this.type = type;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mUvcCameraHelper != null && mUvcCameraHelper.getUvcCameraHandler().isOpened()) {
                mUvcCameraUtil.setCameraModeValue(type, progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    public static boolean saveBmpToFile(Bitmap bmp, String path, Bitmap.CompressFormat format) {
        if (bmp == null || bmp.isRecycled())
            return false;

        OutputStream stream = null;
        try {
            File file = new File(path);
            File filePath = file.getParentFile();
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            stream = new FileOutputStream(path);
            return bmp.compress(format, 100, stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (null != stream) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }


    private ICameraListener iCameraListener = new ICameraListener() {
        @Override
        public void onOpenCamera(String device, boolean isOpen, int code, String msg) {
            Log.d(TAG, "device: " + device + "isOpen: " + isOpen + "code: " + code + ", msg: " + msg);
            if (CameraPrinterLog.getInstance() != null)
                CameraPrinterLog.getInstance().collectDeviceLog(device, msg);
        }

        @Override
        public void onConnected(final String device, final boolean isConnected, int code, String msg) {
            Log.d(TAG, "device: " + device + "isConnected: " + isConnected + ", code: " + code + ", msg: " + msg);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isConnected) {
                        showToast(device + "认证成功");
                    } else {
                        showToast(device + "认证失败");
                    }
                }
            });

            if (CameraPrinterLog.getInstance() != null)
                CameraPrinterLog.getInstance().collectDeviceLog(device, msg);

            /*new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (mUvcCameraHelper != null && mUvcCameraHelper.getUvcCameraHandler() != null && mUvcCameraHelper.getUvcCameraHandler().isOpened()) {

                        mUvcCameraHelper.resetCameraModeValue(UVCCamera.PU_BRIGHTNESS);
                        mUvcCameraHelper.resetCameraModeValue(UVCCamera.PU_CONTRAST);
                        mUvcCameraHelper.resetCameraModeValue(UVCCamera.PU_GAMMA);
                        mUvcCameraHelper.resetCameraModeValue(UVCCamera.PU_WB_TEMP);
                        mUvcCameraHelper.resetCameraModeValue(UVCCamera.PU_SATURATION);
                        mUvcCameraHelper.resetCameraModeValue(UVCCamera.PU_HUE);
                        mUvcCameraHelper.resetCameraModeValue(UVCCamera.PU_SHARPNESS);
                        mUvcCameraHelper.resetCameraModeValue(UVCCamera.PU_BACKLIGHT);

                        mSbBrightness.setProgress(mUvcCameraHelper.getCameraModeValue(UVCCamera.PU_BRIGHTNESS));
                        mSbContrast.setProgress(mUvcCameraHelper.getCameraModeValue(UVCCamera.PU_CONTRAST));
                        mSbGamma.setProgress(mUvcCameraHelper.getCameraModeValue(UVCCamera.PU_GAMMA));
                        mSbTemp.setProgress(mUvcCameraHelper.getCameraModeValue(UVCCamera.PU_WB_TEMP));
                        mSbSaturation.setProgress(mUvcCameraHelper.getCameraModeValue(UVCCamera.PU_SATURATION));
                        mSbHue.setProgress(mUvcCameraHelper.getCameraModeValue(UVCCamera.PU_HUE));
                        mSbSharpness.setProgress(mUvcCameraHelper.getCameraModeValue(UVCCamera.PU_SHARPNESS));
                        mSbBacklight.setProgress(mUvcCameraHelper.getCameraModeValue(UVCCamera.PU_BACKLIGHT));
                    }
                }
            }).start();*/
        }

        @Override
        public void onAttached(UsbDevice usbDevice) {
            Log.d(TAG, "onAttached: " + usbDevice.getProductId());
            if (usbDevice.getProductId() == OPEM_CAMERA_PID) {
                try {
                    mUvcCameraUtil.openCamera(OPEM_CAMERA_PID, DeviceDAQConfig.DEVICE_HIGH_CAMERA);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

}
