package com.capstone.while1.beaconandroidstudio;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import static android.content.SharedPreferences.Editor;

/**
 * Created by AP047572 on 4/15/2017.
 */

public class SavedPreferences {
    //does this need to be set initially?
    static final String PREF_USER = "username";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setUserName(Context ctx, String userName) {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER, userName);
        editor.commit();
    }

    public static String getUserName(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_USER, "");
    }

    public static void saveString(Context ctx, String key, String value) {
        getSharedPreferences(ctx).edit().putString(key, value).apply();
    }

    public static String getString(Context ctx, String key) {
        return getSharedPreferences(ctx).getString(key, ctx.getString(R.string.stringNotFound));
    }

    public static void removeString(Context ctx, String key) {
        getSharedPreferences(ctx).edit().remove(key).apply();
    }
}
