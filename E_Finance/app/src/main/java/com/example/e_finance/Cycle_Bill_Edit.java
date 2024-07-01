package com.example.e_finance;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.loper7.date_time_picker.number_picker.NumberPicker;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class Cycle_Bill_Edit extends AppCompatActivity {

    private String all = "",ledgerid,cyclenote,cyclenumber, cycletype,cycleid,billcycle;
    private ImageView clear,pgbar;
    private AnimationDrawable ad;
    private Button button0, button1, button2, button3, button4, button5, button6, button7, button8, button9,
            Subtraction, Addition, Result, point, Again;
    private TextView cycledate,notes, cycleledger,cyclenum;
    private BottomSheetDialog dialog;
    private ViewPager2 fragment;
    private TabLayout tabLayout;
    private Boolean isfirst=true,cycleBstate=true,lastcyclestate=false;
    private List<TypeShowFragment> fragmentlist=new ArrayList<>();
    private String getType="每月",getInfo="1日";
    private Adapter adapter;
    private int sel=0;
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
                    if (all.endsWith("+ ") || all.endsWith("- ")) {
                        all = all.substring(0, all.length() - 3);
                        result(all);
                        break;
                    } else if (all.startsWith("-") && all.lastIndexOf("-") == 0) {
                        all = all.substring(1);
                        result(all);
                        break;
                    } else {
                        String res = calculate(all);
                        if (res.equals("0")) {
                            all = "";
                        } else {
                            all = res;
                        }
                        result(res);
                        break;
                    }

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
                        Toast.makeText(Cycle_Bill_Edit.this, "请输入账单金额", Toast.LENGTH_SHORT).show();
                    } else {
                        SaveCycle(true);
                        all="";
                        result("0");
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(Cycle_Bill_Edit.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_cycle_bill_edit);

        initView();

    }
    private void initView(){

        cycledate = findViewById(R.id.billdate);
        notes = findViewById(R.id.notes);
        cycleledger = findViewById(R.id.billledger);

        fragment=findViewById(R.id.fragment);
        tabLayout=findViewById(R.id.tab_layout);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

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

        Result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveCycle(false);
            }
        });

        CreateFragment();

        //账本选择弹窗
        cycleledger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new BottomSheetDialog(Cycle_Bill_Edit.this, R.style.BottomSheetEdit);//要加上这个，否则键盘会覆盖一部分弹窗。
                View view = LayoutInflater.from(Cycle_Bill_Edit.this).inflate(R.layout.ledger_bottomsheetdialog, null);
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

                BillAddledgerdialogAdapter ladapter=new BillAddledgerdialogAdapter("cycle");
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
                        pgbar1.setVisibility(View.GONE);
                        Toast.makeText(Cycle_Bill_Edit.this, "账本加载失败，" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    public void onComplete() {}
                });

            }
        });

        //备注添加弹窗
        notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new BottomSheetDialog(Cycle_Bill_Edit.this, R.style.BottomSheetEdit);//要加上这个，否则键盘会覆盖一部分弹窗。
                View view = LayoutInflater.from(Cycle_Bill_Edit.this).inflate(R.layout.note_bottomsheetdialog, null);
                dialog.setContentView(view);
                EditText usernote = view.findViewById(R.id.editText);
                Button certain =  view.findViewById(R.id.button);
                Button cancel = view.findViewById(R.id.cancel);

                if (!notes.getText().toString().isEmpty()) {
                    usernote.setText(cyclenote);
                }
                certain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cyclenote = usernote.getText().toString();
                        if (cycleledger.getText().toString().length() >= 4 && cyclenote.length() > 6) {
                            notes.setText(cyclenote.substring(0, 6) + "...");
                        } else if (cyclenote.length() > 10) {
                            notes.setText(cyclenote.substring(0, 10) + "...");
                        } else {
                            notes.setText(cyclenote);
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

        //周期选择弹窗
        cycledate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = new BottomSheetDialog(Cycle_Bill_Edit.this, R.style.BottomSheetEdit);//要加上这个，否则键盘会覆盖一部分弹窗。
                View view = LayoutInflater.from(Cycle_Bill_Edit.this).inflate(R.layout.cycledate_bottomsheetdialog, null);
                dialog.setContentView(view);

                NumberPicker numberPicker=view.findViewById(R.id.numberPicker);
                NumberPicker numberPicker2=view.findViewById(R.id.numberPicker2);
                String[] datas = new String[]{"每日", "每月","每日","每月"};
                numberPicker.setDisplayedValues(datas); //设置文字
                numberPicker.setMaxValue(datas.length - 1); //设置最大值，最大值是数组最大下标
                numberPicker.setMinValue(0);
                numberPicker.setDividerColor(Color.parseColor("#E6E6E6"));
                numberPicker.setSelectedTextColor(Color.parseColor("#FFC107"));

                numberPicker2.setDividerColor(Color.parseColor("#E6E6E6"));
                numberPicker2.setSelectedTextColor(Color.parseColor("#FFC107"));

                if (billcycle!=null){
                    int info=Integer.parseInt(billcycle.substring(2,billcycle.length()-1));
                    if (billcycle.startsWith("每日")){
                        numberPicker.setValue(0);
                        numberPicker2.setMaxValue(24);
                        numberPicker2.setFormatter(new NumberPicker.Formatter() {
                            @Override
                            public String format(int value) {
                                String data=value+"时";
                                return data;
                            }
                        });
                        getType="每日";
                        getInfo=info+"时";
                    }else {
                        getType="每月";
                        getInfo=info+"日";
                        numberPicker2.setMaxValue(30);
                        numberPicker2.setFormatter(new NumberPicker.Formatter() {
                            @Override
                            public String format(int value) {
                                String data=value+"日";
                                return data;
                            }
                        });
                    }
                    numberPicker2.setValue(info);
                }else {
                    numberPicker2.setMaxValue(30);
                    numberPicker2.setFormatter(new NumberPicker.Formatter() {
                        @Override
                        public String format(int value) {
                            String data=value+"日";
                            return data;
                        }
                    });
                }

                numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        getType=datas[newVal];
                        if (newVal % 2 == 0) {
                            numberPicker2.setMaxValue(24);
                            numberPicker2.setFormatter(new NumberPicker.Formatter() {
                                @Override
                                public String format(int value) {
                                    String data=value+"时";
                                    return data;
                                }
                            });
                        } else {
                            numberPicker2.setMaxValue(30);
                            numberPicker2.setFormatter(new NumberPicker.Formatter() {
                                @Override
                                public String format(int value) {
                                    String data=value+"日";
                                    return data;
                                }
                            });
                        }
                    }
                });

                numberPicker2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        if (picker.getMaxValue()>24){
                            getInfo=newVal+"日";
                        }else {
                            getInfo=newVal+"时";
                        }
                    }
                });


                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        billcycle=getType+getInfo;
                        cycledate.setText(billcycle);
                    }
                });
                dialog.show();
            }
        });

    }

    private void result(String re){
        TypeShowFragment typeShowFragment=fragmentlist.get(tabLayout.getSelectedTabPosition());
        typeShowFragment.setResult(re);
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

//    设置检测金额是否存在加减运算，存在则更改完成按钮点击事件为计算总和
//    不存在则设置点击事件为保存周期账单并启动
    public void setNumberText(TextView textView) {
        this.cyclenum=textView;
        if (all.isEmpty()) {
            Result.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Cycle_Bill_Edit.this, "请输入账单金额", Toast.LENGTH_SHORT).show();
                }
            });
        }

        cyclenum.addTextChangedListener(new TextWatcher() {
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
                            SaveCycle(false);
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    private void SaveCycle(Boolean isNext){
        if (all.isEmpty() || (all.startsWith(String.valueOf(0)) && !all.startsWith("0."))) {
            Toast.makeText(Cycle_Bill_Edit.this, "请输入账单金额", Toast.LENGTH_SHORT).show();
        } else {
            LCObject bill;
            if  (cycleid!=null){
                bill = LCObject.createWithoutData("Ecycle", cycleid);
            }else {
                bill = new LCObject("Ecycle");
            }

            bill.put("Cdate", cycledate.getText().toString());
            if (all.startsWith("-")) {
                all = all.substring(1, all.length());
            }
            bill.put("Cnum", all);

            if (cyclenote == null) {
                bill.put("Cnotes","");
            } else {
                bill.put("Cnotes", cyclenote);
            }

            if (tabLayout.getSelectedTabPosition()==0){
                bill.put("Cbstate", true);
            }else {
                bill.put("Cbstate", false);
            }

            bill.put("Ctype", cycletype);
            bill.put("Cledger", ledgerid);
            bill.put("Cstate",true);
            bill.saveInBackground().subscribe(new Observer<LCObject>() {
                @Override
                public void onSubscribe(Disposable d) {
                    pgbar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onNext(LCObject lcObject) {
                    pgbar.setVisibility(View.GONE);
                    Calendar calendar=Calendar.getInstance();
                    Calendar curDate=Calendar.getInstance();
                    calendar.set(Calendar.MINUTE,0);
                    calendar.set(Calendar.SECOND, 0);
                    String Billcycle=lcObject.getString("Cdate");

                    if (Billcycle.startsWith("每月")){
                        try {
                            int number = Integer.parseInt(Billcycle.substring(2,Billcycle.length()-1));
                            // 如果到这里没有异常，那么str是一个有效的整数
                            if (number>calendar.getActualMaximum(Calendar.DAY_OF_MONTH)){
                                calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                            }else {
                                calendar.set(Calendar.DAY_OF_MONTH,number);
                            }
                            calendar.set(Calendar.HOUR_OF_DAY, 0);
                            if (calendar.before(curDate)){
                                calendar.add(Calendar.MONTH,1);
                            }
                        } catch (NumberFormatException e) {
                            // str不是一个有效的整数，处理异常
                            e.printStackTrace();
                        }
                    } else if (Billcycle.startsWith("每日")) {
                        try {
                            int number = Integer.parseInt(Billcycle.substring(2,Billcycle.length()-1));
                            // 如果到这里没有异常，那么str是一个有效的整数
                            calendar.set(Calendar.HOUR_OF_DAY,number);
                            if (calendar.before(curDate)){
                                calendar.add(Calendar.DAY_OF_MONTH,1);
                            }
                        } catch (NumberFormatException e) {
                            // str不是一个有效的整数，处理异常
                            e.printStackTrace();
                        }
                    }

                    if (lastcyclestate){
                        WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(cycleid);
                    }

                    // 创建约束条件：可选地设置任务的网络状态、电池状态等
                    Constraints constraints = new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED) // 网络连接时才执行任务
                            .build();
                    // 创建输入数据
                    Data data = new Data.Builder()
                            .putString("cycle", Billcycle)
                            .putString("cycleid", lcObject.getObjectId())
                            .build();
                    long timeDiff = calendar.getTimeInMillis() - curDate.getTimeInMillis();
                    WorkRequest dailyWorkRequest =
                            new OneTimeWorkRequest.Builder(CycleWorker.class)
                                    .setConstraints(constraints)
                                    .setInputData(data)
                                    .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                                    .addTag(lcObject.getObjectId())
                                    .build();
                    WorkManager.getInstance(getApplicationContext()).enqueue(dailyWorkRequest);

                    if (!isNext){
                        finish();
                    }else {
                        Toast.makeText(Cycle_Bill_Edit.this,"账单已添加",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Throwable e) {
                    pgbar.setVisibility(View.GONE);
                    if (cycleid!=null){
                        Toast.makeText(Cycle_Bill_Edit.this, "账单修改失败，" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(Cycle_Bill_Edit.this, "账单添加失败，" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onComplete() {}
            });

        }
    }

    private void CreateFragment(){
        //获取数据
        Intent intent = getIntent();
        ledgerid = intent.getStringExtra("ledgerid");
        cycleBstate = intent.getBooleanExtra("CBstate", true);
        cyclenote = intent.getStringExtra("note");
        billcycle = intent.getStringExtra("cycle");
        cyclenumber = intent.getStringExtra("number");
        cycletype = intent.getStringExtra("typeid");
        cycleid=intent.getStringExtra("cycleid");
        lastcyclestate=intent.getBooleanExtra("lastCstate",false);

        if (cyclenumber != null) {
            all = cyclenumber;
        }
        if (billcycle!=null){
            cycledate.setText(billcycle);
        }
        if (cyclenote != null) {
            notes.setText(cyclenote);
        }
        if (ledgerid!=null){
            GetType(ledgerid);
        }else {
            LCUser user = LCUser.getCurrentUser();
            LCQuery<LCObject> query = new LCQuery<>("Eledger");
            query.whereEqualTo("Euser", user);
            query.whereEqualTo("isUsed", true);
            query.getFirstInBackground().subscribe(new Observer<LCObject>() {
                @Override
                public void onSubscribe(Disposable d) {
                    pgbar.setVisibility(View.VISIBLE);
                }
                @Override
                public void onNext(LCObject lcObject) {
                    ledgerid = lcObject.getObjectId();
                    GetType(ledgerid);
                }
                @Override
                public void onError(Throwable e) {
                    pgbar.setVisibility(View.GONE);
                    Toast.makeText(Cycle_Bill_Edit.this, "用户账本信息加载失败，" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onComplete() {
                    pgbar.setVisibility(View.GONE);
                }
            });
        }

    }

    public void GetType(String ledgerid){
        LCQuery<LCObject> query = new LCQuery<>("Eledger");
        query.getInBackground(ledgerid).subscribe(new Observer<LCObject>() {
            public void onSubscribe(Disposable disposable) {
                if (isfirst){
                    fragment.setVisibility(View.INVISIBLE);
                    isfirst=false;
                }
                pgbar.setVisibility(View.VISIBLE);
            }
            public void onNext(LCObject ledger) {
                cycleledger.setText(ledger.getString("Lname"));
                String[] type = ledger.getJSONArray("Ltype").toArray(new String[0]);
                LCQuery<LCObject> typeinfo = new LCQuery<>("Etype");
                typeinfo.whereContainedIn("objectId", Arrays.asList(type));
                typeinfo.findInBackground().subscribe(new Observer<List<LCObject>>() {
                    public void onSubscribe(Disposable disposable) {
                    }

                    public void onNext(List<LCObject> back) {
                        List<Typeitem> list = new ArrayList<>();
                        List<Typeitem> listR = new ArrayList<>();
                        List<String> ida = new ArrayList<>();
                        List<String> idr = new ArrayList<>();

                        for (int i = 0; i < back.size(); i++) {
                            LCObject type = back.get(i);
                            Typeitem typeitem = new Typeitem(type.getString("TypeName"), type.getString("TypeColor"), type.getString("TypeImg"), type.getString("TypeImgSelect"), type.getObjectId(), type.getBoolean("Tstate"));
                            if (type.getBoolean("Tstate")){
                                list.add(typeitem);
                                ida.add(type.getObjectId());
                            }else {
                                idr.add(type.getObjectId());
                                listR.add(typeitem);
                            }
                        }

                        Typeitem typeitem = new Typeitem("管理分类","#FFFFFFFF","http://lc-5r6UPI7d.cn-n1.lcfile.com/8QQDnK9jm0s3107nvin9ALaht1kqUv0s/%E7%AE%A1%E7%90%86.png","http://lc-5r6UPI7d.cn-n1.lcfile.com/4yHhr5zPqxwgxnD0bS9MlOGm72VdmUsl/typeedit.png","655b07fe0fc49353657f2536",null);
                        list.add(typeitem);
                        listR.add(typeitem);

                        if (cycletype!=null){
                            if (cycleBstate){
                                sel=ida.indexOf(cycletype);
                            }else {
                                sel=idr.indexOf(cycletype);
                            }
                        }

                        if (fragmentlist.size()==0){
                            if (cycleBstate){
                                fragmentlist.add(new TypeShowFragment(true,list,ledgerid,sel,"cycle"));
                                fragmentlist.add(new TypeShowFragment(false, listR, ledgerid,0,"cycle"));
                            }else {
                                fragmentlist.add(new TypeShowFragment(true,list,ledgerid,0,"cycle"));
                                fragmentlist.add(new TypeShowFragment(false, listR, ledgerid,sel,"cycle"));
                            }
                        }else {
                            if (cycleBstate){
                                fragmentlist.get(0).update(true, list, ledgerid,sel);
                                fragmentlist.get(1).update(false, listR, ledgerid,0);
                            }else {
                                fragmentlist.get(0).update(true, list, ledgerid,0);
                                fragmentlist.get(1).update(false, listR, ledgerid,sel);
                            }
                        }

                        if (adapter==null){
                            adapter=new Adapter(Cycle_Bill_Edit.this,fragmentlist);
                            fragment.setAdapter(adapter);
                            new TabLayoutMediator(tabLayout, fragment, (tab, position) -> {
                                // 为每个标签设置文本
                                switch (position){
                                    case 0:
                                        tab.setText("支出");
                                        break;
                                    case 1:
                                        tab.setText("收入");
                                        break;
                                }
                            }).attach(); // 连接 TabLayout 和 ViewPager2

                            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                                String getNum;
                                @Override
                                public void onTabSelected(TabLayout.Tab tab) {
                                    if (tab.getPosition()==0){
                                        cycleBstate=true;
                                    }else {
                                        cycleBstate=false;
                                    }

                                    TextView textView=fragmentlist.get(tab.getPosition()).getTextView();
                                    if (textView!=null){
                                        getNum = textView.getText().toString();
                                        all = getNum;
                                        if (all.equals("0")) {
                                            all = "";
                                        }
                                    }

                                    sel=fragmentlist.get(tab.getPosition()).getSelectview();
                                    if (tab.getPosition()==0){
                                        cycletype=ida.get(sel);
                                    }else {
                                        cycletype=idr.get(sel);
                                    }
                                }

                                @Override
                                public void onTabUnselected(TabLayout.Tab tab) {}

                                @Override
                                public void onTabReselected(TabLayout.Tab tab) {}
                            });
                        }

                        if (!cycleBstate){
                            fragment.setCurrentItem(1, false);
                        }

                        if (all.isEmpty()) {
                            result("0");
                        } else {
                            result(all);
                        }
                    }

                    public void onError(Throwable throwable) {
                        pgbar.setVisibility(View.GONE);
                        Toast.makeText(Cycle_Bill_Edit.this, "加载失败，" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }

                    public void onComplete() {
                        pgbar.setVisibility(View.GONE);
                        fragment.setVisibility(View.VISIBLE);
                    }
                });

            }

            public void onError(Throwable throwable) {
                Toast.makeText(Cycle_Bill_Edit.this, "加载失败，" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
            public void onComplete() {
            }
        });
    }

    public void setCycletype(String cycletype) {
        this.cycletype = cycletype;
    }

    public void setLedgerid(String ledgerid) {
        this.ledgerid = ledgerid;
    }

    //再次启动时刷新数据
    @Override
    public void onRestart() {
        super.onRestart();
       GetType(ledgerid);
    }

    public class Adapter extends FragmentStateAdapter{
        private List<TypeShowFragment> fragmentList;
        public Adapter(@NonNull FragmentActivity fragmentActivity,List<TypeShowFragment> fragmentList) {
            super(fragmentActivity);
            this.fragmentList=fragmentList;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getItemCount() {
            return fragmentList.size();
        }
    }
}