package com.fx.device;

/**
 * 后台设备数据采集使用
 */
public class DeviceDAQConfig {
    /**
     * 高拍仪
     */
    public static final int DEVICE_HIGH_CAMERA = 1;

    /**
     * 门禁
     */
    public static final int DEVICE_DOOR = 2;

    /**
     * 身份证读取
     */
    public static final int DEVICE_ID_CARD = 3;

    /**
     * 打印机
     */
    public static final int DEVICE_PRINTER = 4;

    /**
     * 指纹识别
     */
    public static final int DEVICE_FINGER = 5;

    /**
     * 双目摄像头
     */
    public static final int DEVICE_DOUBLE_CAMERA = 6;

    /**
     * 使用标识（0：开始；1：关闭）
     */
    public static class Flag {
        public static final int START = 0;
        public static final int CLOSE = 1;
    }

    /**
     * 设备状态（0：成功；1：失败）
     */
    public static class Status {
        public static final int SUCCESS = 0;
        public static final int FAILURE = 1;
    }
}
