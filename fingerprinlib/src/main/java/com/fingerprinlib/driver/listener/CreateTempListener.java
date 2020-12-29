package com.fingerprinlib.driver.listener;

public interface CreateTempListener {
    void createTempResult(byte[] ret, String msg);//成功结果

    void createTempFailure(int ret, String msg);//失败

    void createTempStart(int ret, String msg);//开始错误信息
}
