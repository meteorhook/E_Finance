package com.example.e_finance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.e_finance.util.StatusBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class ledgerAdd extends AppCompatActivity {
    private ImageView dailyledger, marriageledger, babyledger;
    private TextView customledger;
    private String[] daily = {"6544b87f42d451506a168f43", "6544b7c544fa007c597ca6a2", "6544f6b744fa007c597cb597", "6544b8d444fa007c597ca6e3", "6544b90144fa007c597ca6e7"};
    private String[] marriage = {"6544fd6344fa007c597cb756", "6544fe1642d451506a169f87", "6544fe4744fa007c597cb78b", "6544fe7e44fa007c597cb798", "6544f62942d451506a169d7e", "6544fec644fa007c597cb7aa"};
    private String[] baby = {"6544b7c544fa007c597ca6a2", "6544b80644fa007c597ca6a7", "6544b87f42d451506a168f43", "6544b8ad42d451506a168f52", "6544fdd844fa007c597cb771", "6544ff0444fa007c597cb7b6", "6544f62942d451506a169d7e", "6544fec644fa007c597cb7aa"};
    private String[] custom = {};
    private ImageView pgbar;
    private AnimationDrawable ad;
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.dailyledger:
                    createledger("日常");
                    break;
                case R.id.marriageledger:
                    createledger("结婚");
                    break;
                case R.id.babyledger:
                    createledger("宝宝");
                    break;
                case R.id.customledger:
                    createledger("自定义");
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(ledgerAdd.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_ledger_add);

        dailyledger=findViewById(R.id.dailyledger);
        marriageledger=findViewById(R.id.marriageledger);
        babyledger=findViewById(R.id.babyledger);
        customledger=findViewById(R.id.customledger);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        Glide.with(ledgerAdd.this)
                .load("http://lc-5r6UPI7d.cn-n1.lcfile.com/HIPHn8x6TweOsQloHf7WeBMTfyKJ2bf9/Daily%20ledger.png")
                .into(dailyledger);
        Glide.with(ledgerAdd.this)
                .load("http://lc-5r6UPI7d.cn-n1.lcfile.com/EhCVh4qtoLHLTLN5nDcUQKSUaaEVbEAD/Marriage%20ledger.png")
                .into(marriageledger);
        Glide.with(ledgerAdd.this)
                .load("http://lc-5r6UPI7d.cn-n1.lcfile.com/l3QaWHqhy6xuDqOqDlP3qyKUpma5XLt1/baby_ledger.png")
                .into(babyledger);

        dailyledger.setOnClickListener(listener);
        marriageledger.setOnClickListener(listener);
        babyledger.setOnClickListener(listener);
        customledger.setOnClickListener(listener);
    }

    private void createledger(String Lname){
        LCUser user=LCUser.getCurrentUser();
        LCQuery<LCObject> query = new LCQuery<>("Eledger");
        query.whereEqualTo("Euser", user);
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            @Override
            public void onSubscribe(Disposable d) {
                pgbar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onNext(List<LCObject> lcObjects) {
                String[] eledger=new String[lcObjects.size()];
                for (int i=0;i<lcObjects.size();i++){
                    LCObject obj=lcObjects.get(i);
                    eledger[i] =obj.getString("Lname");
                }
                ArrayList<Integer> Nums = new ArrayList<>();
                for(String s : eledger) {
                    if(s.startsWith(Lname)) {
                        if (s.equals(Lname)){
                            Nums.add(1);
                        }else {
                            try {
                                Nums.add(Integer.parseInt(s.substring(Lname.length())));
                            }catch (NumberFormatException e) {
                                // 如果s.substring(Lname.length())不能解析为整数，则捕获此异常
                                Nums.add(1);
                            }
                        }
                    }
                }
                Collections.sort(Nums);
                int i;
                for(i = 1; i <= Nums.size(); i++) {
                    if(!Nums.contains(i)) {
                        break;
                    }
                }
                String name;
                if (i==1){
                    name=Lname;
                }else {
                    name=Lname+i;
                }

                LCObject ledger = new LCObject("Eledger");
                ledger.put("Euser", user);
                ledger.put("Lname", name);
                if (Lname.equals("日常")){
                    ledger.put("Ltype",daily);
                }if (Lname.equals("宝宝")){
                    ledger.put("Ltype",baby);
                }if (Lname.equals("结婚")){
                    ledger.put("Ltype",marriage);
                }if(Lname.equals("自定义")){
                    ledger.put("Ltype",custom);
                }
                ledger.saveInBackground().subscribe(new Observer<LCObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {}
                    @Override
                    public void onNext(LCObject lcObject) {
                        pgbar.setVisibility(View.GONE);
                        Intent intent=new Intent(ledgerAdd.this,LedgerInfo.class);
                        intent.putExtra("ledgerid",lcObject.getObjectId());
                        intent.putExtra("action","添加账本");
                        startActivity(intent);
                    }
                    @Override
                    public void onError(Throwable e) {Toast.makeText(ledgerAdd.this,"加载失败"+e.getMessage(),Toast.LENGTH_SHORT).show();}
                    @Override
                    public void onComplete() {}
                });

            }
            @Override
            public void onError(Throwable e) {
                Toast.makeText(ledgerAdd.this,"加载失败"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onComplete() {pgbar.setVisibility(View.GONE);}
        });

    }
}