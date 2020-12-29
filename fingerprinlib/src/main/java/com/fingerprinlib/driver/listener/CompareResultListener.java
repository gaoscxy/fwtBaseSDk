package com.fingerprinlib.driver.listener;

public interface CompareResultListener {
    void compareResult(int score);//比对分值

    void compareResultMsg(String s);//没有存储指纹的时候直接验证成功

    void compareError(Exception e);//异常信息对象

    void compareErrorMsg(String msg);//异常信息String
}
