package com.fx.basesdktest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.fx.basesdktest.R;
import com.fx.device.ServerInterfaceTestActivity;

public class CameraMainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = CameraMainActivity.class.getSimpleName();
    private Button mBtnFace, mBtnHighCamera, mBtnOpenCamera, mBtnTest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_main);
        initView();
    }

    private void initView() {
        mBtnFace = (Button) findViewById(R.id.btn_face);
        mBtnHighCamera = (Button) findViewById(R.id.btn_high_camera);
        mBtnOpenCamera = (Button) findViewById(R.id.btn_opencamera);
        mBtnTest = (Button) findViewById(R.id.btn_test);

        mBtnFace.setOnClickListener(this);
        mBtnHighCamera.setOnClickListener(this);
        mBtnOpenCamera.setOnClickListener(this);
        mBtnTest.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_face:
                startActivity(new Intent(CameraMainActivity.this, DoubleCameraActivity.class));
                break;
            case R.id.btn_high_camera:
                startActivity(new Intent(CameraMainActivity.this, HighCameraActivity.class));
                break;
            case R.id.btn_opencamera:
                startActivity(new Intent(CameraMainActivity.this, MultiCameraActivity.class));
                break;
            case R.id.btn_test:
                startActivity(new Intent(CameraMainActivity.this, ServerInterfaceTestActivity.class));
                break;
        }
    }

}
