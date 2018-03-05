package com.example.scancore.ui;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.scancore.R;
import com.example.scancore.camera.CameraManager;
import com.example.scancore.decoding.CaptureActivityHandler;
import com.example.scancore.decoding.InactivityTimer;
import com.example.scancore.scanBoxView.BaseScanBox;
import com.example.scancore.scanBoxView.ScanBoxView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by cuibowen on 2018/3/2.
 */

public class ScanFragment extends Fragment implements SurfaceHolder.Callback{
    private static final String TAG=ScanFragment.class.getSimpleName();
    //扫码
    private CaptureActivityHandler handlers;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Handler mHandler = new Handler();
    private BaseScanBox scanViewBox;
    private ImageView image;
    private SurfaceView surfaceView;
    private boolean vibrate;
    private ScanUtil.ScanCallback scanCallback;


    public void setScanCallback(ScanUtil.ScanCallback scanCallback) {
        this.scanCallback = scanCallback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraManager.init(getActivity().getApplication());
        hasSurface = false;
        inactivityTimer = new InactivityTimer(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        View view = null;
        if (bundle != null) {
            int layoutId = bundle.getInt(ScanUtil.LAYOUT_ID);
            if (layoutId != -1) {
                view = inflater.inflate(layoutId, null);
            }
        }

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_delfault_scan, null);
        }
        scanViewBox = view.findViewById(R.id.viewfinder_view);
        image=view.findViewById(R.id.image);
        surfaceView =view.findViewById(R.id.preview_view);
        surfaceHolder = surfaceView.getHolder();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try{
            initScan();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (scanViewBox!=null){
                        scanViewBox.drawViewfinder();
                    }
                }
            }, 500);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    //第一次加载摄像头
    private void initScan(){
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;

        playBeep = true;
        AudioManager audioService = (AudioManager) getActivity().getSystemService(getActivity().AUDIO_SERVICE);
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            playBeep = false;
        }
        initBeepSound();
        vibrate = true;
    }
    //加载摄像头
    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
            camera = CameraManager.get().getCamera();
        } catch (Exception e) {
            return;
        }

        if (handlers == null) {
            handlers = new CaptureActivityHandler(this, decodeFormats, characterSet, scanViewBox);
        }
    }

    //重新扫描
    public void scanAgain() {
        if (scanViewBox!=null){
            scanViewBox.drawViewfinder();
            CameraManager.get().startPreview();
            handlers.restartPreviewAndDecode();
        }
    }
    //扫码成功方法
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        if (result == null || TextUtils.isEmpty(result.getText())) {
            if (scanCallback != null) {
                scanCallback.onScanFailed();
            }
        } else {
            if (scanCallback != null) {
                if (scanViewBox!=null){
                    scanViewBox.closeDrawViewfinder();
                }
                scanCallback.onScanSuccess(barcode, result.getText());
            }
        }
    }
    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }
    //第一次进入的时候创建视图在进行摄像头的加载
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (!hasSurface) {
                hasSurface = true;
                initCamera(holder);
            }

        }catch (Exception e){
            Log.e("CameraException",e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
        try {
            if (camera != null) {
                if (camera != null && CameraManager.get().isPreviewing()) {
                    if (!CameraManager.get().isUseOneShotPreviewCallback()) {
                        camera.setPreviewCallback(null);
                    }
                    if (camera!=null){
                        camera.stopPreview();
                    }
                    CameraManager.get().getPreviewCallback().setHandler(null, 0);
                    CameraManager.get().getAutoFocusCallback().setHandler(null, 0);
                    CameraManager.get().setPreviewing(false);
                }
            }
        }catch (Exception e){
            Log.e("CameraException",e.getMessage());
        }

    }

    //一些其他页面调用的方法
    public Handler getHandler() {
        return handlers;
    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };
    //闪光灯
    public void setTorch(boolean isTorch){
        if (scanViewBox!=null){
            CameraManager.get().setTorch(isTorch);
        }
    }
    public void drawViewfinder() {
        if (scanViewBox!=null){
            scanViewBox.drawViewfinder();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (handlers != null) {
            handlers.quitSynchronously();
            handlers = null;
        }
        CameraManager.get().closeDriver();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mHandler!=null){
            mHandler.removeCallbacksAndMessages(null);
        }
        if(camera != null) {
            camera.release();
        }
        inactivityTimer.shutdown();

    }


}
