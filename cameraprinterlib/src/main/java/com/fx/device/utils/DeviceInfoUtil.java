package com.fx.device.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 设备信息采集
 */
public class DeviceInfoUtil {
    private static final String TAG = DeviceInfoUtil.class.getSimpleName();

    //日志存储路径
    private String PATH_LOG;
    private String LOG_FILE_NAME = "deviceInfo.txt";    //设备信息文件

    private static DeviceInfoUtil INSTANCE = new DeviceInfoUtil();
    private Context mContext;// 程序的Context对象
    private Map<String, String> info = new HashMap<String, String>();// 用来存储信息

    private StringBuffer sb;

    /**
     * 保证只有一个DeviceInfoUtil实例
     */
    private DeviceInfoUtil() {

    }

    /**
     * DeviceInfoUtil ,单例模式
     */
    public static DeviceInfoUtil getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public void init(Context context) {
        mContext = context;
        PATH_LOG = StorageUtil.getSDKLogRootPath(context);
        getEquipmentInfo();
    }

    private void getEquipmentInfo() {
        String brand, model, androidversion, romname, romversion, sign, sdk;
        String device, product, cpu, board, display, id, version_codes_base, maker, user, tags;
        String hardware, host, unknown, type, time, radio, serial, cpu2;

        product = " 产品 : " + android.os.Build.PRODUCT;
        cpu = " CPU_ABI : " + android.os.Build.CPU_ABI;
        tags = " 标签 : " + android.os.Build.TAGS;
        version_codes_base = " VERSION_CODES.BASE: " + android.os.Build.VERSION_CODES.BASE;
        model = " 型号 : " + android.os.Build.MODEL;
        sdk = " SDK : " + android.os.Build.VERSION.SDK;
        androidversion = " Android 版本 : " + android.os.Build.VERSION.RELEASE;
        device = " 驱动 : " + android.os.Build.DEVICE;
        display = " DISPLAY: " + android.os.Build.DISPLAY;
        brand = " 品牌 : " + android.os.Build.BRAND;
        board = " 基板 : " + android.os.Build.BOARD;
        sign = " 设备标识 : " + android.os.Build.FINGERPRINT;
        id = " 版本号 : " + android.os.Build.ID;
        maker = " 制造商 : " + android.os.Build.MANUFACTURER;
        user = " 用户 : " + android.os.Build.USER;
        cpu2 = " CPU_ABI2 : " + android.os.Build.CPU_ABI2;
        hardware = " 硬件 : " + android.os.Build.HARDWARE;
        host = " 主机地址 :" + android.os.Build.HOST;
        unknown = " 未知信息 : " + android.os.Build.UNKNOWN;
        type = " 版本类型 : " + android.os.Build.TYPE;
        time = " 时间 : " + String.valueOf(android.os.Build.TIME);
        radio = " Radio : " + android.os.Build.RADIO;
        serial = " 序列号 : " + android.os.Build.SERIAL;

        String msg = "\r\n" + product + "\r\n" + cpu + "\r\n" + tags + "\r\n" + version_codes_base + "\r\n" +
                model + "\r\n" + sdk + "\r\n" + androidversion + "\r\n" + device + "\r\n" +
                display + "\r\n" + brand + "\r\n" + board + sign + "\r\n" + id + "\r\n" +
                maker + "\r\n" + user + "\r\n" + cpu2 + "\r\n" + hardware + "\r\n" + host + "\r\n" +
                unknown + "\r\n" + type + "\r\n" + time + "\r\n" + radio + "\r\n" + serial;
        collectDeviceInfo(mContext, msg);
    }

    /**
     * 收集参数信息
     *
     * @param context
     */
    private void collectDeviceInfo(Context context, String deviceInfo) {
        try {
            PackageManager pm = context.getPackageManager();// 获得包管理器
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);// 得到该应用的信息，即主Activity
            if (pi != null) {
                String versionName = pi.versionName == null ? "null"
                        : pi.versionName;
                String versionCode = pi.versionCode + "";
                info.put("versionName", versionName);
                info.put("versionCode", versionCode);
                String timestamp = new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
                info.put("timestamp", timestamp);
                info.put("deviceInfo", deviceInfo);
            }

            saveInfo2File();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String saveInfo2File() {
        sb = new StringBuffer();
        sb.append(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + "\r\n");
        for (Map.Entry<String, String> entry : info.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\r\n");
        }
        sb.append(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + "\r\n\r\n");
        Log.e(TAG, sb.toString());

        //保存文件
//        String timetamp = format.format(new Date(System.currentTimeMillis()));
//        FILE_NAME = "Log-" + timetamp + ".log";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                File dir = new File(PATH_LOG);
                if (!dir.exists()) dir.mkdir();

                FileOutputStream fos = new FileOutputStream(dir.getAbsolutePath() + File.separator + LOG_FILE_NAME, false);
//                OutputStreamWriter writer=new OutputStreamWriter(fos, StandardCharsets.UTF_8);
//                writer.write(sb.toString());
//                writer.close();
                fos.write(sb.toString().getBytes());
                fos.close();
                return LOG_FILE_NAME;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
