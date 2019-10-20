package com.gyh.fileindex.util;

import android.app.Application;

public class AppConfig extends Application {
    public static AppConfig mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }
}
