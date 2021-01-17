package com.smallraw.library.smallpermissions.permisson.handler;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.util.SparseArray;

import com.smallraw.library.smallpermissions.callback.PermissionsCallback;
import com.smallraw.library.smallpermissions.check.DoublePermissionsCheck;
import com.smallraw.library.smallpermissions.check.IPermissionsCheck;
import com.smallraw.library.smallpermissions.check.NormalPermissionsCheck;

import java.util.ArrayList;
import java.util.List;

public class PermissionsHandler {
  private final SparseArray<PermissionsCallback> mCallbackList = new SparseArray<>();

  private static class PermissionsHandlerProvider {
    private static final PermissionsHandler PERMISSIONS_HANDLER = new PermissionsHandler();
  }

  public final static PermissionsHandler getInstance() {
    return PermissionsHandlerProvider.PERMISSIONS_HANDLER;
  }

  public void addPermissionCallback(int key, PermissionsCallback callback) {
    mCallbackList.put(key, callback);
  }

  public void removePermissionCallback(int key) {
    mCallbackList.remove(key);
  }

  public PermissionsCallback getPermissionCallback(int key) {
    return mCallbackList.get(key);
  }

  public void onRequestPermissionsResult(Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    PermissionsCallback permissionsCallback = getPermissionCallback(requestCode);
    if (permissionsCallback != null) {
      if (grantResults.length > 0) {
        List<String> grantedPermission = new ArrayList<>();
        List<String> deniedPermission = new ArrayList<>();
        //判读没有授予权限的权限，并放到一个集合里
        for (int i = 0; i < grantResults.length; i++) {
          boolean isTip = ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[i]);
          if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
            deniedPermission.add(permissions[i]);
            if (isTip) {//表明用户没有彻底禁止弹出权限请求
              // requestPermission(PermissionHelper.getInstance().filterPermissions(permissions));
            } else {//表明用户已经彻底禁止弹出权限请求
              // PermissionMonitorService.start(this);//这里一般会提示用户进入权限设置界面
            }
          } else {
            grantedPermission.add(permissions[i]);
          }
        }
        //判断是否全部通过授权
        if (deniedPermission.isEmpty()) {
          permissionsCallback.onPermissionGranted(grantedPermission);
        } else {
          permissionsCallback.onPermissionDenied(deniedPermission);
        }
      }
      removePermissionCallback(requestCode);
    }
  }

  /**
   * 检察权限
   *
   * @param context
   * @param permissions 准备请求的权限
   * @return 返回需要获取权限的权限
   */
  public String[] checkPermissions(Context context, String[] permissions) {
    return checkPermissions(context, permissions, true);
  }

  /**
   * 检察权限
   *
   * @param context
   * @param permissions 准备请求的权限
   * @param actual      是否使用严格模式
   * @return 返回需要获取权限的权限
   */
  public String[] checkPermissions(Context context, String[] permissions, boolean actual) {
    IPermissionsCheck checkPermissions;
    if (actual) {
      checkPermissions = new DoublePermissionsCheck();
    } else {
      checkPermissions = new NormalPermissionsCheck();
    }

    ArrayList<String> permissionList = new ArrayList<>();
    for (String permission : permissions) {
      if (!checkPermissions.checkPermissions(context, permission)) {
        permissionList.add(permission);
      }
    }
    return permissionList.toArray(new String[permissionList.size()]);
  }
}
