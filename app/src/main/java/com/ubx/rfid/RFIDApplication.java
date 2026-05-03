package com.ubx.rfid;

import android.app.Application;
import android.content.Context;

import com.ubx.rfid.util.sharedPreference.SPUtils;

/**
 * Application class de la app RFID Urovo.
 */
public class RFIDApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        SPUtils.init(this);
    }

    public static Context getContext() {
        return mContext;
    }
}
