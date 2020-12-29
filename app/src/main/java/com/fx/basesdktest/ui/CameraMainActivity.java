package com.fx.basesdktest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.fx.basesdktest.R;
import com.fx.device.camera.Constants;

public class CameraMainActivity extends AppCompatActivity {

    private final String TAG = CameraMainActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_main);
    }

    /**
     * 使用UVC API打开摄像头
     * 双目摄像头
     *
     * @param view
     */
    public void onOpenDoubleCamera(View view) {
        startActivity(new Intent(CameraMainActivity.this, DoubleCameraActivity.class));
    }

    /**
     * 使用UVC API打开摄像头
     * 高拍仪
     *
     * @param view
     */
    public void onOpenHighCamera(View view) {
        startActivity(new Intent(CameraMainActivity.this, HighCameraActivity.class));
    }

    /**
     * 使用Android Camera API打开摄像头
     * 双目摄像头
     *
     * @param view
     */
    public void onOpenDoubleCamera2(View view) {
        Intent intent = new Intent(CameraMainActivity.this, OpenCameraActivity.class);
        intent.putExtra("cameraFlag", Constants.CAMERA_FLAG_DOUBLE_COLOR);
        startActivity(intent);
    }

    /**
     * 使用Android Camera API打开摄像头
     * 高拍仪
     *
     * @param view
     */
    public void onOpenHighCamera2(View view) {
        Intent intent = new Intent(CameraMainActivity.this, OpenCameraActivity.class);
        intent.putExtra("cameraFlag", Constants.CAMERA_FLAG_HIGH);
        startActivity(intent);
    }

    /**
     * 使用Android Camera API打开摄像头
     * 视频摄像头
     *
     * @param view
     */
    public void onOpenCamera(View view) {
        Intent intent = new Intent(CameraMainActivity.this, OpenCameraActivity.class);
        intent.putExtra("cameraFlag", Constants.CAMERA_FLAG_VOIP);
        startActivity(intent);
    }

}
