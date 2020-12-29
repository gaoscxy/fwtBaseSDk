package fx.com.aggregate.listener;

import java.io.Serializable;
import java.util.List;

import fx.com.aggregate.entity.DeviceStatus;

public interface SelfCheckingListener extends Serializable {

    /**
     * 设备状态回调
     * @param deviceStatuses
     */
    void onDeviceStatus(List<DeviceStatus> deviceStatuses);
}
