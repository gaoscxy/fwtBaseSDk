package com.fx.basesdktest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fingerprinlib.driver.FPDriver;
import com.fingerprinlib.driver.listener.CompareResultListener;
import com.fingerprinlib.driver.listener.CreateTempListener;
import com.fingerprinlib.driver.listener.OpenDeviceListener;
import com.fingerprinlib.exception.DevOpenExcep;
import com.fingerprinlib.exception.InitFpException;
import com.fx.basesdktest.R;
import com.fx.device.ServerInterfaceTestActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fx.com.aggregate.entity.DeviceStatus;
import fx.com.aggregate.listener.SelfCheckingListener;
import fx.com.aggregate.util.AggregateUtil;
import fx.com.idcard.IdCardReader;
import fx.com.idcard.entity.IdCardEntity;
import fx.com.idcard.excep.InitException;
import fx.com.idcard.listener.ReadIDCardListener;


public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    @BindView(R.id.tv1)
    TextView tv1;
    @BindView(R.id.cycle_btn)
    Button cycleBtn;
    private IdCardEntity idCardEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        tv1.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @OnClick({R.id.cycle_btn, R.id.init_btn, R.id.btn1, R.id.btn2, R.id.clear_tv, R.id.btn_open_camera, R.id.btn_printer, R.id.btn_request_test})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cycle_btn:
                tv1.setText("一键启动开始循环");
                cycleBtn.setEnabled(false);
                cycleBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));

                /*
                //摄像头和打印机检测的Services
                AggregateUtil.getInstance().bindCameraService(MainActivity.this, MainActivity.this, 60 * 60 * 1000, new SelfCheckingListener() {
                    @Override
                    public void onDeviceStatus(List<DeviceStatus> deviceStatuses) {
                        for (DeviceStatus deviceStatus : deviceStatuses)
                            Log.e(TAG, deviceStatus.toString());
                    }
                });*/

                /*AggregateUtil.getInstance().bindService(MainActivity.this, MainActivity.this, 60 * 60 * 1000, new SelfCheckingListener() {
                    @Override
                    public void onDeviceStatus(List<DeviceStatus> deviceStatuses) {
                        String msg = "";
                        for (int i = 0; i < deviceStatuses.size(); i++) {
                            msg += deviceStatuses.get(i).toString() + "\n";
                        }
                        setTextViewData(msg);
                    }
                });*/

                if (AggregateUtil.getInstance() == null)
                    AggregateUtil.init(getApplicationContext());
                AggregateUtil.getInstance().startCheckServer(MainActivity.this, 60 * 60 * 1000, new SelfCheckingListener() {
                    @Override
                    public void onDeviceStatus(List<DeviceStatus> deviceStatuses) {
                        for (DeviceStatus deviceStatus : deviceStatuses) {
                            Log.e(TAG, "onDeviceStatus: " + (deviceStatus.toString() + "\r\n"));
                        }
                    }
                });
                break;
            case R.id.init_btn: //初始化
                init();
                openFpDev();
                break;
            case R.id.btn1: //读取身份证
                getIdCardInfo();
                break;
            case R.id.btn2://比对指纹
                compareFp();
                break;
            case R.id.clear_tv://清除Log
                tv1.setText("");
                break;
            case R.id.btn_open_camera: //打开摄像头
                startActivity(new Intent(MainActivity.this, CameraMainActivity.class));
                break;
            case R.id.btn_printer: //打印机
                startActivity(new Intent(MainActivity.this, PrinterShareActivity.class));
                break;
            case R.id.btn_request_test: //接口测试
                startActivity(new Intent(MainActivity.this, ServerInterfaceTestActivity.class));
                break;
        }
    }

    private void getIdCardInfo() {
        try {
            IdCardReader.getInstance().readIDCard_Fp(new ReadIDCardListener() {

                @Override
                public void readIDCardResult(IdCardEntity idCardEntity) {
                    setTextViewData(idCardEntity.toString());
                    MainActivity.this.idCardEntity = idCardEntity;
                }

                @Override
                public void readIDCardReeor(int errCode, String errMsg) {
                    setTextViewData("错误:" + errCode + "-->" + errMsg);
                }
            });
        } catch (InitException e) {
            e.printStackTrace();
        }
    }

    private void openFpDev() {
        try {
            FPDriver.getInstance().openDevice(new OpenDeviceListener() {
                @Override
                public void openResult(int m_hDevice, String msg) {

                }
            });
        } catch (InitFpException e) {
            e.printStackTrace();
        }
    }

    private void compareFp() {
        try {
            FPDriver.getInstance().createTemp(new CreateTempListener() {
                @Override
                public void createTempResult(byte[] ret, String msg) {
                    //成功
                    setTextViewData("createTemp:-->" + msg);
                    if (idCardEntity == null) {
                        Toast.makeText(MainActivity.this, "请先读取身份证", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    byte[] fpInfo = idCardEntity.getFpInfo();
                    try {
                        FPDriver.getInstance().compareTemps(new CompareResultListener() {
                            @Override
                            public void compareResult(int score) {
                                setTextViewData("分数:-->" + score);
                            }

                            @Override
                            public void compareResultMsg(String s) {
                                setTextViewData("compareResultMsg:-->" + s);
                            }

                            @Override
                            public void compareError(Exception e) {
                                setTextViewData("compareError:-->" + e.getMessage());
                            }

                            @Override
                            public void compareErrorMsg(String msg) {
                                setTextViewData("compareErrorMsg:-->" + msg);
                            }
                        }, fpInfo, ret);
                    } catch (InitFpException e) {
                        e.printStackTrace();
                    } catch (DevOpenExcep devOpenExcep) {
                        devOpenExcep.printStackTrace();
                    }
                }

                @Override
                public void createTempFailure(int ret, String msg) {
                    //失败
                    setTextViewData("createTempFailure:-->" + msg);
                }

                @Override
                public void createTempStart(int ret, String msg) {
                    //手指放上的时候
                    setTextViewData("createTempStart:-->" + msg);
                }
            });

        } catch (InitFpException e) {
            e.printStackTrace();
        } catch (DevOpenExcep devOpenExcep) {
            devOpenExcep.printStackTrace();
        }
    }

    private void init() {
        IdCardReader.getInstance().init(this);
        FPDriver.getInstance().initFp(this);
        setTextViewData("初始化...");
    }

    private void setTextViewData(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StringBuilder stringBuilder = new StringBuilder(tv1.getText().toString());
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                String date = df.format(new Date());// new Date()为获取当前系统时间，也可使用当前时间戳
                stringBuilder.append("\n" + date + "：" + s);
                stringBuilder.append("\n" + "============================================");
                tv1.setText(stringBuilder);
            }
        });
    }


}
