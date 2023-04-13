import { ChatGPTAPI } from "chatgpt";
import proxy from "https-proxy-agent";
import nodeFetch from "node-fetch";

export class ChatGPT {
  
    constructor(http_proxy, apiKey) {
        this.api = this.init(http_proxy, apiKey);
        this.conversationId = null;
        this.ParentMessageId = null;
    }
    init(http_proxy, apiKey) {
        console.log(http_proxy, apiKey)
        return new ChatGPTAPI({
            apiKey: apiKey,
            fetch: (url, options = {}) => {
                const defaultOptions = {
                    agent: proxy(http_proxy),
                };

                const mergedOptions = {
                    ...defaultOptions,
                    ...options,
                };

                return nodeFetch(url, mergedOptions);
            },
        });
    }
    //调用chatpgt 
    chat(text, cb) {
        let that = this
        console.log("正在向ChatGPT发送提问:", text)
        that.api.sendMessage(text, {
            conversationId: that.ConversationId,
            parentMessageId: that.ParentMessageId
        }).then(
            function (res) {
                that.ConversationId = res.conversationId
                that.ParentMessageId = res.id
                cb && cb(true, res.text)
            }
        ).catch(function (err) {
            console.log(err)
            cb && cb(false, err);
        });
    }
} 