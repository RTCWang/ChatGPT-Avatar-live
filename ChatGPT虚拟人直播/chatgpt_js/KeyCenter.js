//------------即构相关配置------------//
// 控制台地址: https://console.zego.im/dashboard
// 可以在控制台中获取APPID，并将APPID设置为long型，例如：APPID = 123456789L.
const APPID = ;  //这里填写APPID
// 在控制台找到ServerSecret，并填入如下
const SERVER_SECRET = ""; //这里填写服务器端密钥

//---------------chatGPT相关-----------------//

const HTTP_PROXY = "http://127.0.0.1:7890";//本地vpn代理端口
//openAI的key
const API_KEY = "";
//bing cookie
const BING_USER_COOKIE = '';

!!!!!!请根据自己的配置填写以上变量

module.exports = {
    APPID: APPID,
    SERVER_SECRET: SERVER_SECRET,
    HTTP_PROXY: HTTP_PROXY,
    API_KEY: API_KEY,
    BING_USER_COOKIE:BING_USER_COOKIE
}
