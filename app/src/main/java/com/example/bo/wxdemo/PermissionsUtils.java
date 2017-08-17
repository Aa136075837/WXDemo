package com.example.bo.wxdemo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class PermissionsUtils {

    //申请权限
    public static void requestPermissions(String[] permissions, int requestCode, Object object) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            List<String> deniedPermissions = findDeniedPermissions((Activity) object, permissions);

            if (deniedPermissions.size() > 0) {
                if (object instanceof Activity) {
                    ((Activity) object).requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]),
                                                           requestCode);
                } else if (object instanceof Fragment) {
                    ((Fragment) object).requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]),
                                                           requestCode);
                } else {
                    throw new IllegalArgumentException(object.getClass()
                                                             .getName() + " is not supported");
                }
            }
        }

    }

    /**
     * 检查权限是否授权
     * @param activity
     * @param permission
     * @return 未被授权的集合
     */
    @TargetApi (value = Build.VERSION_CODES.M)
    private static List<String> findDeniedPermissions(Activity activity, String... permission)
    {
        List<String> denyPermissions = new ArrayList<> ();
        for (String value : permission) {
            if (activity.checkSelfPermission(value) != PackageManager.PERMISSION_GRANTED) {
                denyPermissions.add(value);
            }
        }
        return denyPermissions;
    }
}
