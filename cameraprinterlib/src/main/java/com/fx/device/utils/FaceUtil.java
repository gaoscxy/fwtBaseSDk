package com.fx.device.utils;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.fx.device.DeviceDAQConfig;
import com.fx.device.GlobalConstant;
import com.fx.device.listener.IFaceListener;
import com.sadhana.sdk.FaceEngine;
import com.sadhana.util.BitmapUtil;

public class FaceUtil {

    private final String TAG = FaceUtil.class.getSimpleName();

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private int FACE_COMPARE_THRESHOLD = 80;    //相似度

    private UsbManager mUsbManager;
    private FaceEngine mFaceEngine = new FaceEngine();

    private float result;    //识别结果，0~100

    private final int MSG_RESULT = 0;

    private Bitmap mBitmap1, mBitmap2;

    private static FaceUtil mInstance;

    private IFaceListener mIFaceListener;

    private Context mContext;
    private String mMacStr;
    private String mIDCard;

    private FaceUtil(Context context) {
        this.mContext = context;
        this.mMacStr = MacUtil.getMac(mContext);
    }

    /**
     * init
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        if (mInstance == null) {
            synchronized (FaceUtil.class) {
                if (mInstance == null) {
                    mInstance = new FaceUtil(context);
                }
            }
        }
    }

    /**
     * UvcCameraUtil实例。
     *
     * @return UvcCameraUtil
     */
    public static FaceUtil getInstance() {
        return mInstance;
    }

    public void setIFaceListener(IFaceListener iFaceListener) {
        this.mIFaceListener = iFaceListener;
    }

