package com.example.cuibowen.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.example.cuibowen.camera.R;
import com.example.scancore.camera.CameraManager;
import com.example.scancore.scanBoxView.BaseScanBox;


/**
 * Created by cuibowen on 2017/11/19.
 */

public class ScanView extends BaseScanBox {
    private Bitmap scanLight;
    private Bitmap box;
    private Rect frameRect;
    private int x;
    private int y;
    private Paint paint;
    private Paint boxPaint;
    private int innercornercolor;
    // 扫描线移动的y
    private int scanLineTop;
    // 扫描线移动速度
    private int SCAN_VELOCITY=7;

    private boolean isStartLine=false;

    private ValueAnimator animator = ValueAnimator.ofInt(0,1);

    private OnTimeEndListener listener;

    public void setListener(OnTimeEndListener listener) {
        this.listener = listener;
    }

    public ScanView(Context context) {
        this(context,null);
    }

    public ScanView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ScanView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources resources = getResources();

        scanLight = BitmapFactory.decodeResource(resources,
                R.mipmap.icon_wangge);

        box = BitmapFactory.decodeResource(resources,
                R.mipmap.icon_saomiaokuang);

        paint = new Paint();
        boxPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        boxPaint.setAntiAlias(true);

        innercornercolor = Color.WHITE;
    }
    @Override
    public void drawViewfinder() {
        isStartLine = true;
        invalidate();
    }
    @Override
    public void closeDrawViewfinder(){
        isStartLine = false;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

       // drawFrameBounds(canvas,frameRect);
        drawBox(canvas,frameRect);
       // drawBg(canvas,frameRect);


        if (isStartLine){
            drawScanLight(canvas,frameRect);
            postInvalidateDelayed(1, frameRect.left, frameRect.top, frameRect.right, frameRect.bottom);
        }

    }



    private void drawBox(Canvas canvas, Rect frame) {
        int boxWidth=box.getWidth()/2;
        int boxHeight=box.getHeight()/2;

        canvas.drawBitmap(box,x-boxWidth,y-boxHeight,boxPaint);
    }

    private void drawFrameBounds(Canvas canvas, Rect frame) {

        paint.setColor(innercornercolor);
        paint.setStyle(Paint.Style.FILL);

        int corWidth = 7;
        int corLength = 40;

        // 左上角
        canvas.drawRect(frame.left, frame.top, frame.left + corWidth, frame.top
                + corLength, paint);
        canvas.drawRect(frame.left, frame.top, frame.left
                + corLength, frame.top + corWidth, paint);
        // 右上角
        canvas.drawRect(frame.right - corWidth, frame.top, frame.right,
                frame.top + corLength, paint);
        canvas.drawRect(frame.right - corLength, frame.top,
                frame.right, frame.top + corWidth, paint);
        // 左下角
        canvas.drawRect(frame.left, frame.bottom - corLength,
                frame.left + corWidth, frame.bottom, paint);
        canvas.drawRect(frame.left, frame.bottom - corWidth, frame.left
                + corLength, frame.bottom, paint);
        // 右下角
        canvas.drawRect(frame.right - corWidth, frame.bottom - corLength,
                frame.right, frame.bottom, paint);
        canvas.drawRect(frame.right - corLength, frame.bottom - corWidth,
                frame.right, frame.bottom, paint);
    }
    public void setScanLine(boolean isStart){
        this.isStartLine=isStart;
        invalidate();
    }

    private void drawScanLight(Canvas canvas, Rect frame) {
        if (scanLineTop ==-scanLight.getHeight()+25) {
            scanLineTop = frame.top-scanLight.getHeight()+25;
        }

        if (scanLineTop+ scanLight.getHeight()/3>= frame.bottom-10) {
            scanLineTop = frame.top-scanLight.getHeight()+25;
        } else {
            scanLineTop += SCAN_VELOCITY;
        }
        Rect scanRect = new Rect(frame.left+12, scanLineTop, frame.right-12,
                scanLineTop + scanLight.getHeight()/3);
        canvas.drawBitmap(scanLight, null, scanRect, paint);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        x=w/2;
        y=h/2;

        box=setImgSize(box,w,h);
        scanLight=setImgSize(scanLight,w,h);


        // 获得图片的宽高.
        int width = box.getWidth();
        int height = box.getHeight();

        frameRect=new Rect(x-width/2,y-height/2,x+width/2,y+height/2);

        scanLineTop=-scanLight.getHeight()+20;

        animator.setDuration(2000);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value= (int) valueAnimator.getAnimatedValue();
                if (value==1){
                    if (listener!=null){
                        listener.onTimeEndListener();
                    }
                }
            }
        });

    }
    public void timeStart(){
        animator.start();
    }

    public interface OnTimeEndListener{
        void onTimeEndListener();
    }

    static int dp2px(Context context, float dpValue) {
        if (context == null) return (int) (dpValue * 1.5f + 0.5f);
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public Bitmap setImgSize(Bitmap bm, int newWidth , int newHeight){
        // 获得图片的宽高.
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例.
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数.
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片.
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }

    public void recycle(){
        if (box!=null&&!box.isRecycled()){
            box.recycle();
            box=null;
        }
        if (scanLight!=null&&!scanLight.isRecycled()){
            scanLight.recycle();
            scanLight=null;
        }

    }
}
