package com.netease.demo;


import android.app.Application;

import com.faceunity.beautycontrolview.FURenderer;


public class CrashApplication extends Application {

    private static CrashApplication crashApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        crashApplication = this;
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        FURenderer.initFURenderer(this);
    }

    public static CrashApplication getInstance() {
        return crashApplication;
    }
}

