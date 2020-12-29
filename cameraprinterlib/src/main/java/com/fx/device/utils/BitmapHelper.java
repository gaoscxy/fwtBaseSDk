package com.fx.device.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapHelper {
    private static final String TAG = "BitmapHelper";

    public BitmapHelper() {
    }

    public static Bitmap getRotateBitmap(Bitmap bitmap, int rotate) {
        if (bitmap == null) {
            return null;
        } else {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Matrix matrix = new Matrix();
            matrix.setRotate((float) rotate);
            Bitmap newBm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
            if (newBm.equals(bitmap)) {
                return newBm;
            } else {
                bitmap.recycle();
                return newBm;
            }
        }
    }

    public static Bitmap mergeBitmap(Bitmap backBitmap, Bitmap frontBitmap) {
        if (backBitmap != null && !backBitmap.isRecycled() && frontBitmap != null && !frontBitmap.isRecycled()) {
            Bitmap bitmap = backBitmap.copy(Config.ARGB_8888, true);
            Canvas canvas = new Canvas(bitmap);
            Rect baseRect = new Rect(0, 0, backBitmap.getWidth(), backBitmap.getHeight());
            Rect frontRect = new Rect(0, 0, frontBitmap.getWidth(), frontBitmap.getHeight());
            canvas.drawBitmap(frontBitmap, frontRect, baseRect, (Paint) null);
            return bitmap;
        } else {
            Log.e("BitmapHelper", "backBitmap=" + backBitmap + ";frontBitmap=" + frontBitmap);
            return null;
        }
    }

    public static Bitmap mergeBitmap_LR(Bitmap leftBitmap, Bitmap rightBitmap, boolean isBaseMax) {
        if (leftBitmap != null && !leftBitmap.isRecycled() && rightBitmap != null && !rightBitmap.isRecycled()) {
            int height;
            if (isBaseMax) {
                height = leftBitmap.getHeight() > rightBitmap.getHeight() ? leftBitmap.getHeight() : rightBitmap.getHeight();
            } else {
                height = leftBitmap.getHeight() < rightBitmap.getHeight() ? leftBitmap.getHeight() : rightBitmap.getHeight();
            }

            Bitmap tempBitmapL = leftBitmap;
            Bitmap tempBitmapR = rightBitmap;
            if (leftBitmap.getHeight() != height) {
                tempBitmapL = Bitmap.createScaledBitmap(leftBitmap, (int) ((float) leftBitmap.getWidth() * 1.0F / (float) leftBitmap.getHeight() * (float) height), height, false);
            } else if (rightBitmap.getHeight() != height) {
                tempBitmapR = Bitmap.createScaledBitmap(rightBitmap, (int) ((float) rightBitmap.getWidth() * 1.0F / (float) rightBitmap.getHeight() * (float) height), height, false);
            }

            int width = tempBitmapL.getWidth() + tempBitmapR.getWidth();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Rect leftRect = new Rect(0, 0, tempBitmapL.getWidth(), tempBitmapL.getHeight());
            Rect rightRect = new Rect(0, 0, tempBitmapR.getWidth(), tempBitmapR.getHeight());
            Rect rightRectT = new Rect(tempBitmapL.getWidth(), 0, width, height);
            canvas.drawBitmap(tempBitmapL, leftRect, leftRect, (Paint) null);
            canvas.drawBitmap(tempBitmapR, rightRect, rightRectT, (Paint) null);
            return bitmap;
        } else {
            Log.d("BitmapHelper", "leftBitmap=" + leftBitmap + ";rightBitmap=" + rightBitmap);
            return null;
        }
    }

    public static Bitmap mergeBitmap_TB(Bitmap topBitmap, Bitmap bottomBitmap, boolean isBaseMax) {
        if (topBitmap != null && !topBitmap.isRecycled() && bottomBitmap != null && !bottomBitmap.isRecycled()) {
            int width;
            if (isBaseMax) {
                width = topBitmap.getWidth() > bottomBitmap.getWidth() ? topBitmap.getWidth() : bottomBitmap.getWidth();
            } else {
                width = topBitmap.getWidth() < bottomBitmap.getWidth() ? topBitmap.getWidth() : bottomBitmap.getWidth();
            }

            Bitmap tempBitmapT = topBitmap;
            Bitmap tempBitmapB = bottomBitmap;
            if (topBitmap.getWidth() != width) {
                tempBitmapT = Bitmap.createScaledBitmap(topBitmap, width, (int) ((float) topBitmap.getHeight() * 1.0F / (float) topBitmap.getWidth() * (float) width), false);
            } else if (bottomBitmap.getWidth() != width) {
                tempBitmapB = Bitmap.createScaledBitmap(bottomBitmap, width, (int) ((float) bottomBitmap.getHeight() * 1.0F / (float) bottomBitmap.getWidth() * (float) width), false);
            }

            int height = tempBitmapT.getHeight() + tempBitmapB.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Rect topRect = new Rect(0, 0, tempBitmapT.getWidth(), tempBitmapT.getHeight());
            Rect bottomRect = new Rect(0, 0, tempBitmapB.getWidth(), tempBitmapB.getHeight());
            Rect bottomRectT = new Rect(0, tempBitmapT.getHeight(), width, height);
            canvas.drawBitmap(tempBitmapT, topRect, topRect, (Paint) null);
            canvas.drawBitmap(tempBitmapB, bottomRect, bottomRectT, (Paint) null);
            return bitmap;
        } else {
            Log.d("BitmapHelper", "topBitmap=" + topBitmap + ";bottomBitmap=" + bottomBitmap);
            return null;
        }
    }

    public static boolean saveBmpToFile(Bitmap bmp, String path, CompressFormat format, int compress) {
        if (bmp != null && !bmp.isRecycled()) {
            FileOutputStream stream = null;
            boolean isSaveSuccess;
            try {
                File file = new File(path);
                File filePath = file.getParentFile();
                if (!filePath.exists()) {
                    filePath.mkdirs();
                }

                if (!file.exists()) {
                    file.createNewFile();
                }

                stream = new FileOutputStream(path);
                isSaveSuccess = bmp.compress(format, compress, stream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                isSaveSuccess = false;
            } catch (IOException e) {
                e.printStackTrace();
                isSaveSuccess = false;
            } finally {
                if (null != stream) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return isSaveSuccess;
        } else {
            return false;
        }
    }

    public static Bitmap zoomBitmap(Bitmap originBitmap, int newWidth, int newHeight) {
        if (originBitmap == null) {
            return null;
        } else {
            int width = originBitmap.getWidth();
            int height = originBitmap.getHeight();
            float scaleWidth = (float) newWidth / (float) width;
            float scaleHeight = (float) newHeight / (float) height;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            return Bitmap.createBitmap(originBitmap, 0, 0, width, height, matrix, true);
        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, float rotateDegree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotateDegree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }
}

