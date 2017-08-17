package com.example.bo.wxdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.os.Environment;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageUntil {
    private final static String FILE_NAME = "/SMARTMOVT/";

    /**
     * 保存图片并通知系统相册更新
     *
     * @param context
     * @param data
     * @param fileName
     */
    public static void saveImge(Context context, byte[] data, String fileName) {

        String path = saveTofile(data, fileName);
        int degree = getExifOrientation(path);
        if (0 != degree) {
            saveToNormal(data, path, degree);
        }
        MediaScannerConnection.scanFile(context, new String[]{path}, null, null);
    }

    /**
     * 将旋转的图片旋转会正常的位置并保存
     *
     * @param data
     * @param path
     * @param degree
     */
    private static void saveToNormal(byte[] data, String path, int degree) {
        // TODO Auto-generated method stub
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        OutputStream ops = null;
        try {
            ops = new BufferedOutputStream (new FileOutputStream (file));
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, ops);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (null != ops) {
                try {
                    ops.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    ops.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            newBitmap.recycle();
        }
    }

    /**
     * 将原始数据保存成图片文件
     *
     * @param data
     * @param fileName
     * @return
     */
    private static String saveTofile(byte[] data, String fileName) {
        // TODO Auto-generated method stub

        String path = null;
        final String ALBUM_PATH = Environment.getExternalStorageDirectory() + FILE_NAME;

        File dirFile = new File(ALBUM_PATH);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        File myCaptureFile = new File(ALBUM_PATH + fileName);
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
            bos.write(data);
            path = myCaptureFile.getAbsolutePath();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (null != bos) {
                try {
                    bos.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {
                    bos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return path;
    }

    /**
     * 获取图片旋转的角度
     *
     * @param path
     * @return
     */
    private static int getExifOrientation(String path) {
        // TODO Auto-generated method stub
        if (null == path || path.equals("")) {
            return 0;
        }
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException ex) {
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
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
                }
            }
        }
        return degree;
    }

    /**
     * 获取指定宽高的缩略图
     *
     * @param path
     * @param width
     * @param height
     * @return
     */
    public static Bitmap getThumbFromPath(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inSampleSize = computeScale(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 计算相片的缩小比例
     *
     * @param options
     * @param width
     * @param height
     * @return
     */
    private static int computeScale(BitmapFactory.Options options, int width, int height) {
        int inSampleSize = 1;
        if (width == 0 || height == 0) {
            return inSampleSize;
        }
        int bitmapWidth = options.outWidth;
        int bitmapHeight = options.outHeight;
        if (bitmapWidth > width || bitmapHeight > height) {
            int widthScale = Math.round((float) bitmapWidth / (float) width);
            int heightScale = Math.round((float) bitmapHeight / (float) height);
            inSampleSize = widthScale > heightScale ? widthScale : heightScale;
        }
        return inSampleSize;
    }

    public static Bitmap getThumbFromPath(byte[] data, Camera.Size size, int targetWidth, int targetHeight, float degree) {
        if (null == data || null == size) {
            return null;
        }
        int xScale = size.height / targetWidth;
        int yScale = size.width / targetHeight;
        int option = yScale > xScale ? yScale : xScale;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = option;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
                data.length, options);
        Matrix matrix = null;
        if (android.os.Build.BRAND.endsWith("sumsung") ||
                android.os.Build.BRAND.endsWith("samsung")) {
            matrix = new Matrix();
            matrix.postRotate(degree);
        }
        if (null != matrix) {
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }

    public static String getHistoryImage() {
        String ALBUM_PATH = Environment.getExternalStorageDirectory() + FILE_NAME;
        File file = new File(ALBUM_PATH);
        if (!file.exists()) {
            return null;
        }
        File[] files = file.listFiles();
        String path = "";
        for (int i = files.length - 1; i > -1; i--) {
            if (files[i].getAbsolutePath().endsWith("png") || files[i].getAbsolutePath().endsWith("jpeg")) {
                path = files[i].getAbsolutePath();
                break;
            }
        }
        return path;
    }


}
