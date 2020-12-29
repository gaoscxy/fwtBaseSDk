package com.fingerprinlib.api;

import java.util.Map;

import com.fingerprinlib.entity.BaseEntity;
import io.reactivex.Observable;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

public interface ApiServer {

    @POST(ApiUrl.DEVICE_SERVICE)
    Observable<BaseEntity> deviceService(@QueryMap Map<String, Object> map);
}
