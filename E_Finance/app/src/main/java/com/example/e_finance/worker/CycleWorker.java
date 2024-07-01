package com.example.e_finance.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class CycleWorker extends Worker {
    public CycleWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Calendar calendar=Calendar.getInstance();
        Calendar curDate=Calendar.getInstance();
        Calendar modelDate=Calendar.getInstance();
        modelDate.set(Calendar.MINUTE,0);
        modelDate.set(Calendar.SECOND, 0);
        String Billcycle=getInputData().getString("cycle");

        try {
            int number = Integer.parseInt(Billcycle.substring(2,Billcycle.length()-1));
            if (Billcycle.startsWith("每月")){
                if (number>modelDate.getActualMaximum(Calendar.DAY_OF_MONTH)){
                    modelDate.set(Calendar.DAY_OF_MONTH,modelDate.getActualMaximum(Calendar.DAY_OF_MONTH));
                }else {
                    modelDate.set(Calendar.DAY_OF_MONTH,number);
                }
                modelDate.set(Calendar.HOUR_OF_DAY, 0);
                addBill(modelDate.getTime());
                if (modelDate.before(curDate)){
                    modelDate.add(Calendar.MONTH,1);
                    calendar=modelDate;//如果modelDate在当前日期之前，则增加一天
                }else {
                    calendar.add(Calendar.MONTH,1);
                }
            } else if (Billcycle.startsWith("每日")) {
                // 如果到这里没有异常，那么number是一个有效的整数
                modelDate.set(Calendar.HOUR_OF_DAY,number);
                addBill(modelDate.getTime());
                if (modelDate.before(curDate)){
                    modelDate.add(Calendar.DAY_OF_MONTH,1);
                    calendar=modelDate;// 如果modelDate在当前日期之前，则增加一天
                }else {
                    calendar.add(Calendar.DAY_OF_MONTH,1);
                }
            }
        }catch (NumberFormatException e) {
            // number不是一个有效的整数，处理异常
            e.printStackTrace();
            System.out.println("workError："+e.getMessage());
        }

        // 创建约束条件：可选地设置任务的网络状态、电池状态等
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // 网络连接时才执行任务
                .build();
        // 创建输入数据
        Data data = new Data.Builder()
                .putString("cycle",getInputData().getString("cycle"))
                .putString("cycleid",getInputData().getString("cycleid"))
                .build();
        long timeDiff = calendar.getTimeInMillis() - curDate.getTimeInMillis();
        WorkRequest dailyWorkRequest =
                new OneTimeWorkRequest.Builder(CycleWorker.class)
                        .setConstraints(constraints)
                        .setInputData(data)
                        .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                        .addTag(getInputData().getString("cycleid"))
                        .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(dailyWorkRequest);
        return Result.success();
    }

    private void addBill(Date date){
        String id=getInputData().getString("cycleid");
        LCQuery<LCObject> query = new LCQuery<>("Ecycle");
        query.getInBackground(id).subscribe(new Observer<LCObject>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(LCObject Ecycle) {
                LCObject bill = new LCObject("Ebill");
                bill.put("Bdate",date);
                bill.put("Bnum", Ecycle.getString("Cnum"));
                bill.put("Bstate", Ecycle.getBoolean("Cbstate"));
                bill.put("Btype", Ecycle.getString("Ctype"));
                bill.put("Bledger", Ecycle.getString("Cledger"));
                bill.put("Bnotes", Ecycle.getString("Cnotes"));
                bill.saveInBackground().subscribe(new Observer<LCObject>() {
                    public void onSubscribe(Disposable disposable) {}
                    public void onNext(LCObject todo) {
                        // 成功保存之后，执行其他逻辑
                    }
                    public void onError(Throwable throwable) {
                        // 异常处理
                    }
                    public void onComplete() {}
                });
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });
    }
}
