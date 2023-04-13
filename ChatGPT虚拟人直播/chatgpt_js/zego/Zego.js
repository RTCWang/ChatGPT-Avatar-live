
var fs = require('fs');
//先清理缓存
fs.readdirSync('./local_storage').forEach(function (fileName) {
    fs.unlinkSync('./local_storage/' + fileName);
});

const KEY_CENTER = require("../KeyCenter.js");
const APPID = KEY_CENTER.APPID, SERVER_SECRET = KEY_CENTER.SERVER_SECRET;
const generateToken04 = require('./TokenUtils.js').generateToken04;
var LocalStorage = require('node-localstorage').LocalStorage;
localStorage = new LocalStorage('./local_storage');
var indexedDB = require("fake-indexeddb/auto").indexedDB;
const jsdom = require("jsdom");
const { JSDOM } = jsdom;
const dom = new JSDOM(``, {
    url: "http://localhost/",
    referrer: "http://localhost/",
    contentType: "text/html",
    includeNodeLocations: true,
    storageQuota: 10000000
});
window = dom.window;
document = window.document;
navigator = window.navigator;
location = window.location;
WebSocket = window.WebSocket;
XMLHttpRequest = window.XMLHttpRequest;
const ZIM = require('./index.js').ZIM;
// console.log("<<<",window.WebSocket)

function newToken(userId) {
    const token = generateToken04(APPID, userId, SERVER_SECRET, 60 * 60 * 24, '');
    return token;
}
function leaveRoom(zim, roomId) {

    zim.leaveRoom(roomId)
        .then(function ({ roomID }) {
            // 操作成功
            console.log("已离开房间", roomID)
        })
        .catch(function (err) {
            // 操作失败
            console.log("离开房间失败", err)
        });

}

function createZIM(onError, onRcvMsg, onTokenWillExpire) {
    var zim = ZIM.create(APPID);
    zim.on('error', onError);
    zim.on('receivePeerMessage', function (zim, msgObj) {
        console.log("收到P2P消息")
        onRcvMsg(false, zim, msgObj)
    });
    // 收到群组消息的回调
    zim.on('receiveRoomMessage', function (zim, msgObj) {
        console.log("收到群组消息")
        onRcvMsg(true, zim, msgObj)
    });

    zim.on('tokenWillExpire', onTokenWillExpire);

    return zim;
}
function login(zim, userId, token, cb) {
    var userInfo = { userID: userId, userName: userId };

    zim.login(userInfo, token)
        .then(function () {
            cb(true, null);
        })
        .catch(function (err) {
            cb(false, err);
        });
}
function joinRoom(zim, roomId, cb = null) {
    zim.joinRoom(roomId)
        .then(function ({ roomInfo }) {

            cb && cb(true, roomInfo);
        })
        .catch(function (err) {
            cb && cb(false, err);
        });
}

function sendMsg(zim, isGroup, msg, toUID, cb) {
    // if (isGroup) sendRoomMessage(zim, toUID, msg, cb);
    // else sendP2PMsg(zim, toUID, msg, cb);

    var type = isGroup ? 1 : 0; // 会话类型，取值为 单聊：0，房间：1，群组：2
    var config = {
        priority: 1, // 设置消息优先级，取值为 低：1（默认），中:2，高：3
    };

    var messageTextObj = { type: 20, message: msg, extendedData: '' };
    var notification = {
        onMessageAttached: function (message) {
            // todo: Loading
            console.log("已发送", message)
        }
    }

    zim.sendMessage(messageTextObj, toUID, type, config, notification)
        .then(function ({ message }) {
            // 发送成功
            cb(true, null);
        })
        .catch(function (err) {
            // 发送失败
            cb(false, err)
        });

}

function parseMsg(zim, isFromGroup, msg, fromUid) {
    // console.log("rcv", isFromGroup, msg, fromUid);
    var out = {};
    out.fromUID = fromUid;
    out.isFromGroup = isFromGroup;
    if (msg.startsWith("danmu://")) {
        msg = msg.substring(8);
        out.proto = "danmu";
        var idx = msg.indexOf(",");
        if (idx > 0) {
            msg = msg.substring(idx + 1)
        }
        out.msg = msg;
        return out;
    } else if (msg.startsWith("invite://")) {
        out.proto = "invite";
        var roomId = msg.substring(9);
        zim.joinRoom(roomId)
            .then(function ({ roomInfo }) {
                console.log(true, roomInfo);
            })
            .catch(function (err) {
                console.log(false, err);
            });
        return null;
    } else if (msg.startsWith("dismiss://")) {
        var roomId = msg.substring(10);
        leaveRoom(zim, roomId)
        return null;
    }
    return null;
}
function initZego(onError, onRcvMsg, myUID) {
    var token = newToken(myUID);
    var startTimestamp = new Date().getTime();
    function _onError(zim, err) {
        onError(err);
    }
    function _onRcvMsg(isFromGroup, zim, msgObj) {
        var msgList = msgObj.messageList;
        var fromConversationID = msgObj.fromConversationID;
        msgList.forEach(function (msg) {
            if (msg.timestamp - startTimestamp >= 0) { //过滤掉离线消息
                var out = parseMsg(zim, isFromGroup, msg.message, fromConversationID)
                if (out)
                    onRcvMsg(out);
                // sendMsg(zim, true, "收到消息", fromConversationID, function (succ, msg) {
                //     console.log(succ, msg)
                // })
            }
        })

    }
    function onTokenWillExpire(zim, second) {
        token = newToken(userId);
        zim.renewToken(token);
    }
    var zim = createZIM(_onError, _onRcvMsg, onTokenWillExpire);
    login(zim, myUID, token, function (succ, data) {
        if (succ) {
            console.log("登录成功！")

        } else {
            console.log("登录失败！", data)
        }
    })
    return zim;
}
module.exports = {
    initZego: initZego,
    sendMsg: sendMsg,
    joinRoom: joinRoom
}