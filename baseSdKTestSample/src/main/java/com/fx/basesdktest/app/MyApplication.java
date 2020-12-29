package com.fx.basesdktest.app;

import android.app.Application;
import android.os.Environment;

import com.fx.device.utils.CameraPrinterLog;
import com.fx.device.utils.CrashHandler;
import com.fx.device.utils.DeviceInfoUtil;

import java.io.File;

import fx.com.aggregate.util.AggregateUtil;

public class MyApplication extends Application {

    private String PATH_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    //日志存储路径
    private String PATH_LOG = PATH_ROOT + File.separator + "sdkLog";
    //crash日志存储路径
    private String PATH_LOG_CRASH = PATH_LOG + File.separator + "crash";

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
        CameraPrinterLog.getInstance().init(this);
        DeviceInfoUtil.getInstance().init(this);
        AggregateUtil.init(this);
    }
}
