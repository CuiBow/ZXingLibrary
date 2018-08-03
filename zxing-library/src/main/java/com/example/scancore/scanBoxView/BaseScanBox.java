package com.example.scancore.scanBoxView;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by cuibowen on 2018/3/5.
 */

public abstract class BaseScanBox extends View {
    public BaseScanBox(Context context) {
        super(context);
    }

    public BaseScanBox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseScanBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract void drawViewfinder();

    public abstract void closeDrawViewfinder();

    public abstract void recycle();

}
