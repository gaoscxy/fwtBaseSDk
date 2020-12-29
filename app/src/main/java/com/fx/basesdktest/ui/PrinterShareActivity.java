package com.fx.basesdktest.ui;

import android.app.ActivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.fx.basesdktest.R;
import com.fx.device.listener.IPrinterShareListener;
import com.fx.device.utils.CameraPrinterLog;
import com.fx.device.utils.PrinterUtil;

public class PrinterShareActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = PrinterShareActivity.class.getSimpleName();
    private EditText editURL;
    private Button mBtnPrint, mBtnClosePrinter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_share);
        initView();
        PrinterUtil.init(PrinterShareActivity.this);
    }

    private void initView() {
        editURL = (EditText) findViewById(R.id.edit_url);
        editURL.setText("https://www.baidu.com/");
        mBtnPrint = (Button) findViewById(R.id.btn_print);
        mBtnClosePrinter = (Button) findViewById(R.id.btn_printer_close);

        mBtnPrint.setOnClickListener(this);
        mBtnClosePrinter.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_print:
                String url = editURL.getText().toString();
                if (url != null && url.length() >= 0) {
                    PrinterUtil.getInstance().turn2printer(url, "", new IPrinterShareListener() {

                        @Override
                        public void onIsAppInstalled(String device, boolean isInstalled, String msg) {
                            Log.e(TAG, "device: " + device + ", msg: " + msg);
                            CameraPrinterLog.getInstance().collectDeviceLog(device, msg);
                        }

                        @Override
                        public void onPrint(String device, String msg) {
                            Log.e(TAG, "device: " + device + ", msg: " + msg);
                            CameraPrinterLog.getInstance().collectDeviceLog(device, msg);
                        }
                    });
                }
                break;
            case R.id.btn_printer_close:
                finishPrintApp();
                break;
        }
    }

    /**
     * 关闭打印机
     */
    private void finishPrintApp() {
        ActivityManager mAm = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        mAm.killBackgroundProcesses("com.dynamixsoftware.printershare");
    }
}
