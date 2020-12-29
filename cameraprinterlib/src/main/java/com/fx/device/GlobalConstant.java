package com.fx.device;

public class GlobalConstant {
    /**
     * 成功码
     */
    public static final int CODE_SUCCESS = 200;

    /**
     * Camera打开失败
     */
    public static final int CODE_CAMERA_OPEN_ERROR = 201;

    /**
     * UVC设备认证失败
     */
    public static final int CODE_UVC_DEVICE_ERROR = 301;

    /**
     * 人脸引擎初始化失败
     */
    public static final int CODE_FACE_ENGINE_INIT_ERROR = 401;

    /**
     * 人脸识别失败
     */
    public static final int CODE_FACE_COMPARE_ERROR = 402;

    /**
     * 未安装打印机应用
     */
    public static final int CODE_APP_NO_INSTALL_ERROR = 501;

    /**
     * 打印失败
     */
    public static final int CODE_PRINT_ERROR = 502;

    public interface URLContact {
        /**
         * <p>联调访问:"http://192.168.10.236:9000"</p>
         * <p>线上环境访问:"http://www.renrenlv.com.cn:9000"</p>
         */
        //String BASE_URL = "http://192.168.10.236:9000";
        String BASE_URL = "http://www.renrenlv.com.cn:81";

        String DEVICE_SERVICE = BASE_URL + "/tss/service/deviceService!doUse.do";
    }

}
