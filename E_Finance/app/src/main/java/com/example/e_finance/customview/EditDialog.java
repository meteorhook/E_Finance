package com.example.e_finance.customview;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.e_finance.R;

public class EditDialog extends Dialog {
    private TextView title, cancel, submit,tv1,tv2;
    private EditText editText, editText2;
    private ImageView DisMiss;

    public EditDialog(@NonNull Context context) {
        super(context);
    }

    public void setTitle(String titleStr) {
        title.setText(titleStr);
    }

    public void setTv1(String tv1,String ed1h) {
        this.tv1.setText(tv1);
        this.editText.setHint(ed1h);
    }

    public void setTv2(String tv2,String ed2h) {
        this.tv2.setText(tv2);
        this.editText2.setHint(ed2h);
    }

    public void setEditText(String ed1,String ed2) {
        this.editText.setText(ed1);
        this.editText2.setText(ed2);
    }

    public EditText getEditText2() {
        return editText2;
    }
    public EditText getEditText() {
        return editText;
    }

    public void setSubmit(String submitStr, View.OnClickListener submit) {
        this.submit.setText(submitStr);
        this.submit.setOnClickListener(submit);
    }
    public void setCancel(String cancelStr,View.OnClickListener cancel) {
        this.cancel.setText(cancelStr);
        this.cancel.setOnClickListener(cancel);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.desire_add_dialog_layout);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //点击返回关闭弹窗
        setCancelable(false);
        //点击弹窗外部关闭弹窗
        setCanceledOnTouchOutside(false);

        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        Point size = new Point();
        d.getSize(size);
        p.width = (int) ((size.x) * 0.9);        //设置为屏幕的0.9倍宽度
        getWindow().setAttributes(p);

        title = findViewById(R.id.title);
        cancel = findViewById(R.id.cancel);
        submit = findViewById(R.id.submit);
        editText = findViewById(R.id.desirename);
        editText2 = findViewById(R.id.desirenum);
        DisMiss = findViewById(R.id.dismiss);
        tv1=findViewById(R.id.tv1);
        tv2=findViewById(R.id.tv2);

        DisMiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}