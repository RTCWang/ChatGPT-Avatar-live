package com.proj.avatar.zego.avatar.apiservice.text.internal;

import android.annotation.SuppressLint;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import com.proj.avatar.zego.avatar.apiservice.text.TextConstants;
import com.proj.avatar.zego.avatar.apiservice.text.TextExpressionAudioData;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 内置的TextExpressionRequestDelegate
 */
public class ZegoTextExpressionRequestDelegate {

    public final static String TAG = "ZegoTextExpressionRequestDelegate";

    public static String API_URL = "https://general-ai-gugong-smprod-base.zego.im/general/text_driven";

    private String text;
    private long timestamp;
    private long app_id = 0;
    private String app_sign = null;
    private String sign;
    private double fps = 30;

    private double volume = 5;
    private double speed = 5;
    private int voice_type = 11002;
    private int primary_language = 1;
    private int sample_rate = 16000;
    private String codec = "pcm";

    public void setAppId(long id){
        app_id = id;
    }

    public void setAppSign(String sign){
        app_sign = sign;
    }

    public void setFps(double fps) {
        this.fps = fps;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setVoiceType(int voice_name) {
        this.voice_type = voice_name;
    }

    public void setPrimaryLanguage(int primary_language) {
        this.primary_language = primary_language;
    }

    public void setSampleRate(int sample_rate) {
        this.sample_rate = sample_rate;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    @SuppressLint("LongLogTag")
    public TextExpressionAudioData request(String textBlock) {

        //生成16进制随机字符串(16位)
        byte[] bytes = new byte[8];
        //使用SecureRandom获取高强度安全随机数生成器
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(bytes);
        String signatureNonce = bytesToHex(bytes);
        timestamp = System.currentTimeMillis() ;
        sign = GenerateSignature(app_id, signatureNonce, app_sign.substring(0, 32), timestamp);

        String url = API_URL;
        //1,创建OKhttpClient对象
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS);
        OkHttpClient mOkHttpClient = builder.build();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("text", textBlock);
            jsonObject.put("timestamp", timestamp);
            jsonObject.put("app_id", app_id);
            jsonObject.put("sign", sign);
            jsonObject.put("nonce", signatureNonce);
            jsonObject.put("fps", fps);
            jsonObject.put("volume", volume);
            jsonObject.put("speed", speed);
            jsonObject.put("voice_type", voice_type);
            jsonObject.put("primary_language", primary_language);
            jsonObject.put("sample_rate", sample_rate);
            jsonObject.put("codec", codec);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //2,创建Request
        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json;charset=utf-8"));

        Request request = new Request.Builder().url(url).post(body).build();
        //3，创建call对象并将请求对象添加到调度中
        try {
            Response response = mOkHttpClient.newCall(request).execute();
            if (response.isSuccessful())
            {
                String ret = response.body().string();
                Log.d(TAG, "request text block = " + textBlock);
                Log.d(TAG, "response = " + ret);
                return parseRequest(ret);
            }else{
                response.body().close();
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 字节数组转16进制
     * @param bytes 需要转换的byte数组
     * @return  转换后的Hex字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuffer md5str = new StringBuffer();
        //把数组每一字节换成16进制连成md5字符串
        int digital;
        for (int i = 0; i < bytes.length; i++) {
            digital = bytes[i];
            if (digital < 0) {
                digital += 256;
            }
            if (digital < 16) {
                md5str.append("0");
            }
            md5str.append(Integer.toHexString(digital));
        }
        return md5str.toString();
    }

    // Signature=md5(AppId + SignatureNonce + ServerSecret + Timestamp)
    private String GenerateSignature(long appId, String signatureNonce, String serverSecret, long timestamp){
        String str = String.valueOf(appId) + signatureNonce + serverSecret + String.valueOf(timestamp);
        String signature = "";
        try{
            //创建一个提供信息摘要算法的对象，初始化为md5算法对象
            MessageDigest md = MessageDigest.getInstance("MD5");
            //计算后获得字节数组
            byte[] bytes = md.digest(str.getBytes("utf-8"));
            //把数组每一字节换成16进制连成md5字符串
            signature = bytesToHex(bytes);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return signature;
    }

    @SuppressLint("LongLogTag")
    private TextExpressionAudioData parseRequest(String body){

        TextExpressionAudioData audioData = new TextExpressionAudioData();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(body);
            if(jsonObject.has("code")){
                audioData.code = jsonObject.getInt("code");
            }
            if(jsonObject.has("message")){
                audioData.message = jsonObject.getString("message");
            }
            if(jsonObject.has("unique_id")){
                audioData.uniqueID = jsonObject.getString("unique_id");
            }
            if(jsonObject.has("data")){
                JSONObject dataObj = jsonObject.getJSONObject("data");
                if(dataObj.has("fps")){
                    audioData.fps = dataObj.getInt("fps");
                }
                if(dataObj.has("sample_rate")){
                    audioData.sampleRate = dataObj.getInt("sample_rate");
                }
                if(dataObj.has("codec")){
                    audioData.codec = dataObj.getString("codec");
                }
                if(dataObj.has("audio")){
                    audioData.audioData = dataObj.getString("audio");
                }
                if(dataObj.has("frame_sequence")){
                    JSONArray frames = dataObj.getJSONArray("frame_sequence");
                    for(int i = 0 ; i < frames.length() ; i++){
                        JSONArray frame = frames.getJSONArray(i);
                        Integer[] expressions = new Integer[TextConstants.EXP_COUNT];
                        for(int j = 0; j < Math.min(frame.length(), TextConstants.EXP_COUNT); j++){
                            expressions[j] = frame.getInt(j);
                        }
                        audioData.expressionList.add(expressions);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "parseRequest error");
            return null;
        }

        return audioData;
    }
}