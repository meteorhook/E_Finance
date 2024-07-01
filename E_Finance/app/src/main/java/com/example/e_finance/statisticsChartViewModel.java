package com.example.e_finance;

import android.graphics.Color;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.e_finance.fragment.statisticsChartFragment;
import com.example.e_finance.ui.statistics.StatisticsFragment;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class statisticsChartViewModel extends ViewModel {
    private MutableLiveData<ArrayList<Entry>> lineData;
    private MutableLiveData<Map<PieEntry,Integer>> pieData;
    private MutableLiveData<List<statisticsChartFragment.TypeStatisticsItem>> typeStatistics;
    private String type,fragmentDateType,ledgerid;
    private MutableLiveData<Number[]> balance;
    private StatisticsFragment.sandeDate sandeDate;
    private MutableLiveData<List<statisticsChartFragment.BillStatistics>> billStatistics;
    private MutableLiveData<Boolean> showPgbar;
    private MutableLiveData<Throwable> isError;

    public statisticsChartViewModel() {
        lineData = new MutableLiveData<>();
        balance=new MutableLiveData<>();
        pieData=new MutableLiveData<>();
        typeStatistics=new MutableLiveData<>();
        billStatistics=new MutableLiveData<>();
        showPgbar=new MutableLiveData<>();
        isError=new MutableLiveData<>();
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLedgerid(String ledgerid) {
        this.ledgerid = ledgerid;
    }

    public void setSandeDate(StatisticsFragment.sandeDate sandeDate) {
        this.sandeDate = sandeDate;
    }

    public void setFragmentDateType(String fragmentDateType) {
        this.fragmentDateType = fragmentDateType;
    }

    public MutableLiveData<Throwable> getIsError() {
        return isError;
    }

    public MutableLiveData<Boolean> getShowPgbar() {
        return showPgbar;
    }

    public MutableLiveData<Map<PieEntry,Integer>> getPieData() {
        return pieData;
    }

    public MutableLiveData<List<statisticsChartFragment.TypeStatisticsItem>> getTypeStatistics() {
        return typeStatistics;
    }

    public MutableLiveData<ArrayList<Entry>> getLineData() {
        return lineData;
    }

    public MutableLiveData<Number[]> getBalance() {
        return balance;
    }

    public MutableLiveData<List<statisticsChartFragment.BillStatistics>> getBillStatistics() {
        return billStatistics;
    }

    public void refreshData(){

        final LCQuery<LCObject> startDateQuery = new LCQuery<>("Ebill");
        startDateQuery.whereGreaterThanOrEqualTo("Bdate", sandeDate.getStartDate());

        final LCQuery<LCObject> endDateQuery = new LCQuery<>("Ebill");
        endDateQuery.whereLessThan("Bdate", sandeDate.getEndDate());

        LCQuery<LCObject> query = LCQuery.and(Arrays.asList(startDateQuery, endDateQuery));
        query.whereEqualTo("Bledger", ledgerid);
        query.limit(1000);
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            @Override
            public void onSubscribe(Disposable d) {
                showPgbar.setValue(true);
            }

            @Override
            public void onNext(List<LCObject> lcObjects) {
                //计算收支及余额
                Double s,in,P;
                BigDecimal resultIncome=new BigDecimal("0");
                BigDecimal resultPay=new BigDecimal("0");
                BigDecimal result=new BigDecimal("0");

                for (int i = 0; i < lcObjects.size(); i++) {
                    LCObject obj = lcObjects.get(i);
                    BigDecimal b=new BigDecimal(obj.getString("Bnum"));
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
                balance.setValue(r);


                //获取linechart数据
                ArrayList<dateAndNum> billInfo = new ArrayList<>();
                for (int i = 0; i < lcObjects.size(); i++) {
                    LCObject object = lcObjects.get(i);
                    dateAndNum dateAndNum = new dateAndNum(object.getDate("Bdate"),
                            object.getString("Bnum"), object.getBoolean("Bstate"));
                    billInfo.add(dateAndNum);
                }
                // 用于存储计算结果的HashMap，key为日期，value为计算结果
                Map<String, Double> resultMap = new HashMap<>();
                Map<String,Double> pay=new HashMap<>();
                Map<String,Double> income=new HashMap<>();
                Map<String,Double> balance=new HashMap<>();

                SimpleDateFormat sdf;
                if (fragmentDateType.equals("year")){
                    sdf = new SimpleDateFormat("MM");
                }else {
                    sdf = new SimpleDateFormat("dd");
                }
                for (dateAndNum dan : billInfo) {
                    // 获取日期并格式化，日期格式为sdf
                    String formattedDate = sdf.format(dan.date);
                    // 如果该日期还没有结果，则初始化它为0
                    if (!resultMap.containsKey(formattedDate)) {
                        resultMap.put(formattedDate, new BigDecimal("0").doubleValue());
                        pay.put(formattedDate,new BigDecimal("0").doubleValue());
                        income.put(formattedDate,new BigDecimal("0").doubleValue());
                       balance.put(formattedDate,new BigDecimal("0").doubleValue());
                    }
                    BigDecimal result1 = BigDecimal.valueOf(resultMap.get(formattedDate));
                    BigDecimal resultPay1 = BigDecimal.valueOf(resultMap.get(formattedDate));
                    BigDecimal resultIncome1 = BigDecimal.valueOf(resultMap.get(formattedDate));

                    BigDecimal result2 = BigDecimal.valueOf(balance.get(formattedDate));
                    BigDecimal resultPay2 = BigDecimal.valueOf(pay.get(formattedDate));
                    BigDecimal resultIncome2 = BigDecimal.valueOf(income.get(formattedDate));


                    if (dan.bstate) { // true为减，false为加
                        result1 = result1.subtract(new BigDecimal(dan.num));
                        resultPay1 = resultPay1.add(new BigDecimal(dan.num));

                        result2 = result2.subtract(new BigDecimal(dan.num));
                        resultPay2 = resultPay2.add(new BigDecimal(dan.num));
                    } else {
                        resultIncome1 = resultIncome1.add(new BigDecimal(dan.num));
                        result1 = result1.add(new BigDecimal(dan.num));

                        resultIncome2 = resultIncome2.add(new BigDecimal(dan.num));
                        result2 = result2.add(new BigDecimal(dan.num));
                    }

                    pay.put(formattedDate, resultPay2.doubleValue());
                    income.put(formattedDate, resultIncome2.doubleValue());
                    balance.put(formattedDate, result2.doubleValue());

                    if (type.equals("pay")) {
                        resultMap.put(formattedDate, resultPay1.doubleValue());
                    } else if (type.equals("income")) {
                        resultMap.put(formattedDate, resultIncome1.doubleValue());
                    } else if (type.equals("balance")) {
                        resultMap.put(formattedDate, result1.doubleValue());
                    }
                }


                ArrayList<statisticsChartFragment.BillStatistics> statisticslist=new ArrayList<>();

                for (Map.Entry<String, Double> entry : balance.entrySet()) {
                    String date=entry.getKey();
                    statisticsChartFragment.BillStatistics billStatistics
                            = new statisticsChartFragment.BillStatistics(
                                date,
                                income.get(date).toString(),
                                pay.get(date).toString(),
                                entry.getValue().toString());
                    statisticslist.add(billStatistics);
                }
                billStatistics.setValue(statisticslist);


                ArrayList<dateAndNum> resultList = new ArrayList<>();
                for (Map.Entry<String, Double> entry : resultMap.entrySet()) {
                    dateAndNum dateAndNum = new dateAndNum(Integer.parseInt(entry.getKey()), entry.getValue().floatValue());
                    resultList.add(dateAndNum);
                }

                if (fragmentDateType.equals("year")){
                    lineData.setValue(dateCompletion(resultList, 12));
                }else {
                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    lineData.setValue(dateCompletion(resultList, day));
                }

                //获取piechart数据
                String[] typeid=new String[lcObjects.size()];
                Map<String, Double> resultMap1 = new HashMap<>();
                Map<Boolean,Double> allMap =new HashMap<>();

                for (int i=0;i<lcObjects.size();i++){
                    LCObject lcObject=lcObjects.get(i);
                    typeid[i] = lcObject.getString("Btype");

                    String type = lcObject.getString("Btype");
                    if (!resultMap1.containsKey(type)) {
                        resultMap1.put(type, new BigDecimal("0").doubleValue());
                    }
                    BigDecimal result2 = BigDecimal.valueOf(resultMap1.get(type));
                    result2 = result2.add(new BigDecimal(lcObject.getString("Bnum")));
                    resultMap1.put(type, result2.doubleValue());

                    Boolean state=lcObject.getBoolean("Bstate");
                    if (!allMap.containsKey(state)) {
                        allMap.put(state, new BigDecimal("0").doubleValue());
                    }
                    BigDecimal resultall = BigDecimal.valueOf(allMap.get(state));
                    resultall = resultall.add(new BigDecimal(lcObject.getString("Bnum")));
                    allMap.put(state,resultall.doubleValue());
                }

                Map<String , Integer> frequency=new HashMap<>();
                for (String id:typeid){
                    if (!frequency.containsKey(id)) {
                        frequency.put(id, 0);
                    }
                    int i=frequency.get(id);
                    i=i+1;
                    frequency.put(id, i);
                }

                LCQuery<LCObject> typeinfo = new LCQuery<>("Etype");
                typeinfo.whereContainedIn("objectId", Arrays.asList(typeid));
                typeinfo.findInBackground().subscribe(new Observer<List<LCObject>>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(List<LCObject> lcObjects) {
                        showPgbar.setValue(false);
                        String[] typename=new String[lcObjects.size()];
                        String[] color=new String[lcObjects.size()];
                        Double[] num=new Double[lcObjects.size()];
                        String[] ids=new String[lcObjects.size()];
                        ArrayList<Integer> colors=new ArrayList<>(lcObjects.size());
                        ArrayList<PieEntry> entries=new ArrayList<>();
                        Map<PieEntry,Integer> map=new HashMap<>();
                        List<statisticsChartFragment.TypeStatisticsItem> list=new ArrayList<>();
                        for (int i=0;i<lcObjects.size();i++){
                            LCObject lcObject=lcObjects.get(i);
                            if (type.equals("pay")&&lcObject.getBoolean("Tstate")) {
                                typename[i]=lcObject.getString("TypeName");
                                color[i]=lcObject.getString("TypeColor");
                                colors.add(Color.parseColor(color[i]));
                                num[i]=resultMap1.get(lcObject.getObjectId());
                                ids[i]=lcObject.getObjectId();

                                entries.add(new PieEntry(num[i].floatValue(),typename[i]));
                                map.put(new PieEntry(num[i].floatValue(),typename[i]),Color.parseColor(color[i]));

                                Double s=resultMap1.get(lcObject.getObjectId())/allMap.get(lcObject.getBoolean("Tstate"));
                                statisticsChartFragment.TypeStatisticsItem typeStatisticsItem=
                                        new statisticsChartFragment.TypeStatisticsItem(typename[i],lcObject.getString("TypeImgSelect"),
                                                frequency.get(lcObject.getObjectId()),color[i],s.floatValue(),ids[i]);
                                list.add(typeStatisticsItem);

                            } else if (type.equals("income")&&!lcObject.getBoolean("Tstate")) {
                                typename[i]=lcObject.getString("TypeName");
                                color[i]=lcObject.getString("TypeColor");
                                colors.add(Color.parseColor(color[i]));
                                num[i]=resultMap1.get(lcObject.getObjectId());
                                ids[i]=lcObject.getObjectId();

                                entries.add(new PieEntry(num[i].floatValue(),typename[i]));
                                map.put(new PieEntry(num[i].floatValue(),typename[i]),Color.parseColor(color[i]));

                                Double s=resultMap1.get(lcObject.getObjectId())/allMap.get(lcObject.getBoolean("Tstate"));
                                statisticsChartFragment.TypeStatisticsItem typeStatisticsItem=
                                        new statisticsChartFragment.TypeStatisticsItem(typename[i],lcObject.getString("TypeImgSelect"),
                                                frequency.get(lcObject.getObjectId()),color[i],s.floatValue(),ids[i]);
                                list.add(typeStatisticsItem);
                            }
                        }
                        typeStatistics.setValue(list);
                        pieData.setValue(map);
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
            public void onComplete() {

            }
        });
    }

    private ArrayList<Entry> dateCompletion(ArrayList<dateAndNum> data, int day){
        ArrayList<Entry> completeData = new ArrayList<>();
        for (int i = 1; i <= day; i++) {
            boolean found = false;
            for (dateAndNum item : data) {
                if (item.date2 == i) {
                    completeData.add(new Entry(i, item.num2));
                    found = true;
                    break;
                }
            }
            if (!found) {
                completeData.add(new Entry(i, 0));
            }
        }
        return completeData;
    }
    private class dateAndNum{
        private Date date;
        private String num;
        private float num2;
        private int date2;
        private Boolean bstate;
        public dateAndNum(Date date,String num,Boolean bstate){
            this.date=date;
            this.num=num;
            this.bstate=bstate;
        }
        public dateAndNum(int date2,float num2){
            this.date2=date2;
            this.num2=num2;
        }
    }

}



