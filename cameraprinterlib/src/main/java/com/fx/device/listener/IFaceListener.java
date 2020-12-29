package com.fx.device.listener;

public interface IFaceListener {
    void onInitFace(int code, String msg);

    void onFaceCompareTime(long start, long stop);

    void onFaceCompareResult(int score);

    void onFaceCompareError(int code, String msg);

}
