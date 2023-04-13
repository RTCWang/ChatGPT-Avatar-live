package com.proj.avatar.entity;

import android.util.Log;

import java.util.Date;

public class Msg {
    public String msg;
    public long time;
    public String toUID;
    public String fromUID;
    public MsgType type;
    public MsgProto proto;
    public int extInt;

    public enum MsgType {
        P2P,
        ROOM
    }

    public enum MsgProto {
        DanMu,
        InviteJoin,
        DismissRoom,
        ChatGPT

    }

    public Msg(MsgType type, String msg, long time, String fromUID, String toUID) {
        this.msg = msg;
        this.time = time;
        this.fromUID = fromUID;
        this.toUID = toUID;
        this.type = type;
    }

    private Msg() {
    }

    public static Msg newDismissMsg(String roomId, String fromUID) {
        Msg msg = new Msg(MsgType.ROOM, "dismiss://" + roomId, new Date().getTime(), fromUID, roomId);
        return msg;
    }

    public static Msg newDanmuMsg(String roomId, String text, int colorIdx, String fromUID) {
        Msg msg = new Msg(MsgType.ROOM, "danmu://" + colorIdx + "," + text, new Date().getTime(), fromUID, roomId);
        return msg;
    }

    public static Msg newInvite(String roomId, String text, String fromUID, String toUID) {
        Msg msg = new Msg(MsgType.P2P, "invite://" + roomId, new Date().getTime(), fromUID, toUID);
        return msg;
    }

    public static Msg parseMsg(String txt, String fromUID, String toUID, boolean isRoom) {

        Msg msg = new Msg();
        msg.type = isRoom ? MsgType.ROOM : MsgType.P2P;
        msg.time = new Date().getTime();
        msg.fromUID = fromUID;
        msg.toUID = toUID;
        if (fromUID.toLowerCase().equals("chatgpt")) {
            msg.proto = MsgProto.ChatGPT;
            msg.msg = txt;
        } else if (txt.startsWith("dismiss://")) {
            msg.proto = MsgProto.DismissRoom;
            msg.msg = txt.substring("dismiss://".length());
        } else if (txt.startsWith("danmu://")) {
            String content = txt.substring("danmu://".length());
            int idx = content.indexOf(",");
            if (idx > 0) {
                try {
                    msg.extInt = Integer.parseInt(content.substring(0, idx));
                } catch (Exception e) {
                }
            }
            msg.msg = content.substring(idx + 1);
            msg.proto = MsgProto.DanMu;

        } else if (txt.startsWith("invite://")) {
            msg.proto = MsgProto.InviteJoin;
            msg.msg = txt.substring("invite://".length());
        } else {
            return null;
        }
        return msg;
    }

}
