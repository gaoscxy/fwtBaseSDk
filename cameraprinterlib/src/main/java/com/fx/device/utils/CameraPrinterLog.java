package com.fx.device.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志采集: 仅针对摄像头和打印机
 */
public class CameraPrinterLog {
    private static final String TAG = CameraPrinterLog.class.getSimpleName();

    //日志存储路径
    private String PATH_LOG;
    private String LOG_FILE_NAME = "CameraPrinterLog.txt";    //日志文件

    private static CameraPrinterLog INSTANCE = new CameraPrinterLog();// CrashHandler实例
    private Context mContext;// 程序的Context对象
    private Map<String, String> info = new HashMap<String, String>();// 用来存储信息
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");// 用于格式化日期,作为日志文件名的一部分

    private StringBuffer sb;

    /**
     * 保证只有一个DeviceLogCollection实例
     */
    private CameraPrinterLog() {

    }

    /**
     * LogCollection ,单例模式
     */
    public static CameraPrinterLog getInstance() {
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
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     */
    public boolean collectDeviceLog(String device, String msg) {
        // 收集参数信息
        collectDeviceInfo(mContext, device, msg);
        // 保存日志文件
        saveInfo2File();
        return true;
    }

    /**
     * 收集参数信息
     *
     * @param context
     */
    private void collectDeviceInfo(Context context, String device, String msg) {
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
                info.put("device", device);
                info.put("msg", msg);
            }

        } catch (Exception ex) {
            ex.printStackTrace();

            String timestamp = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
            info.put("timestamp", timestamp);
            info.put("device", device);

            Writer writer = new StringWriter();
            PrintWriter pw = new PrintWriter(writer);
            ex.printStackTrace(pw);
            Throwable cause = ex.getCause();
            //循环着把所有的异常信息写入writer中
            while (cause != null) {
                cause.printStackTrace(pw);
                cause = cause.getCause();
            }
            pw.close();//记得关闭
            String result = writer.toString();

            info.put("msg", msg + "\r\n" + result);
        }

        /*Field[] fields = Build.class.getDeclaredFields();// 反射机制
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                info.put(field.getName(), field.get("").toString());
                Log.d(TAG, field.getName() + ":" + field.get(""));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }*/
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
        Log.e(TAG, "Log: " + sb.toString());

        //保存文件
//        String timetamp = format.format(new Date(System.currentTimeMillis()));
//        LOG_FILE_NAME = "Log-" + timetamp + ".log";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                File dir = new File(PATH_LOG);
                if (!dir.exists()) dir.mkdir();

                FileOutputStream fos = new FileOutputStream(dir.getAbsolutePath() + File.separator + LOG_FILE_NAME, true);
//                OutputStreamWriter writer=new OutputStreamWriter(fos, StandardCharsets.UTF_8);
//                writer.write(sb.toString());
//                writer.close();
                fos.write(sb.toString().getBytes());
                fos.close();
                return LOG_FILE_NAME;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getDeviceLog() {
        if (sb != null)
            return sb.toString();
        return "";
    }
}
