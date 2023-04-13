package com.proj.avatar.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class HttpRequest {

    public static final int ErrorJSONFormatInvalid = -1;
    public static final int ErrorFailNetwork = -2;

    private static final String TAG = "HttpRequest";

    private static Gson mGson = new Gson();
    private static final Handler okHandler = new Handler(Looper.getMainLooper());

    public static Gson getGson() {
        return mGson;
    }

    public static class OkHttpInstance{

        private volatile static OkHttpInstance instance;
        private OkHttpClient mOkHttpClient;

        private OkHttpInstance() {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS);
            mOkHttpClient = builder.build();
        }

        public static OkHttpClient getInstance()
        {
            if (instance == null) {
                synchronized (OkHttpInstance.class) {
                    if (instance == null) {
                        instance = new OkHttpInstance();
                    }
                }
            }
            return instance.mOkHttpClient;
        }

    }
    public interface IAsyncGetCallback<T> {
        void onResponse(int errorCode, String message, T responseJsonBean);
    }

    public static <T> void asyncGet(@NotNull String url, final Class<T> classType, final IAsyncGetCallback<T> reqCallback) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        OkHttpInstance.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) {
                String str = "";
                try {
                    str = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final String finalStr = str;
                Log.d(TAG, "onResponse: api: " + call.request().url().toString());
                Log.d(TAG, "onResponse: respJson: " + finalStr);

                try {
                    JSONObject jsonObject = new JSONObject(finalStr);
                    final int code = jsonObject.getInt("Code");
                    final String message = jsonObject.getString("Message");
                    String dataJson = "";
                    try {
                        dataJson = jsonObject.getJSONObject("Data").toString();
                    }catch (Exception jsonException)
                    {
                        dataJson = "";
                    }

                    if (reqCallback != null) {
                        final T bean = mGson.fromJson(dataJson, classType);

                            okHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    reqCallback.onResponse(code, message, bean);
                                }
                            });

                    }
                } catch (Exception jsonException) {
                    jsonException.printStackTrace();
                    if (reqCallback != null) {
                        okHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                reqCallback.onResponse(ErrorJSONFormatInvalid, "Json解析异常", null);
                            }
                        });
                    }
                }


            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());

                if (reqCallback != null) {
                    okHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            reqCallback.onResponse(ErrorFailNetwork, "网络异常", null);
                        }
                    });
                }
            }
        });

    }

}
