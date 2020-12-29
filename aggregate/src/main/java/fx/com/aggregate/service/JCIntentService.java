package fx.com.aggregate.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.fingerprinlib.driver.FPDriver;
import com.fingerprinlib.driver.listener.OpenDeviceListener;
import com.fingerprinlib.exception.InitFpException;
import com.fx.device.camera.CameraInterface;
import com.fx.device.utils.DeviceConfig;
import com.fx.device.utils.PrinterUtil;
import com.orhanobut.logger.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Vector;

import fx.com.aggregate.entity.DeviceStatus;
import fx.com.aggregate.listener.SelfCheckingListener;
import fx.com.aggregate.mode.AidlParam;
import fx.com.aggregate.util.AggregateUtil;
import fx.com.idcard.IdCardReader;
import fx.com.idcard.excep.InitExceptionListener;

/**
 * 设备后台检测（身份证、指纹、双目摄像头、高拍仪、视频摄像头、打印机）
 */
public class JCIntentService extends Service {
    private static String TAG = "JCIntentService->";

//    private static Timer timer;

    /**
     * 设备状态检测数据
     */
    private volatile List<DeviceStatus> mDataList = new Vector<>();

    private AidlParam param;
    private static Handler handler;
    private TimerRunable timeRun;

    public static class JcSerIBinder extends Binder {
        private final JCIntentService service;

        JcSerIBinder(JCIntentService jcIntentService) {
            this.service = jcIntentService;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        public void setParameter(AidlParam param) {
            service.param = param;
            service.handleActionBaz(param.getActivity(), param.getmCheckingListener());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void handleActionBaz(final Activity mActivity,
                                 final SelfCheckingListener mCheckingListener) {
        startNotfication();
        FPDriver.getInstance().initFp(param.getActivity());
        handler = new Handler();
        timeRun = new TimerRunable(JCIntentService.this, mActivity, mCheckingListener);
        handler.postDelayed(timeRun, 0);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new JcSerIBinder(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void startNotfication() {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, param.getActivity().getClass());
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                .setContentTitle("检测中") // 设置下拉列表里的标题
                .setContentText("检测中") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        startForeground(110, notification);// 开始前台服务
    }

    @Override
    public void onDestroy() {
        String date = AggregateUtil.getInstance().getDateTime();
        Logger.d(date + "--->自检服务销毁");
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        if (param != null) {
            try {
                if (AggregateUtil.getInstance() == null)
                    AggregateUtil.init(param.getActivity().getApplicationContext());
                AggregateUtil.getInstance().startCheckServer(param.getActivity(),
                        param.getDelayMillis(), param.getmCheckingListener());
            } catch (Exception e) {
                e.printStackTrace();
                Logger.d(date + "--->自检服务销毁错误" + "\n" + e.getMessage() + "======");
            }

        }
        super.onDestroy();
    }

    static class JcAsyncTask extends AsyncTask<Void, Void, Void> {

        @SuppressLint("StaticFieldLeak")
        private final JCIntentService jcIntentService;
        @SuppressLint("StaticFieldLeak")
        private final Activity mActivity;
        private final SelfCheckingListener mCheckingListener;

        JcAsyncTask(JCIntentService jcIntentService, Activity mActivity, SelfCheckingListener mCheckingListener) {
            this.jcIntentService = jcIntentService;
            this.mActivity = mActivity;
            this.mCheckingListener = mCheckingListener;
        }

        @Override
        protected void onPreExecute() {
            Log.e(TAG, "onPreExecute1 >>>> thread: " + Thread.currentThread().getName());
            jcIntentService.mDataList.clear();
            jcIntentService.initIDCard(mActivity);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.e(TAG, "doInBackground >>>> thread: " + Thread.currentThread().getName());
            jcIntentService.openFpDev();
            jcIntentService.checkPrinter(mActivity);
            jcIntentService.checkCamera();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mCheckingListener != null) {
                mCheckingListener.onDeviceStatus(jcIntentService.mDataList);
            }

            try {
                AggregateUtil.getInstance().saveFile(jcIntentService.mDataList);
            }catch (Exception e){
                e.printStackTrace();
            }

            handler.postDelayed(jcIntentService.timeRun, jcIntentService.param.getDelayMillis());
            super.onPostExecute(aVoid);
        }
    }

    static class TimerRunable implements Runnable {

        private final JCIntentService jcIntentService;
        private final Activity mActivity;
        private final SelfCheckingListener mCheckingListener;

        TimerRunable(JCIntentService jcIntentService, Activity mActivity, SelfCheckingListener mCheckingListener) {
            this.jcIntentService = jcIntentService;
            this.mActivity = mActivity;
            this.mCheckingListener = mCheckingListener;
        }

        @Override
        public void run() {
            JcAsyncTask asyncTask = new JcAsyncTask(jcIntentService, mActivity, mCheckingListener);
            asyncTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
    }

    /**
     * 初始化身份证
     *
     * @param mActivity
     */
    private void initIDCard(Activity mActivity) {
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
    private void checkPrinter(Activity mActivity) {
        Log.e(TAG, "checkPrinter");
        Log.e(TAG, "------------PrinterShare App is installed== " + mActivity);
        final int deviceId = DeviceConfig.DeivceType.PRINTER.getValue();
        final String deviceName = DeviceConfig.DeivceType.PRINTER.getName();
        boolean status;
        String msg;
        if (PrinterUtil.getInstance().isAppInstalled(mActivity, PrinterUtil.PRINTER_SHARE_PACKAGE)) {
            status = DeviceConfig.Status.SUCCESS;
            msg = "PrinterShare App is installed";
        } else {
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
        data.setMac(param.getMacAddr());
        data.setDevice(deviceId);
        data.setDeviceName(deviceName);
        data.setMsg(msg);
        data.setStatus(status);
        data.setTimestamp(System.currentTimeMillis());
        mDataList.add(data);
    }
}
