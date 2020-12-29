package com.fx.device.api;

import com.fx.device.GlobalConstant;
import com.fx.device.entity.BaseEntity;

import java.util.Map;

import io.reactivex.Flowable;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface ApiServer {

    /**
     * 发送设备使用时的数据
     *
     * @param map
     * @return
     */
    @POST(GlobalConstant.URLContact.DEVICE_SERVICE)
    Flowable<BaseEntity> sendDeviceData(@QueryMap Map<String, Object> map);
}
