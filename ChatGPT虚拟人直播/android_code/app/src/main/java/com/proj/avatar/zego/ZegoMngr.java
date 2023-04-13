package com.proj.avatar.zego;

import android.app.Application;

import com.proj.avatar.entity.User;
import com.proj.avatar.utils.CB;
import com.proj.avatar.zego.avatar.AvatarMngr;
import com.proj.avatar.zego.zim.ZIMMngr;

import java.util.ArrayList;

import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMGroupCreatedCallback;

public class ZegoMngr {
    private final static String TAG = "ZegoMngr";
    private static ZegoMngr mInstance;
    private AvatarMngr mAvatarMngr;
    private ZIMMngr mZIMMngr;
    private Application app;

    private User mUser;


    public void init(CB cb) {
        mAvatarMngr.init(new CB() {
            @Override
            public void complete(boolean succ, String msg) {
                if (succ) {
                    mZIMMngr.init(cb);
                } else {
                    cb.complete(succ, msg);
                }
            }
        });
    }

    public ZIMMngr getZIMMngr() {
        return mZIMMngr;
    }

    /**
     * 将登陆房间，启动avatar，自动推流聚合在一个函数
     */
    public void start(User user) {
        mUser = user;
        mAvatarMngr.start(user);
        mZIMMngr.start(user);
    }

    public void updateUser(User user) {
        mAvatarMngr.updateUser(user);
    }


    public AvatarMngr getAvatarMngr() {
        return mAvatarMngr;
    }


    public void stop() {
//        mRTCMngr.stop();

        mZIMMngr.stop();
        mAvatarMngr.stop();
    }


    private ZegoMngr(Application app) {
        this.app = app;
//        mRTCMngr = RTCMngr.getInstance(app);
        mZIMMngr = ZIMMngr.getInstance(app);
        mAvatarMngr = AvatarMngr.getInstance(app);
    }



    public static ZegoMngr getInstance(Application app) {
        if (null == mInstance) {
            synchronized (ZegoMngr.class) {
                if (null == mInstance) {
                    mInstance = new ZegoMngr(app);
                }
            }
        }
        return mInstance;
    }
}
