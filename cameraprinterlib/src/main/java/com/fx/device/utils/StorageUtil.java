package com.fx.device.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class StorageUtil {
    private static final String TAG = "StorageUtil";

    /**
     * 日志存储根目录
     *
     * @param context
     * @return
     */
    public static String getSDKLogRootPath(Context context) {
        return getAppDirectory(context)+ File.separator + "sdkLog";
    }

    /**
     * 设备自检日志存储目录
     *
     * @param context
     * @return
     */
    public static String getCheckLogRootPath(Context context) {
        return getSDKLogRootPath(context) + File.separator + "FuWuTingLog.txt";
    }

    /**
     * Crash日志存储目录
     *
     * @param context
     * @return
     */
    public static String getCrashLogPath(Context context) {
        return getSDKLogRootPath(context) + File.separator + "crash";
    }

    /**
     * 设备信息存储目录
     *
     * @param context
     * @return
     */
    public static String getDeviceInfoPath(Context context) {
        return getSDKLogRootPath(context);
    }

    /**
     * @return 返回app的缓存路径
     */
    public static String getAppDirectory(Context context) {
        String appCacheDir = context.getExternalFilesDir("").getAbsolutePath();
        Log.d(TAG, "appCacheDir: " + appCacheDir);
        return appCacheDir;
    }

    public static boolean deleteFile(File file) {
        if (file.exists()) { // 判断文件是否存在
            if (file.isFile()) { // 判断是否是文件
                file.delete(); // delete()方法 删除的意思;
            } else if (file.isDirectory()) { // 否则如果它是一个目录
                File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
                for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
                    deleteFile(files[i]); // 把每个文件 用这个方法进行迭代
                }
            }
            file.delete();
            return true;
        }
        return false;
    }

}
