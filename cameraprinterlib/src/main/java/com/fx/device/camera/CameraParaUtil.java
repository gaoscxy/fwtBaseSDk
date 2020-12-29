package com.fx.device.camera;

import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class CameraParaUtil {
    private final String TAG = "CameraParaUtil";

    public CameraParaUtil.CameraSizeComparator mComparator = new CameraParaUtil.CameraSizeComparator();
    public static CameraParaUtil mInstance;

    public CameraParaUtil() {
    }

    public static CameraParaUtil getInstance() {
        if (mInstance == null) {
            mInstance = new CameraParaUtil();
            return mInstance;
        } else {
            return mInstance;
        }
    }

    public void printSupportPictureSize(Parameters paras) {
        List<Size> pictureSizes = paras.getSupportedPictureSizes();
        Iterator iterator = pictureSizes.iterator();

        while (iterator.hasNext()) {
            Size size = (Size) iterator.next();
            Log.d(TAG, "SupportPictureSize: 宽:" + size.width + " 高:" + size.height);
        }

    }

    public void printSupportPreviewSize(Parameters paras) {
        List<Size> previewSizes = paras.getSupportedPreviewSizes();
        Iterator iterator = previewSizes.iterator();

        while (iterator.hasNext()) {
            Size size = (Size) iterator.next();
            Log.d(TAG, "SupportPreviewSize: 宽:" + size.width + "高:" + size.height);
        }

    }

    public void printSupportMode(Parameters paras) {
        List<String> modes = paras.getSupportedFocusModes();
        Iterator iterator = modes.iterator();

        while (iterator.hasNext()) {
            String str = (String) iterator.next();
            Log.d(TAG, "SupportMode: " + str);
        }

    }

    public Size getSupportSize(List<Size> list, float th, int minWidth, String tag) {
        Collections.sort(list, this.mComparator);
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            Size s = (Size) iterator.next();
            Log.d(TAG, tag + s.width + "|" + s.height);
        }

        int i = 0;

        for (Iterator iterator2 = list.iterator(); iterator2.hasNext(); ++i) {
            Size s = (Size) iterator2.next();
            if (s.width >= minWidth && this.equalRate(s, th)) {
                Log.d(TAG, "getSupportSize: " + tag + "宽 :" + s.width + "高:" + s.height);
                break;
            }
        }

        if (i == list.size()) {
            i = 0;
            Log.d(TAG, "getSupportSize: 0");
        }

        return (Size) list.get(i);
    }

    public String getSupportSize(Parameters paras) {
        String cameraSupportedPreviewSizes = "";
        List<Size> previewSizes = paras.getSupportedPreviewSizes();
        Collections.sort(previewSizes, this.mComparator);

        Size s;
        for (Iterator iterator = previewSizes.iterator(); iterator.hasNext(); cameraSupportedPreviewSizes = cameraSupportedPreviewSizes + s.width + "x" + s.height + " ") {
            s = (Size) iterator.next();
        }

        return cameraSupportedPreviewSizes;
    }

    public boolean equalRate(Size s, float rate) {
        float r = (float) s.width / (float) s.height;
        return (double) Math.abs(r - rate) <= 0.03D;
    }

    public class CameraSizeComparator implements Comparator<Size> {
        public CameraSizeComparator() {
        }

        public int compare(Size s1, Size s2) {
            if (s1.width > s2.width) {
                return 1;
            } else {
                return s1.width < s2.width ? -1 : 0;
            }
        }
    }
}
