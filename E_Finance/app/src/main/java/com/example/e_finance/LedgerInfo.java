package com.example.e_finance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.WorkManager;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.e_finance.ui.bill.BillFragment;
import com.example.e_finance.ui.bill.BillViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class LedgerInfo extends AppCompatActivity {
    private LedgerInfoFragment ledgerInfoFragment=new LedgerInfoFragment();
    private String ledgerid,action,lName1;
    private String[] typeid;
    private EditText Name;
    private TextView typeA,typeR,namelength;
    private Button ledgerAction;
    private ConstraintLayout bg;
    private ImageView ledgerbg;
    private Boolean move=true;
    private Boolean save=false;
    private ImageView pgbar;
    private AnimationDrawable ad;
    private Boolean ledger=false,type=false,bill=false,cyclebill=false,budget=false;
    private MutableLiveData<Boolean> hidepgbar=new MutableLiveData<>();
    private Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    initView(ledgerid,true);
                    break;
                case 1:
                    initView(ledgerid,false);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(LedgerInfo.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_ledger_info);

        bg=findViewById(R.id.bg);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        ledgerbg=findViewById(R.id.ledgerbg);

        typeA=findViewById(R.id.typeA);
        typeR=findViewById(R.id.typeR);
        namelength = findViewById(R.id.namelength);

        View view=findViewById(R.id.symbol);

        Name=findViewById(R.id.ledgername);

        ledgerAction=findViewById(R.id.ledgeredit);

        Intent intent=LedgerInfo.this.getIntent();
        ledgerid=intent.getStringExtra("ledgerid");
        action=intent.getStringExtra("action");
        if (action.equals("添加账本")){
           save=false;
        }if (action.equals("删除账本")){
            save=true;
        }

        ledgerAction.setText(action);
        setName(ledgerid);
        Name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                namelength.setText(s.length()+"/8");
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                lName1= s.toString();
                namelength.setText(s.length()+"/8");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        typeA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg=Message.obtain();
                        msg.what=0;
                        handler.sendMessage(msg);
                    }
                });
                thread.start();

                if (!move){
                    ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view,"alpha",1,0.75f,0.5f,0,1);
                    ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view,"translationX",typeR.getX()-typeA.getX(),0);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(objectAnimator1)
                            .with(objectAnimator2);
                    animatorSet.setDuration(700);
                    animatorSet.start();
                    move=true;
                }
            }
        });

        typeR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread thread=new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg=Message.obtain();
                        msg.what=1;
                        handler.sendMessage(msg);
                    }
                });
                thread.start();

                if (move) {
                    ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "alpha", 1,0.75f,0.5f,0,1);
                    ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view, "translationX", 0, typeR.getX() - typeA.getX());
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(objectAnimator1)
                            .with(objectAnimator2);
                    animatorSet.setDuration(700);
                    animatorSet.start();
                    move=false;
                }
            }
        });

        ledgerAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (action.equals("添加账本")){
                    savename();
                }if (action.equals("删除账本")){
                    LCUser user=LCUser.getCurrentUser();
                    LCQuery<LCObject> query = new LCQuery<>("Eledger");
                    query.whereEqualTo("Euser",user);
                    query.orderByDescending("createdAt");
                    query.findInBackground().subscribe(new Observer<List<LCObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            pgbar.setVisibility(View.VISIBLE);
                        }
                        @Override
                        public void onNext(List<LCObject> lcObjects) {
                            pgbar.setVisibility(View.GONE);
                            if (lcObjects.size()==1){
                                Toast.makeText(LedgerInfo.this,"请至少保留一个账本",Toast.LENGTH_SHORT).show();
                            }else {
                                myAlertDialog myAlertDialog=new myAlertDialog(LedgerInfo.this);
                                myAlertDialog.create();
                                //设置弹窗信息
                                String ledgername="删除账本将同时删除该账本下所有账单，同时关闭全部周期任务，确认删除"+Name.getText().toString()+"账本吗？";
                                int end=33+Name.getText().toString().length()+2;
                                SpannableString spannable = new SpannableString(ledgername);
                                spannable.setSpan(new ForegroundColorSpan(Color.RED), 33,end , Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                myAlertDialog.setMessage(spannable);
                                //设置取消按钮
                                myAlertDialog.setNegative("取消",new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        myAlertDialog.dismiss();
                                    }
                                });
                                //设置确认按钮
                                myAlertDialog.setPositive("删除",new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Boolean isused = false;
                                        for (int i = 0; i < lcObjects.size(); i++) {
                                            LCObject obj = lcObjects.get(i);
                                            if (obj.getObjectId().equals(ledgerid) && obj.getBoolean("isUsed")) {
                                                LCObject next;
                                                if (i == 0) {
                                                    next = lcObjects.get(i + 1);
                                                } else {
                                                    next = lcObjects.get(0);
                                                }
                                                next.put("isUsed", true);
                                                next.saveInBackground().subscribe(new Observer<LCObject>() {
                                                    @Override
                                                    public void onSubscribe(Disposable d) {
                                                        pgbar.setVisibility(View.VISIBLE);
                                                    }

                                                    @Override
                                                    public void onNext(LCObject lcObject) {
                                                        delete();
                                                        finish();
                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {
                                                        pgbar.setVisibility(View.GONE);
                                                        Log.e("用户账本启用状态修改失败",e.getMessage());
                                                        Toast.makeText(LedgerInfo.this,"删除失败"+e.getMessage(),Toast.LENGTH_SHORT).show();
                                                    }

                                                    @Override
                                                    public void onComplete() {}
                                                });
                                                isused = true;
                                                break;
                                            }
                                        }
                                        if (!isused) {
                                            delete();
                                            finish();
                                        }
                                    }
                                });
                                myAlertDialog.show();
                            }
                        }
                        @Override
                        public void onError(Throwable e) {
                            pgbar.setVisibility(View.GONE);
                            Toast.makeText(LedgerInfo.this,"加载失败"+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onComplete() {}
                    });
                }
            }
        });
        typeA.callOnClick();

        hidepgbar.observe(this, new androidx.lifecycle.Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean){
                    pgbar.setVisibility(View.GONE);
                }
            }
        });
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        if (move){
            typeA.callOnClick();
        }else {
            typeR.callOnClick();
        }
        Name.setText(lName1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hidepgbar.removeObservers(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!save){
            delete();
        }else {
            savename();
        }
    }

    //查询当前账本所拥有的分类并传递数据给fragment
    private void initView(String Lid,Boolean Tstate){
        LCQuery<LCObject> query = new LCQuery<>("Eledger");
        query.getInBackground(Lid).subscribe(new Observer<LCObject>() {
            public void onSubscribe(Disposable disposable) {
                pgbar.setVisibility(View.VISIBLE);
            }
            public void onNext(LCObject ledger) {
                String Lname=ledger.getString("Lname");
                bgLoad(Lname);
                String[] type= ledger.getJSONArray("Ltype").toArray(new String[0]);
                typeid=type;
                LCQuery<LCObject> typeinfo = new LCQuery<>("Etype");
                typeinfo.whereContainedIn("objectId", Arrays.asList(type));
                typeinfo.whereEqualTo("Tstate", Tstate);
                typeinfo.findInBackground().subscribe(new Observer<List<LCObject>>() {
                    public void onSubscribe(Disposable disposable) {
                    }

                    public void onNext(List<LCObject> back) {
                        pgbar.setVisibility(View.GONE);
                            String[] typeName = new String[back.size()+1];
                            String[] typeColor = new String[back.size() + 1];
                            String[] typeImg = new String[back.size() + 1];
                            String[] typeImgSelect = new String[back.size() + 1];

                            for (int i = 0; i < back.size(); i++) {
                                LCObject type = back.get(i);
                                typeName[i] = type.getString("TypeName");
                                typeColor[i] = type.getString("TypeColor");
                                typeImg[i] = type.getString("TypeImg");
                                typeImgSelect[i] = type.getString("TypeImgSelect");
                            }
                            typeName[back.size()] = "管理分类";
                            typeColor[back.size()] = "#FFFFFFFF";
                            typeImgSelect[back.size()] = "http://lc-5r6UPI7d.cn-n1.lcfile.com/8QQDnK9jm0s3107nvin9ALaht1kqUv0s/%E7%AE%A1%E7%90%86.png";
                            typeImg[back.size()] = "http://lc-5r6UPI7d.cn-n1.lcfile.com/4yHhr5zPqxwgxnD0bS9MlOGm72VdmUsl/typeedit.png";
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, ledgerInfoFragment).commit();
                            ledgerInfoFragment.update(typeName, typeImg, typeImgSelect, typeColor,Lid,Tstate);
                    }

                    public void onError(Throwable throwable) {
                        Toast.makeText(LedgerInfo.this,"加载失败"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                    }

                    public void onComplete() {
                    }
                });

            }
            public void onError(Throwable throwable) {
                Toast.makeText(LedgerInfo.this,"加载失败"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
            }
            public void onComplete() {pgbar.setVisibility(View.GONE);}
        });
    }

    //加载账本背景
    private void bgLoad(String Name){
        if(Name.contains("日常")){
            Glide.with(LedgerInfo.this)
                    .load("http://lc-5r6UPI7d.cn-n1.lcfile.com/HIPHn8x6TweOsQloHf7WeBMTfyKJ2bf9/Daily%20ledger.png")
                    .into(ledgerbg);
            ledgerbg.setBackgroundColor(Color.parseColor("#fce9ce"));
            bg.setBackgroundColor(Color.parseColor("#fce9ce"));
        } else if(Name.contains("结婚")){
            Glide.with(LedgerInfo.this)
                    .load("http://lc-5r6UPI7d.cn-n1.lcfile.com/EhCVh4qtoLHLTLN5nDcUQKSUaaEVbEAD/Marriage%20ledger.png")
                    .into(ledgerbg);
            ledgerbg.setBackgroundColor(Color.parseColor("#f2c7b8"));
            bg.setBackgroundColor(Color.parseColor("#f2c7b8"));
        } else if(Name.contains("宝宝")){
            Glide.with(LedgerInfo.this)
                    .load("http://lc-5r6UPI7d.cn-n1.lcfile.com/l3QaWHqhy6xuDqOqDlP3qyKUpma5XLt1/baby_ledger.png")
                    .into(ledgerbg);
            ledgerbg.setBackgroundColor(Color.parseColor("#98A3C5"));
            bg.setBackgroundColor(Color.parseColor("#98A3C5"));
        } else if(Name.contains("自定义")){
            Glide.with(LedgerInfo.this)
                    .load("http://lc-5r6UPI7d.cn-n1.lcfile.com/HIPHn8x6TweOsQloHf7WeBMTfyKJ2bf9/Daily%20ledger.png")
                    .into(ledgerbg);
            ledgerbg.setBackgroundColor(Color.parseColor("#fce9ce"));
            bg.setBackgroundColor(Color.parseColor("#fce9ce"));
        } else {
            Glide.with(LedgerInfo.this)
                    .load("http://lc-5r6UPI7d.cn-n1.lcfile.com/HIPHn8x6TweOsQloHf7WeBMTfyKJ2bf9/Daily%20ledger.png")
                    .into(ledgerbg);
            ledgerbg.setBackgroundColor(Color.parseColor("#fce9ce"));
            bg.setBackgroundColor(Color.parseColor("#fce9ce"));
        }
    }

    //删除账本
    private void delete(){
        //取消全部定时任务
        WorkManager workManager = WorkManager.getInstance(getApplicationContext());
        workManager.cancelAllWork();

        //删除账本
        LCObject todo = LCObject.createWithoutData("Eledger", ledgerid);
        todo.deleteInBackground().subscribe(new Observer<LCNull>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {}
            @Override
            public void onNext(LCNull response) {
                ledger=true;
                hidepgbar.setValue(ledger&&type&&bill&&cyclebill&&budget);
            }
            @Override
            public void onError(@NonNull Throwable e) {
                ledger=true;
                hidepgbar.setValue(ledger&&type&&bill&&cyclebill&&budget);
                Log.e("用户账本删除失败",e.getMessage());
            }
            @Override
            public void onComplete() {}
        });

        //删除当前账本下所有非模板分类
        LCQuery<LCObject> query2 = new LCQuery<>("Etype");
        query2.whereContainedIn("objectId", Arrays.asList(typeid));
        query2.whereEqualTo("isModel",false);
        query2.deleteAllInBackground().subscribe(new Observer<LCNull>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(LCNull lcNull) {
                type=true;
                hidepgbar.setValue(ledger&&type&&bill&&cyclebill&&budget);
            }
            @Override
            public void onError(Throwable e) {
                type = true;
                hidepgbar.setValue(ledger && type && bill && cyclebill && budget);
                Log.e("用户分类删除失败",e.getMessage());
            }
            @Override
            public void onComplete() {}
        });

        //删除当前账本下所有账单
        LCQuery<LCObject> query3 = new LCQuery<>("Ebill");
        query3.whereEqualTo("Bledger",ledgerid);
        query3.limit(1000);
        query3.deleteAllInBackground().subscribe(new Observer<LCNull>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(LCNull lcNull) {
                bill=true;
                hidepgbar.setValue(ledger&&type&&bill&&cyclebill&&budget);
            }
            @Override
            public void onError(Throwable e) {
                bill=true;
                hidepgbar.setValue(ledger&&type&&bill&&cyclebill&&budget);
                Log.e("用户账单删除失败",e.getMessage());
            }
            @Override
            public void onComplete() {}
        });

        //删除该账本下全部周期账单
        LCQuery<LCObject> query4 = new LCQuery<>("Ecycle");
        query4.whereEqualTo("Cledger",ledgerid);
        query4.deleteAllInBackground().subscribe(new Observer<LCNull>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(LCNull lcNull) {
                cyclebill=true;
                hidepgbar.setValue(ledger&&type&&bill&&cyclebill&&budget);
            }
            @Override
            public void onError(Throwable e) {
                cyclebill=true;
                hidepgbar.setValue(ledger&&type&&bill&&cyclebill&&budget);
                Log.e("用户周期账单删除失败",e.getMessage());
            }
            @Override
            public void onComplete() {}
        });

        //删除该账本下全部预算
        LCQuery<LCObject> query5 = new LCQuery<>("Ebudget");
        query5.whereEqualTo("Bledger",ledgerid);
        query5.deleteAllInBackground().subscribe(new Observer<LCNull>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(LCNull lcNull) {
                budget=true;
                hidepgbar.setValue(ledger&&type&&bill&&cyclebill&&budget);
            }
            @Override
            public void onError(Throwable e) {
                budget=true;
                hidepgbar.setValue(ledger&&type&&bill&&cyclebill&&budget);
                Log.e("用户预算删除失败",e.getMessage());
            }
            @Override
            public void onComplete() {}
        });
    }

    private void setName(String Lid){
        LCQuery<LCObject> query = new LCQuery<>("Eledger");
        query.getInBackground(Lid).subscribe(new Observer<LCObject>() {
            @Override
            public void onSubscribe(Disposable d) {
                pgbar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onNext(LCObject ledger) {
                pgbar.setVisibility(View.GONE);
                String Lname=ledger.getString("Lname");
                bgLoad(Lname);
                Name.setText(Lname);
            }
            @Override
            public void onError(Throwable e) {
                pgbar.setVisibility(View.GONE);
                Log.e("用户账本加载失败",e.getMessage());
                Toast.makeText(LedgerInfo.this,"用户账本加载失败"+e.getMessage(),Toast.LENGTH_SHORT).show();}
            @Override
            public void onComplete() {}
        });
    }

    private void savename(){
        if (!Name.getText().toString().isEmpty()){
            LCUser user =LCUser.getCurrentUser();
            LCQuery<LCObject> query = new LCQuery<>("Eledger");
            query.whereEqualTo("Euser",user);
            query.findInBackground().subscribe(new Observer<List<LCObject>>() {
                @Override
                public void onSubscribe(Disposable d) {
                    pgbar.setVisibility(View.VISIBLE);
                }
                @Override
                public void onNext(List<LCObject> lcObjects) {
                    pgbar.setVisibility(View.GONE);
                    Boolean isin=false;
                    for (int i=0;i<lcObjects.size();i++){
                        LCObject object=lcObjects.get(i);
                        if (object.getString("Lname").equals(Name.getText().toString())&&!object.getObjectId().equals(ledgerid)){
                            Toast.makeText(LedgerInfo.this,"已存在相同名称的账本",Toast.LENGTH_SHORT).show();
                            isin=true;
                            break;
                        }
                    }
                    if (!isin){
                        LCObject todo = LCObject.createWithoutData("Eledger", ledgerid);
                        todo.put("Lname", Name.getText().toString());
                        todo.saveInBackground().subscribe(new Observer<LCObject>() {
                            public void onSubscribe(Disposable disposable) {}
                            public void onNext(LCObject savedTodo) {pgbar.setVisibility(View.VISIBLE);finish();}
                            public void onError(Throwable throwable) {Toast.makeText(LedgerInfo.this,"保存失败"+throwable.getMessage(),Toast.LENGTH_SHORT).show();}
                            public void onComplete() {}
                        });
                    }

                }
                @Override
                public void onError(Throwable e) {
                    pgbar.setVisibility(View.GONE);
                    Log.e("用户账本保存失败",e.getMessage());
                    Toast.makeText(LedgerInfo.this,"用户账本保存失败"+e.getMessage(),Toast.LENGTH_SHORT).show();}
                @Override
                public void onComplete() {}
            });
        }else {
            Toast.makeText(LedgerInfo.this,"请输入账本名称",Toast.LENGTH_SHORT).show();
        }
    }


}