package com.netease.demo;


import android.app.Application;

import com.faceunity.FURenderer;

public class CrashApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
        FURenderer.initFURenderer(this);
    }

}

