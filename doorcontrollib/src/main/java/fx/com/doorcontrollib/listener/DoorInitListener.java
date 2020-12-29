package fx.com.doorcontrollib.listener;

public interface DoorInitListener {
    void succeed();

    /**
     * 用户串口加载失败可以再次方法再次加载一次 通过调用 LoadDevicePort的 loadDevicePort方法
     * @param loadDevicePort
     */
    void initDevicePort(LoadDevicePort loadDevicePort);
}