    /**
     * 初始化人脸
     *
     * @param shreshold 相似度阀值
     * @param IDCard    身份证号
     */
    @SuppressLint("StaticFieldLeak")
    public void initFace(int shreshold, String IDCard) {
        this.mIDCard = IDCard;
        if (shreshold > 0) FACE_COMPARE_THRESHOLD = shreshold;
        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                openUsbDevice();
                return null;
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void openUsbDevice() {
        Log.d(TAG, "openUsbDevice: ");
        //before open usb device
        //should try to get usb permission
        tryGetUsbPermission();
    }

    private void tryGetUsbPermission() {
        Log.d(TAG, "tryGetUsbPermission: ");
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        mContext.registerReceiver(mUsbPermissionActionReceiver, filter);

        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0,
                new Intent(ACTION_USB_PERMISSION), 0);

        //here do emulation to ask all connected usb device for permission
        for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            Log.d(TAG, "tryGetUsbPermission: " + usbDevice.getProductId());
            //add some conditional check if necessary
            if (usbDevice.getVendorId() == 11030 && usbDevice.getProductId() == 48342) {
                if (mUsbManager.hasPermission(usbDevice)) {
                    //if has already got permission, just goto connect it
                    //that means: user has choose yes for your previously popup window asking for grant perssion for this usb device
                    //and also choose option: not ask again
                    afterGetUsbPermission(usbDevice);
                } else {
                    //this line will let android popup window, ask user whether to allow this app to have permission to operate this usb device
                    mUsbManager.requestPermission(usbDevice, mPermissionIntent);
                }
            }
        }
    }

    private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //user choose YES for your previously popup window asking for grant perssion for this usb device
                        if (null != usbDevice) {
                            afterGetUsbPermission(usbDevice);
                        }
                    } else {
                        //user choose NO for your previously popup window asking for grant perssion for this usb device
                        Toast.makeText(context, String.valueOf("Permission denied for device" + usbDevice), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    };

    private void afterGetUsbPermission(UsbDevice usbDevice) {
        //call method to set up device communication
        //        Toast.makeText(this, String.valueOf("Got permission for usb device: " + usbDevice), Toast.LENGTH_LONG).show();
        //        Toast.makeText(this, String.valueOf("Found USB device: VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId()), Toast.LENGTH_LONG).show();

        doYourOpenUsbDevice(usbDevice);
    }

    private void doYourOpenUsbDevice(UsbDevice usbDevice) {
        //now follow line will NOT show: User has not given permission to device UsbDevice
        UsbDeviceConnection connection = mUsbManager.openDevice(usbDevice);
        //add your operation code here
        if (connection != null) {
            int fd = connection.getFileDescriptor();
            int code = mFaceEngine.initialize(mContext.getAssets(), "sadhana_base.model", fd);
            if (code == 0) {
                Log.d(TAG, "人脸引擎初始化成功");
                if (mIFaceListener != null)
                    mIFaceListener.onInitFace(code, "face engine init success");
            } else {
                Log.d(TAG, "人脸引擎初始化失败, error: " + code);
                if (mIFaceListener != null)
                    mIFaceListener.onInitFace(GlobalConstant.CODE_FACE_ENGINE_INIT_ERROR, "face engine init failure");
                int hardwareType = DeviceDAQConfig.DEVICE_DOUBLE_CAMERA;
                int useFlag = DeviceDAQConfig.Flag.START;
                int useStatus = DeviceDAQConfig.Status.FAILURE;
                String failReson = "人脸引擎初始化失败, error: " + code;
                String remark = mIDCard;
                DeviceDAQUtil.sendDeviceData(mContext, mMacStr, hardwareType, useFlag, useStatus,
                        failReson, remark);
            }
        } else {
            Log.e(TAG, "UsbManager openDevice failed");
            if (mIFaceListener != null)
                mIFaceListener.onInitFace(GlobalConstant.CODE_FACE_ENGINE_INIT_ERROR, "UsbManager openDevice failed");
            int hardwareType = DeviceDAQConfig.DEVICE_DOUBLE_CAMERA;
            int useFlag = DeviceDAQConfig.Flag.START;
            int useStatus = DeviceDAQConfig.Status.FAILURE;
            String failReson = "UsbManager openDevice failed";
            String remark = mIDCard;
            DeviceDAQUtil.sendDeviceData(mContext, mMacStr, hardwareType, useFlag, useStatus,
                    failReson, remark);
        }
    }

    /**
     * 人脸识别
     *
     * @param bitmap1 识别对象
     * @param bitmap2 识别对象
     */
    public void faceCompare(final Bitmap bitmap1, final Bitmap bitmap2) {
        //获取人脸图像
        this.mBitmap1 = bitmap1;
        this.mBitmap2 = bitmap2;

        if (mBitmap1 != null && mBitmap2 != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    result = faceVerifyEx(bitmap1, bitmap2);
                    long stop = System.currentTimeMillis();
                    Log.d(TAG, "识别时间: " + (stop - start));
                    if (mIFaceListener != null)
                        mIFaceListener.onFaceCompareTime(start, stop);
                    mHandler.sendEmptyMessage(MSG_RESULT);
                }
            }).start();
        } else {
            mIFaceListener.onFaceCompareError(GlobalConstant.CODE_FACE_COMPARE_ERROR, "mBitmap1 is null");
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("StaticFieldLeak")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_RESULT:
                    int resultStr = (int) (result * 100);
                    Log.d(TAG, "score: " + (resultStr + ""));
                    if (mIFaceListener != null)
                        mIFaceListener.onFaceCompareResult(resultStr);
                    if (resultStr > FACE_COMPARE_THRESHOLD) {    //识别成功
                        int hardwareType = DeviceDAQConfig.DEVICE_DOUBLE_CAMERA;
                        int useFlag = DeviceDAQConfig.Flag.START;
                        int useStatus = DeviceDAQConfig.Status.SUCCESS;
                        String failReson = "识别成功";
                        String remark = mIDCard;
                        DeviceDAQUtil.sendDeviceData(mContext, mMacStr, hardwareType, useFlag, useStatus,
                                failReson, remark);
                    } else {    //识别失败
                        int hardwareType = DeviceDAQConfig.DEVICE_DOUBLE_CAMERA;
                        int useFlag = DeviceDAQConfig.Flag.START;
                        int useStatus = DeviceDAQConfig.Status.FAILURE;
                        String failReson = "识别失败";
                        String remark = mIDCard;
                        DeviceDAQUtil.sendDeviceData(mContext, mMacStr, hardwareType, useFlag, useStatus,
                                failReson, remark);
                    }
                    break;
            }
        }
    };

    /**
     * 人脸对比
     *
     * @param idCardImg 身份证照片Bitmap
     * @param bmp2      拍照获取的Bitmap
     * @return
     */
    private float faceVerify(Bitmap idCardImg, Bitmap bmp2) {
        return mFaceEngine.verifyWithBitmap(idCardImg, bmp2);
    }

    /**
     * 人脸对比
     *
     * @param idCardImg 身份证照片Bitmap
     * @param bmp2      拍照获取的Bitmap
     * @return
     */
    private float faceVerifyEx(Bitmap idCardImg, Bitmap bmp2) {
        // 防止身份照取出来后，提取不到体征值问题
        Bitmap bmp1 = idCardImg.copy(Bitmap.Config.ARGB_8888, true);
        byte[] bgr1 = BitmapUtil.getPixelsBGR(bmp1);
        int width1 = bmp1.getWidth();
        int height1 = bmp1.getHeight();
        int pitch1 = width1 * 3;

        byte[] bgr2 = BitmapUtil.getPixelsBGR(bmp2);
        int width2 = bmp2.getWidth();
        int height2 = bmp2.getHeight();
        int pitch2 = width2 * 3;

        return mFaceEngine.verify(bgr1, width1, height1, pitch1, bgr2, width2, height2, pitch2);
    }

    /**
     * 相应的生命周期中调用
     */
    public void onDestory() {
        recycleBitmap();
        unregisterReceiver();
    }

    /**
     * 回收Bitmap
     */
    private void recycleBitmap() {
        if (mBitmap1 != null) {
            mBitmap1.recycle();
            mBitmap1 = null;
        }
        if (mBitmap2 != null) {
            mBitmap2.recycle();
            mBitmap2 = null;
        }
    }

    /**
     * 反注册
     */
    private void unregisterReceiver() {
        if (mContext != null)
            mContext.unregisterReceiver(mUsbPermissionActionReceiver);
    }
}
