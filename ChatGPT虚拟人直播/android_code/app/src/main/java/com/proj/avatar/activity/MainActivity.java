package com.proj.avatar.activity;


import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.proj.avatar.R;
import com.proj.avatar.utils.CB;
import com.proj.avatar.zego.ZegoMngr;
import com.zego.avatar.ZegoAvatarService;
import com.zego.avatar.bean.ZegoAvatarServiceState;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private final static String TAG = "MainActivity";
    private boolean isCreator = false;
    private ZegoMngr mZegoMngr = null;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.enterBtn).setOnClickListener(this);
        findViewById(R.id.createBtn).setOnClickListener(this);
        mZegoMngr = ZegoMngr.getInstance(getApplication());

//        RadioGroup rg = findViewById(R.id.sex_rg);
//        rg.setOnCheckedChangeListener((group, checkedId) -> {//设置组中单选按钮选中事件
//            isMan = checkedId == R.id.sex_man;
//        });
        init();
    }

    private void init() {
        showLoading("正在初始化...");
        userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        mZegoMngr.init(new CB() {
            @Override
            public void complete(boolean succ, String msg) {
                hideLoading();
                if (!succ) {
                    toast(msg);
                } else {
                    login();
                }
            }
        });
    }

    private void login() {
        showLoading("正在连接服务...");
        mZegoMngr.getZIMMngr().login(userId, new CB() {
            @Override
            public void complete(boolean succ, String msg) {
                if (!succ) {
                    hideLoading();
                    toast(msg);
                } else {
                    leaveAllGroup();
                }
            }
        });
    }

    private void leaveAllGroup() {
        mZegoMngr.getZIMMngr().leaveAllRoom(getJoinedRoom(), new CB() {
            @Override
            public void complete(boolean succ, String msg) {
                clearJoinedRoom();
                hideLoading();
            }
        });
    }


    private String getInpRoomID() {
        String roomId = ((EditText) findViewById(R.id.roomId)).getText().toString();
        roomId = roomId.trim();
        if (roomId.length() > 0) return "cr_" + roomId;
        else return null;
    }

    private void openAvatarActivity() {
        if (!checkPermission()) {
            requestPermission();
            return;
        }
        if (ZegoAvatarService.getState() != ZegoAvatarServiceState.InitSucceed) {
            //这里也可以使用ZegoAvatarService.addServiceObserver监听初始化状态
            Log.e(TAG, "avatar init error:" + ZegoAvatarService.getState());
            toast("avatar初始化未完成！");
            return;
        }

        Intent intent = new Intent(MainActivity.this, RoomActivity.class);
        intent.putExtra("roomId", getInpRoomID());//加入前缀
        intent.putExtra("isCreator", isCreator);
        this.startActivity(intent);

    }


    @Override
    protected void onGrantedAllPermission() {
        openAvatarActivity();
    }

    private void enterRoom(String roomId) {
        mZegoMngr.getZIMMngr().joinRoom(roomId, new CB() {
            @Override
            public void complete(boolean succ, String msg) {
                if (succ) {
                    openAvatarActivity();
                } else {
                    toast(msg);
                }
            }
        });

    }

    private void createRoom(String roomId) {
        showLoading("正在创建房间");
        mZegoMngr.getZIMMngr().createRoom(userId, roomId,  userId, new CB() {
            @Override
            public void complete(boolean succ, String msg) {
                hideLoading();
                if (succ) {
                    addJoinedRoom(roomId);
                    openAvatarActivity();
                } else {
                    toast(msg);
                }
            }
        });

    }

    @Override
    public void onClick(View view) {
        String roomId = getInpRoomID();
        if (roomId == null) {
            toast("请输入房间ID!");
            return;
        }
        if (view.getId() == R.id.enterBtn) {
            isCreator = false;
//            openAvatarActivity();
            enterRoom(roomId);
        } else if (view.getId() == R.id.createBtn) {
            isCreator = true;
//            openAvatarActivity();

            createRoom(roomId);
        }
    }
}