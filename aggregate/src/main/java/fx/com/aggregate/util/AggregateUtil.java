package fx.com.aggregate.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.fx.device.camera.CameraInterface;
import com.fx.device.utils.CameraPrinterLog;
import com.fx.device.utils.CrashHandler;
import com.fx.device.utils.DeviceInfoUtil;
import com.fx.device.utils.PrinterUtil;
import com.fx.device.utils.StorageUtil;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import fx.com.aggregate.entity.DeviceStatus;
import fx.com.aggregate.listener.SelfCheckingListener;
import fx.com.aggregate.mode.AidlParam;
import fx.com.aggregate.service.CameraPrinterService;
import fx.com.aggregate.service.JCIntentService;
import fx.com.aggregate.service.SelfCheckingService;

public class AggregateUtil {
    private static final String TAG = AggregateUtil.class.getSimpleName();

    private static String PATH_LOG ; //日志存储根目录
    private static String PATH_LOG_FUWUTING ; //设备自检日志存储目录

    private static SelfCheckingService selfCheckingService;
    private static CameraPrinterService cameraPrinterService;

    private static AggregateUtil mInstance;

    private AggregateUtil() {
    }

    /**
     * init
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        if (mInstance == null) {
            synchronized (AggregateUtil.class) {
                if (mInstance == null) {
                    mInstance = new AggregateUtil();
                    PATH_LOG = StorageUtil.getSDKLogRootPath(context);
                    PATH_LOG_FUWUTING = StorageUtil.getCheckLogRootPath(context);
                }
            }
        }
    }

    public static AggregateUtil getInstance() {
        return mInstance;
    }

    @Deprecated
    public void bindCameraService(@NonNull Context context, final long delayMillis,
                                  @NonNull final SelfCheckingListener checkingListener) {
        initUtil(context);
        ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.e(TAG, "onServiceConnected");
                cameraPrinterService = ((CameraPrinterService.MyBinder) binder).getService();
                cameraPrinterService.setCheckListener(checkingListener);
                cameraPrinterService.startTimer(delayMillis);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e(TAG, "onServiceDisconnected");
                cameraPrinterService = null;
            }
        };
        Intent intent = new Intent(context, CameraPrinterService.class);
        context.getApplicationContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    @Deprecated
    public void bindService(@NonNull final Activity activity, @NonNull Context context,
                            final long delayMillis,
                            @NonNull final SelfCheckingListener checkingListener) {
        Logger.addLogAdapter(new DiskLogAdapter());
        initUtil(context);
        ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.e(TAG, "onServiceConnected");
                selfCheckingService = ((SelfCheckingService.MyBinder) binder).getService();
                selfCheckingService.setPamas(activity, delayMillis, checkingListener);
                selfCheckingService.startCheckDevice();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e(TAG, "onServiceDisconnected");
                selfCheckingService = null;
            }
        };
        Intent intent = new Intent(context, SelfCheckingService.class);
        context.getApplicationContext().bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    public void startCheckServer(@NonNull final Activity activity, long delayMillis, @NonNull SelfCheckingListener checkingListener) {
        Logger.addLogAdapter(new DiskLogAdapter());
        initUtil(activity);
        final AidlParam param = new AidlParam();
        param.setActivity(activity);
        if (delayMillis < 10 * 1000)
            param.setDelayMillis(10 * 1000);
        else
            param.setDelayMillis(delayMillis);
        param.setmCheckingListener(checkingListener);
        param.setMacAddr(MacUtil.getMac(activity));
        startBind(activity, param);
    }

    private void startBind(final Activity activity, final AidlParam param) {
        Intent intent = new Intent(activity, JCIntentService.class);
        activity.getApplicationContext().bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                String date = getDateTime();
                Logger.d(date + "--->自检服务开启");
                JCIntentService.JcSerIBinder jcBinder = (JCIntentService.JcSerIBinder) binder;
                jcBinder.setParameter(param);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                String date = getDateTime();
                Logger.d(date + "--->自检服务断开");
                startBind(activity, param);
            }
        }, Context.BIND_AUTO_CREATE);
    }

    public String getDateTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(new Date());
    }


    private void initUtil(@NonNull Context context) {
        CrashHandler.getInstance().init(context.getApplicationContext());
        CameraPrinterLog.getInstance().init(context.getApplicationContext());
        PrinterUtil.init(context.getApplicationContext());
        CameraInterface.init(context.getApplicationContext());
        DeviceInfoUtil.getInstance().init(context.getApplicationContext());
    }

    public void saveFile(final List<DeviceStatus> deviceStatuses) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msg = "====";
                if (deviceStatuses != null) {
                    for (int i = 0; i < deviceStatuses.size(); i++) {
                        msg += deviceStatuses.get(i).toString() + "\n";
                    }
                }
                Logger.d(getDateTime() + "->" + msg);
                saveMsgToFile(PATH_LOG_FUWUTING, getDateTime() + "->\n" + msg);
            }
        }).start();
    }

    /**
     * 追加文件：使用FileOutputStream，在构造FileOutputStream时，把第二个参数设为true
     *
     * @param file
     * @param conent
     */
    private void saveMsgToFile(String file, String conent) {
        BufferedWriter out = null;
        try {
            File dir = new File(PATH_LOG);
            if (!dir.exists()) dir.mkdir();

            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            out.write(conent);
        } catch (Exception e) {
            Logger.d(getDateTime() + "->" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (null != out)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
                Logger.d(getDateTime() + "->" + e.getMessage());
            }
        }
    }
}