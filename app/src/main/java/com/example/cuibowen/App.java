package com.example.cuibowen;

import android.app.Application;

import com.example.scancore.ui.ZXingLibrary;

/**
 * Created by cuibowen on 2018/3/2.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ZXingLibrary.init(this);
    }
}
