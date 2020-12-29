package com.fx.device.listener;

public interface IPrinterShareListener {
    void onIsAppInstalled(String device, boolean isInstalled, String msg);

    void onPrint(String device, String msg);
}
