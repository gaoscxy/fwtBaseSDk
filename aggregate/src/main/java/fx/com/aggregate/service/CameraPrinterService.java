package fx.com.aggregate.service;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fx.device.camera.CameraInterface;
import com.fx.device.utils.DeviceConfig;
import com.fx.device.utils.PrinterUtil;

import java.util.ArrayList;
import java.util.List;

import fx.com.aggregate.entity.DeviceStatus;
import fx.com.aggregate.listener.SelfCheckingListener;
import fx.com.aggregate.util.MacUtil;

/**
 * 设备后台检测（双目摄像头、高拍仪、音视频摄像头、打印机）
 */
public class CameraPrinterService extends Service {
    private final String TAG = CameraPrinterService.class.getSimpleName();

    protected Handler handler;
    private Runnable timerRunnable;
    private long DEFAULT_DELAY_MILLIS = 60 * 60 * 1000;
    private long mDelayMillis = DEFAULT_DELAY_MILLIS;

    private String MAC = "";

    private SelfCheckingListener mSelfCheckingListener;

    private List<DeviceStatus> deviceStatuses = new ArrayList<>();

    private IBinder binder = null;

    public class MyBinder extends Binder {
        public CameraPrinterService getService() {
            return CameraPrinterService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind");
//        if (intent != null)
//            mDelayMillis = intent.getLongExtra("delayMillis", DEFAULT_DELAY_MILLIS);
        return binder;
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        binder = new MyBinder();
    }

    public void startTimer(long delayMillis) {
        if (delayMillis <= 0) mDelayMillis = DEFAULT_DELAY_MILLIS;
        else mDelayMillis = delayMillis;
        MAC = MacUtil.getMac(getApplicationContext());
        handler = new Handler();
        timerRunnable = new TimerRunable();
        handler.postDelayed(timerRunnable, 0);
    }

    /**
     * 定时任务
     */
    private class TimerRunable implements Runnable {
        @Override
        public void run() {
            deviceStatuses.clear();
            checkPrinter();
            checkCamera();

            if (mSelfCheckingListener != null) {
                mSelfCheckingListener.onDeviceStatus(deviceStatuses);
            }

            handler.postDelayed(timerRunnable, mDelayMillis);
        }

    }

    private void checkPrinter() {
        int deviceId = DeviceConfig.DeivceType.PRINTER.getValue();
        String deviceName = DeviceConfig.DeivceType.PRINTER.getName();
        boolean status;
        String msg;
        if (PrinterUtil.getInstance().isAppInstalled(getApplicationContext(), PrinterUtil.PRINTER_SHARE_PACKAGE)) {
            status = DeviceConfig.Status.SUCCESS;
            msg = "PrinterShare App is installed";
        } else {
            status = DeviceConfig.Status.FAILURE;
            msg = "PrinterShare App is not installed";
        }
        Log.e(TAG, msg);
        addDeivceStatusData(deviceId, deviceName, status, msg);
    }

    private void checkCamera() {
        if (CameraInterface.getInstance() == null)
            CameraInterface.init(getApplicationContext());
        CameraInterface.getInstance().setICameraListener(new CameraInterface.ICameraListener() {
            @Override
            public void onCameraInit(DeviceConfig.DeivceType deivceType, boolean status, String msg) {
                Log.e(TAG, "onCameraInit >>>> device: " + deivceType.getName() + ", msg: " + msg);
                addDeivceStatusData(deivceType.getValue(), deivceType.getName(), status, msg);
            }

            @Override
            public void onCameraOpen(DeviceConfig.DeivceType deivceType, boolean status, String msg) {
                Log.e(TAG, "onCameraOpen >>>> device: " + deivceType.getName() + ", msg: " + msg);
            }
        });
        CameraInterface.getInstance().cameraInit();
    }

    private void addDeivceStatusData(int deviceId, String deviceName, boolean status, String msg) {
        DeviceStatus data = new DeviceStatus();
        data.setMac(MAC);
        data.setDevice(deviceId);
        data.setDeviceName(deviceName);
        data.setMsg(msg);
        data.setStatus(status);
        data.setTimestamp(System.currentTimeMillis());
        deviceStatuses.add(data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        if (handler != null && timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }

    public void setCheckListener(SelfCheckingListener selfCheckingListener) {
        this.mSelfCheckingListener = selfCheckingListener;
    }
}
