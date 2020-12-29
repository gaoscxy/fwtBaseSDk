package com.fx.device.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.fx.device.api.ApiFactory;
import com.fx.device.entity.BaseEntity;
import com.fx.device.listener.ApiListener;
import com.fx.device.DeviceDAQConfig;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 设备数据采集
 */
public class DeviceDAQUtil {
    private static final String TAG = "DeviceDAQUtil";

    /**
     * 发送设备使用时的数据
     *
     * @param context      上下文
     * @param macStr       设备mac地址
     * @param hardwareType 硬件设备类型
     * @param useFlag      使用标识（0：开始；1：关闭）{@link DeviceDAQConfig.Flag}
     * @param useStatus    设备状态（0：成功；1：失败）{@link DeviceDAQConfig.Status}
     * @param failReson    设备原因（设备状态失败时，设置设备启动或关闭失败原因，设备状态为成功时不设定此项目）
     * @param remark       备注（硬件为指纹识别，人脸识别，身份证阅读器时传入身份证信息等）
     */
    @SuppressLint("CheckResult")
    public static void sendDeviceData(Context context, String macStr, int hardwareType, int useFlag,
                                      int useStatus, String failReson, String remark) {
        Map<String, Object> map = getParameterMap(macStr, hardwareType, useFlag, useStatus, failReson,
                remark);
        ApiFactory.sendDeviceData(context, map).subscribe(
                baseEntity -> apiListener.onResponse(baseEntity),
                throwable -> apiListener.onError(throwable.toString()));
    }

    private static ApiListener apiListener = new ApiListener() {
        @Override
        public void onResponse(BaseEntity result) {
            Log.e(TAG, result.toString());
        }

        @Override
        public void onError(String error) {
            Log.e(TAG, error);
        }
    };

    /**
     * 构造请求参数
     *
     * @param macStr       设备mac地址
     * @param hardwareType 硬件设备类型
     * @param useFlag      使用标识（0：开始；1：关闭）{@link DeviceDAQConfig.Flag}
     * @param useStatus    设备状态（0：成功；1：失败）{@link DeviceDAQConfig.Status}
     * @param failReson    设备原因（设备状态失败时，设置设备启动或关闭失败原因，设备状态为成功时不设定此项目）
     * @param remark       备注（硬件为指纹识别，人脸识别，身份证阅读器时传入身份证信息等）
     * @return
     */
    private static Map<String, Object> getParameterMap(String macStr, int hardwareType, int useFlag,
                                                       int useStatus, String failReson, String remark) {
        Map<String, Object> map = new HashMap<>();
        map.put("macId", macStr);
        map.put("hardwareType", hardwareType);
        map.put("useFlag", useFlag);
        map.put("useStatus", useStatus);
        if (useStatus == DeviceDAQConfig.Status.SUCCESS)
            map.put("failReson", "");
        else if (useStatus == DeviceDAQConfig.Status.FAILURE)
            map.put("failReson", encodeStr(failReson));
        map.put("remark", remark);
        return map;
    }

    /**
     * 转码
     */
    private static String encodeStr(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }
}
