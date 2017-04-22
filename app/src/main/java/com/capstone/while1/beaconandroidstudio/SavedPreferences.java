package com.capstone.while1.beaconandroidstudio;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static android.content.SharedPreferences.Editor;

class SavedPreferences {
    //does this need to be set initially?
    static final String PREF_USER = "username";

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    static void setUserName(Context ctx, String userName) {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER, userName);
        editor.apply();
    }

    static String getUserName(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_USER, "");
    }

    static void saveString(Context ctx, String key, String value) {
        getSharedPreferences(ctx).edit().putString(key, value).apply();
    }

    static String getString(Context ctx, String key) {
        return getSharedPreferences(ctx).getString(key, ctx.getString(R.string.stringNotFound));
    }

    static void removeString(Context ctx, String key) {
        getSharedPreferences(ctx).edit().remove(key).apply();
    }
}
