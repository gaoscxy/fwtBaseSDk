package fx.com.doorcontrollib.door;

import android.app.Application;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import ch.wrzw.CPUMsger;
import ch.wrzw.DoorStateResult;
import ch.wrzw.Result;
import ch.wrzw.TempSensorResult;
import ch.wrzw.TempSensorStateResult;
import fx.com.doorcontrollib.entity.DoorResult;
import fx.com.doorcontrollib.entity.DoorState;
import fx.com.doorcontrollib.entity.SensorStateEntity;
import fx.com.doorcontrollib.entity.TempSensorEntity;
import fx.com.doorcontrollib.enumIndex.TempEnum;
import fx.com.doorcontrollib.exception.CollectException;
import fx.com.doorcontrollib.exception.InitException;
import fx.com.doorcontrollib.listener.DoorInitListener;
import fx.com.doorcontrollib.listener.LoadDevicePort;

public class DoorController implements LoadDevicePort {

    private CPUMsger mCpuMsger;
    private boolean initResult;//串口初始化结果
    private Application app;


    private DoorController() {
    }

    private static class SingletonInstance {
        private static final DoorController INSTANCE = new DoorController();
    }

    public static DoorController getInstance() {
        return SingletonInstance.INSTANCE;
    }

    /*初始化*/
    public void init(Application app, DoorInitListener listener) {
        this.app = app;
        mCpuMsger = CPUMsger.getInstance();
        initDevice();
        callbackInitResult(listener);
    }

    /*初始化*/
    public void init(Application app, String port, DoorInitListener listener) {
        this.app = app;
        mCpuMsger = CPUMsger.getInstance();
        initDevice(port);
        callbackInitResult(listener);
    }

    private void callbackInitResult(DoorInitListener listener) {
        if (initResult) {
            listener.succeed();
        } else {
            listener.initDevicePort(this);
        }
    }

    @Override
    public boolean loadDevicePort(String port) {
        //默认串口加载失败会让用户从新指定新的串口回调此方法从新加载指定串口
        initDevice(port);
        return initResult;
    }


    private void initDevice() {
        initDevice(null);
    }

    private void initDevice(String port) {
        if (TextUtils.isEmpty(port)) {
            initResult = mCpuMsger.initDevice();//使用默认串口路
        } else {
            initResult = mCpuMsger.initDevice(port);//指定串口路
        }
        CollectException.getInstance(app).initDevice(initResult);
    }

    /**
     * 开门
     *
     * @return DoorResult
     * @throws InitException
     */
    public DoorResult openDoor() throws InitException {
        chenckInit();
        Result openResult = mCpuMsger.openDoor();
        CollectException.getInstance(app).openDoor(openResult);
        return new DoorResult(openResult.getCode(), openResult.getErrDes(), openResult.isSuccess(), openResult.getSend_bytes(), openResult.getReturn_bytes());
    }

    /**
     * 先检查门的状态在开门
     *
     * @return DoorResult null=代表开着呢
     * @throws InitException
     */
    public DoorResult openChenckDoor() throws InitException {
        chenckInit();
        DoorResult doorResult = null;
        DoorStateResult state = mCpuMsger.getDoorState();
        if (state == null){
            doorResult = getDoorResult("查询门状态失败");
            return doorResult;
        }
        if (state.getDoorState() == null){
             doorResult = getDoorResult("获取门状态失败");
            return doorResult;
        }
        if (state.getDoorState() == ch.wrzw.DoorState.DOOR_CLOSED) {
            //代表门是关闭状态,可以进行开门
            Result openResult = mCpuMsger.openDoor();
            CollectException.getInstance(app).openDoor(openResult);
            doorResult = new DoorResult(openResult.getCode(), openResult.getErrDes(),
                    openResult.isSuccess(), openResult.getSend_bytes(), openResult.getReturn_bytes());
        }
        return doorResult;
    }

    /*关门*/
    private DoorResult closeDoor() throws InitException {
        chenckInit();
        Result closeResult = mCpuMsger.closeDoor();
        CollectException.getInstance(app).closeDoor(closeResult);
        return new DoorResult(closeResult.getCode(), closeResult.getErrDes(), closeResult.isSuccess(),
                closeResult.getSend_bytes(), closeResult.getReturn_bytes());
    }

