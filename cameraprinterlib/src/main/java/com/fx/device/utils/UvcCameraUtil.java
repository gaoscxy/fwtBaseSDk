package com.fx.device.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.util.Log;

import com.fx.device.DeviceDAQConfig;
import com.fx.device.GlobalConstant;
import com.fx.device.camera.CameraInterface;
import com.fx.device.listener.ICameraListener;
import com.serenegiant.helper.UvcCameraHelper;
import com.serenegiant.widget.CameraViewInterface;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.List;

/**
 * UVC Camera
 */
public class UvcCameraUtil {

    private final String TAG = UvcCameraUtil.class.getSimpleName();

    private Context mContext;

    private ICameraListener mICameraListener;
    private CameraViewInterface mCameraView;
    private UvcCameraHelper mHelper;

    private String mMacStr;   //设备mac地址
    private String mIDCard;

    //private int OPEM_PU_PID = 48342;    //双目彩色
    //private int OPEM_PU_PID = 48343;    //双目黑白

    private final static int PREVIEW_WIDth = 640;
    private final static int PREVIEW_HEIGHT = 480;

    private static UvcCameraUtil mInstance;

    private UvcCameraUtil(Context context) {
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
            synchronized (UvcCameraUtil.class) {
                if (mInstance == null) {
                    mInstance = new UvcCameraUtil(context);
                }
            }
        }
    }

    /**
     * UvcCameraUtil实例。
     *
     * @return UvcCameraUtil
     */
    public static UvcCameraUtil getInstance() {
        return mInstance;
    }

    public void setCameraListener(ICameraListener iCameraListener) {
        this.mICameraListener = iCameraListener;
    }

    /**
     * 初始化设备
     *
     * @param cameraView    预览控件
     * @param previewWidth  PreView的宽度
     * @param previewHeight PreView的高度
     * @param deviceType    {@link DeviceDAQConfig}
     * @param IDCard        身份证号
     */
    public void initDevice(CameraViewInterface cameraView, int previewWidth, int previewHeight,
                           int deviceType, String IDCard) {
        if (cameraView == null) return;
        if (CameraInterface.getInstance() == null)
            CameraInterface.init(mContext.getApplicationContext());
        CameraInterface.getInstance().setCameraOccupied(true);
        this.mCameraView = cameraView;
        if (previewWidth <= 0 || previewHeight <= 0)
            mCameraView.setAspectRatio(PREVIEW_WIDth, PREVIEW_HEIGHT);
        else
            mCameraView.setAspectRatio(previewHeight, previewHeight);

        initCameraHelper((Activity) mContext, mContext, mCameraView, 1, 1,
                deviceType, IDCard);
        updateResolution(PREVIEW_WIDth, PREVIEW_HEIGHT); //设置分辨率
    }

    /**
     * @param activity   Activity
     * @param context    Context
     * @param cameraView 预览控件
     * @param encode     编码方式 0: use MediaSurfaceEncoder, 1: use MediaVideoEncoder, 2: use MediaVideoBufferEncoder
     * @param format     0: use UVCCamera.FRAME_FORMAT_YUYV， 1: use UVCCamera.FRAME_FORMAT_MJPEG
     * @param deviceType {@link DeviceDAQConfig}
     * @param IDCard     身份证号
     */
    public void initCameraHelper(Activity activity, Context context, CameraViewInterface cameraView,
                                 int encode, int format, int deviceType, String IDCard) {
        this.mIDCard = IDCard;
        //获取实例
        mHelper = UvcCameraHelper.getInstance(new WeakReference<>(activity),
                new WeakReference<>(context), cameraView, encode, format);
        mHelper.setConnectedListener(new UvcCameraHelper.onDeviceConnectedListener() {
            @Override
            public void onConnected(final boolean isOpen) {
                Log.d(TAG, "onPrint: " + isOpen);
                if (isOpen) {
                    Log.d(TAG, "设备认证成功");
                    if (deviceType == DeviceDAQConfig.DEVICE_DOUBLE_CAMERA) {
                        if (mICameraListener != null)
                            mICameraListener.onConnected(
                                    DeviceConfig.DeivceType.CAMERA_DOUBLE.getName(),
                                    true,
                                    GlobalConstant.CODE_SUCCESS,
                                    "device connect success");
                    } else if (deviceType == DeviceDAQConfig.DEVICE_HIGH_CAMERA) {
                        if (mICameraListener != null)
                            mICameraListener.onConnected(
                                    DeviceConfig.DeivceType.CAMERA_HIGH.getName(),
                                    true,
                                    GlobalConstant.CODE_SUCCESS,
                                    "device connect success");
                    }
                } else {
                    Log.d(TAG, "设备认证失败");
                    if (DeviceDAQConfig.DEVICE_DOUBLE_CAMERA == deviceType) {
                        if (mICameraListener != null)
                            mICameraListener.onConnected(
                                    DeviceConfig.DeivceType.CAMERA_DOUBLE.getName(),
                                    false,
                                    GlobalConstant.CODE_UVC_DEVICE_ERROR,
                                    "device connect failure");
                        int hardwareType = DeviceDAQConfig.DEVICE_DOUBLE_CAMERA;
                        int useFlag = DeviceDAQConfig.Flag.START;
                        int useStatus = DeviceDAQConfig.Status.FAILURE;
                        String failReson = "设备认证失败";
                        String remark = mIDCard;
                        DeviceDAQUtil.sendDeviceData(mContext, mMacStr, hardwareType, useFlag, useStatus,
                                failReson, remark);
                    } else if (DeviceDAQConfig.DEVICE_HIGH_CAMERA == deviceType) {
                        mICameraListener.onConnected(DeviceConfig.DeivceType.CAMERA_HIGH.getName(),
                                false,
                                GlobalConstant.CODE_UVC_DEVICE_ERROR,
                                "device connect failure");
                        int hardwareType = DeviceDAQConfig.DEVICE_HIGH_CAMERA;
                        int useFlag = DeviceDAQConfig.Flag.START;
                        int useStatus = DeviceDAQConfig.Status.FAILURE;
                        String failReson = "设备认证失败";
                        String remark = mIDCard;
                        DeviceDAQUtil.sendDeviceData(mContext, mMacStr, hardwareType, useFlag, useStatus,
                                failReson, remark);
                    }
                }
            }

            @Override
            public void onAttached(UsbDevice usbDevice) {
                if (mICameraListener != null)
                    mICameraListener.onAttached(usbDevice);
                Log.d(TAG, "onAttached: " + usbDevice.getProductId());
                /*if (usbDevice.getProductId() == OPEM_PU_PID) {
                    openCamera(mHelper, OPEM_PU_PID);
                }*/
            }
        });

        mHelper.initUvcCamera();
    }

    /**
     * get UvcCameraHelper
     *
     * @return UvcCameraHelper
     */
    public UvcCameraHelper getUvcCameraHelper() {
        return mHelper;
    }

    /**
     * 相应的生命周期中调用
     */
    public void workOnStart() {
        if (mHelper != null) {
            mHelper.workOnStart();
        }
    }

    /**
     * 相应的生命周期中调用
     */
    public void workOnStop() {
        if (mHelper != null) {
            mHelper.workOnStop();
        }
    }

    /**
     * 相应的生命周期中调用
     *
     * @param deviceType 设备类型{@link DeviceDAQConfig}
     */
    public void workOnDestroy(int deviceType) {
        if (mHelper != null) {
            mHelper.workOnDestroy();
            if (CameraInterface.getInstance() == null)
                CameraInterface.init(mContext.getApplicationContext());
            CameraInterface.getInstance().setCameraOccupied(false);
            int hardwareType = deviceType;
            int useFlag = DeviceDAQConfig.Flag.CLOSE;
            int useStatus = DeviceDAQConfig.Status.SUCCESS;
            String failReson = "";
            String remark = mIDCard;
            DeviceDAQUtil.sendDeviceData(mContext, mMacStr, hardwareType, useFlag, useStatus,
                    failReson, remark);
        }
    }

    /**
     * open Camera
     *
     * @param productId  pId
     * @param deviceType 设备类型{@link DeviceDAQConfig}
     */
    public void openCamera(int productId, int deviceType) {
        if (mHelper != null) {
            try {
                mHelper.openCamera(productId);
                if (deviceType == DeviceDAQConfig.DEVICE_DOUBLE_CAMERA) {
                    if (mICameraListener != null)
                        mICameraListener.onOpenCamera(
                                DeviceConfig.DeivceType.CAMERA_DOUBLE.getName(),
                                true,
                                GlobalConstant.CODE_SUCCESS,
                                "DoubleCamera open success");
                } else if (deviceType == DeviceDAQConfig.DEVICE_HIGH_CAMERA) {
                    if (mICameraListener != null)
                        mICameraListener.onOpenCamera(DeviceConfig.DeivceType.CAMERA_HIGH.getName(),
                                true,
                                GlobalConstant.CODE_SUCCESS,
                                "HighCamera open success");
                }
            } catch (Exception e) {
                e.printStackTrace();
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String exceptionStr = "\r\n" + sw.toString() + "\r\n";
                Log.e(TAG, exceptionStr);
                if (deviceType == DeviceDAQConfig.DEVICE_DOUBLE_CAMERA) {
                    if (mICameraListener != null)
                        mICameraListener.onOpenCamera(DeviceConfig.DeivceType.CAMERA_DOUBLE.getName(),
                                false,
                                GlobalConstant.CODE_CAMERA_OPEN_ERROR,
                                "DoubleCamera open exception, Exception: " + exceptionStr);
                } else if (deviceType == DeviceDAQConfig.DEVICE_HIGH_CAMERA) {
                    if (mICameraListener != null)
                        mICameraListener.onOpenCamera(DeviceConfig.DeivceType.CAMERA_HIGH.getName(),
                                false,
                                GlobalConstant.CODE_CAMERA_OPEN_ERROR,
                                "HighCamera open exception, Exception: " + exceptionStr);
                }
            }

            int hardwareType = deviceType;
            int useFlag = DeviceDAQConfig.Flag.START;
            int useStatus = DeviceDAQConfig.Status.SUCCESS;
            String failReson = "";
            String remark = mIDCard;
            DeviceDAQUtil.sendDeviceData(mContext, mMacStr, hardwareType, useFlag, useStatus,
                    failReson, remark);
        }
    }

    /**
     * 展示UVCCamera列表
     * <p>摄像头预览时调用，关闭摄像头；</p>
     * <p>摄像头未预览时调用，弹出UVCCamera列表。</p>
     * <p>调用的这个方法的Activity需实现接口CameraDialog.CameraDialogParent</p>
     */
    public void showOrDismissCameraList() {
        if (mHelper != null) {
            mHelper.showOrDismissCameraList();
        }
    }

    /**
     * 拍照
     *
     * @return Bitmap
     */
    public Bitmap takePhoto() {
        if (mHelper != null) {
            return mHelper.takePhoto();
        }
        return null;
    }

    /**
     * 获取摄像头支持的分辨率列表
     *
     * @return List<String>
     */
    public List<String> getSupportPreviewSizesStr() {
        if (mHelper != null) {
            return mHelper.getSupportPreviewSizesStr();
        }
        return null;
    }

    /**
     * 旋转预览界面
     * <P>对预览界面进行旋转</P>
     *
     * @param rotate 旋转角度(0  90  180  270)
     */
    public void setRotate(int rotate) {
        if (mHelper != null) {
            mHelper.setRotate(rotate);
        }
    }

    /**
     * 更改分辨率
     *
     * @param width  宽度
     * @param height 高度
     */
    public void updateResolution(int width, int height) {
        if (mHelper != null)
            mHelper.updateResolution(width, height);
    }

    /**
     * 设置参数
     *
     * @param flag  {@link com.serenegiant.usb.UVCCamera}
     *              PU_CONTRAST   对比度
     *              PU_GAMMA      伽马
     *              PU_WB_TEMP    白平衡
     *              PU_SATURATION 色彩饱和度
     *              PU_HUE        色调
     *              PU_SHARPNESS  锐利度
     *              PU_BACKLIGHT  背光补偿
     * @param value 范围为（0-100）
     */
    public void setCameraModeValue(int flag, int value) {
        if (mHelper != null)
            mHelper.setCameraModeValue(flag, value);
    }

    /**
     * 设置参数
     *
     * @return flag 对应参数类别：{@link com.serenegiant.usb.UVCCamera}
     * PU_CONTRAST   对比度
     * PU_GAMMA      伽马
     * PU_WB_TEMP    白平衡
     * PU_SATURATION 色彩饱和度
     * PU_HUE        色调
     * PU_SHARPNESS  锐利度
     * PU_BACKLIGHT  背光补偿
     */
    public int getCameraModeValue(int flag) {
        if (mHelper != null)
            return mHelper.getCameraModeValue(flag);
        return 0;
    }

    /**
     * 重置参数
     *
     * @param flag 对应参数类别：{@link com.serenegiant.usb.UVCCamera}
     *             PU_CONTRAST   对比度
     *             PU_GAMMA      伽马
     *             PU_WB_TEMP    白平衡
     *             PU_SATURATION 色彩饱和度
     *             PU_HUE        色调
     *             PU_SHARPNESS  锐利度
     *             PU_BACKLIGHT  背光补偿
     */
    public void resetCameraModeValue(int flag) {
        if (mHelper != null)
            mHelper.resetCameraModeValue(flag);
    }

    /**
     * 汉字转码
     */
    private String encodeStr(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
