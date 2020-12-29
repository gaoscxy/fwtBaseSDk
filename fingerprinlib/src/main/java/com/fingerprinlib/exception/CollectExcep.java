package com.fingerprinlib.exception;

import android.annotation.SuppressLint;
import android.app.Application;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Map;

import com.fingerprinlib.entity.BaseEntity;
import com.fingerprinlib.enumIndex.ActivateStatus;
import com.fingerprinlib.enumIndex.ResultStatus;
import com.fingerprinlib.presenter.base.AbsPresenter;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class CollectExcep extends AbsPresenter {

    private static volatile CollectExcep singleton;

    private CollectExcep(Application application) {
        super(application);
    }

    public static CollectExcep getInstance(Application application) {
        if (singleton == null) {
            synchronized (CollectExcep.class) {
                if (singleton == null) {
                    singleton = new CollectExcep(application);
                }
            }
        }
        return singleton;
    }


    /*启动设备成功*/
    public void openDevResult(int m_hDevice, String msg) {
        Map<String, Object> map = getParameterMap(ActivateStatus.START,ResultStatus.SUCCEED,null,"");
        Observable<BaseEntity> observable = apiService.deviceService(map);
        subscriptionObservable(observable).subscribe(new Consumer<BaseEntity>() {
            @Override
            public void accept(BaseEntity baseEntity) throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });
    }

    /*启动设备失败*/
    @SuppressLint("CheckResult")
    public void openDevError(int m_hDevice, String msg) {
        Map<String, Object> map = getParameterMap(ActivateStatus.START,ResultStatus.FAILURE,"启动设备失败"+msg,null);
        Observable<BaseEntity> observable = apiService.deviceService(map);
        subscriptionObservable(observable).subscribe(new Consumer<BaseEntity>() {
            @Override
            public void accept(BaseEntity baseEntity) throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });
    }

    /*关闭设备*/
    @SuppressLint("CheckResult")
    public void closeDevResult(int result, String msg) {
        Map<String, Object> map = getParameterMap(ActivateStatus.STOP,ResultStatus.SUCCEED,null,null);
        Observable<BaseEntity> observable = apiService.deviceService(map);
        subscriptionObservable(observable).subscribe(new Consumer<BaseEntity>() {
            @Override
            public void accept(BaseEntity baseEntity) throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });
    }

    /*设备关闭失败*/
    @SuppressLint("CheckResult")
    public void closeDevError(int result, String msg) {
        Map<String, Object> map = getParameterMap(ActivateStatus.STOP,ResultStatus.FAILURE,"设备关闭失败"+msg,null);
        Observable<BaseEntity> observable = apiService.deviceService(map);
        subscriptionObservable(observable).subscribe(new Consumer<BaseEntity>() {
            @Override
            public void accept(BaseEntity baseEntity) throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });
    }

    /*获取图片失败*/
    @SuppressLint("CheckResult")
    public void getImageError(int ret, String msg) {
        Map<String, Object> map = getParameterMap(ActivateStatus.START,ResultStatus.FAILURE,"获取图片失败"+msg,null);
        Observable<BaseEntity> observable = apiService.deviceService(map);
        subscriptionObservable(observable).subscribe(new Consumer<BaseEntity>() {
            @Override
            public void accept(BaseEntity baseEntity) throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });
    }

    /*读取图片成功*/
    @SuppressLint("CheckResult")
    public void getImageSucceed(int ret, String msg) {
        Map<String, Object> map = getParameterMap(ActivateStatus.START,ResultStatus.FAILURE,null,null);
        Observable<BaseEntity> observable = apiService.deviceService(map);
        subscriptionObservable(observable).subscribe(new Consumer<BaseEntity>() {
            @Override
            public void accept(BaseEntity baseEntity) throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });
    }

    @SuppressLint("CheckResult")
    public void getImageQualityError(String s) {
        Map<String, Object> map = getParameterMap(ActivateStatus.START,ResultStatus.FAILURE,s,null);
        Observable<BaseEntity> observable = apiService.deviceService(map);
        subscriptionObservable(observable).subscribe(new Consumer<BaseEntity>() {
            @Override
            public void accept(BaseEntity baseEntity) throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });
    }

    /*创建指纹模板失败*/
    @SuppressLint("CheckResult")
    public void createTempError(int ret, String msg) {
        Map<String, Object> map = getParameterMap(ActivateStatus.START,ResultStatus.FAILURE,msg,null);
        Observable<BaseEntity> observable = apiService.deviceService(map);
        subscriptionObservable(observable).subscribe(new Consumer<BaseEntity>() {
            @Override
            public void accept(BaseEntity baseEntity) throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });
    }

    /*创建指纹模板成功*/
    @SuppressLint("CheckResult")
    public void createTempSucceed(String msg) {
        Map<String, Object> map = getParameterMap(ActivateStatus.START,ResultStatus.SUCCEED,null,msg);
        Observable<BaseEntity> observable = apiService.deviceService(map);
        subscriptionObservable(observable).subscribe(new Consumer<BaseEntity>() {
            @Override
            public void accept(BaseEntity baseEntity) throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });
    }

    /*指纹比对失败*/
    @SuppressLint("CheckResult")
    public void compareTempsError(String msg, byte[] m_itemplate_1) {
        String zw = Arrays.toString(m_itemplate_1);
        if (TextUtils.isEmpty(zw)){
            zw = null;
        }
        Map<String, Object> map = getParameterMap(ActivateStatus.START,ResultStatus.FAILURE,"compareTemps()指纹比对失败"+msg,zw);
        Observable<BaseEntity> observable = apiService.deviceService(map);
        subscriptionObservable(observable).subscribe(new Consumer<BaseEntity>() {
            @Override
            public void accept(BaseEntity baseEntity) throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });
    }


    public void compareTempsSucceed(String msg, byte[] fpInfo) {
        String zw = Arrays.toString(fpInfo);
        if (TextUtils.isEmpty(zw)){
            zw = null;
        }
        Map<String, Object> map = getParameterMap(ActivateStatus.START,ResultStatus.SUCCEED,msg,zw);
        Observable<BaseEntity> observable = apiService.deviceService(map);
        subscriptionObservable(observable).subscribe(new Consumer<BaseEntity>() {
            @Override
            public void accept(BaseEntity baseEntity) throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });
    }
}