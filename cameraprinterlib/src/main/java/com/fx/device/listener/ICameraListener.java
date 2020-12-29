package com.fx.device.listener;

import android.hardware.usb.UsbDevice;

public interface ICameraListener {

    void onOpenCamera(String device, boolean isOpen, int code, String msg);

    void onConnected(String device, boolean isConnected, int code, String msg);

    void onAttached(UsbDevice usbDevice);
}
