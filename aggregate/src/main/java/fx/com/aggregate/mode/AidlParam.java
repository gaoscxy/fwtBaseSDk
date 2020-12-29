package fx.com.aggregate.mode;

import android.app.Activity;

import java.io.Serializable;

import fx.com.aggregate.listener.SelfCheckingListener;

public class AidlParam implements Serializable {
    private Activity activity;
    private SelfCheckingListener mCheckingListener;
    private long delayMillis;
    private String macAddr;

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public SelfCheckingListener getmCheckingListener() {
        return mCheckingListener;
    }

    public void setmCheckingListener(SelfCheckingListener mCheckingListener) {
        this.mCheckingListener = mCheckingListener;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    public void setDelayMillis(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public String getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(String macAddr) {
        this.macAddr = macAddr;
    }

    @Override
    public String toString() {
        return "AidlParam{" +
                "activity=" + activity +
                ", mCheckingListener=" + mCheckingListener +
                ", delayMillis=" + delayMillis +
                ", macAddr='" + macAddr + '\'' +
                '}';
    }
}
