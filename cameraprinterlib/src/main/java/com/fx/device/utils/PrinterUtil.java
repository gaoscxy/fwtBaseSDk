package com.fx.device.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.fx.device.DeviceDAQConfig;
import com.fx.device.listener.IPrinterShareListener;

/**
 * 打印机
 */
public class PrinterUtil {

    public static final String PRINTER_SHARE_PACKAGE = "com.dynamixsoftware.printershare";

    private Context mContext;
    private String mMacStr;

    private static PrinterUtil mInstance;

    private PrinterUtil(Context context) {
        this.mContext = context;
        this.mMacStr = MacUtil.getMac(mContext);
    }

    /**
     * init
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        if (mInstance == null) {
            synchronized (PrinterUtil.class) {
                if (mInstance == null) {
                    mInstance = new PrinterUtil(context);
                }
            }
        }
    }

    /**
     * PrinterUtil。
     *
     * @return PrinterUtil
     */
    public static PrinterUtil getInstance() {
        return mInstance;
    }

    /**
     * 打印
     *
     * @param url    地址
     * @param IDCard 身份证
     */
    public void turn2printer(String url, String IDCard, IPrinterShareListener iPrinterShareListener) {
        String deviceName = DeviceConfig.DeivceType.PRINTER.getName();
        boolean isInstalled;
        String msg;
        if (!isAppInstalled(mContext, PRINTER_SHARE_PACKAGE)) {
            isInstalled = false;
            msg = "not install PrinterShare App";
            DeviceDAQUtil.sendDeviceData(mContext, mMacStr, DeviceDAQConfig.DEVICE_PRINTER,
                    DeviceDAQConfig.Flag.START, DeviceDAQConfig.Status.FAILURE, "请安装打印机应用PrinterShare", IDCard);
        } else {
            isInstalled = true;
            msg = "installed PrinterShare App";
            try {
                ComponentName comp = new ComponentName(PRINTER_SHARE_PACKAGE, PRINTER_SHARE_PACKAGE + ".ActivityWeb");
                Intent intent = new Intent();
                intent.setComponent(comp);
                intent.setAction("android.intent.action.VIEW");
                intent.setType("text/html");
                intent.setData(Uri.parse(url));
                mContext.startActivity(intent);
                iPrinterShareListener.onPrint(deviceName, "启动打印机成功，开始执行打印任务");
                DeviceDAQUtil.sendDeviceData(mContext, mMacStr, DeviceDAQConfig.DEVICE_PRINTER,
                        DeviceDAQConfig.Flag.START, DeviceDAQConfig.Status.SUCCESS, "", IDCard);
            } catch (Exception e) {
                e.printStackTrace();
                iPrinterShareListener.onPrint(deviceName, "启动打印机失败");
                DeviceDAQUtil.sendDeviceData(mContext, mMacStr, DeviceDAQConfig.DEVICE_PRINTER,
                        DeviceDAQConfig.Flag.START, DeviceDAQConfig.Status.FAILURE, "启动打印机失败", IDCard);
            }
        }
        iPrinterShareListener.onIsAppInstalled(deviceName, isInstalled, msg);
    }

    /**
     * 判断app是否安装
     *
     * @param context
     * @param packageName 应用包名
     * @return boolean
     */
    public boolean isAppInstalled(Context context, String packageName) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return packageInfo != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

}
