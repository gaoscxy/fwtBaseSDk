package com.fx.device;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.fx.device.utils.DeviceDAQUtil;

/**
 * 服务器接口测试
 */
public class ServerInterfaceTestActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = ServerInterfaceTestActivity.class.getSimpleName();

    String macStr = "c4:f0:81:8b:e6:ee";
    int hardwareType;
    int useFlag;
    int useStatus;
    String failReson;
    String remark;
    String IDCard = "370725197603096743";//身份证

    private Button mBbtnDoor, mBtnIdcard, mBtnFinger, mBtnFace, mBtnHighCamera, mBtnPrint;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
    }

    private void initView() {
        mBbtnDoor = (Button) findViewById(R.id.btn_door);
        mBtnIdcard = (Button) findViewById(R.id.btn_idcard);
        mBtnFinger = (Button) findViewById(R.id.btn_finger);
        mBtnFace = (Button) findViewById(R.id.btn_face);
        mBtnHighCamera = (Button) findViewById(R.id.btn_high_camera);
        mBtnPrint = (Button) findViewById(R.id.btn_print);

        mBbtnDoor.setOnClickListener(this);
        mBtnIdcard.setOnClickListener(this);
        mBtnFinger.setOnClickListener(this);
        mBtnFace.setOnClickListener(this);
        mBtnHighCamera.setOnClickListener(this);
        mBtnPrint.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_door) {
            Log.e(TAG, "身份证阅读器(门禁)");
            hardwareType = DeviceDAQConfig.DEVICE_DOOR;
            useFlag = DeviceDAQConfig.Flag.START;
            useStatus = DeviceDAQConfig.Status.SUCCESS;
            failReson = "";
        } else if (i == R.id.btn_idcard) {
            Log.e(TAG, "身份证阅读器");
            hardwareType = DeviceDAQConfig.DEVICE_ID_CARD;
            useFlag = DeviceDAQConfig.Flag.START;
            useStatus = DeviceDAQConfig.Status.SUCCESS;
            failReson = "";
        } else if (i == R.id.btn_finger) {
            Log.e(TAG, "指纹");
            hardwareType = DeviceDAQConfig.DEVICE_FINGER;
            useFlag = DeviceDAQConfig.Flag.START;
            useStatus = DeviceDAQConfig.Status.SUCCESS;
            failReson = "";
        } else if (i == R.id.btn_face) {
            Log.e(TAG, "人脸识别");
            hardwareType = DeviceDAQConfig.DEVICE_DOUBLE_CAMERA;
            useFlag = DeviceDAQConfig.Flag.START;
            useStatus = DeviceDAQConfig.Status.SUCCESS;
            failReson = "";
        } else if (i == R.id.btn_high_camera) {
            Log.e(TAG, "高拍仪");
            hardwareType = DeviceDAQConfig.DEVICE_HIGH_CAMERA;
            useFlag = DeviceDAQConfig.Flag.START;
            useStatus = DeviceDAQConfig.Status.SUCCESS;
            failReson = "";
        } else if (i == R.id.btn_print) {
            Log.e(TAG, "打印机");
            hardwareType = DeviceDAQConfig.DEVICE_PRINTER;
            useFlag = DeviceDAQConfig.Flag.START;
            useStatus = DeviceDAQConfig.Status.SUCCESS;
            failReson = "";
        }
        DeviceDAQUtil.sendDeviceData(ServerInterfaceTestActivity.this, macStr, hardwareType, useFlag,
                useStatus, failReson, IDCard);
    }
}
