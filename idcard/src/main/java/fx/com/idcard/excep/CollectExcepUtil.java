package fx.com.idcard.excep;

import android.annotation.SuppressLint;
import android.app.Application;

import java.util.Map;

import fx.com.idcard.entity.BaseEntity;
import fx.com.idcard.entity.IdCardEntity;
import fx.com.idcard.enumIndex.ActivateStatus;
import fx.com.idcard.enumIndex.ResultStatus;
import fx.com.idcard.presenter.base.AbsPresenter;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class CollectExcepUtil extends AbsPresenter {

    private static volatile CollectExcepUtil singleton;

    private CollectExcepUtil(Application application) {
        super(application);
    }

    public static CollectExcepUtil getInstance(Application application) {
        if (singleton == null) {
            synchronized (CollectExcepUtil.class) {
                if (singleton == null) {
                    singleton = new CollectExcepUtil(application);
                }
            }
        }
        return singleton;
    }

    @SuppressLint("CheckResult")
    public void init(Exception e) {
        //初始化异常
        Map<String, Object> map = getParameterMap(ActivateStatus.START, ResultStatus.FAILURE, "init()异常USB设备异常或者没有连接", null);
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
    public void sendSAMIDException(Exception e) {
        //SAMID 读取异常
        Map<String, Object> map = getParameterMap(ActivateStatus.START, ResultStatus.FAILURE, "SAMID 读取异常"+e.getMessage(), null);
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
    public void readIDCardStart(String readIDCard) {
        //开始读取卡
        Map<String, Object> map = getParameterMap(ActivateStatus.START, ResultStatus.SUCCEED, null, readIDCard);
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
    public void readIDCardStop(IdCardEntity idCardEntity) {
        Map<String, Object> map = getParameterMap(ActivateStatus.STOP, ResultStatus.SUCCEED, null, idCardEntity.getID_Num());
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
    public void readIDCardExcep(int errCode, String errMsg) {
        //读取身份证失败
        Map<String, Object> map = getParameterMap(ActivateStatus.START, ResultStatus.FAILURE, "读取身份证失败readIDCard()"+errMsg, null);
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
    public void readIDCard_FpStart(String readIDCard_fp) {
        //获取包含指纹的卡
        Map<String, Object> map = getParameterMap(ActivateStatus.START, ResultStatus.SUCCEED, null, readIDCard_fp);
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
    public void readIDCard_FpStop(IdCardEntity entity) {
        //获取包含指纹的卡
        Map<String, Object> map = getParameterMap(ActivateStatus.STOP, ResultStatus.SUCCEED, null, entity.getID_Num());
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
    public void fpInfoError(IdCardEntity e, String msg) {
        //获取指纹异常
        Map<String, Object> map = getParameterMap(ActivateStatus.START, ResultStatus.FAILURE, "获取指纹异常"+msg, e.getID_Num());
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
    public void readIDCard_FpError(int errCode, String errMsg) {
        Map<String, Object> map = getParameterMap(ActivateStatus.START, ResultStatus.FAILURE, "读取身份证含指纹异常:readIDCard_Fp()"+errMsg,null);
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