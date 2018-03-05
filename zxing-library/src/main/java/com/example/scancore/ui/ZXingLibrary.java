package com.example.scancore.ui;

import android.content.Context;
import android.util.DisplayMetrics;


/**
 * Created by aaron on 16/9/7.
 */

public class ZXingLibrary {
    private static Context mContext;

    public static void init(Context context) {
        mContext=context;
    }

    public static Context getContext() {
        return mContext;
    }
}
