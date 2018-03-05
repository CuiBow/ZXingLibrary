package com.example.scancore.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.example.scancore.ui.ZXingLibrary;

/**
 * @author yang
 */
public class ScreenUtils {
    private ScreenUtils() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 获得屏幕高度
     * * @return
     */
    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) ZXingLibrary.getContext().getSystemService(
                Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();       //获取当前窗口的一些信息数据
        wm.getDefaultDisplay().getMetrics(outMetrics);          //将获取到的宽高维度存放到 DisplayMetris类中
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕宽度
     * * @return
     */
    public static int getScreenHeight() {
        WindowManager wm = (WindowManager) ZXingLibrary.getContext().getSystemService(
                Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * 获得状态栏的高度
     * * @return
     */
    public static int getStatusHeight() {

        int statusHeight = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField("status_bar_height")
                    .get(object).toString());
            statusHeight = ZXingLibrary.getContext().getResources()
                    .getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusHeight;
    }

    /**
     * 获取屏幕高度-状态栏高度
     * @return
     */
    public static int getScreenHeiSubStatusHei(){
        return getScreenHeight()-getStatusHeight();
    }

    /**
     * 获取当前屏幕截图，包含状态栏
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int width = getScreenWidth();
        int height = getScreenHeight();
        Bitmap bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
        bmp.recycle();
        view.destroyDrawingCache();
        return bp;

    }

    /**
     * 获取当前屏幕截图，不包含状态栏
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithoutStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        int width = getScreenWidth();
        int height = getScreenHeight();
        Bitmap bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height
                - statusBarHeight);
        bmp.recycle();
        view.destroyDrawingCache();
        return bp;
    }


}
