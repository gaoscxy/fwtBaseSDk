package com.fingerprinlib.driver;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.IDWORLD.LAPI;
import com.fingerprinlib.driver.listener.CloseDeviceListener;
import com.fingerprinlib.driver.listener.CompareResultListener;
import com.fingerprinlib.driver.listener.CreateTempListener;
import com.fingerprinlib.driver.listener.GetImageBitmapListener;
import com.fingerprinlib.driver.listener.GetImageByteListener;
import com.fingerprinlib.driver.listener.ImageQualityListener;
import com.fingerprinlib.driver.listener.OpenDeviceListener;
import com.fingerprinlib.exception.CollectExcep;
import com.fingerprinlib.exception.DevOpenExcep;
import com.fingerprinlib.exception.InitFpException;

public class FPDriver {

    private static volatile FPDriver singleton;
    private LAPI m_cLAPI;
    private int m_hDevice = 0;
    private byte[] m_image = new byte[LAPI.WIDTH * LAPI.HEIGHT];
    private byte[] m_itemplate_1 = new byte[LAPI.FPINFO_STD_MAX_SIZE];
    private Application context;

    private FPDriver() {
    }

    public static FPDriver getInstance() {
        if (singleton == null) {
            synchronized (FPDriver.class) {
                if (singleton == null) {
                    singleton = new FPDriver();
                }
            }
        }
        return singleton;
    }

    public void initFp(Activity activity) {
        this.context = activity.getApplication();
        m_cLAPI = new LAPI(activity);
    }

    //打开设备
    public void openDevice(final OpenDeviceListener listener) throws InitFpException {
        chenckInit();
        Runnable r = new Runnable() {
            public void run() {
                OPEN_DEVICE(listener);
            }
        };
        Thread s = new Thread(r);
        s.start();
    }

    private void OPEN_DEVICE(OpenDeviceListener listener) {
        try {
            m_hDevice = m_cLAPI.OpenDeviceEx();
            String msg;
            if (m_hDevice == 0) {
                msg = "指纹设备打开异常";
                CollectExcep.getInstance(context).openDevError(m_hDevice, msg);
            } else {
                msg = "指纹设备打开成功";
                CollectExcep.getInstance(context).openDevResult(m_hDevice, msg);
            }
            if (listener != null) {
                listener.openResult(m_hDevice, msg);
            }
        } catch (Exception e) {
            CollectExcep.getInstance(context).openDevError(m_hDevice, e.getMessage());
        }
    }

    //关闭
    public void closeDevice(final CloseDeviceListener listener) throws InitFpException, DevOpenExcep {
        chenckInit();
        chenckDevOpen();
        Runnable r = new Runnable() {
            public void run() {
                CLOSE_DEVICE(listener);
            }
        };
        Thread s = new Thread(r);
        s.start();

    }

    private void CLOSE_DEVICE(CloseDeviceListener listener) {
        String msg;
        if (m_hDevice == 0) {
            return;
        }
        int result = m_cLAPI.CloseDeviceEx(m_hDevice);
        m_hDevice = 0;
        if (result == 0) {
            msg = "关闭失败";
            CollectExcep.getInstance(context).closeDevError(result, msg);
        } else {
            msg = "关闭成功";
            CollectExcep.getInstance(context).closeDevResult(result, msg);
        }
        listener.closeDeviceResult(result, msg);
    }

    //获取设备读取身份证的图片
    public void getImage(GetImageByteListener listener) throws DevOpenExcep, InitFpException {
        chenckInit();
        chenckDevOpen();
        String msg;
        int ret = m_cLAPI.GetImage(m_hDevice, m_image);
        if (ret != LAPI.TRUE) {
            msg = "获取图片失败";
            CollectExcep.getInstance(context).getImageError(ret, msg + "getImage()调用失败");//获取图片失败
            if (listener != null) {
                listener.errorByte(-1, msg);
            }
        } else {
            msg = "获取图片成功";
            CollectExcep.getInstance(context).getImageSucceed(ret, msg + "getImage()调用成功");//获取图片成功
            if (listener != null) {
                listener.succeedByte(LAPI.TRUE, msg, LAPI.WIDTH, LAPI.HEIGHT, m_image);
            }
        }
    }

    //获取图片(bitmap)
    public void getImage(GetImageBitmapListener listener) throws InitFpException, DevOpenExcep {
        chenckInit();
        chenckDevOpen();
        String msg;
        int ret = m_cLAPI.GetImage(m_hDevice, m_image);
        if (ret != LAPI.TRUE) {
            msg = "获取图片失败";
            CollectExcep.getInstance(context).getImageError(ret, msg + "getImage()调用失败(Bitmap)");//获取图片失败
            if (listener != null) {
                listener.errorBitmap(-1, msg);
            }
        } else {
            msg = "获取图片成功";
            CollectExcep.getInstance(context).getImageError(ret, msg + "getImage()调用成功(Bitmap)");//获取图片成功
            if (listener != null) {
                Bitmap bitmap = creatBitmap(m_image, LAPI.WIDTH, LAPI.HEIGHT);
                listener.succeedBitmap(LAPI.TRUE, msg, bitmap);
            }
        }
    }

