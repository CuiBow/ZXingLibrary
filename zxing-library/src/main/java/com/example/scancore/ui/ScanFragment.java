package com.example.scancore.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import android.widget.Toast;

import com.example.scancore.R;
import com.example.scancore.camera.CameraManager;
import com.example.scancore.decoding.BitmapDecoder;
import com.example.scancore.decoding.CaptureActivityHandler;
import com.example.scancore.decoding.InactivityTimer;
import com.example.scancore.scanBoxView.BaseScanBox;
import com.example.scancore.utils.BitmapUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.result.ResultParser;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Vector;

import static android.app.Activity.RESULT_OK;

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
    //相册
    private static final int REQUEST_CODE = 100;
    private String photoPath;
    private static final int PARSE_BARCODE_FAIL = 300;
    private static final int PARSE_BARCODE_SUC = 200;

    private MyHandler mPhotoAlbumHandler;

    static class MyHandler extends Handler {

        private WeakReference<Activity> activityReference;
        private ScanUtil.ScanCallback callback;
        private String path;

        public void setPath(String path) {
            this.path = path;
        }

        public MyHandler(Activity activity, ScanUtil.ScanCallback callback) {
            activityReference = new WeakReference<Activity>(activity);
            this.callback=callback;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PARSE_BARCODE_SUC: // 解析图片成功
                    if (callback!=null){
                        Bitmap bitmap;
                        if (path!=null){
                            bitmap= BitmapFactory.decodeFile(path);
                        }else{
                            bitmap= null;
                        }
                        callback.onScanSuccess(bitmap, (String) msg.obj);
                    }
                    break;
                case PARSE_BARCODE_FAIL:// 解析图片失败
                    if (callback!=null){
                        callback.onScanFailed();
                    }
                    break;
            }

            super.handleMessage(msg);
        }
    }

    public void setScanCallback(ScanUtil.ScanCallback scanCallback) {
        this.scanCallback = scanCallback;
        mPhotoAlbumHandler = new MyHandler(getActivity(),scanCallback);
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
    //震动
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
    //震动
    private final MediaPlayer.OnCompletionListener beepListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };
    //跳转拍照
    public void startPhotoAlbum(){
        Intent innerIntent = new Intent(Intent.ACTION_PICK); // "android.intent.action.GET_CONTENT"
        innerIntent.setType("image/*");
        this.startActivityForResult(innerIntent, REQUEST_CODE);
    }
    //闪光灯
    public void setTorch(boolean isTorch){
        if (scanViewBox!=null){
            CameraManager.get().setTorch(isTorch);
        }
    }
    //是否开启扫描
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
    //用于识别相册二维码
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE:
                    // 获取选中图片的路径
                    if (mPhotoAlbumHandler!=null){
                        Cursor cursor = getActivity().getContentResolver().query(
                                data.getData(), null, null, null, null);
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                int columnIndex = cursor
                                        .getColumnIndex(MediaStore.Images.Media.DATA);
                                photoPath = cursor.getString(columnIndex);

                                mPhotoAlbumHandler.setPath(photoPath);
                            }
                            cursor.close();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap img = BitmapUtils
                                            .getCompressedBitmap(photoPath);

                                    BitmapDecoder decoder = new BitmapDecoder(
                                            getActivity());
                                    Result result = decoder.getRawResult(img);
                                    if (result != null) {
                                        Message m = mHandler.obtainMessage();
                                        m.what = PARSE_BARCODE_SUC;
                                        String resultStr = ResultParser.parseResult(result)
                                                .toString();
                                        m.obj = resultStr;

                                        mPhotoAlbumHandler.sendMessage(m);
                                    } else {
                                        Message m = mHandler.obtainMessage();
                                        m.what = PARSE_BARCODE_FAIL;
                                        mPhotoAlbumHandler.sendMessage(m);
                                    }
                                }
                            }).start();
                            cursor.close();
                        }
                    }
                    break;
            }
        }
    }
    //防止内存泄漏关闭一些资源以及handler
    public void recycle(){
        if(handlers!=null){
            handlers.removeCallbacksAndMessages(null);
        }
        if(mHandler!=null){
            mHandler.removeCallbacksAndMessages(null);
        }
        if(mPhotoAlbumHandler!=null){
            mPhotoAlbumHandler.removeCallbacksAndMessages(null);
        }
        if (scanViewBox!=null){
            scanViewBox.recycle();
        }
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
