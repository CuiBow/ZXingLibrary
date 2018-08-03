package com.example.scancore.scanBoxView;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import com.example.scancore.R;

public class QRCodeView extends BaseScanBox {
    private Bitmap scanLight;//中间扫描线图片
    private Rect frameRect;//大框
    private Rect bgRect;//小框
    private Paint paint;//四边画笔
    private Paint boxPaint;//边框画笔
    private Paint bgPaint;//阴影画笔
    private int scanLineTop;// 扫描线移动的y
    private int SCAN_VELOCITY=7;//扫描线移动速度
    private boolean isStartLine=false;//是否开始扫描
    private int scanBorderWidth;//边框宽度
    private int padding=180;//边框横向边距
    private int width;//整屏宽度
    private int height;//整屏高度
    private int maskColor;//四周阴影颜色

    public QRCodeView(Context context) {
        this(context,null);
    }

    public QRCodeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public QRCodeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Resources resources = getResources();

        maskColor = resources.getColor(R.color.viewfinder_mask);

        scanLight = BitmapFactory.decodeResource(resources,
                R.drawable.icon_scan_line);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(context.getResources().getColor(R.color.primart_blue));

        boxPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        boxPaint.setAntiAlias(true);
        boxPaint.setColor(Color.WHITE);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(4);

        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(maskColor);
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
    public void recycle() {
        if (scanLight!=null&&!scanLight.isRecycled()){
            scanLight.recycle();
            scanLight=null;
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // drawBorderBg(canvas,frameRect);
        drawBorderBg(canvas,bgRect);
        //drawBg
        drawBg(canvas,frameRect);
        // drawFrameBounds(canvas,frameRect);
        drawFrameBounds(canvas,frameRect);

        if (isStartLine){
            drawScanLight(canvas,frameRect);
            postInvalidateDelayed(1, frameRect.left, frameRect.top, frameRect.right, frameRect.bottom);
        }

    }
    //绘制深色背景
    private void drawBg(Canvas canvas, Rect frame) {
        canvas.drawRect(0, 0, width, frame.top, bgPaint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, bgPaint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, bgPaint);
        canvas.drawRect(0, frame.bottom + 1, width, height, bgPaint);
    }
    //绘制边框
    private void drawBorderBg(Canvas canvas, Rect frameRect) {
        canvas.drawRect(frameRect,boxPaint);
    }
    //画边框四个角
    private void drawFrameBounds(Canvas canvas, Rect frame) {
        paint.setStyle(Paint.Style.FILL);
        int corWidth = 7;
        int corLength = 60;
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
    //画扫描线
    private void drawScanLight(Canvas canvas, Rect frame) {
        if (scanLineTop ==height/2-scanBorderWidth/2) {
            scanLineTop = frame.top;
        }

        if (scanLineTop+ scanLight.getHeight()/3>= frame.bottom-10) {
            scanLineTop = frame.top;
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
        height=h;
        width=w;
        scanBorderWidth=w-padding*2;
        frameRect=new Rect(padding,h/2-scanBorderWidth/2,w-padding,h/2+scanBorderWidth/2);
        bgRect=new Rect(padding+2,h/2-scanBorderWidth/2+2,w-padding-2,h/2+scanBorderWidth/2-2);
        scanLineTop=h/2-scanBorderWidth/2;
    }

}
