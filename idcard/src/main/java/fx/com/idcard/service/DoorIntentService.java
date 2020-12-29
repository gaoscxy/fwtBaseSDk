package fx.com.idcard.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import fx.com.idcard.IdCardReader;
import fx.com.idcard.entity.IdCardEntity;
import fx.com.idcard.excep.InitException;
import fx.com.idcard.listener.ReadIDCardListener;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DoorIntentService extends IntentService {
    public static final String CODE = "code";
    private static boolean DoorIntentService_isStop = false;
    public static final String BROADCAST = "DoorIntentService";
    public static final String BROADCAST_PARAM = "DoorIntentService_param";
    public static final String ERROR_MSG = "doorintentservice_error_msg";
    public static final String ERROR_CODE= "doorintentservice_error_code";
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "fx.com.idcard.service.action.FOO";
    private static final String ACTION_BAZ = "fx.com.idcard.service.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "fx.com.idcard.service.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "fx.com.idcard.service.extra.PARAM2";
    private String action;
    private static long delayMillis = 2000;

    public DoorIntentService() {
        super("DoorIntentService");
    }

    public static void setDelayMillis(long delayMillis) {
        DoorIntentService.delayMillis = delayMillis;
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, DoorIntentService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, DoorIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

//    @Override
//    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
//        return START_STICKY;
//    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: 读取身份证
        try {
                IdCardReader.getInstance().readIDCard(new ReadIDCardListener() {
                    @Override
                    public void readIDCardResult(IdCardEntity idCardEntity) {
                        //发送广播
                        if (idCardEntity != null){
                            sendBroadcastMsg(idCardEntity);
                        }
                    }

                    @Override
                    public void readIDCardReeor(int errCode, String errMsg) {
                        sendBroadcastMsg(errCode,errMsg);
                    }
                });
        } catch (InitException e) {
            e.printStackTrace();
            sendBroadcastMsg(-1,e.getMessage());
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: 读取身份证(含指纹的)
        try {
                IdCardReader.getInstance().readIDCard_Fp(new ReadIDCardListener() {
                    @Override
                    public void readIDCardResult(IdCardEntity idCardEntity) {
                        //发送广播
                        if (idCardEntity != null)
                            sendBroadcastMsg(idCardEntity);
                    }

                    @Override
                    public void readIDCardReeor(int errCode, String errMsg) {
                        sendBroadcastMsg(errCode,errMsg);
                    }
                });

        } catch (InitException e) {
            e.printStackTrace();
            sendBroadcastMsg(-1,e.getMessage());
        }
    }

    private void sendBroadcastMsg(int errCode, String errMsg) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(BROADCAST);
        intent.putExtra(ERROR_MSG, errMsg);
        intent.putExtra(ERROR_CODE, errCode);
        intent.putExtra(CODE,0);
        DoorIntentService.this.sendBroadcast(intent);
    }

    private void sendBroadcastMsg(IdCardEntity idCardEntity) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(BROADCAST);
        intent.putExtra(CODE,1);
        intent.putExtra(BROADCAST_PARAM, idCardEntity);
        DoorIntentService.this.sendBroadcast(intent);
    }

    public static void setDoorIntentService_isStop(boolean doorIntentService_isStop) {
        DoorIntentService_isStop = doorIntentService_isStop;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!DoorIntentService_isStop){
            Handler handler = new IntervalHandler(this,action);
            handler.sendEmptyMessageDelayed(0,delayMillis);
        }
    }

    public static class IntervalHandler extends Handler {
        private DoorIntentService doorIntentService;
        private String action;

        public IntervalHandler(DoorIntentService doorIntentService, String action) {
            this.action = action;
            this.doorIntentService = doorIntentService;
        }

        @Override
        public void handleMessage(Message msg) {
            if (ACTION_FOO.equals(action)) {
                startActionFoo(doorIntentService, null, null);
            } else if (ACTION_BAZ.equals(action)) {
                startActionBaz(doorIntentService, null, null);
            }
        }
    }
}
