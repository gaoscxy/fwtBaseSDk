package fx.com.doorcontrollib.exception;

import android.annotation.SuppressLint;
import android.app.Application;

import java.util.Map;

import ch.wrzw.Result;
import ch.wrzw.TempSensorResult;
import ch.wrzw.TempSensorStateResult;
import fx.com.doorcontrollib.entity.BaseEntity;
import fx.com.doorcontrollib.enumIndex.ActivateStatus;
import fx.com.doorcontrollib.enumIndex.ResultStatus;
import fx.com.doorcontrollib.presenter.base.AbsPresenter;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class CollectException extends AbsPresenter {

    private static volatile CollectException singleton;

    private CollectException(Application application) {
        super(application);
    }

    public static CollectException getInstance(Application application) {
        if (singleton == null) {
            synchronized (CollectException.class) {
                if (singleton == null) {
                    singleton = new CollectException(application);
                }
            }
        }
        return singleton;
    }

    /*初始化串口*/
    @SuppressLint("CheckResult")
    public void initDevice(boolean initResult) {
        String msg;
        ResultStatus index;
        if (initResult){
             index = ResultStatus.SUCCEED;
             msg = "初始化串口成功";
        }else {
             index = ResultStatus.FAILURE;
            msg = "初始化串口失败";
        }
        Map<String, Object> map = getParameterMap(ActivateStatus.START,index,msg,msg);
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

    /*开门*/
    @SuppressLint("CheckResult")
    public void openDoor(Result result) {
        String msg;
        ResultStatus index;
        if (result.isSuccess()){
            index = ResultStatus.SUCCEED;
            msg = "成功";
        }else {
            index = ResultStatus.FAILURE;
            msg = result.getErrDes();
        }
        Map<String, Object> map = getParameterMap(ActivateStatus.START,index,msg,"openDoor");
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

    /*关门*/
    @SuppressLint("CheckResult")
    public void closeDoor(Result result) {

        String msg;
        ResultStatus index;
        if (result.isSuccess()){
            index = ResultStatus.SUCCEED;
            msg = "成功";
        }else {
            index = ResultStatus.FAILURE;
            msg = result.getErrDes();
        }
        Map<String, Object> map = getParameterMap(ActivateStatus.START,index,msg,"closeDoor");
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

    /*关闭串口*/
    @SuppressLint("CheckResult")
    public void closeDevice(boolean closeDevice) {
        String msg;
        ResultStatus index;
        if (closeDevice){
            index = ResultStatus.SUCCEED;
            msg = "串口关闭成功";
        }else {
            index = ResultStatus.FAILURE;
            msg = "串口关闭失败";
        }
        Map<String, Object> map = getParameterMap(ActivateStatus.START,index,msg,"closeDevice");
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

    public void sensorState(TempSensorStateResult mResult) {
        String msg;
        ResultStatus index;
        Map<String, Object> map;
        if (mResult != null){
            index = ResultStatus.SUCCEED;
            msg = "获取传感器状态成功";
            map = getParameterMap(ActivateStatus.START,index,"","");
        }else {
            index = ResultStatus.FAILURE;
            msg = "获取传感器状态失败";
            map = getParameterMap(ActivateStatus.START,index,msg,"");
        }
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

    public void tempSensor(TempSensorResult mResult) {
        String msg;
        ResultStatus index;
        Map<String, Object> map;
        if (mResult != null){
            index = ResultStatus.SUCCEED;
            msg = "获取温度成功";
            map = getParameterMap(ActivateStatus.START,index,"","");
        }else {
            index = ResultStatus.FAILURE;
            msg = "获取温度失败";
            map = getParameterMap(ActivateStatus.START,index,msg,"");
        }
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