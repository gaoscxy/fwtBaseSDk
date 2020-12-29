package com.fx.device.camera;

public class Constants {
    /**
     * 连接在主板上实际的camera数量
     * <p>双目摄像头（彩色）</p>
     * <p>视频摄像头</p>
     * <p>高拍仪</p>
     */
    public static final int CAMERA_REAL_NUM = 3;


    /**
     * camera初始化的id
     */
    public static int CAMERA_ID_DEFAULT = -1;

    /*
      cameraId之前的固定值：
      双目摄像头：0
      音视频摄像头：1
      高拍仪：2
     */
    /**
     * 双目（彩色）CameraId
     */
    public static int CAMERA_ID_DOUBLE_COLOR = CAMERA_ID_DEFAULT;

    /**
     * 视频CameraId
     */
    public static int CAMERA_ID_VOIP = CAMERA_ID_DEFAULT;

    /**
     * 高拍仪CameraId
     */
    public static int CAMERA_ID_HIGH = CAMERA_ID_DEFAULT;

    /**
     * 双目（彩色）摄像头支持的PreviewSize
     */
    public static final String CAMERA_FLAG_DOUBLE_COLOR = "160x120 320x240 352x288 640x480 800x600 1024x768 1280x720 1280x1024 1280x960 1600x1200 1920x1080 2048x1536 ";

    /**
     * 视频摄像头支持的PreviewSize
     */
    public static final String CAMERA_FLAG_VOIP = "320x240 640x480 1280x960 1280x720 1920x1080 2048x1536 2592x1944 2592x1944 ";

    /**
     * 高拍仪摄像头支持的PreviewSize
     */
    public static final String CAMERA_FLAG_HIGH = "640x480 800x600 1024x768 1280x960 1280x720 1600x1200 1920x1080 2048x1536 2592x1944 3256x2440 ";

    public static String CAMERA_PREVIEW_SIZE_160x120 = "160x120";
    public static String CAMERA_PREVIEW_SIZE_320x240 = "320x240";
    public static String CAMERA_PREVIEW_SIZE_325x288 = "352x288";
    public static String CAMERA_PREVIEW_SIZE_640x480 = "640x480";
    public static String CAMERA_PREVIEW_SIZE_800x600 = "800x600";
    public static String CAMERA_PREVIEW_SIZE_1024x768 = "1024x768";
    public static String CAMERA_PREVIEW_SIZE_1280x720 = "1280x720";
    public static String CAMERA_PREVIEW_SIZE_1280x960 = "1280x960";
    public static String CAMERA_PREVIEW_SIZE_1280x1024 = "1280x1024";
    public static String CAMERA_PREVIEW_SIZE_1600x1200 = "1600x1200";
    public static String CAMERA_PREVIEW_SIZE_1920x1080 = "1920x1080";
    public static String CAMERA_PREVIEW_SIZE_2048x1536 = "2048x1536";
    public static String CAMERA_PREVIEW_SIZE_2592x1944 = "2592x1944";
    public static String CAMERA_PREVIEW_SIZE_3256x2440 = "3256x2440";
}
