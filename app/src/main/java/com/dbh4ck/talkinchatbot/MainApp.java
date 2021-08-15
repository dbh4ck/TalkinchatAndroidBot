package com.dbh4ck.talkinchatbot;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

public class MainApp extends Application {

    private static MainApp mainApp;
    private Handler mHandler;
    private SharedPreferences mSharedPrefs;

    public static MainApp getMainApp() {
        return mainApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mainApp = this;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void runOnUiThread(Runnable runnable) {
        if(mHandler == null){
            mHandler = new Handler(Looper.getMainLooper());
        }
        mHandler.post(runnable);
    }

    public SharedPreferences getSharedPrefs() {
        if(mSharedPrefs == null){
            mSharedPrefs = getSharedPreferences("default", MODE_PRIVATE);
        }
        return mSharedPrefs;
    }

}
