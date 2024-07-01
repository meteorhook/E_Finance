package com.example.e_finance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.iflytek.sparkchain.plugins.mail.Mail;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MyInvestmentActivity extends AppCompatActivity {
    private ImageView pgbar;
    private AnimationDrawable ad;
    private String ledgerid;
    private RecyclerView recyclerView;
    private ConstraintLayout nopro;
    private UserInvestmentAdapter userInvestmentAdapter;
    private List<MapEuseq> infolist=new ArrayList<>();
    private boolean isfirst=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(MyInvestmentActivity.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_my_investment);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        ledgerid=getIntent().getStringExtra("ledgerid");

        recyclerView=findViewById(R.id.RecyclerView);
        nopro=findViewById(R.id.nopro);

        initview();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isfirst){
            initview();
        }
    }
    private void initview(){
        LCQuery<LCObject> query = new LCQuery<>("Euseq");
        query.whereEqualTo("Euser", LCUser.getCurrentUser());
        query.include("Eequities");
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            public void onSubscribe(Disposable disposable) {
                pgbar.setVisibility(View.VISIBLE);
            }
            public void onNext(List<LCObject> useqs) {
                isfirst=false;
                pgbar.setVisibility(View.GONE);
                if (useqs.isEmpty()){
                    nopro.setVisibility(View.VISIBLE);
                    infolist.clear();
                    if (userInvestmentAdapter!=null){
                        userInvestmentAdapter.notifyDataSetChanged();
                    }
                }else {
                    nopro.setVisibility(View.GONE);
                    infolist.clear();
                    for (LCObject lcObject:useqs){
                        LCObject obj= lcObject.getLCObject("Eequities");
                        InvestmentActivity.Ininfo ininfo=new InvestmentActivity.Ininfo(obj.getObjectId()
                                ,obj.getLCFile("Epic").getUrl()
                                ,obj.getString("Ename")
                                ,obj.getString("Erate")
                                ,obj.getString("Econtent")
                                ,obj.getString("Ecircle")
                        );
                        infolist.add(new MapEuseq(lcObject.getObjectId()
                                ,lcObject.getString("Enum")
                                ,ininfo
                                ,lcObject.getDate("Eddl")));
                    }
                    if (userInvestmentAdapter==null){
                        recyclerView.addItemDecoration(new billDecoration(25, false));
                    }
                    userInvestmentAdapter = new UserInvestmentAdapter(infolist);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MyInvestmentActivity.this);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.setAdapter(userInvestmentAdapter);
                    userInvestmentAdapter.notifyDataSetChanged();
                }
            }
            public void onError(Throwable throwable) {
                pgbar.setVisibility(View.GONE);
                Log.e("useqError",throwable.getMessage());
                Toast.makeText(MyInvestmentActivity.this, "用户理财产品加载失败，"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
            public void onComplete() {
                pgbar.setVisibility(View.GONE);
            }
        });
    }
    public class UserInvestmentAdapter extends RecyclerView.Adapter<UserInvestmentAdapter.ViewHolder>{
        private List<MapEuseq> list;
        public UserInvestmentAdapter(List<MapEuseq> list){
            this.list=list;
        }
        @NonNull
        @Override
        public UserInvestmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.userinvestment_item,parent,false);
            UserInvestmentAdapter.ViewHolder viewHolder =new UserInvestmentAdapter.ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull UserInvestmentAdapter.ViewHolder holder, int position) {
            MapEuseq mapEuseq=list.get(position);
            InvestmentActivity.Ininfo ininfo=mapEuseq.ininfo;
            holder.proname.setText(ininfo.getName());
            Glide.with(MyInvestmentActivity.this)
                    .load(ininfo.getSrc())
                    .into(holder.propic);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(MyInvestmentActivity.this,InvestmentInfoActivity.class);
                    intent.putExtra("name",ininfo.getName());
                    intent.putExtra("circle",ininfo.getCircle());
                    intent.putExtra("content",ininfo.getContent());
                    intent.putExtra("id",ininfo.getId());
                    intent.putExtra("pic",ininfo.getSrc());
                    intent.putExtra("rate",ininfo.getRate());
                    intent.putExtra("ledgerid",ledgerid);

                    intent.putExtra("iswithdraw",true);
                    intent.putExtra("useqnum",mapEuseq.useqnum);
                    intent.putExtra("useqddl",mapEuseq.useqddl.getTime());
                    intent.putExtra("useqID",mapEuseq.useqid);

                    startActivity(intent);
                }
            });

            //追加投资
            holder.transfusion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(MyInvestmentActivity.this,InvestmentInfoActivity.class);
                    intent.putExtra("name",ininfo.getName());
                    intent.putExtra("circle",ininfo.getCircle());
                    intent.putExtra("content",ininfo.getContent());
                    intent.putExtra("id",ininfo.getId());
                    intent.putExtra("pic",ininfo.getSrc());
                    intent.putExtra("rate",ininfo.getRate());
                    intent.putExtra("ledgerid",ledgerid);
                    startActivity(intent);
                }
            });

            //提取收益
            holder.withdraw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //计算结果
                    BigDecimal capital=new BigDecimal(mapEuseq.useqnum);
                    String percentageString = ininfo.getRate();
                    String decimalString = percentageString.replace("%", ""); // 去除百分号
                    BigDecimal rate = new BigDecimal(decimalString).divide(new BigDecimal("100")); // 转换为小数

                    Calendar calendar = Calendar.getInstance();
                    Date now=calendar.getTime();
                    Date ddl=mapEuseq.useqddl;
                    calendar.setTime(ddl);
                    if (ininfo.getCircle().equals("年")){
                        calendar.add(Calendar.YEAR,-1);
                    } else if (ininfo.getCircle().equals("月")) {
                        calendar.add(Calendar.MONTH,-1);
                    }
                    Date past=calendar.getTime();

                    // 计算时间差（毫秒）
                    long timeToNow = now.getTime() - past.getTime();
                    long timeToDdl = ddl.getTime() - past.getTime();

                    BigDecimal withdraw;
                    if (timeToNow<timeToDdl){
                        BigDecimal bigRatio = BigDecimal.valueOf((double) timeToNow / timeToDdl).setScale(2, RoundingMode.HALF_UP); // 保留两位小数
                        withdraw = capital.add(capital.multiply(rate).multiply(bigRatio));
                    }else {
                        withdraw=capital.add(capital.multiply(rate));
                    }
                    withdraw = withdraw.setScale(2, RoundingMode.HALF_UP);
                    LCObject bill = new LCObject("Ebill");
                    bill.put("Bdate", new Date(System.currentTimeMillis()));
                    bill.put("Bnum", withdraw.stripTrailingZeros().toPlainString());
                    bill.put("Bnotes",ininfo.getName()+"收益");
                    bill.put("Bledger", ledgerid);
                    bill.put("Bstate", false);
                    bill.put("Btype", "663cc296e62a672ee1872a6d");
                    bill.put("Bbudget",true);
                    bill.saveInBackground().subscribe(new Observer<LCObject>() {
                        public void onSubscribe(Disposable disposable) {}
                        public void onNext(LCObject todo) {
                            pgbar.setVisibility(View.GONE);
                            Toast.makeText(MyInvestmentActivity.this, "收益提取成功，已自动记账", Toast.LENGTH_SHORT).show();
                        }
                        public void onError(Throwable throwable) {
                            pgbar.setVisibility(View.GONE);
                            Log.e("billaddError",throwable.getMessage());
                            Toast.makeText(MyInvestmentActivity.this, "添加账单失败," + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        public void onComplete() {
                            pgbar.setVisibility(View.GONE);
                        }
                    });

                    //删除投资
                    LCObject todo = LCObject.createWithoutData("Euseq", mapEuseq.useqid);
                    todo.deleteInBackground().subscribe(new Observer<LCNull>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {}
                        @Override
                        public void onNext(LCNull response) {
                            initview();
                        }
                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.e("useqError",e.getMessage());
                            Toast.makeText(MyInvestmentActivity.this, "用户投资删除失败"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onComplete() {}
                    });

                }
            });
        }
        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            private TextView proname,transfusion,withdraw;
            private ImageView propic;

            public ViewHolder(@NonNull View view){
                super(view);
                proname=(TextView)view.findViewById(R.id.proname);
                transfusion=(TextView)view.findViewById(R.id.transfusion);
                withdraw=(TextView)view.findViewById(R.id.withdraw);
                propic=(ImageView)view.findViewById(R.id.imageView);
            }
        }
    }

    private class MapEuseq{
        private InvestmentActivity.Ininfo ininfo;
        private String useqid,useqnum;
        private Date useqddl;
        public MapEuseq(String useqid, String useqnum, InvestmentActivity.Ininfo ininfo,Date useqddl){
            this.ininfo=ininfo;
            this.useqddl=useqddl;
            this.useqid=useqid;
            this.useqnum=useqnum;
        }
    }

}