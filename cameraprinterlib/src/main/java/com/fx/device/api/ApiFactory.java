package com.fx.device.api;

import android.content.Context;

import com.fx.device.entity.BaseEntity;

import java.util.Map;

import io.reactivex.Flowable;

/**
 * Created by gaos on 2017/8/8.
 */

public class ApiFactory {

    /**
     * 发送设备使用时的数据
     *
     * @param context 上下文
     * @param map     提交的参数
     */
    public static Flowable<BaseEntity> sendDeviceData(Context context, Map<String, Object> map) {
        return Api.getInstance(context).apiServer.sendDeviceData(map).compose(RxSchedulers.io_main());
    }
}
