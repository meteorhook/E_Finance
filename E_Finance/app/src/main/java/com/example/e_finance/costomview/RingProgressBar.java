package com.example.e_finance;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class RingProgressBar extends View {
    private Paint mBackPaint;
    private Paint mFrontPaint;
    private float mStrokeWidth = DisplayUtil.dp2px(getContext(),10);
    private float mRadius = DisplayUtil.dp2px(getContext(),80);
    private RectF mRect;
    private int mProgress = 0; //静态模式目标值
    private int mTargetProgress = 0;//动画模式目标值
    private int mMax = 100;
    private int mWidth;
    private int mHeight;
    private boolean useAnim=true;

    public RingProgressBar(Context context) {
        super(context);
        init();
    }

    public RingProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RingProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        mBackPaint = new Paint();
        mBackPaint.setColor(Color.WHITE);
        mBackPaint.setAntiAlias(true);
        mBackPaint.setStyle(Paint.Style.STROKE);
        mBackPaint.setStrokeWidth(mStrokeWidth);

        mFrontPaint = new Paint();
        mFrontPaint.setAntiAlias(true);
        mFrontPaint.setStyle(Paint.Style.STROKE);
        mFrontPaint.setStrokeWidth(mStrokeWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mTargetProgress>=80){
            mFrontPaint.setColor(getResources().getColor(R.color.yellow));
        }else {
            mFrontPaint.setColor(getResources().getColor(R.color.yellowalpha));
        }
        initRect();
        canvas.translate(0,mHeight/2);
        canvas.drawCircle(mWidth / 2, mHeight / 2, mRadius, mBackPaint);
        if (useAnim){
            float angle = mProgress / (float) mMax * 180;
            canvas.drawArc(mRect, -180, angle, false, mFrontPaint);
            if (mProgress < mTargetProgress) {
                mProgress += 1;
                invalidate();
            }
        }else {
            float angle = mTargetProgress / (float) mMax * 180;
            canvas.drawArc(mRect, -180, angle, false, mFrontPaint);
        }


    }
    //重写测量大小的onMeasure方法和绘制View的核心方法onDraw()
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getRealSize(widthMeasureSpec,false);
        mHeight = getRealSize(heightMeasureSpec,true);
        setMeasuredDimension(mWidth, mHeight);
    }
    public int getRealSize(int measureSpec,boolean isHeight) {
        int result = 1;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);

        if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.UNSPECIFIED) {
            if (isHeight){
                result = (int) (mRadius + mStrokeWidth);
            }else {
                result = (int) (mRadius *2 + mStrokeWidth);
            }
        } else {
            result = size;
        }

        return result;
    }

    private void initRect() {
        if (mRect == null) {
            mRect = new RectF();
            int viewSize = (int) (mRadius * 2);
            int left = (mWidth - viewSize) / 2;
            int top = (mHeight - viewSize) / 2;
            int right = left + viewSize;
            int bottom = top + viewSize;
            mRect.set(left, top, right, bottom);
        }
    }

    public void setmProgress(int mProgress) {
        this.mProgress = mProgress;
    }

    public void setmTargetProgress(int mTargetProgress) {
        this.mTargetProgress = mTargetProgress;
    }

    public void setmRadius(float mRadius) {
        this.mRadius = DisplayUtil.px2dip(getContext(),mRadius);
    }

    public void setUseAnim(boolean useAnim) {
        this.useAnim = useAnim;
    }
}
