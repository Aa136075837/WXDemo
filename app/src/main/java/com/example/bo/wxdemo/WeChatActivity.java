package com.example.bo.wxdemo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Hashtable;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public class WeChatActivity extends AppCompatActivity implements View.OnClickListener {
    private final String url = "https://github.com/dashboard";
    private TextView mCancel;
    private TextView mSave;
    private ImageView mQRCode;
    private Bitmap mBitmap;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_we_chat);
        initView ();
        initListener ();
    }

    @Override protected void onStart () {
        super.onStart ();
        try {
            mQRCode.setImageBitmap (createQRCode (url, 200));
        } catch (WriterException e) {
            e.printStackTrace ();
        }
    }

    private void initListener () {
        mCancel.setOnClickListener (this);
        mSave.setOnClickListener (this);
    }

    private void initView () {
        mCancel = (TextView) findViewById (R.id.wechat_cancel);
        mSave = (TextView) findViewById (R.id.wechat_save);
        mQRCode = (ImageView) findViewById (R.id.wechat_2Dcode);
    }

    @Override public void onClick (View v) {
        switch (v.getId ()) {
            case R.id.wechat_cancel:
                finish ();
                break;
            case R.id.wechat_save:
                try {
                    screenShot (mQRCode, "Aa136075837");
                } catch (Exception e) {
                    Toast.makeText (this, "保存失败", Toast.LENGTH_SHORT).show ();
                    return;
                }
                toWeChat ();
                break;
        }
    }

    private void toWeChat () {
        String packName = "com.tencent.mm";
        PackageManager packageManager = getPackageManager ();

        Intent intent = packageManager.getLaunchIntentForPackage (packName);
        startActivity (intent);
    }

    public void screenShot (View view, String fileName) throws Exception {
        Log.e ("BOWX", "屏幕截图");
        view.setDrawingCacheEnabled (true);
        view.buildDrawingCache ();
        // 上面2行必须加入，如果不加如view.getDrawingCache()返回null
        //从缓存中获取当前屏幕的图片
        mBitmap = view.getDrawingCache ();
        FileOutputStream fos = null;
        mBitmap = Bitmap.createBitmap (mBitmap);
        view.destroyDrawingCache ();
        try {
            // 判断sd卡是否存在
            boolean sdCardExist = Environment.getExternalStorageState ().equals (Environment.MEDIA_MOUNTED);
            if (sdCardExist) {
                // 获取sdcard的根目录
                String sdPath = Environment.getExternalStorageDirectory ().getPath ();

                // 创建程序自己创建的文件夹
                File tempFile = new File (sdPath + File.separator + fileName);
                if (!tempFile.exists ()) {
                    tempFile.mkdirs ();
                }
                // 创建图片文件
                File file = new File (sdPath + File.separator + fileName + File.separator + "screen" + ".png");
                if (!file.exists ()) {
                    file.createNewFile ();
                }

                // image.setImageBitmap(bitmap);
                // 其次把文件插入到系统图库
                try {
                    MediaStore.Images.Media.insertImage (getContentResolver (), file.getAbsolutePath (), fileName,
                            null);
                } catch (FileNotFoundException e) {

                    Log.e ("BOWX", "保存失败");
                    e.printStackTrace ();
                }
                // 最后通知图库更新

                sendBroadcast (new Intent (Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile (file)));
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


    /**
     *   解决生成的二维码保存到相册是显示黑屏的问题
     * @param str
     * @param widthAndHeight
     * @return
     * @throws WriterException
     */
    public Bitmap createQRCode (String str, int widthAndHeight) throws WriterException {
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String> ();
        hints.put (EncodeHintType.CHARACTER_SET, "utf-8");
        BitMatrix matrix = new MultiFormatWriter ().encode (str, BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight);
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
}
