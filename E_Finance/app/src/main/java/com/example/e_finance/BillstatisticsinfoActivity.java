package com.example.e_finance;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_finance.ui.bill.BillViewModel;

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
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class BillstatisticsinfoActivity extends AppCompatActivity {

    private Date date1,date2;
    private boolean isMonth;
    private String info,typeid;
    private TextView dateView,notBillText;
    private ImageView notBill,back;
    private RecyclerView recyclerView;
    private DateBillAdapter dateBillAdapter;
    private List<Billitem> list = new ArrayList<>();
    private List<List<Billitem>> billitemsByDateList=new ArrayList<>();
    private ImageView pgbar;
    private AnimationDrawable ad;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(BillstatisticsinfoActivity.this);
        statusBar.setColor(R.color.transparent);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billstatisticsinfo);

        initView();
    }
    private void initView(){
        back=findViewById(R.id.back);
        dateView=findViewById(R.id.date);
        notBill=findViewById(R.id.notBill);
        notBillText=findViewById(R.id.notfindtext);
        recyclerView=findViewById(R.id.RecyclerView);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        notBill.setVisibility(GONE);
        notBillText.setVisibility(GONE);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent=getIntent();
        typeid=intent.getStringExtra("typeid");
        Calendar calendar=Calendar.getInstance(TimeZone.getTimeZone("GMT+08"));
        calendar.setTimeInMillis(intent.getLongExtra("dateStart",System.currentTimeMillis()));
        date1=calendar.getTime();
        calendar.setTimeInMillis(intent.getLongExtra("dateEnd",System.currentTimeMillis()));
        date2=calendar.getTime();
        isMonth=intent.getBooleanExtra("isMonth",true);
        info=intent.getStringExtra("typename");
        if (typeid==null){
            SimpleDateFormat sdf;
            if (isMonth){
                sdf= new SimpleDateFormat("yyyy年MM月dd日");
            }else {
                sdf = new SimpleDateFormat("yyyy年MM月");
            }
            info=sdf.format(date1);
        }
        dateView.setText(info);

        final LCQuery<LCObject> startDateQuery = new LCQuery<>("Ebill");
        startDateQuery.whereGreaterThanOrEqualTo("Bdate", date1);

        final LCQuery<LCObject> endDateQuery = new LCQuery<>("Ebill");
        endDateQuery.whereLessThan("Bdate", date2);

        LCQuery<LCObject> query = LCQuery.and(Arrays.asList(startDateQuery, endDateQuery));
        if (typeid!=null){
            final LCQuery<LCObject> billTypeQuery = new LCQuery<>("Ebill");
            billTypeQuery.whereEqualTo("Btype",typeid);
            query=LCQuery.and(Arrays.asList(query,billTypeQuery));
        }
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            @Override
            public void onSubscribe(Disposable d) {
                pgbar.setVisibility(VISIBLE);
            }
            @Override
            public void onNext(List<LCObject> lcObjects) {
                if (lcObjects.size()==0){
                    pgbar.setVisibility(GONE);
                    recyclerView.setVisibility(GONE);
                    notBill.setVisibility(VISIBLE);
                    notBillText.setVisibility(VISIBLE);
                }else {
                    recyclerView.setVisibility(VISIBLE);
                    notBill.setVisibility(GONE);
                    notBillText.setVisibility(GONE);

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
                            LinearLayoutManager linearLayoutManager=new LinearLayoutManager(BillstatisticsinfoActivity.this);
                            recyclerView.setLayoutManager(linearLayoutManager);
                            recyclerView.setAdapter(dateBillAdapter);
                            recyclerView.addItemDecoration(new billDecoration(25,true));
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(BillstatisticsinfoActivity.this,"加载失败"+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onComplete() {}
                    });
                }
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(BillstatisticsinfoActivity.this,"加载失败"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                pgbar.setVisibility(GONE);
            }
        });


    }
}