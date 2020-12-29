package com.fx.basesdktest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.fx.basesdktest.R;
import com.fx.device.camera.Constants;

public class MultiCameraActivity extends AppCompatActivity {

    private final String TAG = MultiCameraActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_camera);
    }

    /**
     * 双目(彩色)摄像头
     *
     * @param view
     */
    public void onOpenDoubleCamera(View view) {
        Intent intent = new Intent(MultiCameraActivity.this, OpenCameraActivity.class);
        intent.putExtra("cameraFlag", Constants.CAMERA_FLAG_DOUBLE_COLOR);
        startActivity(intent);
    }

    /**
     * 视频摄像头
     *
     * @param view
     */
    public void onOpenVoIPCamera(View view) {
        Intent intent = new Intent(MultiCameraActivity.this, OpenCameraActivity.class);
        intent.putExtra("cameraFlag", Constants.CAMERA_FLAG_VOIP);
        startActivity(intent);
    }

    /**
     * 高拍仪
     *
     * @param view
     */
    public void onOpenHighCamera(View view) {
        Intent intent = new Intent(MultiCameraActivity.this, OpenCameraActivity.class);
        intent.putExtra("cameraFlag", Constants.CAMERA_FLAG_HIGH);
        startActivity(intent);
    }
}
