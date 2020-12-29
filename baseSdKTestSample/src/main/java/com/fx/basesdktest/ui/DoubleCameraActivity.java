package com.fx.basesdktest.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fx.basesdktest.R;
import com.fx.device.DeviceDAQConfig;
import com.fx.device.DropMenuPop;
import com.fx.device.listener.ICameraListener;
import com.fx.device.listener.IFaceListener;
import com.fx.device.utils.DeviceConfig;
import com.fx.device.utils.FaceUtil;
import com.fx.device.utils.CameraPrinterLog;
import com.fx.device.utils.UvcCameraUtil;
import com.serenegiant.helper.BitmapHelper;
import com.serenegiant.helper.UvcCameraHelper;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.widget.CameraViewInterface;

import java.util.List;
import java.util.Objects;

/**
 * 双目摄像头示例
 */
public class DoubleCameraActivity extends AppCompatActivity implements View.OnClickListener,
        CameraDialog.CameraDialogParent {

    private final String TAG = DoubleCameraActivity.class.getSimpleName();

    String mFaceModelDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.jpg";
    String mFaceModelDir1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test1.jpg";

    private ImageView mIvFirst, mIvSecond;
    private Button mBtnSelect, mBtnUpdate, mBtnFaceCompare;
    private EditText mEtResulution;
    private SeekBar mSbBrightness, mSbContrast, mSbGamma, mSbTemp, mSbSaturation, mSbHue, mSbSharpness, mSbBacklight;

    private CameraViewInterface mCameraView;

    private Bitmap mBitmap1, mBitmap2;

    private UvcCameraUtil mUvcCameraUtil;
    private UvcCameraHelper mUvcCameraHelper;

    private FaceUtil mFaceUtil;

    private int rotateInt = 0;

    private int OPEM_CAMERA_PID = 48342;    //双目彩色
//    private int OPEM_CAMERA_PID = 48343;    //双目黑白

    private final int PREVIEW_WIDth = 640;
    private final int PREVIEW_HEIGHT = 480;
    private final String IDCard = "382679199809235685";

    private int FACE_COMPARE_THRESHOLD = 80;    //相似度

    private final int MSG_GET_PICTURE = 1;
    private final int MSG_GET_PICTURE_SECOND = 2;

    private DropMenuPop menuPop;

    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_uvc);
        initView();
        initCameraFace();
    }

    private void initCameraFace() {
        UvcCameraUtil.init(DoubleCameraActivity.this);
        mUvcCameraUtil = UvcCameraUtil.getInstance();
        mUvcCameraUtil.setCameraListener(iCameraListener);
        mUvcCameraUtil.initDevice(mCameraView, PREVIEW_WIDth, PREVIEW_HEIGHT, DeviceDAQConfig.DEVICE_DOUBLE_CAMERA, IDCard);
        mUvcCameraHelper = mUvcCameraUtil.getUvcCameraHelper();

        FaceUtil.init(DoubleCameraActivity.this);
        mFaceUtil = FaceUtil.getInstance();
        mFaceUtil.setIFaceListener(iFaceListener);
        mFaceUtil.initFace(FACE_COMPARE_THRESHOLD, IDCard);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
        mBtnFaceCompare = (Button) findViewById(R.id.btn_face_compare);
        mBtnFaceCompare.setVisibility(View.VISIBLE);
        mBtnUpdate = (Button) findViewById(R.id.btn_update);
        Button btnPid = (Button) findViewById(R.id.btn_device_pid);
        Button btnFragment = (Button) findViewById(R.id.btn_fragment);
        mEtResulution = (EditText) findViewById(R.id.pic_resulution);

        btnFragment.setOnClickListener(this);
        btnPid.setOnClickListener(this);
        mBtnUpdate.setOnClickListener(this);
        btnSace.setOnClickListener(this);
        btnMerge.setOnClickListener(this);
        mBtnSelect.setOnClickListener(this);
        btnFirst.setOnClickListener(this);
        btnSecond.setOnClickListener(this);
        btnRotate.setOnClickListener(this);
        btnFind.setOnClickListener(this);
        mBtnFaceCompare.setOnClickListener(this);

        initSb();

        initDialog("人脸识别中...");
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

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        if (mUvcCameraUtil != null) mUvcCameraUtil.workOnStart();
        if (mCameraView != null) {
            mCameraView.onResume();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        if (mUvcCameraHelper != null && !mUvcCameraHelper.getUvcCameraHandler().isOpened()) {
            Log.d(TAG, "onResume: camera");
            mUvcCameraHelper.getUsbMonitor().register();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        if (mUvcCameraUtil != null) mUvcCameraUtil.workOnStop();
        if (mCameraView != null) {
            mCameraView.onPause();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (mUvcCameraUtil != null) mUvcCameraUtil.workOnDestroy(DeviceDAQConfig.DEVICE_DOUBLE_CAMERA);
        if (mFaceUtil != null) mFaceUtil.onDestory();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_device: {
                mUvcCameraUtil.showOrDismissCameraList();
            }
            break;

            case R.id.btn_getPicture: {
                mBitmap1 = mUvcCameraUtil.takePhoto();
                if (mBitmap1 != null) {
                    sendMessage(MSG_GET_PICTURE);
                }
            }
            break;

            case R.id.btn_getPicture_second: {
                mBitmap2 = mUvcCameraUtil.takePhoto();
                if (mBitmap2 != null) {
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
                if (mBitmap1 != null && mBitmap2 != null) {
                    BitmapHelper.saveBmpToFile(mBitmap1, mFaceModelDir, Bitmap.CompressFormat.JPEG, 100);
                    //mBitmap1 = ThumbnailUtils.extractThumbnail(mBitmap1, 640, 480);
                    BitmapHelper.saveBmpToFile(mBitmap2, mFaceModelDir1, Bitmap.CompressFormat.JPEG, 100);
                    Toast.makeText(DoubleCameraActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(DoubleCameraActivity.this, "请先采集照片", Toast.LENGTH_SHORT).show();
                }


            }
            break;

            case R.id.btn_merge: {
                if (mBitmap1 == null || mBitmap2 == null) {
                    return;
                }

                mBitmap2 = BitmapHelper.mergeBitmap_TB(mBitmap1, mBitmap2, false);
                sendMessage(MSG_GET_PICTURE_SECOND);
            }
            break;

            case R.id.btn_find: {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bitmap = mUvcCameraUtil.takePhoto();
                        DoubleCameraActivity.this.mBitmap1 = BitmapHelper.autoCutBitmap(bitmap);
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
                mUvcCameraUtil.openCamera(OPEM_CAMERA_PID, DeviceDAQConfig.DEVICE_DOUBLE_CAMERA);
            }
            break;

            case R.id.btn_fragment: {
                if (mUvcCameraHelper != null && mUvcCameraHelper.getUvcCameraHandler().isPreviewing()) {
                    mUvcCameraHelper.getUvcCameraHandler().close();
                }
                Intent intent = new Intent(DoubleCameraActivity.this, UvcActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.btn_face_compare: {
                if (mProgressDialog != null)
                    mProgressDialog.show();
                //身份证件照
//                mBitmap1 = BitmapUtil.getImageFromAssetsFile(DoubleCameraActivity.this, "IDCardImage.png");
                //拍照获取
//                mBitmap2 = BitmapUtil.getImageFromAssetsFile(DoubleCameraActivity.this, "cameraImage.png");
                if (mUvcCameraHelper != null && mUvcCameraHelper.getUvcCameraHandler() != null) {
                    if (mUvcCameraHelper.getUvcCameraHandler().isPreviewing()) {
                        mUvcCameraHelper.getUvcCameraHandler().stopRecording();
                    }
                    mFaceUtil.faceCompare(mBitmap1, mBitmap2);
                }
            }
            break;

        }
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
                case MSG_GET_PICTURE:
                    mIvFirst.setImageBitmap(mBitmap1);
                    break;

                case MSG_GET_PICTURE_SECOND:
                    mIvSecond.setImageBitmap(mBitmap2);
                    break;
            }
        }
    };

    private ICameraListener iCameraListener = new ICameraListener() {
        @Override
        public void onOpenCamera(String device, boolean isOpen, int code, String msg) {
            Log.d(TAG, "device: " + device + ", isOpen: " + isOpen + ", code: " + code + ", msg: " + msg);
            if (CameraPrinterLog.getInstance() != null)
                CameraPrinterLog.getInstance().collectDeviceLog(device, msg);
        }

        @Override
        public void onConnected(final String device, final boolean isConnected, int code, String msg) {
            Log.d(TAG, "device: " + device + ", isConnected: " + isConnected + ", code: " + code + ", msg: " + msg);
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

                    if (mUvcCameraHelper != null && mUvcCameraHelper.getUvcCameraHandler().isOpened()) {

//                        mUvcCameraUtil.resetCameraModeValue(UVCCamera.PU_BRIGHTNESS);
//                        mUvcCameraUtil.resetCameraModeValue(UVCCamera.PU_CONTRAST);
//                        mUvcCameraUtil.resetCameraModeValue(UVCCamera.PU_GAMMA);
//                        mUvcCameraUtil.resetCameraModeValue(UVCCamera.PU_WB_TEMP);
//                        mUvcCameraUtil.resetCameraModeValue(UVCCamera.PU_SATURATION);
//                        mUvcCameraUtil.resetCameraModeValue(UVCCamera.PU_HUE);
//                        mUvcCameraUtil.resetCameraModeValue(UVCCamera.PU_SHARPNESS);
//                        mUvcCameraUtil.resetCameraModeValue(UVCCamera.PU_BACKLIGHT);

                        mSbBrightness.setProgress(mUvcCameraUtil.getCameraModeValue(UVCCamera.PU_BRIGHTNESS));
                        mSbContrast.setProgress(mUvcCameraUtil.getCameraModeValue(UVCCamera.PU_CONTRAST));
                        mSbGamma.setProgress(mUvcCameraUtil.getCameraModeValue(UVCCamera.PU_GAMMA));
                        mSbTemp.setProgress(mUvcCameraUtil.getCameraModeValue(UVCCamera.PU_WB_TEMP));
                        mSbSaturation.setProgress(mUvcCameraUtil.getCameraModeValue(UVCCamera.PU_SATURATION));
                        mSbHue.setProgress(mUvcCameraUtil.getCameraModeValue(UVCCamera.PU_HUE));
                        mSbSharpness.setProgress(mUvcCameraUtil.getCameraModeValue(UVCCamera.PU_SHARPNESS));
                        mSbBacklight.setProgress(mUvcCameraUtil.getCameraModeValue(UVCCamera.PU_BACKLIGHT));
                    }
                }
            }).start();*/
        }

        @Override
        public void onAttached(UsbDevice usbDevice) {
            Log.d(TAG, "onAttached: " + usbDevice.getProductId());
            if (usbDevice.getProductId() == OPEM_CAMERA_PID) {
                try {
                    mUvcCameraUtil.openCamera(OPEM_CAMERA_PID, DeviceDAQConfig.DEVICE_DOUBLE_CAMERA);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public USBMonitor getUSBMonitor() {
        return mUvcCameraHelper.getUsbMonitor();
    }

    @Override
    public void onDialogResult(boolean b) {

    }

    private IFaceListener iFaceListener = new IFaceListener() {
        @Override
        public void onInitFace(int code, String msg) {
            Log.d(TAG, "code: " + code + ", msg: " + msg);
            if (code == 0) {
                Log.d(TAG, "onInitFace: 人脸引擎初始化成功");
            } else {
                Log.d(TAG, "onInitFace: 人脸引擎初始化失败, error: " + code);
            }

            if (CameraPrinterLog.getInstance() != null)
                CameraPrinterLog.getInstance().collectDeviceLog(DeviceConfig.DeivceType.CAMERA_DOUBLE.getName(), msg);
        }

        @Override
        public void onFaceCompareTime(long start, long stop) {
            Log.d(TAG, "onFaceCompareTime, 识别时间: " + (stop - start));
        }

        @Override
        public void onFaceCompareResult(int score) {
            if (mUvcCameraHelper != null && !mUvcCameraHelper.getUvcCameraHandler().isPreviewing()) {
                //mUvcCameraHelper.getUvcCameraHandler().close();
                mUvcCameraHelper.getUvcCameraHandler().startRecording();
            }
            if (mProgressDialog != null)
                mProgressDialog.dismiss();
            Log.d(TAG, "onFaceCompareResult, 识别结果, score: " + score);
            if (score > FACE_COMPARE_THRESHOLD)
                showToast("识别成功 >>>> score: " + score);
            else showToast("识别失败 >>>> score: " + score);
        }

        @Override
        public void onFaceCompareError(int code, String msg) {
            if (mUvcCameraHelper != null && mUvcCameraHelper.getUvcCameraHandler().isPreviewing()) {
                mUvcCameraHelper.getUvcCameraHandler().stopRecording();
            }
            if (mProgressDialog != null)
                mProgressDialog.dismiss();
            showToast("请先拍照");
            Log.d(TAG, "onFaceCompareError, 请先拍照");
        }
    };

    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DoubleCameraActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Dialog
     *
     * @param tips 提示信息
     */
    public void initDialog(String tips) {
        mProgressDialog = new Dialog(DoubleCameraActivity.this, R.style.progress_dialog);
        mProgressDialog.setContentView(R.layout.dialog_check_face);
        Objects.requireNonNull(mProgressDialog.getWindow()).setGravity(Gravity.CENTER);
        mProgressDialog.setCancelable(true);
        //mProgressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView msg = (TextView) mProgressDialog.findViewById(R.id.id_tv_loadingmsg);
        if (!TextUtils.isEmpty(tips)) {
            msg.setText(tips);
        } else
            msg.setText(R.string.dialog_tips);
    }
}
