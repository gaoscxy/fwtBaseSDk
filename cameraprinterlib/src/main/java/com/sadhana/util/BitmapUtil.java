package com.sadhana.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class BitmapUtil {

    private static final String TAG = "BitmapUtils";

    // 图片按比例大小压缩方法（根据路径获取图片并压缩）：
    public static Bitmap getImageByPath(String srcPath) {
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        BitmapFactory.Options newOpts = new BitmapFactory.Options();

        newOpts.inJustDecodeBounds = true;
        // 打开图片获取分辨率
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        newOpts.inJustDecodeBounds = false;
        // 传过来图片分辨率的宽度和高度
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;

        // 这里设置高度为800f
        // 这里设置宽度为480f
        float hh = 320.0F;
        float ww = 480.0F;

        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        // be=1表示不缩放
        int be = 1;
        // 如果宽度大的话根据宽度固定大小缩放
        if ((w > h) && (w > ww))
            be = (int) (newOpts.outWidth / ww);
            // 如果高度高的话根据宽度固定大小缩放
        else if ((w < h) && (h > hh)) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;

        // 设置缩放比例
        newOpts.inSampleSize = be;

        System.out.println("newOpts.inSampleSize........." + be); // 13

        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        // 压缩好比例大小后再进行质量压缩
        return compressImage(bitmap);
    }

    public static Bitmap getImageByBitmap(Bitmap image) {
        /*******************************************/
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if (baos.toByteArray().length / 1024 > 100) {// 判断如果图片大于100K,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 70, baos);// 这里压缩70%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 320f;// 这里设置高度为800f
        float ww = 480f;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;// 降低图片从ARGB888到RGB565
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
        /*******************************************/
    }

    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 90;

        // 循环判断如果压缩后图片是否大于80kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > 100) {

            // 重置baos即清空baos
            baos.reset();
            // 这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);

            // 每次都减少10
            options -= 10;
        }

        // 把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());

        // 把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    /**
     * 从相册获取图片后，从uri获取路径
     */
    public static String getFilePathFromUrl(Context context, Uri uri) {
        String path = null;
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            String projection[] = {MediaStore.Images.ImageColumns.DATA};
            Cursor c = context.getContentResolver().query(uri, projection, null, null, null);
            if (c != null && c.moveToFirst()) {
                path = c.getString(0);
            }
            if (c != null)
                c.close();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            path = uri.getPath();
        }
        return path;
    }

    /**
     * 从文件中解图 解大图内存不足时尝试5此, samplesize增大
     *
     * @param
     * @param max 宽或高的最大值, <= 0 , 能解多大解多大, > 0, 最大max, 内存不足解更小
     */
    public static Bitmap getBitmapFromFileLimitSize(String filePath, int max) {

        if (TextUtils.isEmpty(filePath) || !new File(filePath).exists()) {
            return null;
        }
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;

        if (max > 0) {
            options.inJustDecodeBounds = true;
            // 获取这个图片的宽和高
            bm = BitmapFactory.decodeFile(filePath, options);
            options.inJustDecodeBounds = false;
            options.inSampleSize = calculateInSampleSize(options, 700, 600);
        }
        int i = 0;
        while (i <= 10) {
            i++;
            try {
                bm = BitmapFactory.decodeFile(filePath, options);
                Log.d(TAG, "压缩后的图片宽高: " + options.outWidth + " x " + options.outHeight);
                break;
            } catch (OutOfMemoryError e) {
                options.inSampleSize++;
                e.printStackTrace();
            }
        }
        return compressImage(bm);
    }


    /**
     * 保存图片到文件
     *
     * @param bmp
     * @param path
     * @param format  The format of the compressed image
     * @param quality 0-100
     * @return
     */
    public static boolean saveBmpToFile(Bitmap bmp, String path, Bitmap.CompressFormat format, int quality) {
        if (bmp == null || bmp.isRecycled())
            return false;

        OutputStream stream = null;
        try {
            File file = new File(path);
            File filePath = file.getParentFile();
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            stream = new FileOutputStream(path);
            return bmp.compress(format, quality, stream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (null != stream) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取图片旋转角度
     */
    public static int getRotateDegree(Context context, Uri uri) {
        if (uri == null) {
            return 0;
        }
        String file = uri.getPath();
        if (TextUtils.isEmpty(file)) {
            return 0;
        }
        ExifInterface exif;
        try {
            exif = new ExifInterface(file);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        int degree = 0;
        if (orientation != ExifInterface.ORIENTATION_UNDEFINED) {
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    degree = 0;
                    break;
            }
        } else {
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                String orientationDb = cursor.getString(cursor.getColumnIndex("orientation"));
                cursor.close();
                if (!TextUtils.isEmpty(orientationDb)) {
                    degree = Integer.parseInt(orientationDb);
                }
            }
        }
        return degree;
    }

    /**
     * 旋转图片
     */
    public static Bitmap rotateBitmap(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);

            try {
                Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {

            }
        }
        return b;
    }

    /**
     * 压缩图片
     *
     * @param options   BitmapFactory.Options
     * @param reqWidth  要求的宽度
     * @param reqHeight 要求的高度
     * @return 返回 bitmap
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        Log.d(TAG, "取得图片的宽高: " + width + " x " + height);
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = (int) Math.ceil((float) height / (float) reqHeight);
            final int widthRatio = (int) Math.ceil((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static byte[] file2Byte(File file) {
        try {
            FileInputStream in = new FileInputStream(file);
            byte buffer[] = read(in);// 把图片文件流转成byte数组
            return Base64.encode(buffer, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            System.out.println("===没有找到图片===");
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("===转换出错===");
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getImageByte(String imagePath) {
        try {
            FileInputStream in = new FileInputStream(imagePath);
            byte buffer[] = read(in);// 把图片文件流转成byte数组
            return Base64.encode(buffer, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            System.out.println("===没有找到图片===");
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("===转换出错===");
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] read(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (in != null) {
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            out.close();
            in.close();
            return out.toByteArray();
        }
        return null;
    }

    /**
     * 按正方形裁切图片
     */
    public static Bitmap cropBitmap(Bitmap bitmap, int left, int top, int right, int bottom) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        if (left >= w || top >= h)
            return null;

        if (left < 0)
            left = 0;

        if (top < 0)
            top = 0;

        if (right > w)
            right = w;

        if (bottom > h)
            bottom = h;

        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top, null, false);
    }

    public static byte[] getPixelsBGR(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();

        // Create a new buffer
        ByteBuffer buffer = ByteBuffer.allocate(bytes);

        // Move the byte data to the buffer
        image.copyPixelsToBuffer(buffer);

        // Get the underlying array containing the data.
        byte[] buf = buffer.array();

        // Allocate for BGR
        int pixel_count = buf.length / 4;
        byte[] bgr = new byte[pixel_count * 3];

        // Copy pixels into place
        for (int i = 0; i < pixel_count; i++) {
            bgr[i * 3] = buf[i * 4 + 2];        //B
            bgr[i * 3 + 1] = buf[i * 4 + 1];    //G
            bgr[i * 3 + 2] = buf[i * 4];        //R
        }

        return bgr;
    }

    /**
     * 从Assets中读取图片
     */
    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}
