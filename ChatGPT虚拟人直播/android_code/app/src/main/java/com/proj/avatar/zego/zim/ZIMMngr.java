package com.proj.avatar.zego.zim;

import android.app.Application;
import android.util.Log;

import com.proj.avatar.entity.Msg;
import com.proj.avatar.entity.User;
import com.proj.avatar.utils.CB;
import com.proj.avatar.zego.KeyCenter;
import com.proj.avatar.zego.ZegoMngr;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.callback.ZIMLoggedInCallback;
import im.zego.zim.callback.ZIMMessageSentCallback;
import im.zego.zim.callback.ZIMRoomCreatedCallback;
import im.zego.zim.callback.ZIMRoomJoinedCallback;
import im.zego.zim.callback.ZIMRoomLeftCallback;
import im.zego.zim.callback.ZIMTokenRenewedCallback;
import im.zego.zim.entity.ZIMBarrageMessage;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMGroupFullInfo;
import im.zego.zim.entity.ZIMGroupOperatedInfo;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageSendConfig;
import im.zego.zim.entity.ZIMPushConfig;
import im.zego.zim.entity.ZIMRoomFullInfo;
import im.zego.zim.entity.ZIMRoomInfo;
import im.zego.zim.entity.ZIMTextMessage;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMGroupEvent;
import im.zego.zim.enums.ZIMGroupState;
import im.zego.zim.enums.ZIMMessagePriority;
import im.zego.zim.enums.ZIMRoomEvent;
import im.zego.zim.enums.ZIMRoomState;

public class ZIMMngr {
    private static final String CHATGPT_ID = "chatgpt";
    private static final String TAG = "ZIMMngr";
    private LinkedBlockingQueue<Msg> msgQueue = new LinkedBlockingQueue<>();
    private long startTime = new Date().getTime();
    private static ZIMMngr mInstance;
    private ZIM zim;
    private User mUser;
    private MsgListener mListener;

    public void init(CB cb) {
        cb.complete(true, null);
    }

    public void start(User user) {
        this.mUser = user;
    }

    public void stop() {

    }


    /**
     * 离开群
     */
    public void leaveRoom(String roomId, CB cb) {
        zim.leaveRoom(roomId, new ZIMRoomLeftCallback() {
                    @Override
                    public void onRoomLeft(String roomID, ZIMError errorInfo) {
                        cb.complete(errorInfo.code == ZIMErrorCode.SUCCESS, errorInfo.message);
                    }
                }
        );
    }

    /**
     * 向群里面发送一段特殊格式的消息，全部退群
     */
    public void dismissRoom(String userId, String roomId, CB cb) {
        sendMsg(Msg.newDismissMsg(roomId, userId), new CB() {
            @Override
            public void complete(boolean succ, String msg) {
                leaveRoom(roomId, cb);
            }
        });
    }

    /**
     * 离开指定的所有房间
     */
    public void leaveAllRoom(List<String> roomList, CB cb) {
        if (roomList.size() <= 0) {
            cb.complete(true, null);
            return;
        }
        String roomId = roomList.get(0);
        leaveRoom(roomId, new CB() {
            @Override
            public void complete(boolean succ, String msg) {
                leaveAllRoom(roomList.subList(1, roomList.size()), cb);
            }
        });

    }

    public void login(String userId, CB cb) {

        String token = ZIMMngr.getToken(userId);
        ZIMMngr.login(zim, token, userId, new ZIMLoggedInCallback() {
            @Override
            public void onLoggedIn(ZIMError errorInfo) {

                if (errorInfo.getCode() != ZIMErrorCode.SUCCESS) {
                    Log.e(TAG, "login error:" + errorInfo.getMessage());
                    cb.complete(false, "登录失败");
                } else {
                    cb.complete(true, null);
                }
            }
        });
    }



