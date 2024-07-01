package com.example.e_finance;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.e_finance.ui.statistics.StatisticsFragment;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MyMarkerView extends MarkerView {

    private Boolean isMonth=true;//是否是月份账单界面
    private String head;//datelabel数据显示头
    public static final int ARROW_SIZE = 20; // 箭头的大小
    private static final float CIRCLE_OFFSET = 10;//因为折点是圆圈，所以要偏移，防止直接指向圆心
    private static final float STOKE_WIDTH = 5;//对于stroke_width的宽度也要做一定偏移
    private Bitmap bitmapForDot;//选中点图片
    private int bitmapWidth;//点宽
    private int bitmapHeight;//点高
    protected float drawingPosX;//点击x坐标范围
    protected float drawingPosY;//点击y坐标范围
    private static final int MAX_CLICK_DURATION = 500;//最大点击时间
    private long startClickTime;//点击开始时间
    private StatisticsFragment.sandeDate sandeDate;
    private int dateValue=1;
    private TextView datelabel,numlabel;
    private ConstraintLayout markerContainerView;

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public MyMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        datelabel=findViewById(R.id.datelabel);
        numlabel=findViewById(R.id.numlabel);
        markerContainerView=findViewById(R.id.markerContainerView);
        bitmapForDot = getBitmap(context, R.drawable.mymarkviewdot);
        bitmapWidth = bitmapForDot.getWidth();
        bitmapHeight = bitmapForDot.getHeight();

        markerContainerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(),BillstatisticsinfoActivity.class);
                Calendar calendar=Calendar.getInstance(TimeZone.getTimeZone("GMT+08"));
                calendar.setTime(sandeDate.getStartDate());
                int year=calendar.get(Calendar.YEAR);
                int month=calendar.get(Calendar.MONTH);
                Date date1,date2;
                if (isMonth){
                    calendar.set(year,month,dateValue,0,0,0);
                    date1=calendar.getTime();
                    calendar.add(Calendar.DATE,1);
                    date2=calendar.getTime();
                }else {
                    calendar.set(year,dateValue-1,1,0,0,0);
                    date1=calendar.getTime();
                    int m=calendar.getMaximum(Calendar.DAY_OF_MONTH);
                    calendar.add(Calendar.DATE,m);
                    date2=calendar.getTime();
                }
                intent.putExtra("dateStart",date1.getTime());
                intent.putExtra("dateEnd",date2.getTime());
                intent.putExtra("isMonth",isMonth);
                getContext().startActivity(intent);
            }
        });
    }

    public void setIsMonth(Boolean isMonth){
        this.isMonth=isMonth;
    }

    public void setHead(String head){
        this.head=head;
    }
    public void setSandeDate(StatisticsFragment.sandeDate sandeDate) {
        this.sandeDate = sandeDate;
    }
    private static Bitmap getBitmap(Context context, int vectorDrawableId) {
        Bitmap bitmap;
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP){
            Drawable vectorDrawable = context.getDrawable(vectorDrawableId);
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                    vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
        }else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), vectorDrawableId);
        }
        return bitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                startClickTime = Calendar.getInstance().getTimeInMillis();
                break;
            }
            case MotionEvent.ACTION_UP: {
                long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                if(clickDuration < MAX_CLICK_DURATION) {
                    markerContainerView.callOnClick();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        dateValue=(int)highlight.getX();
        if (isMonth){
            datelabel.setText(head+(int)highlight.getX()+"日");
        }else {
            datelabel.setText(head+(int)highlight.getX()+"月");
        }
        String num=String.valueOf(highlight.getY());
        if (num.endsWith(".0")){
            num=String.valueOf((int)highlight.getY());
        }
        numlabel.setText(num+"元");
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {
        MPPointF offset = getOffset();
        Chart chart = getChartView();
        float width = getWidth();
        float height = getHeight();
        // posY \posX 指的是markerView左上角点在图表上面的位置
        //处理Y方向
        if (posY <= height + ARROW_SIZE) {// 如果点y坐标小于markerView的高度，如果不处理会超出上边界，处理了之后这时候箭头是向上的，我们需要把图标下移一个箭头的大小
            offset.y = ARROW_SIZE;
        } else {//否则属于正常情况，因为我们默认是箭头朝下，然后正常偏移就是，需要向上偏移markerView高度和arrow size，再加一个stroke的宽度，因为你需要看到对话框的上面的边框
            offset.y = -height - ARROW_SIZE - STOKE_WIDTH; // 40 arrow height   5 stroke width
        }
        //处理X方向，分为3种情况，1、在图表左边 2、在图表中间 3、在图表右边
        //
        if (posX > chart.getWidth() - width) {//如果超过右边界，则向左偏移markerView的宽度
            offset.x = -width;
        } else {//默认情况，不偏移（因为是点是在左上角）
            offset.x = 0;
            if (posX > width / 2) {//如果大于markerView的一半，说明箭头在中间，所以向右偏移一半宽度
                offset.x = -(width / 2);
            }
        }
        return offset;
    }

    @Override
    public void draw(Canvas canvas, float posX, float posY) {
        super.draw(canvas, posX, posY);
        Paint paint = new Paint();//绘制的画笔
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#5566D6"));

        Chart chart = getChartView();
        float width = getWidth();
        float height = getHeight();

        canvas.drawBitmap(bitmapForDot, posX-bitmapWidth / 2f , posY-bitmapHeight / 2f ,null);

        MPPointF offset = getOffsetForDrawingAtPoint(posX, posY);
        int saveId = canvas.save();
        this.drawingPosX = posX + offset.x;
        this.drawingPosY = posY + offset.y;

        Path path;
        if (posY < height + ARROW_SIZE) {//处理超过上边界
            path = new Path();
            path.moveTo(0, 0);

            if (posX > chart.getWidth() - width) {//超过右边界
                path.moveTo(width - ARROW_SIZE, 0);
                path.lineTo(width, -ARROW_SIZE + CIRCLE_OFFSET);
                path.lineTo(width, height/2);
                path.lineTo(width - ARROW_SIZE,0);
            } else {
                if (posX > width / 2) {//在图表中间
                    path.moveTo(width / 2 - ARROW_SIZE / 2, 0);
                    path.lineTo(width / 2, -ARROW_SIZE + CIRCLE_OFFSET);
                    path.lineTo(width / 2 + ARROW_SIZE / 2, 0);
                    path.lineTo(width / 2 - ARROW_SIZE / 2, 0);
                } else {//超过左边界
                    path.lineTo(0, -ARROW_SIZE + CIRCLE_OFFSET);
                    path.lineTo(0 + ARROW_SIZE, 0);
                    path.lineTo(0, height/2);
                    path.lineTo(0,0);
                }
            }
            path.offset(posX + offset.x, posY + offset.y);
        } else {//没有超过上边界
            path = new Path();
            path.moveTo(width, height);
            if (posX > chart.getWidth() - width) {
                path.moveTo(width, height/2);
                path.lineTo(width, height + ARROW_SIZE - CIRCLE_OFFSET);
                path.lineTo(width - ARROW_SIZE, 0 + height);
                path.lineTo(width, height/2);
            } else {
                if (posX > width / 2) {
                    path.moveTo(width / 2 + ARROW_SIZE / 2, height);
                    path.lineTo(width / 2, height + ARROW_SIZE - CIRCLE_OFFSET);
                    path.lineTo(width / 2 - ARROW_SIZE / 2, 0 + height);
                    path.lineTo(width / 2 + ARROW_SIZE / 2, height);
                } else {
                    path.moveTo(0 + ARROW_SIZE, 0 + height);
                    path.lineTo(0, height + ARROW_SIZE - CIRCLE_OFFSET);
                    path.lineTo(0, 0 + height/2);
                    path.lineTo(0 + ARROW_SIZE, 0 + height);
                }
            }

            path.offset(posX + offset.x, posY + offset.y);
        }

        canvas.drawPath(path, paint);
        canvas.translate(posX + offset.x, posY + offset.y);
        draw(canvas);
        canvas.restoreToCount(saveId);
    }
}
