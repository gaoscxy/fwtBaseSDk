package com.fx.device.utils;

public class DeviceConfig {
    public enum DeivceType {
        NONE(0, "none"),
        ID_CARD(1, "idCard"),    //身份证
        FINGER(2, "finger"),    //指纹识别
        CAMERA_DOUBLE(3, "doubleCamera"),    //双目摄像头
        CAMERA_HIGH(4, "highCmaera"),    //高拍仪
        CAMERA_VOIP(5, "voipCamera"),    //视频摄像头
        PRINTER(6, "printer");    //打印机

        private int value = 0;
        private String name = "none";

        private DeivceType(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return this.value;
        }

        public String getName() {
            return this.name;
        }

        public static DeivceType setValue(int code) {
            DeivceType[] var1 = values();
            int var2 = var1.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                DeivceType c = var1[var3];
                if (code == c.getValue()) {
                    return c;
                }
            }

            return NONE;
        }
    }

    /**
     * 设备状态（0：成功；1：失败）
     */
    public static class Status {
        public static final boolean SUCCESS = true;
        public static final boolean FAILURE = false;
    }
}
