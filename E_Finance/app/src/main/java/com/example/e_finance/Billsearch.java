package com.example.e_finance;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_finance.adapter.Billsearch_ledgerAdapter;
import com.example.e_finance.adapter.DateBillAdapter;
import com.example.e_finance.customview.billDecoration;
import com.example.e_finance.entity.Billitem;
import com.example.e_finance.entity.Item;
import com.example.e_finance.entity.Typeitem;
import com.example.e_finance.ui.bill.BillViewModel;
import com.example.e_finance.util.StatusBar;
import com.loper7.date_time_picker.DateTimeConfig;
import com.loper7.date_time_picker.DateTimePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class Billsearch extends AppCompatActivity {
    private SearchView tv_search;
    private List<Billitem> list = new ArrayList<>();
    private List<List<Billitem>> billitemsByDateList=new ArrayList<>();
    private View maskLayer,topbg;
    private PopupWindow mLedgerPopupwindow,mDatePopupwindow;
    private TextView cancel,billledger,billdate,notfindtext;
    private ImageView ledgerdown,datedown,notfind;
    private RecyclerView recyclerView;
    private DateBillAdapter dateBillAdapter;
    private String ledgername,typeid;
    private List<String> ledgerid=new ArrayList<>();
    private int nowwidth,selectview=-1;
    private Billsearch_ledgerAdapter billsearchLedgerAdapter;
    private Float multiple;
    private Boolean finishing=false,isfirst=true;
    private Date dateStart,dateEnd;
    private ImageView pgbar;
    private AnimationDrawable ad;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(Billsearch.this);
        statusBar.setColor(R.color.transparent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billsearch);

        tv_search=findViewById(R.id.search);
        cancel=findViewById(R.id.cancelSearch);
        billledger=findViewById(R.id.billledger);
        billdate=findViewById(R.id.billdate);
        ledgerdown=findViewById(R.id.ledgerdown);
        datedown=findViewById(R.id.datedown);
        notfind=findViewById(R.id.notfind);
        notfindtext=findViewById(R.id.notfindtext);
        recyclerView=findViewById(R.id.RecyclerView);
        maskLayer = findViewById(R.id.mask_layer);
        topbg=findViewById(R.id.topbg);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        //初始隐藏以下元素
        billledger.setVisibility(View.GONE);
        billdate.setVisibility(View.GONE);
        ledgerdown.setVisibility(View.GONE);
        datedown.setVisibility(View.GONE);
        notfind.setVisibility(View.GONE);
        notfindtext.setVisibility(View.GONE);

        //监听布局是否发生变化
        tv_search.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                nowwidth=tv_search.getWidth();
                tv_search.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                inAnimation();
            }
        });

        initView();

    }
    //返回时执行页面退出动画
    @Override
    public void onBackPressed() {
        if(!finishing){
            finishing=true;
            if (mLedgerPopupwindow != null && mLedgerPopupwindow.isShowing()) {
                dismissPopWindow(mLedgerPopupwindow);
            }
            if (mDatePopupwindow != null && mDatePopupwindow.isShowing()) {
                dismissPopWindow(mDatePopupwindow);
            }
            outAnimation();
        }else {
            finish();
        }
    }

    //重启页面时刷新页面数据
    @Override
    protected void onResume() {
        super.onResume();
        searchfunction();
    }

    //初始化
    private void initView() {
        billledger.setText(getIntent().getStringExtra("ledgername"));
        tv_search.setIconified(false);
        ledgerid.add(getIntent().getStringExtra("ledgerid"));

        //设置点击关闭按钮时不关闭searchview
        tv_search.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return true;
            }
        });

        tv_search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                billledger.setVisibility(VISIBLE);
                billdate.setVisibility(VISIBLE);
                ledgerdown.setVisibility(VISIBLE);
                datedown.setVisibility(VISIBLE);
                tv_search.clearFocus();

                searchfunction();
                finishing=true;
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (billledger.getVisibility()== View.VISIBLE){
                    finishing=true;
                    onBackPressed();
                }else {
                    onBackPressed();
                }
            }
        });

        //点击弹出popwindow
        billledger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDatePopupwindow != null && mDatePopupwindow.isShowing()) {
                    dismissPopWindow(mDatePopupwindow);
                    if (mLedgerPopupwindow == null) {
                        mLedgerPopupwindow = initmPopupWindowView(0, R.layout.popwindow_layout);
                    }
                    showPopWindow(0, view);
                } else {
                    if (mLedgerPopupwindow != null && mLedgerPopupwindow.isShowing()){
                       dismissPopWindow(mLedgerPopupwindow);
                    }else {
                        if (mLedgerPopupwindow == null) {
                            mLedgerPopupwindow = initmPopupWindowView(0, R.layout.popwindow_layout);
                        }
                        showPopWindow(0, view);
                    }
                }
            }
        });

        billdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLedgerPopupwindow != null && mLedgerPopupwindow.isShowing()) {
                    dismissPopWindow(mLedgerPopupwindow);
                    if (mDatePopupwindow == null) {
                        mDatePopupwindow = initmPopupWindowView(1, R.layout.billsearch_date_popwindow);
                    }
                    showPopWindow(1, view);
                } else {
                    if (mDatePopupwindow != null && mDatePopupwindow.isShowing()){
                        dismissPopWindow(mDatePopupwindow);
                    }else {
                        if (mDatePopupwindow == null) {
                            mDatePopupwindow = initmPopupWindowView(1, R.layout.billsearch_date_popwindow);
                        }
                        showPopWindow(1, view);
                    }
                }
            }
        });

        //点击背景收起PopupWindow
        maskLayer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        if (mLedgerPopupwindow != null && mLedgerPopupwindow.isShowing()) {
                            dismissPopWindow(mLedgerPopupwindow);
                        }
                        if (mDatePopupwindow != null && mDatePopupwindow.isShowing()) {
                            dismissPopWindow(mDatePopupwindow);
                        }
                        break;
                }
                return false;
            }
        });
    }

    //查询前先确定是否存在名称含有输入数据的分类
    private void searchTypeid(List<String> ledger,String message,Date start,Date end){
        LCQuery<LCObject> query = new LCQuery<>("Etype");
        query.whereContains("TypeName",message);
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            @Override
            public void onSubscribe(Disposable d) {
                pgbar.setVisibility(VISIBLE);
            }

            @Override
            public void onNext(List<LCObject> lcObjects) {
                typeid = "";
                if (lcObjects.size()!=0) {
                    typeid = lcObjects.get(0).getObjectId();
                }
                searchBill(ledger,message,start,end);
            }

            @Override
            public void onError(Throwable e) {Toast.makeText(Billsearch.this,"加载失败"+e.getMessage(),Toast.LENGTH_SHORT).show();}
            @Override
            public void onComplete() {pgbar.setVisibility(GONE);}
        });
    }

    //根据选择的账本、日期，及输入数据（分类、备注、金额）查询相应数据
    private void searchBill(List<String> ledger,String message,Date start,Date end){
        final LCQuery<LCObject> startDateQuery = new LCQuery<>("Ebill");
        startDateQuery.whereGreaterThanOrEqualTo("Bdate", start);

        final LCQuery<LCObject> endDateQuery = new LCQuery<>("Ebill");
        endDateQuery.whereLessThan("Bdate", end);

        final LCQuery<LCObject> billNumberQuery = new LCQuery<>("Ebill");
        billNumberQuery.whereEqualTo("Bnum",message);

        final LCQuery<LCObject> billNoteQuery = new LCQuery<>("Ebill");
        billNoteQuery.whereContains("Bnotes",message);

        final LCQuery<LCObject> billTypeQuery = new LCQuery<>("Ebill");
        billTypeQuery.whereEqualTo("Btype",typeid);

        LCQuery<LCObject> query = LCQuery.and(Arrays.asList(startDateQuery, endDateQuery));

        LCQuery<LCObject> query2 = LCQuery.or(Arrays.asList(billNumberQuery,billNoteQuery,billTypeQuery));

        LCQuery<LCObject> query3 = LCQuery.and(Arrays.asList(query,query2));

        query3.whereContainedIn("Bledger",ledger);
        query3.findInBackground().subscribe(new io.reactivex.Observer<List<LCObject>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(List<LCObject> lcObjects) {
                if (lcObjects.size()==0){
                    recyclerView.setVisibility(GONE);
                    notfind.setVisibility(VISIBLE);
                    notfindtext.setVisibility(VISIBLE);
                    String ledgername="没有找到“"+tv_search.getQuery().toString()+"”相关结果呢~";
                    int end=4+tv_search.getQuery().toString().length()+2;
                    SpannableString spannable = new SpannableString(ledgername);
                    spannable.setSpan(new ForegroundColorSpan(Color.RED), 4,end , Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    notfindtext.setText(spannable);
                }else {
                    recyclerView.setVisibility(VISIBLE);
                    notfind.setVisibility(GONE);
                    notfindtext.setVisibility(GONE);

                    String[] nums = new String[lcObjects.size()];
                    String[] notes = new String[lcObjects.size()];
                    String[] dates = new String[lcObjects.size()];
                    String[] id = new String[lcObjects.size()];
                    String[] typeid = new String[lcObjects.size()];
                    Date d;
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    formatter.setTimeZone(TimeZone.getTimeZone("GMT+08"));

                    List<BillViewModel.billinfo> billinfos=new ArrayList<>(lcObjects.size());
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
                        BillViewModel.billinfo binfo=new BillViewModel.billinfo(id[i],typeid[i],notes[i],dates[i],nums[i]);
                        billinfos.add(binfo);
                    }

                    LCQuery<LCObject> typeinfo = new LCQuery<>("Etype");
                    typeinfo.whereContainedIn("objectId", Arrays.asList(typeid));
                    typeinfo.findInBackground().subscribe(new Observer<List<LCObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {}

                        @Override
                        public void onNext(List<LCObject> back) {
                            pgbar.setVisibility(GONE);
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
                            list.clear();
                            //遍历第一个list通过对应的typeid设置对应属性
                            for (BillViewModel.billinfo billinfo : billinfos) {
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

                            //按日期分组
                            Map<String, List<Billitem>> billitemsByDate = list.stream()
                                    .collect(Collectors.groupingBy(Billitem::getDate));

                            billitemsByDateList = billitemsByDate.entrySet().stream()
                                    .map(e -> e.getValue().stream().collect(Collectors.toList()))
                                    .collect(Collectors.toList());

                            // 按日期从大到小排序
                            billitemsByDateList.sort((list1, list2) -> {
                                String date1 = list1.get(0).getDate();
                                String date2 = list2.get(0).getDate();
                                return date2.compareTo(date1); // 注意这里是逆序，所以是date2 - date1
                            });


                            dateBillAdapter=new DateBillAdapter(billitemsByDateList);
                            dateBillAdapter.notifyDataSetChanged();
                            LinearLayoutManager linearLayoutManager=new LinearLayoutManager(Billsearch.this);
                            recyclerView.setLayoutManager(linearLayoutManager);
                            recyclerView.setAdapter(dateBillAdapter);

                            if (isfirst){
                                isfirst=false;
                                recyclerView.addItemDecoration(new billDecoration(25,true));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(Billsearch.this,"加载失败"+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onComplete() {}
                    });
                }
            }

            @Override
            public void onError(Throwable e) {Toast.makeText(Billsearch.this,"加载失败"+e.getMessage(),Toast.LENGTH_SHORT).show();}
            @Override
            public void onComplete() {
                pgbar.setVisibility(GONE);
            }
        });
    }

    //查询账单方法
    private void searchfunction(){
        if (!tv_search.getQuery().toString().isEmpty()){
            if (dateStart==null||dateEnd==null){
                //获取当前时间
                Date curDate = new Date(System.currentTimeMillis());
                searchTypeid(ledgerid,tv_search.getQuery().toString(),new Date(0),curDate);
            }else {
                searchTypeid(ledgerid,tv_search.getQuery().toString(),dateStart,dateEnd);
            }
        }
    }

    //初始化popwindow上的账本信息
    private void initledger(View customView){
        ImageView pgbar1=customView.findViewById(R.id.pgbar);
        AnimationDrawable ad1=(AnimationDrawable)pgbar.getDrawable();
        pgbar1.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad1.start();
            }
        }, 100);

        LCUser user=LCUser.getCurrentUser();
        LCQuery<LCObject> query = new LCQuery<>("Eledger");
        query.whereEqualTo("Euser", user);
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            public void onSubscribe(Disposable disposable) {
                pgbar1.setVisibility(VISIBLE);
            }
            public void onNext(List<LCObject> back) {
                pgbar1.setVisibility(GONE);
                String[] name=new String[back.size()+1];
                String[] ledgerid=new String[back.size()+1];

                List<Item> list=new ArrayList<>(back.size()+1);
                name[0]="不限账本";
                ledgerid[0]="0";
                String ledger=getIntent().getStringExtra("ledgerid");
                for (int i=1;i<back.size()+1;i++){
                    LCObject object=back.get(i-1);
                    name[i]=object.getString("Lname");
                    ledgerid[i]=object.getObjectId();
                    if (object.getObjectId().equals(ledger)){
                        selectview=i;
                    }
                }
                for (int i = 0; i < name.length; i++) {
                    Item item = new Item(name[i],ledgerid[i]);
                    list.add(item);
                }

                RecyclerView recyclerView=customView.findViewById(R.id.radio);
                billsearchLedgerAdapter=new Billsearch_ledgerAdapter(list);
                billsearchLedgerAdapter.setSelectview(selectview);
                GridLayoutManager gridLayout=new GridLayoutManager(Billsearch.this,3);
                recyclerView.setLayoutManager(gridLayout);
                recyclerView.addItemDecoration(new billDecoration(25));
                recyclerView.setAdapter(billsearchLedgerAdapter);
            }
            public void onError(Throwable throwable) {
                Toast.makeText(Billsearch.this,"加载失败"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
            }
            public void onComplete() {
                pgbar1.setVisibility(GONE);
            }
        });
    }

    //初始化popwindow
    private PopupWindow initmPopupWindowView(int type,int layoutid) {
        // 获取自定义布局文件popwindow_layout.xml的视图
        View customView= LayoutInflater.from(this).inflate(layoutid,null);
        final PopupWindow popupwindow = new PopupWindow(customView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupwindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupwindow.setOutsideTouchable(false);
        popupwindow.setAnimationStyle(R.style.popup_animation);
        popupwindow.setFocusable(false);
        TextView reset,finish;
        switch(type){
            case 0:
                finish=customView.findViewById(R.id.finish);
                finish.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (billsearchLedgerAdapter!=null){
                            if (billsearchLedgerAdapter.getLedgerName().size()==0){
                                Toast.makeText(Billsearch.this,"请至少选择一个账本",Toast.LENGTH_SHORT).show();
                            }else {
                                ledgername="";
                                dismissPopWindow(popupwindow);
                                for (int i=0;i<billsearchLedgerAdapter.getLedgerName().size();i++){
                                    if (i==billsearchLedgerAdapter.getLedgerName().size()-1){
                                        ledgername+=billsearchLedgerAdapter.getLedgerName().get(i);
                                    }else {
                                        ledgername+=billsearchLedgerAdapter.getLedgerName().get(i)+" / ";
                                    }
                                }
                                ledgerid.clear();
                                ledgerid.addAll(billsearchLedgerAdapter.getLedgerid());
                                billledger.setText(ledgername);

                                searchfunction();
                            }
                        }
                    }
                });

                reset=customView.findViewById(R.id.reset);
                reset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismissPopWindow(popupwindow);
                        ledgername=getIntent().getStringExtra("ledgername");
                        if (billsearchLedgerAdapter!=null){
                            billsearchLedgerAdapter.setSelectview(selectview);
                            billsearchLedgerAdapter.setIsselectall(false);
                            billsearchLedgerAdapter.notifyDataSetChanged();
                            ledgerid.clear();
                            ledgerid.add(getIntent().getStringExtra("ledgerid"));
                            billledger.setText(ledgername);
                            searchfunction();
                        }

                    }
                });

                initledger(customView);

                break;
            case 1:
                DateTimePicker dateTimePicker=customView.findViewById(R.id.dateTimePicker);
                TextView startDate=customView.findViewById(R.id.startDate);
                TextView endDate=customView.findViewById(R.id.endDate);
                reset=customView.findViewById(R.id.reset);
                finish=customView.findViewById(R.id.finish);
                dateTimePicker.setDisplayType(new int[]{
                        DateTimeConfig.YEAR,//显示年
                        DateTimeConfig.MONTH,//显示月
                        DateTimeConfig.DAY,//显示日
                });
                dateTimePicker.setLayout(R.layout.year_month_day_layout);//自定义layout resId
                dateTimePicker.showLabel(true);
                dateTimePicker.setTextSize(20,20);
                dateTimePicker.setThemeColor(Color.parseColor("#FFC107"));
                dateTimePicker.setDefaultMillisecond(System.currentTimeMillis());

                //设置时间格式
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                //设置时区
                formatter.setTimeZone(TimeZone.getTimeZone("GMT+08"));

                startDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startDate.setSelected(true);
                        startDate.setTextColor(Color.parseColor("#E66E4E"));
                        if (endDate.isSelected()){
                            endDate.setSelected(false);
                            endDate.setTextColor(Color.BLACK);
                            dateTimePicker.setOnDateTimeChangedListener(null);
                            dateTimePicker.setDefaultMillisecond(System.currentTimeMillis());
                        }

                        dateTimePicker.setOnDateTimeChangedListener(new Function1<Long, Unit>() {
                            @Override
                            public Unit invoke(Long aLong) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(aLong);
                                Date date=calendar.getTime();
                                dateStart=calendar.getTime();
                                //格式转换
                                String createDate = formatter.format(date);
                                startDate.setText(createDate);
                                return null;
                            }
                        });
                    }
                });

                endDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        endDate.setSelected(true);
                        endDate.setTextColor(Color.parseColor("#E66E4E"));
                        if (startDate.isSelected()){
                            startDate.setSelected(false);
                            startDate.setTextColor(Color.BLACK);
                            dateTimePicker.setOnDateTimeChangedListener(null);
                            dateTimePicker.setDefaultMillisecond(System.currentTimeMillis());
                        }

                        dateTimePicker.setOnDateTimeChangedListener(new Function1<Long, Unit>() {
                            @Override
                            public Unit invoke(Long aLong) {
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(aLong);
                                Date date=calendar.getTime();

                                calendar.setTimeInMillis(aLong+(24 * 60 * 60 * 1000));
                                dateEnd=calendar.getTime();
                                //格式转换
                                String createDate = formatter.format(date);
                                endDate.setText(createDate);
                                return null;
                            }
                        });

                    }
                });

                reset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startDate.setText("开始时间");
                        endDate.setText("结束时间");
                        dateStart=null;
                        dateEnd=null;

                        if (startDate.isSelected()){
                            startDate.setTextColor(Color.BLACK);
                            startDate.setSelected(false);
                        } else if (endDate.isSelected()) {
                            endDate.setTextColor(Color.BLACK);
                            endDate.setSelected(false);
                        }
                        dateTimePicker.setOnDateTimeChangedListener(null);
                        dateTimePicker.setDefaultMillisecond(System.currentTimeMillis());
                        dismissPopWindow(popupwindow);
                        billdate.setText("不限时间");

                        searchfunction();
                    }
                });

                finish.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (startDate.getText().toString().equals("开始时间")&&endDate.getText().toString().equals("结束时间")){
                            dismissPopWindow(popupwindow);
                            iconRotate(datedown,500,180,360);
                        } else if (startDate.getText().toString().equals("开始时间")){
                            Toast.makeText(Billsearch.this,"请选择开始时间",Toast.LENGTH_SHORT).show();
                        } else if (endDate.getText().toString().equals("结束时间")) {
                            Toast.makeText(Billsearch.this,"请选择结束时间",Toast.LENGTH_SHORT).show();
                        } else if (dateEnd.getTime()<dateStart.getTime()) {
                            Toast.makeText(Billsearch.this,"结束日期需要大于等于起始日期",Toast.LENGTH_SHORT).show();
                        }else {
                            dismissPopWindow(popupwindow);
                            billdate.setText(startDate.getText()+"-"+endDate.getText());
                            searchfunction();
                        }
                    }
                });

                break;
        }

       return popupwindow;
    }

    //弹出popwindow
    private void showPopWindow(int type, final View view) {
        showMaskLayer();
        tv_search.getBackground().setColorFilter(Color.parseColor("#F4F4F4"), PorterDuff.Mode.SRC_IN);
        Animation showAction = AnimationUtils.loadAnimation(this, R.anim.alpha_in);
        switch (type) {
            case 0:
                billledger.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getWindow().setStatusBarColor(Color.WHITE);//设置状态栏颜色
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//实现状态栏图标和文字颜色为暗色
                        }
                        topbg.startAnimation(showAction);
                        topbg.setVisibility(VISIBLE);
                        mLedgerPopupwindow.showAsDropDown(view);
                    }
                }, 50);
                iconRotate(ledgerdown,500,0,180);
                break;
            case 1:
                billdate.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            getWindow().setStatusBarColor(Color.WHITE);//设置状态栏颜色
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//实现状态栏图标和文字颜色为暗色
                        }
                        topbg.startAnimation(showAction);
                        topbg.setVisibility(VISIBLE);
                        mDatePopupwindow.showAsDropDown(view);
                    }
                }, 50);
                iconRotate(datedown,500,0,180);
                break;
            default:
                break;
        }
    }

    //关闭单个popWindow
    private void dismissPopWindow(PopupWindow popupWindow) {
        tv_search.getBackground().setColorFilter(Color.parseColor("#D3D2D2"), PorterDuff.Mode.SRC_IN);
        StatusBar statusBar = new StatusBar(Billsearch.this);
        statusBar.setColor(R.color.transparent);
        Animation hiddenAction = AnimationUtils.loadAnimation(this, R.anim.alpha_out);
        topbg.startAnimation(hiddenAction);
        topbg.setVisibility(GONE);
        dismissMaskLayer();
        if (popupWindow==mDatePopupwindow){
            iconRotate(datedown,500,180,360);
        }
        if (popupWindow==mLedgerPopupwindow){
            iconRotate(ledgerdown,500,180,360);
        }
        popupWindow.dismiss();
    }

    //三角图标旋转动画
    private void iconRotate(ImageView imageView,int time,int initial,int angle){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView,"rotation",initial,angle);
        objectAnimator.setDuration(time);
        objectAnimator.start();
    }

    //显示popupWindow弹出时候的背景遮罩层
    private void showMaskLayer() {
        if (maskLayer.getVisibility() == VISIBLE) {
            return;
        }
        Animation showAction = AnimationUtils.loadAnimation(this, R.anim.alpha_in);
        maskLayer.startAnimation(showAction);
        maskLayer.setVisibility(VISIBLE);
    }

    //取消遮罩层
    private void dismissMaskLayer() {
        if (maskLayer.getVisibility() == GONE) {
            return;
        }
        Animation hiddenAction = AnimationUtils.loadAnimation(this, R.anim.alpha_out);
        maskLayer.startAnimation(hiddenAction);
        maskLayer.setVisibility(GONE);
    }

    //页面进入动画
    private void inAnimation() {

        int widthInPx = getIntent().getIntExtra("width",0);
        multiple= (float) ((double)nowwidth/widthInPx);
        int heightInPx = (int) (40 * Billsearch.this.getResources().getDisplayMetrics().density);
        // 创建一个LinearLayout.LayoutParams对象，设置宽度和高度
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(widthInPx, heightInPx);
        tv_search.setLayoutParams(layoutParams);


        float originY=getIntent().getIntExtra("y",0);
        //获取到搜索框在TwoActivity界面的位置
        int[] location=new int[2];
        tv_search.getLocationOnScreen(location);
        //计算位置的差值
        final float translateY=originY-(float)location[1];
        //将第一个界面的位置设置给搜索框
        tv_search.setY(tv_search.getY()+translateY);
        float top = getResources().getDisplayMetrics().density * 40;
        //ValueAnimator是一个很厉害的东西，你只需要给他初始值和结束值，他会自动计算中间的过度
        final ValueAnimator translateVa = ValueAnimator.ofFloat(tv_search.getY(), top);
        //这个是由下移动到上面的监听
        translateVa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                tv_search.setY((Float) valueAnimator.getAnimatedValue());
                cancel.setY((Float) valueAnimator.getAnimatedValue());
            }
        });

        translateVa.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //动画加载完毕后重新设置约束
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) billledger.getLayoutParams();
                params.leftMargin=(int) (20 * Billsearch.this.getResources().getDisplayMetrics().density);
                params.topMargin=(int) (15 * Billsearch.this.getResources().getDisplayMetrics().density);
                billledger.setLayoutParams(params);

            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });


        //这个是缩小搜索框的监听
        ValueAnimator scaleVa = ValueAnimator.ofFloat(1,multiple);
        scaleVa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                tv_search.setScaleX((Float) valueAnimator.getAnimatedValue());
            }
        });

        //这个是设置透明度
        ValueAnimator alphaVa = ValueAnimator.ofFloat(0, 1f);
        alphaVa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                cancel.setAlpha((Float) valueAnimator.getAnimatedValue());
                tv_search.setAlpha((Float) valueAnimator.getAnimatedValue());

            }
        });

        alphaVa.setDuration(500);
        translateVa.setDuration(500);
        scaleVa.setDuration(500);

        alphaVa.start();
        translateVa.start();
        scaleVa.start();
    }

    //页面退出动画
    private void outAnimation() {
        float originY=getIntent().getIntExtra("y",0);

        int[] location=new int[2];
        tv_search.getLocationOnScreen(location);

        final float translateY=originY-(float)location[1];
        tv_search.setY(tv_search.getY()+translateY);
        float top = getResources().getDisplayMetrics().density * 20;
        final ValueAnimator translateVa = ValueAnimator.ofFloat(top, tv_search.getY());

        translateVa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                tv_search.setY((Float) valueAnimator.getAnimatedValue());
            }
        });

        translateVa.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                finish();
                overridePendingTransition(0, 0);
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });

        ValueAnimator scaleVa = ValueAnimator.ofFloat(multiple, 1);
        scaleVa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                tv_search.setScaleX((Float) valueAnimator.getAnimatedValue());
            }
        });


        ValueAnimator alphaVa = ValueAnimator.ofFloat(1f, 0);
        alphaVa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                cancel.setAlpha((Float) valueAnimator.getAnimatedValue());
                tv_search.setAlpha((Float) valueAnimator.getAnimatedValue());
            }
        });

        alphaVa.setDuration(500);
        translateVa.setDuration(500);
        scaleVa.setDuration(500);

        alphaVa.start();
        translateVa.start();
        scaleVa.start();
    }

}