package com.proj.avatar.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.proj.avatar.R;
import com.proj.avatar.dm.DanmuAdapter;
import com.proj.avatar.dm.DanmuContainerView;
import com.proj.avatar.dm.DanmuEntity;
import com.proj.avatar.entity.Msg;
import com.proj.avatar.entity.User;
import com.proj.avatar.utils.CB;
import com.proj.avatar.view.KeyboardLayout;
import com.proj.avatar.view.ShowUtils;
import com.proj.avatar.zego.ZegoMngr;
import com.proj.avatar.zego.zim.MsgListener;
import com.zego.avatar.ZegoAvatarView;

import java.util.Date;

import androidx.appcompat.widget.Toolbar;

public class RoomActivity extends BaseActivity implements KeyboardLayout.KeyboardLayoutListener, View.OnClickListener, MsgListener {
    private final static String TAG = "RoomActivity";
    private User mUser;
    private ZegoAvatarView mTextureView;
    private ZegoMngr mZegoMngr = ZegoMngr.getInstance(getApplication());
    private EditText msgET;
    private LinearLayout bottomBar;
    private boolean isOnPause = true;
    private DanmuContainerView danmuView;
    int[] colors = new int[]{Color.RED, Color.GREEN, Color.BLUE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        msgET = findViewById(R.id.msgET);
        bottomBar = findViewById(R.id.bottomBar);
        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(this);

        mTextureView = findViewById(R.id.texture);
        danmuView = findViewById(R.id.danmu);
        danmuView.setAdapter(new DanmuAdapter(this));
        initData();
        mZegoMngr.start(mUser);
        mZegoMngr.getZIMMngr().setListener(this);
    }


    private void initData() {

        Intent intent = getIntent();
        String roomId = intent.getStringExtra("roomId");
        boolean isCreator = intent.getBooleanExtra("isCreator", false);
        ((KeyboardLayout) findViewById(R.id.keyboard_layout)).setKeyboardListener(this);
        String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        mUser = new User(userId, userId);
        mUser.isCreator = isCreator;
        mUser.roomId = roomId;
        mUser.isMan = false;
        mUser.avatarView = mTextureView; //findViewById(R.id.remote_view);

        mUser.bgColor = Color.argb(200, 93, 29, 60);


    }


    //点击发送按钮
    public void onClkSendMsgBtn(View view) {
        String txt = msgET.getText().toString().trim();
        if (txt.length() <= 0) return;
        int colorIdx = randInt(0, colors.length - 1);
        Msg msg = Msg.newDanmuMsg(mUser.roomId, txt, colorIdx, mUser.userId);
        mZegoMngr.getZIMMngr().sendMsg(msg, new CB() {
            @Override
            public void complete(boolean succ, String msg) {
                if (!succ) {
                    toast("评论失败！");
                } else {
                    msgET.setText("");
                    sendDM(txt, colorIdx);
                }
            }
        });
    }

    //展示评论弹幕
    private void sendDM(String dm, int colorIdx) {
        DanmuEntity danmuEntity = new DanmuEntity();
        danmuEntity.setContent(dm);
        danmuEntity.setType(0);
        danmuEntity.setTextColor(colors[colorIdx]);
        danmuView.addDanmu(danmuEntity);

    }

    //接收到文字消息
    @Override
    public void onRcvMsg(Msg msg) {
        Log.e(TAG, "rct" + msg.proto);
        switch (msg.proto) {
            case ChatGPT: {
                mZegoMngr.getAvatarMngr().playText(msg.msg);
                sendDM(msg.msg.replace("\n", ""), 0);
                break;
            }
            case DanMu: {
                sendDM(msg.msg, msg.extInt);
                break;
            }
            case DismissRoom: {
                mZegoMngr.getZIMMngr().leaveRoom(msg.msg, new CB() {
                    @Override
                    public void complete(boolean succ, String msg) {
                        ShowUtils.alert(RoomActivity.this, "提示", "房间已解散！", new ShowUtils.OnClickOkListener() {
                            @Override
                            public void onOk() {
                                back();
                            }
                        });
                    }
                });
                break;
            }
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mZegoMngr.stop();//清理引用，防止内存泄露
    }

    public int dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void onKeyboardStateChanged(boolean isActive, int keyboardHeight) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bottomBar.getLayoutParams();

        if (isActive) {
            params.bottomMargin = keyboardHeight + dp2px(40);
        } else {
            params.bottomMargin = dp2px(10);
        }
        bottomBar.setLayoutParams(params);
    }

    private void back() {//返回

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });
    }

    @Override
    public void onClick(View view) { //目前只有点击返回箭头才有回调，如果增加其他点击事件注意要添加if/else判断
        ShowUtils.comfirm(this, "提示", "确定离开直播间？", "确定", new ShowUtils.OnClickOkCancelListener() {
            @Override
            public void onOk() {
                if (mUser.isCreator) {//如果是群主，解散房间
                    mZegoMngr.getZIMMngr().dismissRoom(mUser.userId, mUser.roomId, new CB() {
                        @Override
                        public void complete(boolean succ, String msg) {
                            back();
                        }
                    });
                } else {//如果是观众，直接离开
                    mZegoMngr.getZIMMngr().leaveRoom(mUser.roomId, new CB() {
                        @Override
                        public void complete(boolean succ, String msg) {
                            back();
                        }
                    });
                }
            }

            @Override
            public void onCancel() {

            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();

        isOnPause = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOnPause = false;
    }

}