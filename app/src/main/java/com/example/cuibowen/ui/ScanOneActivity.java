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

public class ScanOneActivity extends AppCompatActivity {
    private  ScanFragment scanFragment;
    private ImageView image;
    private boolean isTorch=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_two);
        image=findViewById(R.id.image);
        scanFragment =new ScanFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.scan_fragment, scanFragment).commit();
        scanFragment.setScanCallback(new ScanUtil.ScanCallback() {
            @Override
            public void onScanSuccess(Bitmap mBitmap, String result) {
                Toast.makeText(ScanOneActivity.this,result,Toast.LENGTH_SHORT).show();
                image.setImageBitmap(mBitmap);
            }
            @Override
            public void onScanFailed() {

            }
        });
    }
    public void scanAgain(View view){
        if (scanFragment!=null){
            scanFragment.scanAgain();
        }
        if (image!=null){
            image.setImageBitmap(null);
        }
    }
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

}
