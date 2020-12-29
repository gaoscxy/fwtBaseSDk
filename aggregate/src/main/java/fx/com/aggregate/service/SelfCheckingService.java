package fx.com.aggregate.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fx.device.camera.CameraInterface;
import com.fingerprinlib.driver.FPDriver;
import com.fingerprinlib.driver.listener.OpenDeviceListener;
import com.fingerprinlib.exception.InitFpException;
import com.fx.device.utils.DeviceConfig;
import com.fx.device.utils.PrinterUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Vector;

import fx.com.aggregate.entity.DeviceStatus;
import fx.com.aggregate.listener.SelfCheckingListener;
import fx.com.aggregate.util.AggregateUtil;
import fx.com.aggregate.util.MacUtil;
import fx.com.idcard.IdCardReader;
import fx.com.idcard.excep.InitExceptionListener;

@Deprecated
public class SelfCheckingService extends Service {
    private final String TAG = SelfCheckingService.class.getSimpleName();

    private SelfCheckingListener mCheckingListener;
    private Activity mActivity;
    private long mSelfCheckingTime = 10000;

    protected static Handler handler;
    private Runnable timerRunnable;

    private volatile List<DeviceStatus> mDataList = new Vector<>();

    private Thread thread;

    private IBinder binder = null;

    private String MAC = "";

    public void setPamas(Activity activity, long selfCheckingTime, SelfCheckingListener checkingListener) {
        this.mActivity = activity;
        this.mSelfCheckingTime = selfCheckingTime;
        this.mCheckingListener = checkingListener;
        MAC = MacUtil.getMac(activity.getApplicationContext());
        FPDriver.getInstance().initFp(mActivity);
    }

    /**
     * 定时任务
     */
    private class TimerRunable implements Runnable {
        @SuppressLint("StaticFieldLeak")
        @Override
        public void run() {
            Log.e(TAG, "TimerRunable >>>> thread: " + Thread.currentThread().getName());

            (new AsyncTask<Void, Void, Void>() {

                @Override
                protected void onPreExecute() {
                    Log.e(TAG, "onPreExecute >>>> thread: " + Thread.currentThread().getName());
                    mDataList.clear();
                    initIDCard();
                    super.onPreExecute();
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    Log.e(TAG, "doInBackground >>>> thread: " + Thread.currentThread().getName());
                    openFpDev();
                    checkPrinter();
                    checkCamera();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    if (mCheckingListener != null) {
                        mCheckingListener.onDeviceStatus(mDataList);
                    }

                    try {
                        AggregateUtil.getInstance().saveFile(mDataList);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    handler.postDelayed(timerRunnable, mSelfCheckingTime);
                    super.onPostExecute(aVoid);
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public class MyBinder extends Binder {
        public SelfCheckingService getService() {
            return SelfCheckingService.this;
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
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        binder = new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void startCheckDevice() {
        Log.e(TAG, "startCheckDevice");
        handler = new Handler();
        timerRunnable = new TimerRunable();
        handler.postDelayed(timerRunnable, 0);

    }

    /**
     * 初始化身份证
     */
    private void initIDCard() {
        Log.e(TAG, "initDev");
        final int deviceId = DeviceConfig.DeivceType.ID_CARD.getValue();
        final String deviceName = DeviceConfig.DeivceType.ID_CARD.getName();
        IdCardReader.getInstance().init(mActivity, new InitExceptionListener() {
            @Override
            public void erroe(String msg) {
                setTypeData(deviceId, deviceName, DeviceConfig.Status.FAILURE, msg);
            }

            @Override
            public void success() {
                setTypeData(deviceId, deviceName, DeviceConfig.Status.SUCCESS, "身份证设备正常");
            }
        });
    }

    /**
     * 打开指纹
     */
    private void openFpDev() {
        Log.e(TAG, "openFpDev");
        final int deviceId = DeviceConfig.DeivceType.FINGER.getValue();
        final String deviceName = DeviceConfig.DeivceType.FINGER.getName();
        try {
            FPDriver.getInstance().openDevice(new OpenDeviceListener() {
                @Override
                public void openResult(int m_hDevice, String msg) {
                    boolean theSuccessOf = false;
                    if (m_hDevice != 0) {
                        theSuccessOf = true;
                    }
                    setTypeData(deviceId, deviceName, theSuccessOf, msg);
                }
            });
        } catch (InitFpException e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String exceptionStr = "\r\n" + sw.toString() + "\r\n";
            setTypeData(deviceId, deviceName, false, exceptionStr);
        }
    }

    /**
     * 打印机检测
     */
    private void checkPrinter() {
        Log.e(TAG, "checkPrinter");
        int deviceId = DeviceConfig.DeivceType.PRINTER.getValue();
        String deviceName = DeviceConfig.DeivceType.PRINTER.getName();
        boolean status;
        String msg;
        if (PrinterUtil.getInstance().isAppInstalled(getApplicationContext(), PrinterUtil.PRINTER_SHARE_PACKAGE)) {
            status = DeviceConfig.Status.SUCCESS;
            msg = "PrinterShare App is installed";
        } else {
            Log.e(TAG, "PrinterShare App is not installed");
            status = DeviceConfig.Status.FAILURE;
            msg = "PrinterShare App is not installed";
        }
        Log.e(TAG, msg);
        setTypeData(deviceId, deviceName, status, msg);
    }

    /**
     * Camera检测
     */
    private void checkCamera() {
        Log.e(TAG, "checkCamera");
        if (CameraInterface.getInstance() == null)
            CameraInterface.init(getApplicationContext());
        CameraInterface.getInstance().setICameraListener(new CameraInterface.ICameraListener() {
            @Override
            public void onCameraInit(DeviceConfig.DeivceType deivceType, boolean status, String msg) {
                Log.e(TAG, "onCameraInit >>>> device: " + deivceType.getName() + ", msg: " + msg);
                setTypeData(deivceType.getValue(), deivceType.getName(), status, msg);
            }

            @Override
            public void onCameraOpen(DeviceConfig.DeivceType deivceType, boolean status, String msg) {
                Log.e(TAG, "onCameraOpen >>>> device: " + deivceType.getName() + ", msg: " + msg);
            }
        });
        CameraInterface.getInstance().cameraInit();
    }

    private void setTypeData(int deviceId, String deviceName, boolean status, String msg) {
        DeviceStatus data = new DeviceStatus();
        data.setMac(MAC);
        data.setDevice(deviceId);
        data.setDeviceName(deviceName);
        data.setMsg(msg);
        data.setStatus(status);
        data.setTimestamp(System.currentTimeMillis());
        mDataList.add(data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        if (handler != null && timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }

}
