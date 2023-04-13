package com.proj.avatar.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Cfg {

    public static void saveKV(Context ctx, String k, String v) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        prefs.edit().putString(k, v).commit();
    }

    public static String getKV(Context ctx, String k) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String value = prefs.getString(k, "");
        return value;

    }

}
