package com.smallraw.library.smallpermissions.permisson;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smallraw.library.smallpermissions.callback.PermissionsCallback;
import com.smallraw.library.smallpermissions.handler.EnginePermission;
import com.smallraw.library.smallpermissions.handler.PermissionsHandler;
import com.smallraw.library.smallpermissions.thread.MainThread;

import java.util.concurrent.Executor;

public class RequestPermissionsFragment extends Fragment implements IPermission {
    private final PermissionsHandler mPermissionsHandler = PermissionsHandler.getInstance();
    private final EnginePermission mEnginePermission = new EnginePermission();
    private final Executor mMainExecutor = new MainThread();

    @Override
    public void requestPermissions(final String[] permissions, final int requestCode, final PermissionsCallback callback) {
        mEnginePermission.add(new Runnable() {
            @Override
            public void run() {
                String[] permission = mPermissionsHandler.checkPermissions(getActivity(), permissions);
                if (permission.length == 0) {
                    mMainExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.onPermissionGranted();
                        }
                    });
                } else {
                    mPermissionsHandler.addPermissionCallback(requestCode, callback);
                    requestPermissions(permission, requestCode);
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEnginePermission.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionsHandler.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mEnginePermission.quitSafely();
        }else{
            mEnginePermission.removeAllMessage();
            mEnginePermission.quit();
        }
        super.onDestroy();
    }
}
