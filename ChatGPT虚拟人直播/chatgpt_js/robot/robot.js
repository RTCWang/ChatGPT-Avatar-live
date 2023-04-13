
const KEY_CENTER = require("../KeyCenter.js");
var ChatGPTObj = null, BingGPTObj = null;

//初始化chatgpt
function getChatGPT(onInitedCb) {
    if (ChatGPTObj != null) {
        onInitedCb(true, ChatGPTObj);
        return;
    }
    (async () => {
        let { ChatGPT } = await import("./chatgpt.mjs");
        return new ChatGPT(KEY_CENTER.HTTP_PROXY, KEY_CENTER.API_KEY);
    })().then(function (obj) {
        ChatGPTObj = obj;
        onInitedCb(true, obj);
    }).catch(function (err) {
        onInitedCb(false, err);
    });
}

function getBingGPT(onInitedCb){
    if(BingGPTObj!=null) {
        onInitedCb(true, BingGPTObj);
        return;
    }
    (async () => {
        let { BingGPT } = await import("./binggpt.mjs");
        return new BingGPT(KEY_CENTER.HTTP_PROXY, KEY_CENTER.BING_USER_COOKIE);
    })().then(function (obj) {
        BingGPTObj = obj;
        onInitedCb(true, obj);
    }).catch(function (err) {
        console.log(err)
        onInitedCb(false, err);
    });
}

//调用chatgpt聊天
function chatGPT(text, cb) {
    getChatGPT(function (succ, obj) {
        if (succ) {
            obj.chat(text, cb);
        } else {
            cb && cb(false, "chatgpt not inited!!!");
        }
    })
}

function chatBing(text, cb){
    getBingGPT(function (succ, obj) {
        if (succ) {
            obj.chat(text, cb);
        } else {
            cb && cb(false, "chatgpt not inited!!!");
        }
    })

}

module.exports = {
    chatGPT: chatGPT,
    chatBing:chatBing
} 