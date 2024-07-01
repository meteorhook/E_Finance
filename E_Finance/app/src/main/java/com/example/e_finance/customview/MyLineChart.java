package com.example.e_finance.customview;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.LineChart;

public class MyLineChart extends LineChart {
    public MyLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = true;
        // if there is no marker view or drawing marker is disabled
        if (isShowingMarker() && this.getMarker() instanceof MyMarkerView){
            MyMarkerView markerView = (MyMarkerView) this.getMarker();
            Rect rect = new Rect((int)markerView.drawingPosX,(int)markerView.drawingPosY,(int)markerView.drawingPosX + markerView.getWidth(), (int)markerView.drawingPosY + markerView.getHeight());
            if (rect.contains((int) event.getX(),(int) event.getY())) {
                // touch on marker -> dispatch touch event in to marker
                markerView.dispatchTouchEvent(event);
            }else{
                handled = super.onTouchEvent(event);
            }
        }else{
            handled = super.onTouchEvent(event);
        }
        return handled;
    }

    private boolean isShowingMarker(){
        return mMarker != null && isDrawMarkersEnabled() && valuesToHighlight();
    }
}