    private Bitmap creatBitmap(byte[] image, int width, int height) {
        //创建图片
        if (width == 0) return null;
        if (height == 0) return null;

        int[] RGBbits = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            int v;
            if (image != null) v = image[i] & 0xff;
            else v = 0;
            RGBbits[i] = Color.rgb(v, v, v);
        }
        return Bitmap.createBitmap(RGBbits, width, height, Bitmap.Config.RGB_565);
    }

    //获取图片质量
    public void getImageQuality(ImageQualityListener listener) throws InitFpException, DevOpenExcep {
        chenckInit();
        chenckDevOpen();
        try {
            int qr = m_cLAPI.GetImageQuality(m_hDevice, m_image);
            if (listener != null)
                listener.imageQuality(qr);
        } catch (Exception e) {
            CollectExcep.getInstance(context).getImageQualityError(e.getMessage() + "getImageQuality()调用失败");//获取图片成功
            listener.imageQualityError(e);
        }

    }

    //创建模板
    public void createTemp(CreateTempListener listener) throws InitFpException, DevOpenExcep {
        chenckInit();
        chenckDevOpen();
        int ret;
        String msg;
        ret = m_cLAPI.GetImage(m_hDevice, m_image);
        if (ret != LAPI.TRUE) {
            listener.createTempFailure(ret, "获取指图片失败");
            return;
        }
        ret = m_cLAPI.IsPressFinger(m_hDevice, m_image);
        if (ret == 0) {
            msg = "请将手指放在指纹识别区域";
            listener.createTempStart(ret, msg);
            return;
        }

        ret = m_cLAPI.CreateTemplate(m_hDevice, m_image, m_itemplate_1);
        if (ret == 0) {
            msg = "创建模板失败";
            CollectExcep.getInstance(context).createTempError(ret, msg);//创建模板失败
            listener.createTempFailure(ret, msg);
        } else {
            msg = "";
            for (int i = 0; i < LAPI.FPINFO_STD_MAX_SIZE; i++) {
                msg += String.format("%02x", m_itemplate_1[i]);
            }
            CollectExcep.getInstance(context).createTempSucceed(msg);//创建模板成功
            listener.createTempResult(m_itemplate_1, msg);
        }
    }

    /*指纹比对*/
    private void compareTemps(byte[] fpInfo, CompareResultListener listener) {
        if (m_hDevice == 0) {
            CollectExcep.getInstance(context).compareTempsError("打开指纹识别设备失败", fpInfo);
            listener.compareErrorMsg("打开指纹识别设备失败");
        } else {
            int ret;
            ret = m_cLAPI.GetImage(m_hDevice, m_image);
            if (ret != LAPI.TRUE) {
                CollectExcep.getInstance(context).compareTempsError("获取指图片失败", fpInfo);
                listener.compareErrorMsg("获取指图片失败");
            } else {
                ret = m_cLAPI.IsPressFinger(m_hDevice, m_image);
                if (ret == 0) {
                    listener.compareErrorMsg("请将手指放在指纹识别区域");
                } else {
                    ret = m_cLAPI.CreateTemplate(m_hDevice, m_image, m_itemplate_1);
                    if (ret == 0) {
                        CollectExcep.getInstance(context).compareTempsError("指纹识别失败", fpInfo);//指纹比对失败
                        listener.compareErrorMsg("指纹识别失败");
                    } else {
                        if (fpInfo[0] == 0) {
                            CollectExcep.getInstance(context).compareTempsSucceed("指纹验证成功", fpInfo);//
                            listener.compareResultMsg("指纹验证成功");//没有存储指纹的时候直接验证成功
                        } else {
                            int score = m_cLAPI.CompareTemplates(m_hDevice, fpInfo, m_itemplate_1);
                            CollectExcep.getInstance(context).compareTempsSucceed("指纹验证成功", fpInfo);//
                            listener.compareResult(score);
                        }
                    }
                }
            }
        }
    }

    /**
     * 两个指纹入参
     *
     * @param listener
     * @param fpInfo        指纹1(身份证读取到的指纹信息或者是从外部获取的指纹信息)
     * @param m_itemplate_1 指纹2(当前实时获取的指纹信息)
     */
    public void compareTemps(CompareResultListener listener, byte[] fpInfo, byte[] m_itemplate_1) throws InitFpException, DevOpenExcep {
        chenckInit();
        chenckDevOpen();
        try {
            int score = m_cLAPI.CompareTemplates(m_hDevice, fpInfo, m_itemplate_1);
            if (listener != null){
                CollectExcep.getInstance(context).compareTempsSucceed("指纹验证成功", fpInfo);//指纹比对成功
                listener.compareResult(score);
            }
        } catch (Exception e) {
            CollectExcep.getInstance(context).compareTempsError(e.getMessage(), fpInfo);//指纹比对失败
            listener.compareError(e);
        }
    }

    /*检测是否初始化*/
    private void chenckInit() throws InitFpException {
        if (context == null) {
            throw new InitFpException("initFp()未初始化");
        }
    }

    /*检测设备是否开启*/
    private void chenckDevOpen() throws DevOpenExcep {
        if (m_hDevice == 0) {
            throw new DevOpenExcep("设备未开启");
        }
    }


}