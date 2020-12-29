package com.fx.device.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type.Builder;
import android.util.Log;
import android.view.SurfaceHolder;

import com.fx.device.utils.DeviceConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

public class CameraInterface {
    private static final String TAG = "CameraInterface";

    public Camera mCamera;
    private Parameters mParameters;
    private boolean mIsPreviewing = false;
    private float mPreviewRate = -1.0F;

    private int mPreviewWidth = 640;
    private int mPreviewHeight = 480;

    private int mCameraId = -1;
    private RenderScript mRenderScript;
    private ScriptIntrinsicYuvToRGB mYuvToRgbIntrinsic;
    private Builder mYuvType;
    private Builder mRgbaType;
    private Allocation mIn;
    private Allocation mOut;
    ShutterCallback mShutterCallback = new ShutterCallback() {
        public void onShutter() {
        }
    };
    PictureCallback mPictureCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
        }
    };
    private CameraInterface.onPreviewFrame mOnPreviewFrame = null;
    private Bitmap mBitmapLastFrame = null;
    PreviewCallback mPreviewCall = new PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Size previewSize = camera.getParameters().getPreviewSize();
            if (mYuvType == null) {
                mYuvType = (new Builder(mRenderScript, Element.U8(mRenderScript))).setX(data.length);
                mIn = Allocation.createTyped(mRenderScript, mYuvType.create(), 1);
                mRgbaType = (new Builder(mRenderScript, Element.RGBA_8888(mRenderScript))).setX(previewSize.width).setY(previewSize.height);
                mOut = Allocation.createTyped(mRenderScript, mRgbaType.create(), 1);
            }

            mIn.copyFrom(data);
            mYuvToRgbIntrinsic.setInput(mIn);
            mYuvToRgbIntrinsic.forEach(mOut);
            Bitmap bmpout = Bitmap.createBitmap(previewSize.width, previewSize.height, Config.ARGB_8888);
            mOut.copyTo(bmpout);
            mBitmapLastFrame = bmpout.copy(Config.ARGB_8888, false);
            bmpout.recycle();
            if (mOnPreviewFrame != null) {
                mOnPreviewFrame.onPreviewFrame(data, camera);
            }

        }
    };

    private final String CAMERA_NUM_EXCAPTION_STR = "请检查所有摄像头硬件是否正常或所有摄像头硬件与主板的连接是否正常";

    /**
     * Cmaera状态：是否被占用
     */
    private boolean mIsCameraStatus = false;

    /**
     * 获取Camera是否被占用的状态
     *
     * @return <P>true:被占用状态</P>
     * <P>false: 空闲状态</P>
     */
    public boolean isCameraOccupied() {
        return mIsCameraStatus;
    }

    /**
     * 设置Camera是否被占用的状态
     * <P>true:被占用状态</P>
     * <P>false: 空闲状态</P>
     *
     * @param isCameraOccupied
     */
    public void setCameraOccupied(boolean isCameraOccupied) {
        this.mIsCameraStatus = isCameraOccupied;
    }

    private ICameraListener mICameraListener;

    /**
     * CameraListener
     *
     * @param mICameraListener camera状态回调
     */
    public void setICameraListener(ICameraListener mICameraListener) {
        this.mICameraListener = mICameraListener;
    }

    public interface ICameraListener {
        void onCameraInit(DeviceConfig.DeivceType deivceType, boolean status, String msg);

        void onCameraOpen(DeviceConfig.DeivceType deivceType, boolean status, String msg);
    }

    private static CameraInterface mInstance;

    public CameraInterface(Context context) {
        mRenderScript = RenderScript.create(context);
        mYuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(mRenderScript, Element.U8_4(mRenderScript));
    }

    /**
     * init
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        if (mInstance == null) {
            synchronized (CameraInterface.class) {
                if (mInstance == null) {
                    mInstance = new CameraInterface(context);
                }
            }
        }
    }

    /**
     * CameraInterface实例。
     *
     * @return CameraInterface
     */
    public static CameraInterface getInstance() {
        return mInstance;
    }

    /**
     * camera init
     */
    public void cameraInit() {
        if (isCameraOccupied()) {
            String msg = "camera is being used, skip it";
            Log.e(TAG, msg);
            if (null != mICameraListener)
                mICameraListener.onCameraInit(DeviceConfig.DeivceType.NONE, true, msg);
            return;
        }
        int cameraNumber = Camera.getNumberOfCameras();
        Log.e(TAG, "cameraRealNum= " + Constants.CAMERA_REAL_NUM + ", getNumberOfCameras= " + cameraNumber);
        String msg;
        if (cameraNumber == 0) {
            msg = "cameraRealNum= " + Constants.CAMERA_REAL_NUM + ", getNumberOfCameras= " + cameraNumber +
                    "，cause: " + CAMERA_NUM_EXCAPTION_STR;
            Log.e(TAG, msg);
            if (null != mICameraListener)
                mICameraListener.onCameraInit(DeviceConfig.DeivceType.NONE, false, msg);
            return;
        }

        for (int i = 0; i < Constants.CAMERA_REAL_NUM; ++i) {    //以连接在主板上实际的camera数量去循环打开Camera
            try {
                Camera camera = Camera.open(i);
                if (camera != null) {
                    Parameters parameters = camera.getParameters();
                    String cameraSupportPreviewSize = CameraParaUtil.getInstance().getSupportSize(parameters);
                    Log.e(TAG, "camera support previewSize: " + cameraSupportPreviewSize);
                    switch (cameraSupportPreviewSize) {
                        case Constants.CAMERA_FLAG_DOUBLE_COLOR:
                            Constants.CAMERA_ID_DOUBLE_COLOR = i;
                            Log.e(TAG, "双目（彩色）摄像头id为：" + Constants.CAMERA_ID_DOUBLE_COLOR);
                            msg = "cameraId: " + Constants.CAMERA_ID_DOUBLE_COLOR + ", " + DeviceConfig.DeivceType.CAMERA_DOUBLE.getName() + " open success";
                            Log.e(TAG, msg);
                            if (null != mICameraListener)
                                mICameraListener.onCameraInit(DeviceConfig.DeivceType.CAMERA_DOUBLE, true, msg);
                            break;
                        case Constants.CAMERA_FLAG_VOIP:
                            Constants.CAMERA_ID_VOIP = i;
                            Log.e(TAG, "视频摄像头id为：" + Constants.CAMERA_ID_VOIP);
                            msg = "cameraId: " + Constants.CAMERA_ID_VOIP + ", " + DeviceConfig.DeivceType.CAMERA_VOIP.getName() + " open success";
                            Log.e(TAG, msg);
                            if (null != mICameraListener)
                                mICameraListener.onCameraInit(DeviceConfig.DeivceType.CAMERA_VOIP, true, msg);
                            break;
                        case Constants.CAMERA_FLAG_HIGH:
                            Constants.CAMERA_ID_HIGH = i;
                            Log.e(TAG, "高拍仪摄像头id为: " + Constants.CAMERA_ID_HIGH);
                            msg = "cameraId: " + Constants.CAMERA_ID_HIGH + ", " + DeviceConfig.DeivceType.CAMERA_HIGH.getName() + " open success";
                            Log.e(TAG, msg);
                            if (null != mICameraListener)
                                mICameraListener.onCameraInit(DeviceConfig.DeivceType.CAMERA_HIGH, true, msg);
                            break;
                    }
                    camera.release();
                } else {
                    msg = "camera open failure, cameraId: " + i + ", cause: camera object is null";
                    Log.e(TAG, msg);
                    if (null != mICameraListener)
                        mICameraListener.onCameraInit(DeviceConfig.DeivceType.NONE, false, msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String exceptionStr = "\r\n" + sw.toString() + "\r\n";
                    Log.e(TAG, "exceptionStr: " + exceptionStr);
                    DeviceConfig.DeivceType deivceType = DeviceConfig.DeivceType.NONE;
                    if (Constants.CAMERA_ID_DOUBLE_COLOR == Constants.CAMERA_ID_DEFAULT) {
                        deivceType = DeviceConfig.DeivceType.CAMERA_DOUBLE;
                    } else if (Constants.CAMERA_ID_VOIP == Constants.CAMERA_ID_DEFAULT) {
                        deivceType = DeviceConfig.DeivceType.CAMERA_VOIP;
                    } else if (Constants.CAMERA_ID_HIGH == Constants.CAMERA_ID_DEFAULT) {
                        deivceType = DeviceConfig.DeivceType.CAMERA_HIGH;
                    }
                    boolean status;
                    String cameraNumStr;
                    String msgException;
                    if (cameraNumber != Constants.CAMERA_REAL_NUM) {
                        status = DeviceConfig.Status.FAILURE;
                        cameraNumStr = "cameraRealNum= " + Constants.CAMERA_REAL_NUM + ", getNumberOfCameras= " + cameraNumber +
                                "，cause: " + CAMERA_NUM_EXCAPTION_STR;
                        msgException = deivceType.getName() + " camera open exception, cameraId: " + i +
                                ", cause: cameraId is illegal, " + cameraNumStr + ", Exception: " + exceptionStr;
                        Log.e(TAG, "device: " + deivceType.getName() + ", msg: " + msgException);
                    } else {
                        if (exceptionStr.contains("Fail to connect to camera service") ||
                                exceptionStr.contains("android.hardware.Camera.open") ||
                                exceptionStr.contains("getParameters failed (empty parameters)")) {    //对指定异常做特殊处理
                            status = DeviceConfig.Status.SUCCESS;
                            msgException = "camera open success, cameraId: " + i;
                            Log.e(TAG, "device: " + deivceType.getName() + ", msg: " + msgException);
                        } else {
                            status = DeviceConfig.Status.FAILURE;
                            msgException = deivceType.getName() + " camera open exception, Exception: " + exceptionStr;
                            Log.e(TAG, "device: " + deivceType.getName() + ", msg: " + msgException);
                        }
                    }
                    if (null != mICameraListener)
                        mICameraListener.onCameraInit(deivceType, status, msgException);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public void openCamera(String cameraFlag, CameraInterface.onPreviewFrame callback) {
        setCameraOccupied(true);
        DeviceConfig.DeivceType deivceType = DeviceConfig.DeivceType.NONE;
        switch (cameraFlag) {
            case Constants.CAMERA_FLAG_DOUBLE_COLOR:
                mCameraId = Constants.CAMERA_ID_DOUBLE_COLOR;
                deivceType = DeviceConfig.DeivceType.CAMERA_DOUBLE;
                break;
            case Constants.CAMERA_FLAG_VOIP:
                mCameraId = Constants.CAMERA_ID_VOIP;
                deivceType = DeviceConfig.DeivceType.CAMERA_VOIP;
                break;
            case Constants.CAMERA_FLAG_HIGH:
                mCameraId = Constants.CAMERA_ID_HIGH;
                deivceType = DeviceConfig.DeivceType.CAMERA_HIGH;
                break;
        }
        Log.e(TAG, "当前cameraId: " + mCameraId);
        if (mCameraId < 0) {
            boolean status = DeviceConfig.Status.FAILURE;
            String msg = deivceType.getName() + " open failure, cameraId: " + mCameraId +
                    ", cause: cameraId is illegal";
            Log.e(TAG, "device: " + deivceType.getName() + ", msg: " + msg);
            if (null != mICameraListener)
                mICameraListener.onCameraOpen(deivceType, status, msg);
        } else {
            mCamera = Camera.open(mCameraId);
            mOnPreviewFrame = callback;
            if (null != mICameraListener) {
                boolean status;
                String msg;
                if (mCamera != null) {
                    if (mCameraId == Constants.CAMERA_ID_DOUBLE_COLOR) {
                        deivceType = DeviceConfig.DeivceType.CAMERA_DOUBLE;
                    } else if (mCameraId == Constants.CAMERA_ID_VOIP) {
                        deivceType = DeviceConfig.DeivceType.CAMERA_VOIP;
                    } else if (mCameraId == Constants.CAMERA_ID_HIGH) {
                        deivceType = DeviceConfig.DeivceType.CAMERA_HIGH;
                    }
                    status = true;
                    msg = deivceType.getName() + " open success, cameraId: " + mCameraId;
                } else {
                    status = false;
                    msg = deivceType.getName() + " open failure, cameraId: " + mCameraId + ", cause: camera object is null";
                }
                mICameraListener.onCameraOpen(deivceType, status, msg);
            }
        }

    }

    public void setParameters(String previewSize) {
        if (previewSize != null) {
            String[] previewList = previewSize.split("x");
            if (previewList.length == 2) {
                mPreviewWidth = Integer.parseInt(previewList[0]);
                mPreviewHeight = Integer.parseInt(previewList[1]);
            }
        }

        if (mCamera != null) {
            mParameters = mCamera.getParameters();
            mParameters.setPictureFormat(256);
            mParameters.setWhiteBalance("2");
            List<Size> previewSizes = mParameters.getSupportedPreviewSizes();
            Iterator iterator = previewSizes.iterator();
            while (iterator.hasNext()) {
                Size size = (Size) iterator.next();
                Log.e(TAG, "当前摄像头预览分辨率为: 宽:" + size.width + ", 高:" + size.height);
            }

            mParameters.setPictureSize(mPreviewWidth, mPreviewHeight);
            mParameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
            mCamera.setDisplayOrientation(0);

            List<String> modes = mParameters.getSupportedFocusModes();
            if (modes.contains("continuous-video")) {
                mParameters.setFlashMode("continuous-video");
            }

            mCamera.setParameters(mParameters);
            mCamera.setPreviewCallback(mPreviewCall);
        }

    }

    public void CloseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback((PreviewCallback) null);
            mCamera.stopPreview();
            mCamera.release();
            mPreviewRate = -1.0F;
            mIsPreviewing = false;
            mCamera = null;
        }
        setCameraOccupied(false);
    }

    public int getCameraId() {
        return mCameraId;
    }

    public void startPreview(SurfaceHolder holder) throws IOException {
        Log.e(TAG, "doStartPreview: ");
        if (mIsPreviewing) {
            if (mCamera != null)
                mCamera.stopPreview();
        } else {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
                mIsPreviewing = true;
            }
        }
    }

    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mIsPreviewing = false;
        }

    }

    public void takePhoto(String pictureSavePath, CompressFormat format, CameraInterface.TakePhotoCallbakc call) {
        if (mCamera != null) {
            mCamera.takePicture(mShutterCallback, mPictureCallback, new CameraInterface.MyPictureCallback(pictureSavePath, format, call));
        }

    }

    public Bitmap getLastFrame() {
        return mBitmapLastFrame != null ? mBitmapLastFrame : null;
    }

    public interface onPreviewFrame {
        void onPreviewFrame(byte[] data, Camera camera);
    }

    public interface TakePhotoCallbakc {
        void getBitmap(Bitmap bitmap);
    }

    class MyPictureCallback implements PictureCallback {
        String savePath;
        CompressFormat pictureFormat;
        CameraInterface.TakePhotoCallbakc takePhotoCallBack;

        public MyPictureCallback(String pictureSavePath, CompressFormat format, CameraInterface.TakePhotoCallbakc call) {
            savePath = pictureSavePath;
            pictureFormat = format;
            takePhotoCallBack = call;
        }

        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = null;
            if (null != data) {
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                if (takePhotoCallBack != null) {
                    takePhotoCallBack.getBitmap(bitmap);
                }
            }

            if (mCamera != null)
                mCamera.startPreview();
        }
    }
}
