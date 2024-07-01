package com.example.e_finance.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DashedLineView extends View {

    private Paint mPaint;

    public DashedLineView(Context context) {
        super(context);
        init();
    }

    public DashedLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 创建画笔
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#E3DFDF")); // 设置画笔颜色
        mPaint.setStrokeWidth(10); // 设置画笔线宽为10像素
        mPaint.setStyle(Paint.Style.STROKE); // 设置画笔样式为描边
        mPaint.setPathEffect(new DashPathEffect(new float[]{10,25}, 0)); // 设置画笔虚线效果
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2, mPaint);
    }
}
