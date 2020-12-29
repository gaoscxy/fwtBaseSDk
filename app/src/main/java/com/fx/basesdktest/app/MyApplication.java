package com.fx.basesdktest.app;

import android.app.Application;

import com.fx.device.utils.CrashHandler;
import com.fx.device.utils.DeviceInfoUtil;
import com.fx.device.utils.CameraPrinterLog;

import fx.com.aggregate.util.AggregateUtil;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
        CameraPrinterLog.getInstance().init(this);
        DeviceInfoUtil.getInstance().init(this);
        AggregateUtil.init(this);
    }
}
