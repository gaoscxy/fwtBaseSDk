package com.fingerprinlib.driver.listener;

import android.graphics.Bitmap;

public abstract class GetImageBitmapListener {

    public void errorBitmap(int code, String err) {
        //-1代表是失败了
    }


    public void succeedBitmap(int aTrue, String msg,Bitmap bitmap) {
        //aTrue=1代表是成功了
    }
}
