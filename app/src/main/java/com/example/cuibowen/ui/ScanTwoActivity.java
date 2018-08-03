package com.example.cuibowen.ui;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cuibowen.camera.R;
import com.example.scancore.ui.ScanFragment;
import com.example.scancore.ui.ScanUtil;
import com.example.scancore.utils.ScanResultRxFinal;

public class ScanTwoActivity extends AppCompatActivity {
    private ScanFragment scanFragment;
    private boolean isTorch=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scanFragment =new ScanFragment();
        ScanUtil.setFragmentArgs(scanFragment, R.layout.fragment_my_scan);
        getSupportFragmentManager().beginTransaction().replace(R.id.scan_fragment, scanFragment).commit();
        scanFragment.setScanCallback(new ScanUtil.ScanCallback() {
            @Override
            public void onScanSuccess(Bitmap mBitmap, String result) {
                ScanResultRxFinal.get().onScanResult(mBitmap,result);
                finish();
            }
            @Override
            public void onScanFailed() {

            }
        });
    }
    //重新扫描
    public void scanAgain(View view){
        if (scanFragment!=null){
            scanFragment.scanAgain();
        }
    }
    //闪光灯
    public void torch(View view){
        if (scanFragment!=null){
            if (isTorch){
                isTorch=false;
                scanFragment.setTorch(false);
            }else{
                isTorch=true;
                scanFragment.setTorch(true);
            }
        }
    }
    //相册
    public void photo(View view){
        if (scanFragment!=null){
            scanFragment.startPhotoAlbum();
        }
    }

}
