package com.example.scancore.scanBoxView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.example.scancore.R;
import com.example.scancore.ui.ZXingLibrary;
import com.example.scancore.utils.ScreenUtils;


/**
 * 自定义扫码框
 * Created by cuibowen on 2017/06/12.
 */
public class BarCodeView extends BaseScanBox {

    private Bitmap resultBitmap;
    private Bitmap bgBitmap;
    private Paint paint;
    private float density;
    private static final long ANIMATION_DELAY = 1L;
    private static final int OPAQUE = 0xFF;
    private int rectY;
    private Rect frame = new Rect();
    private int maskColor;
    private int resultColor;
    private String textTop = "请在光线充足的地方将条形码/监管码";
    private final String textTop1 = "全部放入扫码框内";
    private boolean isRefesh = true;
    private int measureedWidth;
    private int measureedHeight;

    public BarCodeView(Context context) {
        super(context);
        init();
    }

    public BarCodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarCodeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        Resources resources = getResources();
        resultBitmap = BitmapFactory.decodeResource(resources, R.drawable.image_scan_rect);
        bgBitmap = BitmapFactory.decodeResource(resources, R.drawable.image_scan_bg_rect);
        paint = new Paint();
        density = resources.getDisplayMetrics().density;
        maskColor = resources.getColor(R.color.color_000000_65);
        resultColor = resources.getColor(R.color.color_000000_65);
        rectY = -500;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.setAlpha(OPAQUE);
            if (rectY == -500) {
                rectY = frame.top + 1;
            } else {
                rectY = rectY + 5;
            }
            if (rectY >= frame.bottom) {
                rectY = frame.top;
            }
            Rect rect = new Rect(frame.left + 10, frame.top + 15, frame.right - 10, rectY);
            //modify yanlc
            int srcTop = rectY - frame.top;
            int srcBottom = resultBitmap.getHeight();
            srcTop = resultBitmap.getHeight() - srcTop;
            if (srcTop <= 0)
                srcTop = 0;

            Rect srcRect = new Rect(0, srcTop, resultBitmap.getWidth(), srcBottom);

            canvas.drawBitmap(resultBitmap, srcRect, rect, paint);
            canvas.drawBitmap(bgBitmap, null, new Rect(frame.left, frame.top, frame.right, frame.bottom), paint);
            // Draw the exterior (i.e. outside the framing rect) darkened
            paint.setColor(resultBitmap != null ? resultColor : maskColor);
            //画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
            //扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
            canvas.drawRect(0, 0, width, frame.top, paint);
            canvas.drawRect(0, frame.top, frame.left, frame.bottom, paint);
            canvas.drawRect(frame.right, frame.top, width, frame.bottom, paint);
            canvas.drawRect(0, frame.bottom, width, height, paint);
            paint.setAntiAlias(true);
            // text color - #3D3D3D
            paint.setColor(Color.WHITE);
            paint.setTextSize(ScreenUtils.getScreenWidth() / 100 * 4);
            paint.setDither(true);
            paint.setFilterBitmap(true);
            Rect boundsTop = new Rect();
            paint.getTextBounds(textTop, 0, textTop.length(), boundsTop);
            Rect boundsTop1 = new Rect();
            paint.getTextBounds(textTop1, 0, textTop1.length(), boundsTop1);
            int x1 = (ScreenUtils.getScreenWidth() - boundsTop.width()) / 2;
            int x2 = (ScreenUtils.getScreenWidth() - boundsTop1.width()) / 2;
            int y1 = (ScreenUtils.getScreenWidth() - boundsTop.width()) / 10;
            canvas.drawText(textTop, x1, frame.top - 4 * y1, paint);
            canvas.drawText(textTop1, x2, frame.top - 2 * y1, paint);
            if (isRefesh) {
                postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
            }
        }


    }


    @Override
    protected void onMeasure ( int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureedWidth = MeasureSpec.getSize(widthMeasureSpec);
        measureedHeight = MeasureSpec.getSize(heightMeasureSpec);
        int borderWidth = (int) (measureedWidth - 30 * density);
        int borderHeight = measureedHeight / 4;
        int left = (measureedWidth - borderWidth) / 2;
        int top = (int) ((measureedHeight - borderHeight) /2.5);
        frame.set(left, top, left + borderWidth, top + borderHeight);
    }
    @Override
    public void drawViewfinder() {
        isRefesh = true;
        invalidate();
    }
    @Override
    public void closeDrawViewfinder(){
        isRefesh = false;
    }

    @Override
    public void recycle() {
        if (resultBitmap!=null){
            resultBitmap.recycle();
        }
        if (bgBitmap!=null){
            bgBitmap.recycle();
        }
    }

    public Rect getFrame() {
        return frame;
    }

    public void setTextTop(String text){
        this.textTop=text;
        invalidate();
    }
}
