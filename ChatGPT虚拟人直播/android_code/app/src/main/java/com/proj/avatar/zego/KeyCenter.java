package com.proj.avatar.zego;

import android.content.Context;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;
import com.proj.avatar.utils.HttpRequest;
import com.zego.avatar.ZegoAvatarService;

public class KeyCenter {

    // 控制台地址: https://console.zego.im/dashboard
    // 可以在控制台中获取APPID，并将APPID设置为long型，例如：APPID = 123456789L.
    public static long APP_ID = ;  //这里填写APPID
    public static String APP_SIGN = "";
    // 在控制台找到ServerSecret，并填入如下
    public static String SERVER_SECRET = ""; //这里填写服务器端密钥
    // 鉴权服务器的地址
    public final static String BACKEND_API_URL = "https://aieffects-api.zego.im?Action=DescribeAvatarLicense";
    !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    !!!!!!!!!请根据自己的配置填写以上变量

    public static String avatarLicense = null;
    public static String getURL(String authInfo) {
        Uri.Builder builder = Uri.parse(BACKEND_API_URL).buildUpon();
        builder.appendQueryParameter("AppId", String.valueOf(APP_ID));
        builder.appendQueryParameter("AuthInfo", authInfo);

        return builder.build().toString();
    }

    public interface IGetLicenseCallback {
        void onGetLicense(int code, String message, ZegoLicense license);
    }


    /**
     * 在线拉取 license
     * @param context
     * @param callback
     */
    public static void getLicense(Context context, final IGetLicenseCallback callback) {
        requestLicense(ZegoAvatarService.getAuthInfo(APP_SIGN, context), callback);
    }

    /**
     * 获取license
     * */
    public static void requestLicense(String authInfo, final IGetLicenseCallback callback) {

        String url = getURL(authInfo);

        HttpRequest.asyncGet(url, ZegoLicense.class, (code, message, responseJsonBean) -> {
            if (callback != null) {
                callback.onGetLicense(code, message, responseJsonBean);
            }
        });
    }


    public class ZegoLicense {
        @SerializedName("License")
        private String license;
        public String getLicense() {
            return license;
        }
        public void setLicense(String license) {
            this.license = license;
        }
    }

}
