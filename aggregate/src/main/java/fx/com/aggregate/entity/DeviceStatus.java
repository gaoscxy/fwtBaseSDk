package fx.com.aggregate.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeviceStatus implements Serializable {

    /**
     * mac地址
     */
    private String mac;

    /**
     * 设备id
     * {@link com.fx.device.utils.DeviceConfig.DeivceType}
     */
    private int device;

    /**
     * 设备名称
     * {@link com.fx.device.utils.DeviceConfig.DeivceType}
     */
    private String deviceName;

    /**
     * 设备状态true\false
     * {@link com.fx.device.utils.DeviceConfig.Status}
     */
    private boolean status;

    /**
     * 描述
     */
    private String msg;

    /**
     * 时间戳(格式化：yyyy-MM-dd HH:mm:ss)
     */
    private String timestamp;

    public DeviceStatus() {
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getDevice() {
        return device;
    }

    public void setDevice(int device) {
        this.device = device;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
    }

    @Override
    public String toString() {
        return "DeviceStatus{" +
                "mac='" + mac + '\'' +
                ", device=" + device +
                ", deviceName=" + deviceName +
                ", status=" + status +
                ", msg='" + msg + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
