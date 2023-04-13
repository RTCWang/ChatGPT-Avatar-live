package com.proj.avatar.activity;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tu.loadingdialog.LoadingDailog;
import com.gyf.immersionbar.ImmersionBar;
import com.proj.avatar.R;
import com.proj.avatar.utils.Cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class BaseActivity extends AppCompatActivity {

    private LoadingDailog mLoading = null;

    private static String[] permissionNeeded = {
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    private final static int MY_PERMISSION_REQUEST_CODE = 10001;

    private void initStatusBarAndNavBar() {
        ImmersionBar.with(this)
                .transparentStatusBar()  //状态栏
                .transparentNavigationBar()  //透明导航栏
                .init();

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBarAndNavBar();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // 如果你的app可以横竖屏切换，并且适配4.4或者emui3手机请务必在onConfigurationChanged方法里添加这句话
        initStatusBarAndNavBar();
    }


    @Override
    public void setContentView(View view) {
        translucent();
        super.setContentView(view);
    }

    protected void requestPermission() {
        ActivityCompat.requestPermissions(this, permissionNeeded, MY_PERMISSION_REQUEST_CODE);

    }

    protected void onGrantedAllPermission() {

    }

    protected boolean checkPermission() {
        for (String permission : permissionNeeded) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE: {
                boolean isAllGranted = true;
                // 判断是否所有的权限都已经授予了
                for (int grant : grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        isAllGranted = false;
                        break;
                    }
                }
                if (isAllGranted) {
                    onGrantedAllPermission();

                } else {
                    // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                    toast("请先允许音视频权限");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    public void toast(String msg) {
//        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        View view = LayoutInflater.from(this).inflate(R.layout.custom_toast, null);
        TextView tv_msg = (TextView) view.findViewById(R.id.toast_text);
        tv_msg.setText(msg);
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.CENTER, 0, 20);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }

    protected void translucent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 实现透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); //布局延伸
    }

    public void hideLoading() {
        if (mLoading != null) {
            synchronized (BaseActivity.class) {
                if (mLoading != null) {
                    mLoading.cancel();
                    mLoading = null;
                }
            }
        }
    }

    public void showLoading(String msg) {
        hideLoading();
        LoadingDailog.Builder loadBuilder = new LoadingDailog.Builder(this)
                .setMessage(msg)
                .setCancelable(true)
                .setCancelOutside(false);
        mLoading = loadBuilder.create();
        mLoading.show();
    }

    public int randInt(int a, int b) {
        return new Random().nextInt(b - a + 1) + a;
    }

    protected List<String> getJoinedRoom() {
        List<String> roomList = new ArrayList<>();
        String roomsStr = Cfg.getKV(this, "room_ids");
        if (roomsStr.length() <= 0) return roomList;
        String[] arr = roomsStr.split("@");
        for (String s : arr) {
            roomList.add(s);
        }
        return roomList;
    }

    protected void clearJoinedRoom() {
        Cfg.saveKV(this, "room_ids", "");
    }

    protected void addJoinedRoom(String room) {
        String roomsStr = Cfg.getKV(this, "room_ids");
        String[] arr = roomsStr.split("@");
        Set<String> set = new HashSet<>();
        set.add(room);
        for (String s : arr) {
            set.add(s);
        }
        String v = String.join("@", set);
        Cfg.saveKV(this, "room_ids", v);
    }
}
