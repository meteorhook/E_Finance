package com.example.e_finance;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MyWorkerDay extends Worker {

    public MyWorkerDay(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //查询昨日预算金额和使用金额，计算结转金额，并设置给预算变量
        AutoAddSearch();

        //创建新的定时任务，以实现循环定时
        Calendar currentDate = Calendar.getInstance();
        Calendar dueDate = Calendar.getInstance();
        Calendar modelDate=Calendar.getInstance();
        //设置在大约 每日 00:05:00 AM 执行
        modelDate.set(Calendar.HOUR_OF_DAY, 0);
        modelDate.set(Calendar.MINUTE, 5);
        modelDate.set(Calendar.SECOND, 0);
        if (modelDate.before(currentDate)) {
            modelDate.add(Calendar.HOUR_OF_DAY, 24);
            dueDate=modelDate;//如果modelDate在当前日期之前，则增加一天
        }else {
            dueDate.add(Calendar.HOUR_OF_DAY, 24);
        }

        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
        WorkRequest dailyWorkRequest = new OneTimeWorkRequest.Builder(MyWorkerDay.class)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag("dayautoAdd")
                .build();

        WorkManager.getInstance(getApplicationContext())
                .enqueue(dailyWorkRequest);

        return Result.success();
    }

    private void AutoAddSearch() {
        LCUser user = LCUser.getCurrentUser();
        LCQuery<LCObject> query = new LCQuery<>("Eledger");
        query.whereEqualTo("Euser", user);
        query.whereEqualTo("isUsed", true);
        query.getFirstInBackground().subscribe(new Observer<LCObject>() {
            private String ledid;
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(LCObject lcObject) {
                ledid = lcObject.getObjectId();
                Calendar calendar = Calendar.getInstance();
                // 将Calendar实例的时间设置为00:00:00
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                Date lastDay = calendar.getTime();
                // 将Calendar实例的日期设置为昨天
                calendar.add(Calendar.HOUR_OF_DAY, -24);
                Date firstDay = calendar.getTime();

                final LCQuery<LCObject> startDateQuery = new LCQuery<>("Ebill");
                startDateQuery.whereGreaterThanOrEqualTo("Bdate", firstDay);

                final LCQuery<LCObject> endDateQuery = new LCQuery<>("Ebill");
                endDateQuery.whereLessThan("Bdate", lastDay);

                LCQuery<LCObject> query = LCQuery.and(Arrays.asList(startDateQuery, endDateQuery));
                query.whereEqualTo("Bledger", lcObject.getObjectId());
                query.findInBackground().subscribe(new Observer<List<LCObject>>() {
                    @Override
                    public void onSubscribe(Disposable d) {}
                    @Override
                    public void onNext(List<LCObject> lcObjects) {
                        BigDecimal variation = new BigDecimal("0");
                        for (LCObject obj : lcObjects) {
                            if (obj.getBoolean("Bbudget") && obj.getBoolean("Bstate")) {
                                BigDecimal get = new BigDecimal(obj.getString("Bnum"));
                                variation.add(get);
                            }
                        }
                        getbudget(ledid, variation);
                    }

                    @Override
                    public void onError(Throwable e) {}
                    @Override
                    public void onComplete() {}
                });
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }

    private void getbudget(String ledgerid, BigDecimal lastday) {
        LCQuery query = new LCQuery("Ebudget");
        query.whereEqualTo("Bledger", ledgerid);
        query.getFirstInBackground().subscribe(new Observer<LCObject>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(LCObject lcObject) {
                BigDecimal budget = new BigDecimal(lcObject.getString("Bnum"));
                BigDecimal budgetvariation = new BigDecimal(lcObject.getString("Bvariation"));
                BigDecimal remain;
                if (budgetvariation.compareTo(budget)!=0){
                    remain=budgetvariation.subtract(lastday);
                }else {
                    remain=budget.subtract(lastday);
                }
                BigDecimal result;

                if (!lcObject.getBoolean("BautoReduce")&&remain.compareTo(new BigDecimal("0")) == -1){
                    result = budget;
                }else {
                    result = budget.add(remain);
                }

                lcObject.put("Bvariation", result.toString());
                lcObject.saveInBackground().subscribe(new Observer<LCObject>() {
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
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });
    }


}
