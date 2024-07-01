package com.example.e_finance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.loper7.date_time_picker.DateTimeConfig;
import com.loper7.date_time_picker.dialog.CardDatePickerDialog;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class Bill_Add extends AppCompatActivity {
    private BillAddFragment billAddFragment = new BillAddFragment();

    private Button button0, button1, button2, button3, button4, button5, button6, button7, button8, button9,
            Subtraction, Addition, Result, point, Again;
    private String all = "";
    private ImageView clear,pgbar;
    private AnimationDrawable ad;
    private String ledgerid, typeid, billnote, billnumber, billtype,billid;
    private Boolean move = true,state,isfirst=true;
    private Long nowtime;
    private Date Bdate = null, BdateNow;
    private TextView BillAdd, BillReduce,billdate,notes, billaddtext, billledger;
    private BottomSheetDialog dialog;
    private ConstraintLayout constraintLayout;

    //计算器点击事件
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn0:
                    if (all.isEmpty() || all.equals("0")) {
                        all = "";
                    } else {
                        all += 0;
                        result(all);
                    }
                    break;
                case R.id.btn1:
                    all += 1;
                    result(all);
                    break;
                case R.id.btn2:
                    all += 2;
                    result(all);
                    break;
                case R.id.btn3:
                    all += 3;
                    result(all);
                    break;
                case R.id.btn4:
                    all += 4;
                    result(all);
                    break;
                case R.id.btn5:
                    all += 5;
                    result(all);
                    break;
                case R.id.btn6:
                    all += 6;
                    result(all);
                    break;
                case R.id.btn7:
                    all += 7;
                    result(all);
                    break;
                case R.id.btn8:
                    all += 8;
                    result(all);
                    break;
                case R.id.btn9:
                    all += 9;
                    result(all);
                    break;
                case R.id.point:
                    if (all.isEmpty()) {
                        all += "0.";
                        result(all);
                        break;
                    } else {
                        all += ".";
                        result(all);
                        break;
                    }
                case R.id.Subtraction:
                    if (all.endsWith("+ ") || all.endsWith("- ")) {
                        all = all.substring(0, all.length() - 3);
                    } else if (all.contains(" + ") || all.contains(" - ")) {
                        all = calculate(all);
                    }
                    if (all.isEmpty()) {
                        all += 0;
                    }
                    all += " " + "-" + " ";
                    result(all);
                    break;
                case R.id.Addition:
                    if (all.endsWith("+ ") || all.endsWith("- ")) {
                        all = all.substring(0, all.length() - 3);
                    } else if (all.contains(" + ") || all.contains(" - ")) {
                        all = calculate(all);
                    }
                    if (all.isEmpty()) {
                        all += 0;
                    }
                    all += " " + "+" + " ";
                    result(all);
                    break;
                case R.id.Finish:
                    getResult();
                    break;
//                    if (all.endsWith("+ ") || all.endsWith("- ")) {
//                        all = all.substring(0, all.length() - 3);
//                        result(all);
//                        break;
//                    } else if (all.startsWith("-") && all.lastIndexOf("-") == 0) {
//                        all = all.substring(1);
//                        result(all);
//                        break;
//                    } else {
//                        String res = calculate(all);
//                        if (res.equals("0")) {
//                            all = "";
//                        } else {
//                            all = res;
//                        }
//                        result(res);
//                        break;
//                    }

                case R.id.Clear:
                    if (all.isEmpty()) {
                        result("0");
                        break;
                    }
                    if (all.endsWith("+ ") || all.endsWith("- ")) {
                        all = all.substring(0, all.length() - 3);
                    } else {
                        all = all.substring(0, all.length() - 1);
                        if (all.isEmpty()) {
                            result("0");
                            break;
                        }
                    }
                    result(all);
                    break;
                case R.id.Again:
                    if (all.isEmpty() || (all.startsWith(String.valueOf(0)) && !all.startsWith("0."))) {
                        Toast.makeText(Bill_Add.this, "请输入账单金额", Toast.LENGTH_SHORT).show();
                    } else if ((all.contains("+") || all.contains("-")) && !all.startsWith("-")) {
                        getResult();
                    } else {
                        LCObject bill;
                        if  (billid!=null){
                            bill = LCObject.createWithoutData("Ebill", billid);
                        }else {
                            bill = new LCObject("Ebill");
                        }

                        if (Bdate == null) {
                            BdateNow = new Date(System.currentTimeMillis());
                            bill.put("Bdate", BdateNow);
                        } else {
                            bill.put("Bdate", Bdate);
                        }

                        if (all.startsWith("-")) {
                            all = all.substring(1, all.length());
                        }
                        bill.put("Bnum", all);

                        if (billnote == null) {
                            bill.put("Bnotes","");
                        } else {
                            bill.put("Bnotes", billnote);
                        }

                        bill.put("Bledger", ledgerid);
                        bill.put("Bstate", move);
                        bill.put("Btype", typeid);
                        bill.put("Bbudget",true);
                        bill.saveInBackground().subscribe(new Observer<LCObject>() {
                            public void onSubscribe(Disposable disposable) {
                                pgbar.setVisibility(View.VISIBLE);
                            }

                            public void onNext(LCObject todo) {
                                pgbar.setVisibility(View.GONE);
                                if (billid!=null){
                                    Intent intent = new Intent();
                                    intent.putExtra("BillId",todo.getObjectId());
                                    setResult(RESULT_OK,intent);
                                    billid=null;
                                }
                                Toast.makeText(Bill_Add.this, "账单已添加", Toast.LENGTH_SHORT).show();
                                all = "";
                                result(all + "0");
                            }

                            public void onError(Throwable throwable) {
                                pgbar.setVisibility(View.GONE);
                                Toast.makeText(Bill_Add.this, "添加失败," + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            public void onComplete() {
                                pgbar.setVisibility(View.GONE);
                            }
                        });
                    }
                    break;
            }
        }
    };

    private Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    initView(ledgerid, true);
                    break;
                case 1:
                    initView(ledgerid, false);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(Bill_Add.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_bill_add);

        BillAdd = findViewById(R.id.typeA);
        BillReduce = findViewById(R.id.typeR);
        billdate = findViewById(R.id.billdate);
        notes = findViewById(R.id.notes);
        billledger = findViewById(R.id.billledger);

        constraintLayout=findViewById(R.id.fragment);
        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        View view = findViewById(R.id.symbol);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, billAddFragment).commit();

        //获取数据
        Intent intent = getIntent();
        ledgerid = intent.getStringExtra("ledgerid");
        state = intent.getBooleanExtra("state", true);
        billnote = intent.getStringExtra("note");
        nowtime = intent.getLongExtra("date", 0);
        billnumber = intent.getStringExtra("number");
        billtype = intent.getStringExtra("typeid");
        billid=intent.getStringExtra("billid");

        if (billnumber != null) {
            all = billnumber;
        }

        if (billnote != null) {
            notes.setText(billnote);
        }

        //账本选择弹窗
        billledger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new BottomSheetDialog(Bill_Add.this, R.style.BottomSheetEdit);//要加上这个，否则键盘会覆盖一部分弹窗。
                View view = LayoutInflater.from(Bill_Add.this).inflate(R.layout.ledger_bottomsheetdialog, null);
                dialog.setContentView(view);
                RecyclerView userledger=view.findViewById(R.id.userledger);
                ImageView pgbar1=view.findViewById(R.id.pgbar);
                AnimationDrawable ad1=(AnimationDrawable)pgbar.getDrawable();
                pgbar1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ad1.start();
                    }
                }, 100);

                BillAddledgerdialogAdapter ladapter=new BillAddledgerdialogAdapter();
                List<Item> list=new ArrayList<>();
                LCUser user=LCUser.getCurrentUser();
                LCQuery<LCObject> query = new LCQuery<>("Eledger");
                query.whereEqualTo("Euser", user);
                query.findInBackground().subscribe(new Observer<List<LCObject>>() {
                    public void onSubscribe(Disposable disposable) {
                        pgbar1.setVisibility(View.VISIBLE);
                    }
                    public void onNext(List<LCObject> back) {
                        pgbar1.setVisibility(View.GONE);
                        String[] name=new String[back.size()];
                        String[] ledgerids=new String[back.size()];
                        int select=-1;
                        for (int i=0;i<back.size();i++){
                            LCObject object=back.get(i);
                            name[i]=object.getString("Lname");
                            ledgerids[i]=object.getObjectId();
                            Item item = new Item(name[i],ledgerids[i]);
                            list.add(item);
                            if (object.getObjectId().equals(ledgerid)){
                                select=i;
                            }
                        }
                        ladapter.setData(list);
                        ladapter.setSelectedPosition(select);
                        ladapter.setLdialog(dialog);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
                        userledger.setLayoutManager(layoutManager);
                        userledger.setAdapter(ladapter);
                        dialog.show();
                    }
                    public void onError(Throwable throwable) {
                        Toast.makeText(Bill_Add.this, "账本加载失败，" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    public void onComplete() {
                        pgbar1.setVisibility(View.GONE);
                    }
                });

            }
        });

        //备注添加弹窗
        notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new BottomSheetDialog(Bill_Add.this, R.style.BottomSheetEdit);//要加上这个，否则键盘会覆盖一部分弹窗。
                View view = LayoutInflater.from(Bill_Add.this).inflate(R.layout.note_bottomsheetdialog, null);
                dialog.setContentView(view);
                EditText usernote = view.findViewById(R.id.editText);
                Button certain =  view.findViewById(R.id.button);
                Button cancel = view.findViewById(R.id.cancel);

                if (!notes.getText().toString().isEmpty()) {
                    usernote.setText(billnote);
                }

                certain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        billnote = usernote.getText().toString();
                        if (billledger.getText().toString().length() >= 4 && billnote.length() > 6) {
                            notes.setText(billnote.substring(0, 6) + "...");
                        } else if (billnote.length() > 10) {
                            notes.setText(billnote.substring(0, 10) + "...");
                        } else {
                            notes.setText(billnote);
                        }
                        dialog.dismiss();
                    }
                });

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                //启动输入法
                InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    view.requestFocus();
                    imm.showSoftInput(view, 0);
                }
                dialog.show();
            }
        });

        //日期选择弹窗
        billdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nowtime != 0) {
                    new CardDatePickerDialog
                            .Builder(Bill_Add.this)
                            .setTitle("修改日期")
                            .showBackNow(true)
                            .setDefaultTime(nowtime)
                            .setThemeColor(Color.parseColor("#FFC107"))
                            .showFocusDateInfo(true)
                            .setDisplayType(
                                    DateTimeConfig.YEAR,//显示年
                                    DateTimeConfig.MONTH,//显示月
                                    DateTimeConfig.DAY,//显示日
                                    DateTimeConfig.HOUR,//显示时
                                    DateTimeConfig.MIN//显示分
                            )
                            .setBackGroundModel(0)
                            .setOnChoose("确定", aLong -> {
                                //aLong  = millisecond
                                nowtime = aLong;
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(aLong);
                                Bdate = calendar.getTime();
                                //设置时间格式
                                SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日");
                                //设置时区
                                formatter.setTimeZone(TimeZone.getTimeZone("GMT+08"));
                                //格式转换
                                String createDate = formatter.format(Bdate);
                                billdate.setText(createDate);
                                return null;
                            })
                            .build()
                            .show();
                }else {
                    new CardDatePickerDialog
                            .Builder(Bill_Add.this)
                            .setTitle("修改日期")
                            .showBackNow(true)
                            .setDefaultTime(System.currentTimeMillis())
                            .setThemeColor(Color.parseColor("#FFC107"))
                            .showFocusDateInfo(true)
                            .setDisplayType(
                                    DateTimeConfig.YEAR,//显示年
                                    DateTimeConfig.MONTH,//显示月
                                    DateTimeConfig.DAY,//显示日
                                    DateTimeConfig.HOUR,//显示时
                                    DateTimeConfig.MIN//显示分
                            )
                            .setBackGroundModel(0)
                            .setOnChoose("确定", aLong -> {
                                //aLong  = millisecond
                                nowtime = aLong;
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(aLong);
                                Bdate = calendar.getTime();
                                //设置时间格式
                                SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日");
                                //设置时区
                                formatter.setTimeZone(TimeZone.getTimeZone("GMT+08"));
                                //格式转换
                                String createDate = formatter.format(Bdate);
                                billdate.setText(createDate);
                                return null;
                            })
                            .build()
                            .show();
                }
            }
        });

        //支出账单
        BillAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initdate();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = Message.obtain();
                        msg.what = 0;
                        handler.sendMessage(msg);
                    }
                });
                thread.start();

                if (!move) {
                    ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "alpha", 1, 0.75f, 0.5f, 0, 1);
                    ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view, "translationX", BillReduce.getX() - BillAdd.getX(), 0);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.play(objectAnimator1)
                            .with(objectAnimator2);
                    animatorSet.setDuration(700);
                    animatorSet.start();
                    move = true;
                }
            }
        });

        //收入账单
        BillReduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initdate();
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message msg = Message.obtain();
                        msg.what = 1;
                        handler.sendMessage(msg);
                    }
                });
                thread.start();

                if (move) {
                    // 使用onGlobalLayout 来获取view的最终位置
                    ViewTreeObserver vto = BillReduce.getViewTreeObserver();
                    vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            // 布局完成后移除该监听器
                            BillReduce.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "alpha", 1, 0.75f, 0.5f, 0, 1);
                            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view, "translationX", 0, BillReduce.getX() - BillAdd.getX());

                            AnimatorSet animatorSet = new AnimatorSet();
                            animatorSet.play(objectAnimator1)
                                    .with(objectAnimator2);
                            animatorSet.setDuration(700);
                            animatorSet.start();
                            move = false;
                        }
                    });
                }
            }
        });

        //绑定计算器控件
        button0 = findViewById(R.id.btn0);
        button1 = findViewById(R.id.btn1);
        button2 = findViewById(R.id.btn2);
        button3 = findViewById(R.id.btn3);
        button4 = findViewById(R.id.btn4);
        button5 = findViewById(R.id.btn5);
        button6 = findViewById(R.id.btn6);
        button7 = findViewById(R.id.btn7);
        button8 = findViewById(R.id.btn8);
        button9 = findViewById(R.id.btn9);
        point = findViewById(R.id.point);
        clear = findViewById(R.id.Clear);
        Subtraction = findViewById(R.id.Subtraction);
        Addition = findViewById(R.id.Addition);
        Result = findViewById(R.id.Finish);
        Again = findViewById(R.id.Again);

        //为计算器绑定点击事件
        button0.setOnClickListener(listener);
        button1.setOnClickListener(listener);
        button2.setOnClickListener(listener);
        button3.setOnClickListener(listener);
        button4.setOnClickListener(listener);
        button5.setOnClickListener(listener);
        button6.setOnClickListener(listener);
        button7.setOnClickListener(listener);
        button8.setOnClickListener(listener);
        button9.setOnClickListener(listener);
        point.setOnClickListener(listener);
        Addition.setOnClickListener(listener);
        clear.setOnClickListener(listener);
        Subtraction.setOnClickListener(listener);
        Again.setOnClickListener(listener);


        //初始点击显示支出账单
        if (state) {
            BillAdd.callOnClick();
        } else {
            BillReduce.callOnClick();
        }

    }

    //再次启动时刷新数据
    @Override
    public void onRestart() {
        super.onRestart();
        updatetype();
    }

    //计算器
    private String calculate(String expression) {
        String[] tokens = expression.split(" ");

        BigDecimal b1 = new BigDecimal(tokens[0]);
        String operator = tokens[1];
        BigDecimal b2 = new BigDecimal(tokens[2]);

        Double result = 0.0;
        switch (operator) {
            case "+":
                result = b1.add(b2).doubleValue();
                break;
            case "-":
                result = b1.subtract(b2).doubleValue();
                break;
        }
        String back = String.valueOf(result);
        if (back.endsWith(".0")) {
            back = String.valueOf(result).substring(0, back.length() - 2);
        }
        return back;
    }

    //初始化时间数据
    private void initdate(){
        if (nowtime==0){
            BdateNow = new Date(System.currentTimeMillis());
            SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+08"));
            String createDate = formatter.format(BdateNow);
            billdate.setText(createDate);
        }else {
            SimpleDateFormat formatter = new SimpleDateFormat("MM月dd日");
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+08"));
            String createDate = formatter.format(nowtime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(nowtime);
            Bdate = calendar.getTime();
            billdate.setText(createDate);
        }

    }

    //初始化分类数据
    private void initView(String ledgerid, Boolean Tstate) {
        LCQuery<LCObject> query = new LCQuery<>("Eledger");
        query.getInBackground(ledgerid).subscribe(new Observer<LCObject>() {
            public void onSubscribe(Disposable disposable) {
                if (isfirst){
                    constraintLayout.setVisibility(View.INVISIBLE);
                    isfirst=false;
                }
                pgbar.setVisibility(View.VISIBLE);
            }
            public void onNext(LCObject ledger) {
                billledger.setText(ledger.getString("Lname"));
                String[] type = ledger.getJSONArray("Ltype").toArray(new String[0]);
                LCQuery<LCObject> typeinfo = new LCQuery<>("Etype");
                typeinfo.whereContainedIn("objectId", Arrays.asList(type));
                typeinfo.whereEqualTo("Tstate", Tstate);
                typeinfo.findInBackground().subscribe(new Observer<List<LCObject>>() {
                    public void onSubscribe(Disposable disposable) {
                    }

                    public void onNext(List<LCObject> back) {
                        pgbar.setVisibility(View.GONE);
                        String[] typeName = new String[back.size() + 1];
                        String[] typeColor = new String[back.size() + 1];
                        String[] typeImg = new String[back.size() + 1];
                        String[] typeImgSelect = new String[back.size() + 1];
                        String[] ids = new String[back.size() + 1];
                        Boolean[] tstate = new Boolean[back.size() + 1];

                        for (int i = 0; i < back.size(); i++) {
                            LCObject type = back.get(i);
                            typeName[i] = type.getString("TypeName");
                            typeColor[i] = type.getString("TypeColor");
                            typeImg[i] = type.getString("TypeImg");
                            typeImgSelect[i] = type.getString("TypeImgSelect");
                            ids[i] = type.getObjectId();
                            tstate[i] = type.getBoolean("Tstate");
                        }
                        typeName[back.size()] = "管理分类";
                        typeColor[back.size()] = "#FFFFFFFF";
                        typeImg[back.size()] = "http://lc-5r6UPI7d.cn-n1.lcfile.com/8QQDnK9jm0s3107nvin9ALaht1kqUv0s/%E7%AE%A1%E7%90%86.png";
                        typeImgSelect[back.size()] = "http://lc-5r6UPI7d.cn-n1.lcfile.com/4yHhr5zPqxwgxnD0bS9MlOGm72VdmUsl/typeedit.png";
                        ids[back.size()] = "655b07fe0fc49353657f2536";
                        tstate[back.size()] = null;

                        List<Typeitem> list = new ArrayList<>();
                        for (int i = 0; i < typeName.length; i++) {
                            Typeitem typeitem = new Typeitem(typeName[i], typeColor[i], typeImg[i], typeImgSelect[i], ids[i], tstate[i]);
                            list.add(typeitem);
                        }

                        if (billtype!=null){
                            int select ;
                            List<String> alltype= Arrays.asList(ids);
                            select=alltype.indexOf(billtype);
                            billAddFragment.update(Tstate, list, ledgerid,select);
                        }else {
                            billAddFragment.update(Tstate, list, ledgerid,-1);
                        }

                        if (all.isEmpty()) {
                            result("0");
                        } else {
                            result(all);
                        }
                    }

                    public void onError(Throwable throwable) {
                        if (LCUser.getCurrentUser()!=null){
                            Toast.makeText(Bill_Add.this, "加载失败，" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                        Log.e("BillAdd_Show_Type",throwable.getMessage());
                    }

                    public void onComplete() {
                        pgbar.setVisibility(View.GONE);
                        constraintLayout.setVisibility(View.VISIBLE);
                    }
                });

            }

            public void onError(Throwable throwable) {
                Toast.makeText(Bill_Add.this, "加载失败，" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                onBackPressed();
                Log.e("BillAdd_Show_Ledger",throwable.getMessage());
            }
            public void onComplete() {
            }
        });

    }

    //设置fragment上显示数字
    private void result(String text) {
        billAddFragment.setResult(text);
    }

    //回调更新数据
    public void setBillAdd(String typeid) {
        this.typeid = typeid;
    }

    public void setLedgerid(String ledgerid) {
        this.ledgerid = ledgerid;
    }
    public void updatetype(){
        if (move) {
            BillAdd.callOnClick();
        } else {
            BillReduce.callOnClick();
        }
    }

    //设置检测金额是否存在加减运算，存在则更改完成按钮点击事件为计算总和,不存在则设置点击事件为保存账单
    public void setbilladdtext(TextView view) {
        this.billaddtext = view;
        if (all.isEmpty() || billnumber == null) {
            Result.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Bill_Add.this, "请输入账单金额", Toast.LENGTH_SHORT).show();
                }
            });
        }

        billaddtext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if ((text.contains("+") || text.contains("-")) && !text.startsWith("-")) {
                    Result.setText("=");
                    Result.setOnClickListener(listener);
                } else {
                    Result.setText("完成");
                    Result.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (all.isEmpty() || (all.startsWith(String.valueOf(0)) && !all.startsWith("0."))) {
                                Toast.makeText(Bill_Add.this, "请输入账单金额", Toast.LENGTH_SHORT).show();
                            } else {
                                LCObject bill;
                                if  (billid!=null){
                                    bill = LCObject.createWithoutData("Ebill", billid);
                                }else {
                                    bill = new LCObject("Ebill");
                                }

                                if (Bdate == null) {
                                    BdateNow = new Date(System.currentTimeMillis());
                                    bill.put("Bdate", BdateNow);
                                } else {
                                    bill.put("Bdate", Bdate);
                                }

                                if (all.startsWith("-")) {
                                    all = all.substring(1, all.length());
                                }
                                bill.put("Bnum", all);

                                if (billnote == null) {
                                    bill.put("Bnotes","");
                                } else {
                                    bill.put("Bnotes", billnote);
                                }

                                bill.put("Bstate", move);
                                bill.put("Btype", typeid);
                                bill.put("Bledger", ledgerid);
                                bill.saveInBackground().subscribe(new Observer<LCObject>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        pgbar.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onNext(LCObject lcObject) {
                                        pgbar.setVisibility(View.GONE);
                                        Intent intent = new Intent();
                                        intent.putExtra("BillId",lcObject.getObjectId());
                                        setResult(RESULT_OK,intent);
                                        finish();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        pgbar.setVisibility(View.GONE);
                                        Toast.makeText(Bill_Add.this, "账单添加失败，" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onComplete() {
                                        pgbar.setVisibility(View.GONE);
                                    }
                                });

                            }
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void getResult(){
        if (all.endsWith("+ ") || all.endsWith("- ")) {
            all = all.substring(0, all.length() - 3);
            result(all);
        } else if (all.startsWith("-") && all.lastIndexOf("-") == 0) {
            all = all.substring(1);
            result(all);
        } else {
            String res = calculate(all);
            if (res.equals("0")) {
                all = "";
            } else {
                all = res;
            }
            result(res);
        }
    }
}