    /**
     * 检查关门的状态后在执行是否关门null=代表开着呢
     *
     * @return null=代表关着呢
     * @throws InitException
     */
    private DoorResult closeDoorChenck() throws InitException {
        chenckInit();
        DoorResult doorResult = null;
        DoorStateResult state = mCpuMsger.getDoorState();

        if (state == null){
            doorResult = getDoorResult("查询门状态失败");
            return doorResult;
        }
        if (state.getDoorState() == null){
            doorResult = getDoorResult("获取门状态失败");
            return doorResult;
        }
        if (state.getDoorState() == ch.wrzw.DoorState.DOOR_OPENED) {
            //处于开门状态下才可以进行关闭
            Result closeResult = mCpuMsger.closeDoor();
            doorResult = new DoorResult(closeResult.getCode(), closeResult.getErrDes(), closeResult.isSuccess(),
                    closeResult.getSend_bytes(), closeResult.getReturn_bytes());
            CollectException.getInstance(app).closeDoor(closeResult);
        }
        return doorResult;
    }

    /*获取门的状态*/
    public DoorState getDoorState() throws InitException {
        chenckInit();
        DoorStateResult state = mCpuMsger.getDoorState();
        if (state == null){
            return null;
        }
        ch.wrzw.DoorState stateEnum = state.getDoorState();
        int stateIndex = 0;
        if (stateEnum != null) {
            stateIndex = stateEnum.ordinal();
        }
        return new DoorState(state.getCode(),stateIndex);
    }

    @NonNull
    private DoorResult getDoorResult(String s) {
        DoorResult doorResult;
        doorResult = new DoorResult();
        doorResult.setCode(-1);
        doorResult.setErrDes(s);
        return doorResult;
    }

    /**
     * getCode()	无	Int	返回值为结果标识数据
     * isUpsOk()	无	Boolean		True：UPS 传感器正常
     * isBoxSensorOk()	无	Boolean	True：箱体传感器正常
     * getResult()	无	Result	Result
     *
     * @return 返回传感器状态对象
     * @throws InitException
     */
    public SensorStateEntity querySensorState() throws InitException {
        chenckInit();
        SensorStateEntity entity = null;
        TempSensorStateResult mResult = mCpuMsger.getTempSensorState();
        if (mResult != null) {
            entity = new SensorStateEntity();
            entity.setBoxSensorOk(mResult.isBoxSensorOk());
            entity.setUpsOk(mResult.isUpsOk());
            entity.setCode(mResult.getCode());
            entity.setmResult(mResult.getResult());
        }
        CollectException.getInstance(app).sensorState(mResult);
        return entity;
    }

    /**
     * 获取温度
     * No: 1表示ups温度，2表示箱体温度
     *
     * @param tempEnum
     * @return
     */
    private TempSensorEntity getTempSensorResult(TempEnum tempEnum) throws InitException {
        chenckInit();
        TempSensorEntity entity = null;
        TempSensorResult mResult = mCpuMsger.getTempSensorResult(tempEnum.getIndex());
        if (mResult != null) {
            entity = new TempSensorEntity();
            entity.setCode(mResult.getCode());
            entity.setTemp(mResult.getTemp());
        }
        CollectException.getInstance(app).tempSensor(mResult);
        return entity;
    }

    /*串口是否连接*/
    public boolean isAvailable() throws InitException {
        chenckInit();
        return mCpuMsger.isAvailable();
    }

    /*关闭串口*/
    public boolean closeDevice() throws InitException {
        chenckInit();
        boolean closeDevice = mCpuMsger.closeDevice();
        CollectException.getInstance(app).closeDevice(closeDevice);
        return closeDevice;
    }

    private void chenckInit() throws InitException {
        if (mCpuMsger == null) {
            throw new InitException("未初始化");
        }
        if (!initResult) {
            throw new InitException("串口未初始化未成功");
        }
    }


}