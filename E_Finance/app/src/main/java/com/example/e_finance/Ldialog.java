package com.example.e_finance;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_finance.ui.bill.BillViewModel;

import java.util.ArrayList;
import java.util.List;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class Ldialog extends Dialog{
    private RecyclerView recyclerView;
    private TextView billAdd,billEdit;
    private ImageView cancel,imageView;
    private Ladapter ladapter = new Ladapter();;
    private List<Item> list = new ArrayList<>();
    private Boolean t=true;
    private BillViewModel billViewModel;
    private ImageView pgbar;
    private AnimationDrawable ad;

    public Ldialog(@NonNull Context context) {
        super(context);
    }
    public void setBillViewModel(BillViewModel billViewModel){
        this.billViewModel = billViewModel;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //点击返回关闭弹窗
        setCancelable(true);
        //点击弹窗外部关闭弹窗
        setCanceledOnTouchOutside(true);

        //自定义Dialog宽度
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        Point size = new Point();
        d.getSize(size);
        p.width = (int) ((size.x)*0.9);        //设置为屏幕的0.9倍宽度
        getWindow().setAttributes(p);

        billAdd = findViewById(R.id.billAdd);
        billEdit = findViewById(R.id.billEdit);
        cancel = findViewById(R.id.cancel);
        recyclerView = findViewById(R.id.ledgerInfo);
        imageView=findViewById(R.id.image1);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        billEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (t){
                    billEdit.setTextColor(Color.parseColor("#FFD000"));
                    ladapter.setEdit(true);
                    billEdit.setText("管理完成");
                    imageView.setImageResource(R.drawable.ledger_edits);
                    t=false;
                }else {
                    billEdit.setText("管理账本");
                    billEdit.setTextColor(Color.BLACK);
                    imageView.setImageResource(R.drawable.ledger_edit);
                    ladapter.setEdit(false);
                    t=true;
                }

            }
        });
        billAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                Intent intent = new Intent(getContext(), ledgerAdd.class);
                getContext().startActivity(intent);
            }
        });

        LCUser user=LCUser.getCurrentUser();
        LCQuery<LCObject> query = new LCQuery<>("Eledger");
        query.whereEqualTo("Euser", user);
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            public void onSubscribe(Disposable disposable) {
                pgbar.setVisibility(View.VISIBLE);
            }
            public void onNext(List<LCObject> back) {
                pgbar.setVisibility(View.GONE);
                String[] name=new String[back.size()];
                for (int i=0;i<back.size();i++){
                    LCObject object=back.get(i);
                    name[i]=object.getString("Lname");
                }
                for (int i = 0; i < name.length; i++) {
                    Item item = new Item(name[i]);
                    list.add(item);
                }
                ladapter.setData(list);
                ladapter.setBillViewModel(billViewModel);
                ladapter.setLdialog(Ldialog.this);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(ladapter);
            }
            public void onError(Throwable throwable) {
            }
            public void onComplete() {pgbar.setVisibility(View.GONE);}
        });

    }

}
