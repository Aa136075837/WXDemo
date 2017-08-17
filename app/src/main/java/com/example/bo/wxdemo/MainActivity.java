package com.example.bo.wxdemo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mBtn;
    private WebView mWebView;
    private ImageView mImg;
    private Button mGenerate;
    private Bitmap mBitmap;
    private Button mSave;
    private Bitmap mBitmap1;

    @RequiresApi (api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        PermissionsUtils.requestPermissions (new String[]{Manifest.permission
                .WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 100, this);
        initView ();
        initListener ();
    }

    private void initListener () {
        mBtn.setOnClickListener (this);
        mGenerate.setOnClickListener (this);
        mSave.setOnClickListener (this);
    }

    private void initView () {
        mImg = (ImageView) findViewById (R.id.img_code);
        mBtn = (Button) findViewById (R.id.btn);
        mGenerate = (Button) findViewById (R.id.twocode);
        mSave = (Button) findViewById (R.id.save);
    }

    @Override
    public void onClick (View view) {

        switch (view.getId ()) {
            case R.id.btn:
                String packName = "com.tencent.mm";
                PackageManager packageManager = getPackageManager ();

                Intent intent = packageManager.getLaunchIntentForPackage (packName);
                startActivity (intent);
                break;
            case R.id.twocode:
                String url = "http://we.qq.com/d/AQBR683V93iAxf0mU18YPmk0m-GYdhcR28V_-H9i";
                String url1 = "http://weixin.qq.com/r/jEnl-TDEfG0DrXSe9xye";
                if (!TextUtils.isEmpty (url1)) {

                    try {
                        mBitmap1 = createQRCode (url1, 200);
                        mImg.setImageBitmap (mBitmap1);
                    } catch (WriterException e) {
                        e.printStackTrace ();
                    }
                }
                startActivity (new Intent (this, EditTextActivity.class));
                break;
            case R.id.save:
              /*  Log.e ("BOWX", "保存");
                //将图片放入图库
                try {
                    screenShot (mImg, "/sdcard/BOWX/");
                } catch (Exception e) {
                    Log.e ("BOWX", "yichang");
                    e.printStackTrace ();
                }
                //saveImageToGallery (this, mBitmap1);*/
                intent = new Intent (this, WeChatActivity.class);
                startActivity (intent);
                break;
            default:
                break;
        }
    }

    public Bitmap createQRCode (String str, int widthAndHeight) throws WriterException {
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String> ();
        hints.put (EncodeHintType.CHARACTER_SET, "utf-8");
        BitMatrix matrix = new MultiFormatWriter ().encode (str, BarcodeFormat.QR_CODE,
                widthAndHeight, widthAndHeight);
        int width = matrix.getWidth ();
        int height = matrix.getHeight ();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get (x, y)) {
                    pixels[y * width + x] = BLACK;
                } else {
                    pixels[y * width + x] = WHITE;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap (width, height, Bitmap.Config.RGB_565);
        bitmap.setPixels (pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static void saveImageToGallery (Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File (Environment.getExternalStorageDirectory (), "2Dcode");
        Log.e ("BOWX", "llujing == " + appDir.toString ());

        if (!appDir.exists ()) {
            appDir.mkdir ();
        }
        String fileName = System.currentTimeMillis () + ".jpg";
        File file = new File (appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream (file);
            bmp.compress (Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush ();
            fos.close ();
        } catch (FileNotFoundException e) {
            e.printStackTrace ();
            Log.e ("BOWX", "找不到文件");
        } catch (IOException e) {
            Log.e ("BOWX", "读写错误");
            e.printStackTrace ();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage (context.getContentResolver (), file
                    .getAbsolutePath (), fileName, null);
        } catch (FileNotFoundException e) {

            Log.e ("BOWX", "保存失败");
            e.printStackTrace ();
        }
        // 最后通知图库更新

        context.sendBroadcast (new Intent (Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile
                (file)));
    }

    public void screenShot (View view, String fileName) throws Exception {
        Log.e ("BOWX", "屏幕截图");
        view.setDrawingCacheEnabled (true);
        view.buildDrawingCache ();
        // 上面2行必须加入，如果不加如view.getDrawingCache()返回null
        //从缓存中获取当前屏幕的图片
        mBitmap = mBitmap1;
        FileOutputStream fos = null;
        mBitmap = Bitmap.createBitmap (mBitmap);
        view.destroyDrawingCache ();
        try {
            // 判断sd卡是否存在
            boolean sdCardExist = Environment.getExternalStorageState ().equals (Environment
                    .MEDIA_MOUNTED);
            if (sdCardExist) {
                // 获取sdcard的根目录
                String sdPath = Environment.getExternalStorageDirectory ().getPath ();

                // 创建程序自己创建的文件夹
                File tempFile = new File (sdPath + File.separator + fileName);
                if (!tempFile.exists ()) {
                    tempFile.mkdirs ();
                }
                // 创建图片文件
                File file = new File (sdPath + File.separator + fileName + File.separator +
                        "screen" + ".png");
                if (!file.exists ()) {
                    file.createNewFile ();
                }

                // image.setImageBitmap(bitmap);
                // 其次把文件插入到系统图库
                try {
                    MediaStore.Images.Media.insertImage (getContentResolver (), file
                            .getAbsolutePath (), fileName, null);
                } catch (FileNotFoundException e) {

                    Log.e ("BOWX", "保存失败");
                    e.printStackTrace ();
                }
                // 最后通知图库更新

                sendBroadcast (new Intent (Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile
                        (file)));
                fos = new FileOutputStream (file);
                if (fos != null) {
                    mBitmap.compress (Bitmap.CompressFormat.PNG, 90, fos);
                    fos.close ();
                }
            }
        } catch (Exception e) {

        } finally {

        }
    }

    private void doStartApplicationWithPackageName (String packagename) {

        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = getPackageManager ().getPackageInfo (packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace ();
        }
        if (packageinfo == null) {
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent (Intent.ACTION_MAIN, null);
        resolveIntent.addCategory (Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage (packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = getPackageManager ().queryIntentActivities
                (resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator ().next ();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent (Intent.ACTION_MAIN);
            intent.addCategory (Intent.CATEGORY_LAUNCHER);

            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName (packageName, className);

            intent.setComponent (cn);
            startActivity (intent);
        }
    }
}
