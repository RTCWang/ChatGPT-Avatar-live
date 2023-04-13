
import { BingAIClient } from '@waylaidwanderer/chatgpt-api';

export class BingGPT {
    /*
    * http_proxy, apiKey
    **/
    constructor(http_proxy, userCookie) {
        this.api = this.init(http_proxy, userCookie);
        this.conversationSignature = "";
        this.conversationId = "";
        this.clientId = "";
        this.invocationId = "";
    }
    init(http_proxy, userCookie) {
       console.log(http_proxy, userCookie)
        const options = {
            // Necessary for some people in different countries, e.g. China (https://cn.bing.com)
            host: 'https://www.bing.com',
            // "_U" cookie from bing.com
            userToken: userCookie,
            // If the above doesn't work, provide all your cookies as a string instead
            cookies: '',
            // A proxy string like "http://<ip>:<port>"
            proxy: http_proxy,
            // (Optional) Set to true to enable `console.debug()` logging
            debug: false,
        };

        return new BingAIClient(options);
    }
    //调用chatpgt 
    chat(text, cb) {
        var res=""
        var that = this;
        console.log("正在向bing发送提问", text ) 
        this.api.sendMessage(text, {
            // conversationSignature: that.conversationSignature,
            // conversationId: that.conversationId,
            // clientId: that.clientId,
            // invocationId: that.invocationId, 
            toneStyle: 'balanced',
            onProgress: (token) => { 
                if(token.length==2 && token.charCodeAt(0)==55357&&token.charCodeAt(1)==56842){
                    cb(true, res);
                } 
                res+=token;
            }
        }).then(function(response){

            that.conversationSignature = response.conversationSignature;
            that.conversationId = response.conversationId;
            that.clientId = response.clientId;
            that.invocationId = response.invocationId;
        }) ;  

    }
} 

