package com.fingerprinlib.driver.listener;

public interface ImageQualityListener {
    void imageQuality(int qr);

    void imageQualityError(Exception e);
}
