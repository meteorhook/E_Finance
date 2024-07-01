package com.example.e_finance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.TestLooperManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_finance.ui.bill.BillFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kyleduo.switchbutton.SwitchButton;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class Bill_Budget extends AppCompatActivity {
    private TextView confirm,namelength,endBudget,cycle,cycleAutoAdd,cycleAutoReduce;
    private ConstraintLayout budcycle, budAutoAdd, budAutoReduce;
    private SwitchButton autoAdd, autoReduce;
    private EditText budgetName, budgetNum;
    private ImageView pgbar;
    private AnimationDrawable ad;
    private String budgetid,ledgerid;
    private Boolean autoUnchange=false,lastautoR=false;
    private String unchangecycle="每月",lastBname="",lastBnum="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(Bill_Budget.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_bill_budget);

        Init();
    }
    private void Init (){
        confirm=findViewById(R.id.confirm);
        namelength=findViewById(R.id.namelength);
        cycle=findViewById(R.id.cycle);
        endBudget=findViewById(R.id.endBudget);
        cycleAutoAdd=findViewById(R.id.tv5);
        cycleAutoReduce=findViewById(R.id.tv7);

        budcycle=findViewById(R.id.budcycle);
        budAutoAdd=findViewById(R.id.budAutoAdd);
        budAutoReduce=findViewById(R.id.budAutoReduce);

        budgetName=findViewById(R.id.budgetName);
        budgetNum=findViewById(R.id.budgetNum);

        autoAdd=findViewById(R.id.budAutoSwitch);
        autoReduce=findViewById(R.id.budAutoSwitch2);


        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        Intent intent=getIntent();
        budgetid=intent.getStringExtra("budgetid");
        ledgerid=intent.getStringExtra("ledgerid");

        if (budgetid!=null&&!budgetid.equals("0")){
            LCQuery query=new LCQuery("Ebudget");
            query.getInBackground(budgetid).subscribe(new Observer<LCObject>() {
                @Override
                public void onSubscribe(Disposable d) {pgbar.setVisibility(View.VISIBLE);}
                @Override
                public void onNext(LCObject lcObject) {
                    lastBname=lcObject.getString("Bbudget");
                    budgetName.setText(lastBname);
                    lastBnum=lcObject.getString("Bnum");
                    budgetNum.setText(lastBnum);
                    unchangecycle=lcObject.getString("Bcycle");
                    cycle.setText(unchangecycle);
                    if (unchangecycle.equals("每日")){
                        cycleAutoAdd.setText("开启后,明日预算金额=预算金额+今日剩余预算金额");
                        cycleAutoReduce.setText("开启后,今日预算超支,明日预算会自动扣除");
                    } else if (unchangecycle.equals("每月")) {
                        cycleAutoAdd.setText("开启后,下月预算金额=预算金额+本月剩余预算金额");
                        cycleAutoReduce.setText("开启后,本月预算超支,下月预算会自动扣除");
                    }
                    autoUnchange=lcObject.getBoolean("BautoAdd");
                    autoAdd.setChecked(autoUnchange);
                    lastautoR=lcObject.getBoolean("BautoReduce");
                    autoReduce.setChecked(lastautoR);
                    endBudget.setVisibility(View.VISIBLE);
                }
                @Override
                public void onError(Throwable e) {
                    Toast.makeText(Bill_Budget.this,e.getMessage()+",加载失败，请重试。",Toast.LENGTH_SHORT).show();
                    pgbar.setVisibility(View.GONE);
                }
                @Override
                public void onComplete() {
                    pgbar.setVisibility(View.GONE);
                }
            });
        }

        budgetName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                namelength.setText(s.length()+"/15");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        autoAdd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    budAutoReduce.setVisibility(View.VISIBLE);
                }else {
                    budAutoReduce.setVisibility(View.GONE);
                    autoReduce.setChecked(false);
                }
            }
        });

        budAutoAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoAdd.setChecked(!autoAdd.isChecked());
            }
        });

        budAutoReduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoReduce.setChecked(!autoReduce.isChecked());
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveBudget();
            }
        });

        endBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LCObject budget = LCObject.createWithoutData("Ebudget", budgetid);
                budget.deleteInBackground().subscribe(new Observer<LCNull>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        pgbar.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onNext(LCNull lcNull) {
                        WorkManager.getInstance(Bill_Budget.this).cancelAllWorkByTag("autoAdd");
                        WorkManager.getInstance(Bill_Budget.this).cancelAllWorkByTag("dayautoAdd");
                        Toast.makeText(Bill_Budget.this,"预算已结束",Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        pgbar.setVisibility(View.GONE);
                        Toast.makeText(Bill_Budget.this,e.getMessage()+",预算结束失败，请重试。",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        pgbar.setVisibility(View.GONE);
                    }
                });
            }
        });

        budcycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog dialog=new BottomSheetDialog(Bill_Budget.this, R.style.BottomSheetEdit);//要加上这个，否则键盘会覆盖一部分弹窗。
                View view = LayoutInflater.from(Bill_Budget.this).inflate(R.layout.cycle_bottomsheetdialog, null);
                dialog.setContentView(view);
                TextView month=view.findViewById(R.id.month);
                TextView day=view.findViewById(R.id.day);
                ImageView monthcheck=view.findViewById(R.id.monthcheck);
                ImageView daycheck=view.findViewById(R.id.daycheck);

                if (cycle.getText().toString().equals("每月")){
                    monthcheck.setVisibility(View.VISIBLE);
                    daycheck.setVisibility(View.GONE);
                } else if (cycle.getText().toString().equals("每日")) {
                    monthcheck.setVisibility(View.GONE);
                    daycheck.setVisibility(View.VISIBLE);
                }

                month.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cycle.setText("每月");
                        cycleAutoAdd.setText("开启后,下月预算金额=预算金额+本月剩余预算金额");
                        cycleAutoReduce.setText("开启后,本月预算超支,下月预算会自动扣除");
                        dialog.dismiss();
                    }
                });

                day.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cycle.setText("每日");
                        cycleAutoAdd.setText("开启后,明日预算金额=预算金额+今日剩余预算金额");
                        cycleAutoReduce.setText("开启后,今日预算超支,明日预算会自动扣除");
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
    }

    private void DoChange(LCObject budget){
        if (autoAdd.isChecked()!=autoUnchange){//结转状态改变
            if (autoAdd.isChecked()==true){//结转状态变为开启
                if (cycle.getText().toString().equals(unchangecycle)){//结转状态变为开启,周期未改变
                    if (cycle.getText().toString().equals("每月")){
                        AutoAddWorker(false);
                    }
                    else if (cycle.getText().toString().equals("每日")) {
                        AutoAddWorker(true);
                    }
                }else {//结转状态变为开启,周期改变
                    if (unchangecycle.equals("每日")) {
                        WorkManager.getInstance(Bill_Budget.this).cancelAllWorkByTag("dayautoAdd");
                        AutoAddWorker(false);
                    } else if (unchangecycle.equals("每月")) {
                        WorkManager.getInstance(Bill_Budget.this).cancelAllWorkByTag("autoAdd");
                        AutoAddWorker(true);
                    }
                }
            }else {//结转状态变为关闭
                if (cycle.getText().toString().equals("每日")) {
                    WorkManager.getInstance(Bill_Budget.this).cancelAllWorkByTag("dayautoAdd");
                } else if (cycle.getText().toString().equals("每月")) {
                    WorkManager.getInstance(Bill_Budget.this).cancelAllWorkByTag("autoAdd");
                }
                budget.put("Bvariation", budgetNum.getText().toString());
                budget.saveInBackground().subscribe(new Observer<LCObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {}
                    @Override
                    public void onNext(LCObject lcObject) {}
                    @Override
                    public void onError(Throwable e) {}
                    @Override
                    public void onComplete() {}
                });
            }
        }else {//结转状态未改变,判断周期是否改变
            if (cycle.getText().toString().equals(unchangecycle)){//周期未改变，且结转状态未改变
//                System.out.println("unchange");
            }else{//周期改变
                if (autoAdd.isChecked()){//周期改变，初始结转状态为开启
                    if (unchangecycle.equals("每日")) {
                        WorkManager.getInstance(Bill_Budget.this).cancelAllWorkByTag("dayautoAdd");
                        AutoAddWorker(false);
                    } else if (unchangecycle.equals("每月")) {
                        WorkManager.getInstance(Bill_Budget.this).cancelAllWorkByTag("autoAdd");
                        AutoAddWorker(true);
                    }
                }else {//周期改变，初始结转状态为关闭
//                    System.out.println("cycle change and origain is close");
                }
            }
        }
    }

    private void AutoAddWorker(Boolean isDay){
        // 创建约束条件：可选地设置任务的网络状态、电池状态等
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // 网络连接时才执行任务
                .build();
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        WorkRequest dailyWorkRequest;
        if (isDay){
            //设置在大约 每日 00:05:00 AM 执行
            dueDate.set(Calendar.HOUR_OF_DAY, 0);
            dueDate.set(Calendar.MINUTE, 5);
            dueDate.set(Calendar.SECOND, 0);

            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.HOUR_OF_DAY, 24);
            }
            long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
            dailyWorkRequest =
                    new OneTimeWorkRequest.Builder(MyWorkerDay.class)
                            .setConstraints(constraints)
                            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                            .addTag("dayautoAdd")
                            .build();
        }else {
            //设置在大约 每月1日 00:05:00 AM 执行
            dueDate.set(Calendar.DAY_OF_MONTH, 1);
            dueDate.set(Calendar.HOUR_OF_DAY, 0);
            dueDate.set(Calendar.MINUTE, 5);
            dueDate.set(Calendar.SECOND, 0);
            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.MONTH, 1); // 如果dueDate在当前日期之前，则增加一个月
            }
            long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
            dailyWorkRequest =
                    new OneTimeWorkRequest.Builder(MyWorker.class)
                            .setConstraints(constraints)
                            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                            .addTag("autoAdd")
                            .build();
        }

        WorkManager.getInstance(Bill_Budget.this).enqueue(dailyWorkRequest);
    }
    private void SaveBudget(){
        if (budgetName.getText().toString().isEmpty()){
            Toast.makeText(Bill_Budget.this,"请输入预算名称",Toast.LENGTH_SHORT).show();
        } else if (budgetNum.getText().toString().isEmpty()) {
            Toast.makeText(Bill_Budget.this,"请输入预算金额",Toast.LENGTH_SHORT).show();
        }else {
            LCObject budget;

            if (budgetid!=null&&!budgetid.equals("0")){
                budget = LCObject.createWithoutData("Ebudget", budgetid);
            } else {
                budget=new LCObject("Ebudget");
            }
            budget.put("Bledger", ledgerid);
            budget.put("Bbudget", budgetName.getText().toString());
            budget.put("Bcycle", cycle.getText().toString());
            budget.put("Bnum", budgetNum.getText().toString());
            if (!lastBnum.equals(budgetNum.getText().toString())){//金额变化更新变化值
                budget.put("Bvariation", budgetNum.getText().toString());
            }
            budget.put("BautoAdd", autoAdd.isChecked());
            budget.put("BautoReduce", autoReduce.isChecked());
            budget.saveInBackground().subscribe(new Observer<LCObject>() {
                @Override
                public void onSubscribe(Disposable d) {
                    pgbar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onNext(LCObject lcObject) {
                    DoChange(budget);
                    Toast.makeText(Bill_Budget.this,"预算设置成功",Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(Throwable e) {
                    Toast.makeText(Bill_Budget.this,e.getMessage()+",保存失败，请重试。",Toast.LENGTH_SHORT).show();
                    pgbar.setVisibility(View.GONE);
                }

                @Override
                public void onComplete() {
                    pgbar.setVisibility(View.GONE);
                }
            });
        }
    }
    @Override
    public void onBackPressed() {
        if (autoAdd.isChecked() == autoUnchange
                &&autoReduce.isChecked() == lastautoR
                &&budgetName.getText().toString().equals(lastBname)
                &&budgetNum.getText().toString().equals(lastBnum)
                &&cycle.getText().toString().equals(unchangecycle)){
            //全部数据与初始数据相同
            super.onBackPressed();
        }else {
            //数据出现变化
            myAlertDialog myalertDialog = new myAlertDialog(Bill_Budget.this, false);
            myalertDialog.create();
            myalertDialog.setTitle("保存提示");
            myalertDialog.setMessage("您在当前界面所做的更改尚未保存，确定退出吗？");
            //设置取消按钮
            myalertDialog.setNegative("保存更改", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myalertDialog.dismiss();
                    SaveBudget();
                }
            });
            myalertDialog.setPositive("退出界面", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            myalertDialog.show();
        }
    }
}