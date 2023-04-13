let { chatGPT, chatBing } = require('./robot/robot');

const Zego = require('./zego/Zego.js');
var isSpeaking = false;//一次只对一个评论
var zim;
function onError(err) {
    console.log("on error", err);
}
//调用chatpgt或bingChat
function chat(text, cb) {
    if (isSpeaking) return;
    isSpeaking = true;
    chatGPT(text, function (succ, res) {
        if (!succ) console.log("请求chatgpt失败", res)
        cb(succ, res);
        isSpeaking = false;
    })
}


var msg_buff = [];
function tryReply() {
    console.log("try reply....")
    if (isSpeaking || msg_buff.length <= 0) return;//chatgpt正在回复，等待
    var maxVal = "", maxIdx = -1;

    msg_buff.forEach(function (val, idx) {
        if ((new Set(val.msg)).size > maxIdx) {
            maxVal = val; maxIdx = idx;
        }
    })
    if (maxIdx < 0) return;

    var msgObj = msg_buff[maxIdx];
    msg_buff = [];

    chat(msgObj.msg, function (isSucc, text) {
        if (isSucc)
            Zego.sendMsg(zim, msgObj.isFromGroup, text, msgObj.fromUID, function (succ, err) {
                if (!succ) {
                    console.log("回复即构消息发送失败:", msg, err);
                }
                tryReply();//回复完一条，再从缓存里面查看是否有新的消息继续回复
            })
    })
}
//收到评论文本消息
function onRcvZegoMsg(msgObj) {
    console.log(msgObj)
    var rcvText = msgObj.msg;
    if (rcvText.length <= 1) return;
    msg_buff.push(msgObj);
    tryReply();

}
function main() {
    let zegoChatGPTUID = "chatgpt"
    zim = Zego.initZego(onError, onRcvZegoMsg, zegoChatGPTUID);
    // Zego.joinRoom(zim, 'cr_6666')

}
main();
