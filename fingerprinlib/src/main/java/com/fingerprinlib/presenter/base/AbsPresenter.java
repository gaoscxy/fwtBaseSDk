package com.fingerprinlib.presenter.base;

import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fingerprinlib.api.ApiServer;
import com.fingerprinlib.api.ApiUrl;
import com.fingerprinlib.enumIndex.ActivateStatus;
import com.fingerprinlib.enumIndex.ResultStatus;
import com.fingerprinlib.mac.MacUtil;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public abstract class AbsPresenter implements BasePresenterInterface{
    protected ApiServer apiService;
    private String macAddr;
    private Context context;

    public AbsPresenter(Context context) {
        this.context = context;
        createApiService();
    }

    @Override
    public void createApiService() {
        OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(new MyInterceptor())//拦截器
                .connectTimeout(5, TimeUnit.SECONDS)//连接超时秒数
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiUrl.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
        apiService = retrofit.create(ApiServer.class);
    }


    @Override
    public Map<String,Object> getParameterMap(ActivateStatus activateStatus, ResultStatus resultStatus){
        return getParameterMap(activateStatus,resultStatus,null,null);
    }

    @Override
    public Map<String,Object> getParameterMap(ActivateStatus activateStatus,
                                              ResultStatus resultStatus,
                                              String msgErr,String remark){
        HashMap<String, Object> map = new HashMap<>();
        if (TextUtils.isEmpty(macAddr)){
            macAddr = MacUtil.getMac(context);
        }
        map.put("macId",macAddr);//mac地址
        map.put("hardwareType",5);//硬件类型

        map.put("useFlag",activateStatus.ordinal());//0开始 1关闭
        map.put("useStatus",resultStatus.ordinal());//0成功 1失败
        if (!TextUtils.isEmpty(msgErr)){
            map.put("failReson",msgErr);//原因
        }
        if (!TextUtils.isEmpty(remark)){
            map.put("remark",remark);//原因
        }
        return map;
    }

    @Override
    public <T> Observable<T> subscriptionObservable(Observable<T> observable) {
        return observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }
}
