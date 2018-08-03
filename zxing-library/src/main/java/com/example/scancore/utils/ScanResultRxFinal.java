package com.example.scancore.utils;

import android.graphics.Bitmap;

/**
 * by cuibowen on 03/08/2018.
 * <p>
 * 扫码结果回调
 */

public class ScanResultRxFinal {

    private RxScanResultListener listener = null;

    private static final class ScanResultRxFinalHolder {
        private static final ScanResultRxFinal SCAN_RESULT_RX_FINAL = new ScanResultRxFinal();
    }

    public static ScanResultRxFinal get() {
        return ScanResultRxFinalHolder.SCAN_RESULT_RX_FINAL;
    }


    public ScanResultRxFinal init(RxScanResultListener listener) {
        this.listener = listener;
        return this;
    }

    public void onScanResult(Bitmap bitmap,String result) {
        if (listener!=null){
            if (result==null){
                result="";
            }
            listener.onScanSuccessResult(bitmap,result);
        }
    }
    public interface RxScanResultListener {

        void onScanSuccessResult(Bitmap bitmap, String result);
    }
}
