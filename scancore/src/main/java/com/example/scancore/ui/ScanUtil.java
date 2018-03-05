package com.example.scancore.ui;

import android.graphics.Bitmap;
import android.os.Bundle;

import com.example.scancore.scanBoxView.BaseScanBox;

/**
 * Created by cuibowen on 2018/3/2.
 */

public class ScanUtil {

    public static final String LAYOUT_ID = "layout_id";
    /**
     * 解析二维码结果
     */
    public interface ScanCallback{

        public void onScanSuccess(Bitmap mBitmap, String result);

        public void onScanFailed();
    }


    /**
     * 为CaptureFragment设置layout参数
     * @param captureFragment
     * @param layoutId
     */
    public static void setFragmentArgs(ScanFragment captureFragment, int layoutId) {
        if (captureFragment == null || layoutId == -1) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(LAYOUT_ID, layoutId);
        captureFragment.setArguments(bundle);
    }
}
