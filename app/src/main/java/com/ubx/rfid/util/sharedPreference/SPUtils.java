package com.ubx.rfid.util.sharedPreference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Utilidad de SharedPreferences (singleton).
 */
public class SPUtils {

    private static final String SP_NAME = "rfid_prefs";
    private static SPUtils sInstance;
    private SharedPreferences mSP;

    private SPUtils(Context context) {
        mSP = context.getApplicationContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = new SPUtils(context);
        }
    }

    public static SPUtils getInstance() {
        if (sInstance == null) throw new IllegalStateException("SPUtils not initialized. Call init() first.");
        return sInstance;
    }

    public void putString(String key, String value) {
        mSP.edit().putString(key, value).apply();
    }

    public String getString(String key, String defValue) {
        return mSP.getString(key, defValue);
    }

    public void putInt(String key, int value) {
        mSP.edit().putInt(key, value).apply();
    }

    public int getInt(String key) {
        return mSP.getInt(key, -1);
    }

    public int getInt(String key, int defValue) {
        return mSP.getInt(key, defValue);
    }

    public void putBoolean(String key, boolean value) {
        mSP.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return mSP.getBoolean(key, defValue);
    }
}
