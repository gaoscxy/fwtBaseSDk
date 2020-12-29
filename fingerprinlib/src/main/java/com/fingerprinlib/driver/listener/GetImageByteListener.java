package com.fingerprinlib.driver.listener;

public abstract class GetImageByteListener {
    public void errorByte(int code, String err) {
        //-1代表是失败了
    }


    public void succeedByte(int aTrue, String msg, int width, int height, byte[] m_image) {
        //aTrue=1代表是成功了
    }
}
