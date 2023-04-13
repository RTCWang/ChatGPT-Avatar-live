package com.proj.avatar.entity;

import android.graphics.Color;
import android.view.TextureView;

import com.zego.avatar.ZegoAvatarView;

public class User {
    public String userName;
    public String userId;
    public boolean isMan;
    public int width = 720;
    public int height = 1080;
    public int bgColor;
    public int shirtIdx = 0;
    public int browIdx = 0;
    public String roomId;

    public boolean isCreator = false;

    public ZegoAvatarView avatarView;

    public User(String userName, String userId) {
        this.userName = userName;
        this.userId = userId;
        this.isMan = true;
        bgColor = Color.argb(255, 33, 66, 99);
    }

}
