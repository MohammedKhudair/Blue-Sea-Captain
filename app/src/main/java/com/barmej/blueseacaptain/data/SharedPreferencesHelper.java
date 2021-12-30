package com.barmej.blueseacaptain.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesHelper {
    private static final String CAPTAIN_ID = "CAPTAIN_ID";

    public static void setCaptainId(String id, Context content){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(content);
        sp.edit().putString(CAPTAIN_ID, id).apply();
    }

    public static String getCaptainId(Context content){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(content);
        return sp.getString(CAPTAIN_ID,"");
    }
}