    private void onRcvMsg(ArrayList<ZIMMessage> messageList) {
        if (mListener == null) return;
        for (ZIMMessage zimMessage : messageList) {
            if (zimMessage instanceof ZIMBarrageMessage) {//只看弹幕消息
                ZIMBarrageMessage zimTextMessage = (ZIMBarrageMessage) zimMessage;
                if (zimMessage.getTimestamp() < this.startTime)
                    continue;
                String fromUID = zimTextMessage.getSenderUserID();
                ZIMConversationType ztype = zimTextMessage.getConversationType();
                String toUID = zimTextMessage.getConversationID();
                Msg.MsgType type = Msg.MsgType.P2P;
//                if (ztype == ZIMConversationType.PEER) type = Msg.MsgType.P2P;
//                else if (ztype == ZIMConversationType.ROOM) type = Msg.MsgType.ROOM;
                String data = zimTextMessage.message;
//                String msgTxt = data.substring(1);
//                int colorIdx = 0;
//                try {
//                    colorIdx = Integer.parseInt(data.substring(0, 1));
//                } catch (Exception e) {
//                }
//                Msg msg = new Msg(type, msgTxt, zimMessage.getTimestamp(), fromUID, toUID);
//                msg.extInt = colorIdx;

                Msg msg = Msg.parseMsg(data, fromUID, toUID, ztype == ZIMConversationType.ROOM);
                mListener.onRcvMsg(msg);
            }
        }
    }

    public void setListener(MsgListener listener) {
        mListener = listener;
    }

    /**
     * token快过期了
     */
    private void onRenewToken() {
        zim.renewToken(getToken(mUser.userId), new ZIMTokenRenewedCallback() {
            @Override
            public void onTokenRenewed(String s, ZIMError zimError) {
                if (zimError.getCode() != ZIMErrorCode.SUCCESS) {
                    Log.e(TAG, zimError.getMessage());
                }

            }
        });
    }

    public static ZIMMngr getInstance(Application app) {
        if (null == mInstance) {
            synchronized (ZegoMngr.class) {
                if (null == mInstance) {
                    mInstance = new ZIMMngr(app);
                }
            }
        }
        return mInstance;
    }

    private ZIM createZIM(Application app, ZIMEventHandler handler) {
        // 创建 ZIM 对象，传入 APPID 与 Android 中的 Application
        ZIM zim = ZIM.create(KeyCenter.APP_ID, app);
        zim.setEventHandler(handler);
        return zim;
    }

    public void joinRoom(String roomId, CB cb) {
        zim.joinRoom(roomId, new ZIMRoomJoinedCallback() {
            @Override
            public void onRoomJoined(ZIMRoomFullInfo roomInfo, ZIMError errorInfo) {
                Log.e(TAG, ">>" + errorInfo.code);
                if (errorInfo.code == ZIMErrorCode.ROOM_DOES_NOT_EXIST) {
                    cb.complete(false, "房间不存在！");
                } else if (errorInfo.code == ZIMErrorCode.SUCCESS || errorInfo.code == ZIMErrorCode.THE_ROOM_ALREADY_EXISTS) {
                    cb.complete(true, roomInfo.baseInfo.roomName);
                }
            }
        });
    }

    private void inviteJoinRoom(String masterId, String roomId, String userId, CB cb) {//单方面拉群组，无须用户同意
        Msg msg = new Msg(Msg.MsgType.P2P, "invite://" + roomId, new Date().getTime(), masterId, userId);
        sendMsg(msg, new CB() {
            @Override
            public void complete(boolean succ, String msg) {
                if (succ) {
                    cb.complete(true, null);
                } else {
                    cb.complete(false, "发送邀请" + userId + "失败！");
                }
            }
        });

    }

    public void createRoom(String masterId, String roomId, String roomName, CB cb) {
        // 创建一个群组
        ZIMRoomInfo groupInfo = new ZIMRoomInfo();
        groupInfo.roomID = roomId;
        groupInfo.roomName = roomName;

        zim.createRoom(groupInfo, new ZIMRoomCreatedCallback() {
            @Override
            public void onRoomCreated(ZIMRoomFullInfo roomInfo, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {

                    inviteJoinRoom(masterId, roomId, CHATGPT_ID, cb);//这里把chagpt的用户id硬编码
                } else {
                    Log.e(TAG, "创建房间失败：" + errorInfo.message);
                    cb.complete(false, "房号已存在，请更换一个房间号！");
                }
            }
        });
    }

