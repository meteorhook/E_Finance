package com.example.e_finance;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.location.GnssAntennaInfo;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class myAlertDialog extends Dialog {
    private TextView title,message,positive,negative;
    private Boolean isDown=true;
    public myAlertDialog(@NonNull Context context) {
        super(context);
    }
    public myAlertDialog(@NonNull Context context,Boolean isDown) {
        super(context);
        this.isDown=isDown;
    }
    public void setNegative(String nStr,View.OnClickListener negative) {
        this.negative.setText(nStr);
        this.negative.setOnClickListener(negative);
    }

    public void setPositive(String pStr,View.OnClickListener positive) {
        this.positive.setText(pStr);
        this.positive.setOnClickListener(positive);
    }

    public void setMessage(SpannableString message) {
        this.message.setText(message);
    }
    public void setMessage(String message) {
        this.message.setText(message);
    }

    public void setTitle(String title) {
        try {
            this.title.setText(title);
        }catch (Throwable throwable){
            Log.e("alertdialog","null view");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_myalertdialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //自定义Dialog位置
        Display d = getWindow().getWindowManager().getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        Point size = new Point();
        d.getSize(size);
        p.width = (int) ((size.x)*0.9);        //设置为屏幕的0.9倍宽度
        if (isDown){
            getWindow().setGravity(Gravity.BOTTOM);
        }
        getWindow().setAttributes(p);

        //点击返回关闭弹窗
        setCancelable(true);
        //取消点击弹窗外部关闭弹窗
        setCanceledOnTouchOutside(false);


        title=findViewById(R.id.title);
        message=findViewById(R.id.message);
        positive=findViewById(R.id.Positive);
        negative=findViewById(R.id.Negative);
    }
}
