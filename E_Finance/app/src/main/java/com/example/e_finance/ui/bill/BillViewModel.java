package com.example.e_finance.ui.bill;

import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.e_finance.Billitem;
import com.example.e_finance.StatisticsBudgetFragment;
import com.example.e_finance.Typeitem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class BillViewModel extends ViewModel {
    private List<Billitem> list = new ArrayList<>();
    private MutableLiveData<List<Billitem>> bill;
    private MutableLiveData<String> name;
    private MutableLiveData<String> lid;
    private Boolean isfirst=true;
    private MutableLiveData<Boolean> showPgbar;
    private MutableLiveData<Throwable> isError;
    private MutableLiveData<List<StatisticsBudgetFragment>> fragmentsList;
    private List<StatisticsBudgetFragment> statisticsList=new ArrayList<>();

    //构造函数
    public BillViewModel() {
        bill = new MutableLiveData<>();
        name = new MutableLiveData<>();
        lid = new MutableLiveData<>();
        showPgbar=new MutableLiveData<>();
        isError=new MutableLiveData<>();
        fragmentsList=new MutableLiveData<>();

        statisticsList.add(new StatisticsBudgetFragment(true));
        statisticsList.add(new StatisticsBudgetFragment(false));
        fragmentsList.setValue(statisticsList);
        refreshData();
    }

    public MutableLiveData<List<StatisticsBudgetFragment>> getFragmentsList() {
        return fragmentsList;
    }

    public MutableLiveData<Throwable> getIsError() {
        return isError;
    }

    public Boolean getIsfirst() {
        return isfirst;
    }

    public void setIsfirst(Boolean isfirst) {
        this.isfirst = isfirst;
    }

    public LiveData<List<Billitem>> getBill() {
        return bill;
    }

    public LiveData<String> getlid() {
        return lid;
    }

    public LiveData<String> getName() {
        return name;
    }

    public MutableLiveData<Boolean> getShowPgbar() {
        return showPgbar;
    }

    private void selectbill(String ledgerid) {
        //查询全部账单
        LCQuery<LCObject> query = new LCQuery<>("Ebill");
        query.whereEqualTo("Bledger", ledgerid);
        query.limit(1000);
        query.findInBackground().subscribe(new io.reactivex.Observer<List<LCObject>>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(List<LCObject> lcObjects) {
                // 创建一个Calendar实例
                Calendar calendar = Calendar.getInstance();
                // 将Calendar实例的时间设置为23:59:59
                calendar.set(Calendar.HOUR_OF_DAY,23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                Date curDate=calendar.getTime();
                // 将Calendar实例的时间设置为00:00:00
                calendar.set(Calendar.HOUR_OF_DAY,0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                Date today=calendar.getTime();
                // 将Calendar实例的日期设置为这个月的第一天
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                // 从Calendar实例中获取日期并转换为Date类型
                Date firstDayOfMonth = calendar.getTime();
                List<LCObject> nowBill=new ArrayList<>();
                List<LCObject> todayBill=new ArrayList<>();
                todayBill.clear();
                nowBill.clear();

                for (LCObject lc:lcObjects){
                    Date getdate=lc.getDate("Bdate");
                    if (getdate.before(curDate)){
                        if (getdate.after(firstDayOfMonth)){
                            nowBill.add(lc);
                        }
                        if (getdate.after(today)){
                            todayBill.add(lc);
                        }
                    }
                }

                //以下计算日结余
                BigDecimal resultIncome0=new BigDecimal("0");
                BigDecimal resultPay0=new BigDecimal("0");
                BigDecimal result0=new BigDecimal("0");

                for (int i = 0; i < todayBill.size(); i++) {
                    LCObject obj = todayBill.get(i);
                    BigDecimal b=new BigDecimal(obj.getString("Bnum"));
                    if (obj.getBoolean("Bbudget")){
                        if (obj.getBoolean("Bstate")) {
                            resultPay0=resultPay0.add(b);
                            result0 = result0.subtract(b);
                        } else {
                            resultIncome0=resultIncome0.add(b);
                            result0 = result0.add(b);
                        }
                    }
                }

                //以下计算月结余
                Double s,in,P;
                BigDecimal resultIncome=new BigDecimal("0");
                BigDecimal resultPay=new BigDecimal("0");
                BigDecimal result=new BigDecimal("0");
                BigDecimal resultBudget=new BigDecimal("0");

                for (int i = 0; i < nowBill.size(); i++) {
                    LCObject obj = nowBill.get(i);
                    BigDecimal b=new BigDecimal(obj.getString("Bnum"));
                    if (obj.getBoolean("Bbudget")){
                        if (obj.getBoolean("Bstate")) {
                            resultBudget = resultBudget.add(b);
                        }
                    }

                    if (obj.getBoolean("Bstate")) {
                        resultPay=resultPay.add(b);
                        result = result.subtract(b);
                    } else {
                        resultIncome=resultIncome.add(b);
                        result = result.add(b);
                    }
                }

                P=resultPay.doubleValue();
                in=resultIncome.doubleValue();
                s=result.doubleValue();

                Number[] r = new Number[3];
                r[0] = in;
                r[1] = P;
                r[2] = s;
                statisticsList.get(0).setNumbers(r);
                statisticsList.get(1).setLedgerid(ledgerid);

                getBudget(ledgerid,resultBudget,resultPay0,firstDayOfMonth,P);

                //以下处理全部账单
                String[] nums = new String[lcObjects.size()];
                String[] notes = new String[lcObjects.size()];
                String[] dates = new String[lcObjects.size()];
                String[] id = new String[lcObjects.size()];
                String[] typeid = new String[lcObjects.size()];
                list.clear();
                Date d;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                formatter.setTimeZone(TimeZone.getTimeZone("GMT+08"));

                List<billinfo> billinfos=new ArrayList<>(lcObjects.size());
                for (int i = 0; i < lcObjects.size(); i++) {
                    LCObject obj = lcObjects.get(i);
                    notes[i] = obj.getString("Bnotes");
                    id[i] = obj.getObjectId();
                    if (obj.getBoolean("Bstate")){
                        nums[i]="-"+ obj.getString("Bnum");
                    } else {
                        nums[i] ="+"+ obj.getString("Bnum");
                    }
                    //获取日期,格式为{"yyyy-MM-dd"}
                    d = obj.getDate("Bdate");
                    String Date = formatter.format(d);
                    dates[i] = Date;
                    typeid[i] = obj.getString("Btype");
                    billinfo binfo=new billinfo(id[i],typeid[i],notes[i],dates[i],nums[i]);
                    billinfos.add(binfo);
                }
                LCQuery<LCObject> typeinfo = new LCQuery<>("Etype");
                typeinfo.whereContainedIn("objectId", Arrays.asList(typeid));
                typeinfo.findInBackground().subscribe(new Observer<List<LCObject>>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(List<LCObject> back) {
                        showPgbar.setValue(false);
                        String[] typeName = new String[back.size()];
                        String[] typeColor = new String[back.size()];
                        String[] typeImgSelect = new String[back.size()];
                        Boolean[] typestate = new Boolean[back.size()];
                        String[] typeids = new String[back.size()];
                        List<Typeitem> typeitems=new ArrayList<>(back.size());
                        for (int i = 0; i < back.size(); i++) {
                            LCObject type = back.get(i);
                            typeName[i] = type.getString("TypeName");
                            typeColor[i] = type.getString("TypeColor");
                            typeImgSelect[i] = type.getString("TypeImgSelect");
                            typestate[i] = type.getBoolean("Tstate");
                            typeids[i]=type.getObjectId();
                            Typeitem tinfo=new Typeitem(typeName[i], typeColor[i],null,typeImgSelect[i],
                                    typeids[i],typestate[i]);
                            typeitems.add(tinfo);
                        }

                        //遍历第一个list通过对应的typeid设置对应属性
                        for (billinfo billinfo : billinfos) {
                            Typeitem matchingTypeitem = null;
                            for (Typeitem typeitem : typeitems) {
                                if (billinfo.getTypeid().equals(typeitem.getTypeid())) {
                                    matchingTypeitem = typeitem;
                                    break;
                                }
                            }
                            if (matchingTypeitem != null) {
                                Billitem newBillitem = new Billitem(
                                        matchingTypeitem.getType(),
                                        matchingTypeitem.getimageIdselect(),
                                        billinfo.getNumber(),
                                        matchingTypeitem.getColor(),
                                        billinfo.getNotes(),
                                        billinfo.getDates(),
                                        billinfo.getId(),
                                        matchingTypeitem.getTypeid()
                                );
                                list.add(newBillitem);
                            }
                        }
                        bill.setValue(list);
                    }

                    @Override
                    public void onError(Throwable e) {
                        isError.setValue(e);
                    }
                    @Override
                    public void onComplete() {
                        showPgbar.setValue(false);
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                isError.setValue(e);
            }
            @Override
            public void onComplete() {}
        });
    }

    private void getBudget(String ledgerid,BigDecimal resultBudget
            ,BigDecimal resultPay0,Date firstDayOfMonth,Double P) {
        BigDecimal finalResult = resultBudget;//月预算消费金额
        BigDecimal finalResulttoday = resultPay0;//日预算消费金额

        LCQuery query=new LCQuery("Ebudget");
        query.whereEqualTo("Bledger", ledgerid);
        query.getFirstInBackground().subscribe(new Observer<LCObject>() {
            private Boolean isIn = false;
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(LCObject lcObject) {
                isIn = true;

                Calendar calendar0=Calendar.getInstance();
                int day = calendar0.getActualMaximum(Calendar.DAY_OF_MONTH);
                int remainday=calendar0.get(Calendar.DAY_OF_MONTH);

                SimpleDateFormat formatter = new SimpleDateFormat("MM");
                String month = formatter.format(firstDayOfMonth);
                String datestr = month + "/01-" + month + "/" + day;

                BigDecimal b = new BigDecimal(lcObject.getString("Bvariation"));
                if (b.compareTo(new BigDecimal("0"))!=1){//预算金额被结转成负值或0
                    statisticsList.get(0).setMprogress(1f, "预算已超支！");
                    statisticsList.get(1).setbudget(lcObject.getObjectId()
                            , lcObject.getString("Bbudget")
                            , "0"
                            , "0"
                            , P
                            , datestr);
                    statisticsList.get(1).setProgress(100);
                } else if (b.compareTo(finalResult)==-1) {//预算金额小于已消费金额
                    statisticsList.get(0).setMprogress(1f, "预算已超支！");
                    statisticsList.get(1).setbudget(lcObject.getObjectId()
                            , lcObject.getString("Bbudget")
                            , "0"
                            , "0"
                            , P
                            , datestr);
                    statisticsList.get(1).setProgress(100);
                } else {
                    if (lcObject.getString("Bcycle").equals("每月")){
                        //当前月份天数
                        BigDecimal dayInmonth = new BigDecimal(day);
                        //当前天数
                        BigDecimal dayOfmonth = new BigDecimal(remainday);
                        //每日可消费预算额
                        BigDecimal dayAverage = b.divide(dayInmonth, 2, RoundingMode.HALF_DOWN);
                        //理论可消费预算额
                        BigDecimal alldaybudget = dayAverage.multiply(dayOfmonth);
                        //本月可用预算额
                        BigDecimal remainder = b.subtract(finalResult);
                        //今日可用预算额
                        BigDecimal dayRemain = alldaybudget.subtract(finalResult);
                        if (dayRemain.compareTo(new BigDecimal("0"))!=1){
                            //若可用额小于等于0则值为0
                            dayRemain=new BigDecimal(0);
                        }
                        //预算额百分比
                        BigDecimal percent =new BigDecimal(1).subtract(dayRemain.divide(alldaybudget,2, RoundingMode.HALF_DOWN));
                        statisticsList.get(0).setMprogress(finalResult.divide(b, 2, RoundingMode.HALF_DOWN).floatValue()
                                , "预算剩余:" + remainder);
                        statisticsList.get(1).setbudget(lcObject.getObjectId()
                                , lcObject.getString("Bbudget")
                                , remainder.toString()
                                , dayRemain.toString()
                                , P
                                , datestr);
                        statisticsList.get(1).setProgress(percent.multiply(new BigDecimal(100)).intValue());

                    } else if (lcObject.getString("Bcycle").equals("每日")) {
                        BigDecimal remainder = b.subtract(finalResulttoday);
                        BigDecimal percent =finalResulttoday.divide(b,2, RoundingMode.HALF_DOWN);
                        statisticsList.get(0).setMprogress(finalResulttoday.divide(b, 2, RoundingMode.HALF_DOWN).floatValue()
                                , "今日预算剩余:" + remainder);
                        statisticsList.get(1).setbudget(lcObject.getObjectId()
                                , lcObject.getString("Bbudget")
                                , remainder.toString()
                                , remainder.toString()
                                , P
                                , datestr);
                        statisticsList.get(1).setProgress(percent.multiply(new BigDecimal(100)).intValue());
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                isError.setValue(e);
            }

            @Override
            public void onComplete() {
                if (!isIn) {
                    statisticsList.get(0).setMprogress(1f, "左滑卡片设置预算");
                    statisticsList.get(1).Reset();
                }
            }
        });
    }

    public void refreshData() {
        //设置时间格式
        SimpleDateFormat formatter = new SimpleDateFormat("M月");
        //设置时区
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+08"));
        //获取当前时间
        Date curDate = new Date(System.currentTimeMillis());
        //格式转换
        String createDate = formatter.format(curDate);
        statisticsList.get(0).setMonthinfo(createDate);

        LCUser user = LCUser.getCurrentUser();
        LCQuery<LCObject> query = new LCQuery<>("Eledger");
        query.whereEqualTo("Euser", user);
        query.whereEqualTo("isUsed", true);
        query.getFirstInBackground().subscribe(new Observer<LCObject>() {
            @Override
            public void onSubscribe(Disposable d) {
                showPgbar.setValue(true);
            }

            @Override
            public void onNext(LCObject lcObject) {
                String lname = lcObject.getString("Lname");
                String id = lcObject.getObjectId();
                name.setValue(lname);
                lid.setValue(id);
                selectbill(id);
            }

            @Override
            public void onError(Throwable e) {
                isError.setValue(e);
            }

            @Override
            public void onComplete() {
            }
        });
    }

    public static class billinfo{
        private String notes,dates,id,typeid,number;

        public billinfo(String id,String typeid,String notes,String dates,String number){
            this.id=id;
            this.typeid=typeid;
            this.notes=notes;
            this.dates=dates;
            this.number=number;
        }

        public String getId() {
            return id;
        }

        public String getNumber() {
            return number;
        }

        public String getDates() {
            return dates;
        }

        public String getTypeid() {
            return typeid;
        }
        public String getNotes() {
            return notes;
        }
    }


}