    public static void login(ZIM zim, String token, String userId, ZIMLoggedInCallback cb) {
        // 登录时，需要开发者 按照 "使用 Token 鉴权" 文档生成 token 即可
        // userID 和 userName，最大 32 字节的字符串。仅支持数字，英文字符 和 '~', '!',
        // '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+', '=', '-', '`',
        // ';', '’', ',', '.', '<', '>', '/', '\'。
        ZIMUserInfo zimUserInfo = new ZIMUserInfo();
        zimUserInfo.userID = userId;
        zimUserInfo.userName = userId;
        zim.login(zimUserInfo, token, cb);
    }

    //发送zim消息
    public void sendMsg(Msg msg, CB cb) {
        //p2p消息则发送Text，room发送弹幕类型消息
        ZIMMessage zimMsg = null;
        ZIMConversationType type;
        if (msg.type == Msg.MsgType.P2P) {
            ZIMTextMessage m = new ZIMTextMessage();
            m.message = msg.msg;
            zimMsg = m;
            type = ZIMConversationType.PEER;
        } else {
            ZIMBarrageMessage m = new ZIMBarrageMessage();
            m.message = msg.msg;
            zimMsg = m;
            type = ZIMConversationType.ROOM;
        }

        ZIMMessageSendConfig config = new ZIMMessageSendConfig();
        // 消息优先级，取值为 低:1 默认,中:2,高:3
        config.priority = ZIMMessagePriority.LOW;
        // 设置消息的离线推送配置
        ZIMPushConfig pushConfig = new ZIMPushConfig();
        pushConfig.title = "离线推送的标题";
        pushConfig.content = "离线推送的内容";
        config.pushConfig = pushConfig;


        zim.sendMessage(zimMsg, msg.toUID, type, config, new ZIMMessageSentCallback() {
            @Override
            public void onMessageAttached(ZIMMessage message) {
            }

            @Override
            public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                Log.e(TAG, ">>>>>>" + errorInfo.message);
                cb.complete(errorInfo.code == ZIMErrorCode.SUCCESS, errorInfo.message);

            }
        });

    }

    private ZIMMngr(Application app) {
        zim = createZIM(app, new ZIMEventHandler() {
            @Override
            public void onRoomMemberLeft(ZIM zim, ArrayList<ZIMUserInfo> memberList, final String roomID) {
//                ZIMMngr.this.onRoomMemberLeft(roomID, memberList);
            }

            @Override
            public void onReceivePeerMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromUserID) {
                onRcvMsg(messageList);
            }

            @Override
            public void onReceiveGroupMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromGroupID) {
                onRcvMsg(messageList);
            }

            @Override
            public void onReceiveRoomMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromRoomID) {
                onRcvMsg(messageList);
            }

            @Override
            public void onTokenWillExpire(ZIM zim, int second) {
                onRenewToken();
            }

            @Override
            public void onRoomStateChanged(ZIM zim, ZIMRoomState state, ZIMRoomEvent event, JSONObject extendedData, String roomID) {
                super.onRoomStateChanged(zim, state, event, extendedData, roomID);
                Log.e(TAG, "房间state发生变化" + event);
//                if (event == ZIMRoomEvent.DISMISSED) {
//                    if (null != mListener) {
//                        mListener.onDismissGroup(groupInfo.baseInfo.groupID);
//                    }
//                }
            }

            @Override
            public void onGroupStateChanged(ZIM zim, ZIMGroupState state, ZIMGroupEvent event, ZIMGroupOperatedInfo operatedInfo, ZIMGroupFullInfo groupInfo) {
                super.onGroupStateChanged(zim, state, event, operatedInfo, groupInfo);
//                if (event == ZIMGroupEvent.DISMISSED) {
//                    if (null != mListener) {
//                        mListener.onDismissGroupRoom(groupInfo.baseInfo.groupID);
//                    }
//                }
            }
        });
    }

    public static String getToken(String userId) {
        TokenServerAssistant.VERBOSE = true;    // 调试时，置为 true, 可在控制台输出更多信息；正式运行时，最好置为 false

        try {
            TokenServerAssistant.TokenInfo token = TokenServerAssistant.generateToken(KeyCenter.APP_ID, userId, KeyCenter.SERVER_SECRET, 60 * 60);

            Log.e(">>>", token.data);

            return token.data;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }
}
