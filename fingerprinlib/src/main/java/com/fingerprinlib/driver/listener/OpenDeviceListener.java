package com.fingerprinlib.driver.listener;

public interface OpenDeviceListener {
    /**
     *
     * @param m_hDevice 0代表是打开失败
     * @param msg
     */
    void openResult(int m_hDevice, String msg);
}
