package com.fx.basesdktest.ui;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fx.basesdktest.R;
import com.serenegiant.helper.UvcCameraHelper;
import com.serenegiant.widget.CameraViewInterface;

import java.lang.ref.WeakReference;

public class UvcFragment extends Fragment {

    private static final String TAG = "UvcFragment";

    private CameraViewInterface mCameraViewInterface;

    private UvcCameraHelper mHelper;

    public static UvcFragment getInstance() {
        return new UvcFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmet_preview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCameraViewInterface = (CameraViewInterface) view.findViewById(R.id.preview_content);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initCameraHelper();
    }

    @Override
    public void onStart() {
        super.onStart();
        mHelper.workOnStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mHelper == null || mHelper.getUvcCameraHandler() == null || mHelper.getUsbMonitor() == null) {
            initCameraHelper();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mHelper != null) {
                    try {
                        Thread.sleep(100);
                        mHelper.openCamera(62333);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mHelper.workOnStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHelper.workOnDestroy();
    }

    private void initCameraHelper() {
        mHelper = UvcCameraHelper.getInstance(new WeakReference<Activity>(getActivity()),
                new WeakReference<Context>(getActivity()),
                mCameraViewInterface, 1, 1);

        mHelper.initUvcCamera();
        mHelper.setConnectedListener(new UvcCameraHelper.onDeviceConnectedListener() {

            @Override
            public void onConnected(final boolean isOpen) {
                if (isOpen) {
                    Log.d(TAG, "onPrint: " + isOpen);
                } else {
                    Log.d(TAG, "onPrint: " + isOpen);
                }
            }

            @Override
            public void onAttached(UsbDevice usbDevice) {

            }
        });
    }

}
