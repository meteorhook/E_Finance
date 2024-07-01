package com.example.e_finance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.iflytek.sparkchain.plugins.mail.Mail;

import java.util.ArrayList;
import java.util.List;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class InvestmentActivity extends AppCompatActivity {
    private ImageView pgbar;
    private AnimationDrawable ad;
    private List<Ininfo> infolist=new ArrayList<>();
    private RecyclerView recyclerView;
    private InvestmentAdapter investmentAdapter;
    private ImageView myprojection;
    private String ledgerid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(InvestmentActivity.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_investment);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        recyclerView=findViewById(R.id.RecyclerView);
        myprojection=findViewById(R.id.myprojection);

        ledgerid=getIntent().getStringExtra("ledgerid");

        myprojection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(InvestmentActivity.this, MyInvestmentActivity.class);
                intent.putExtra("ledgerid",ledgerid);
                startActivity(intent);
            }
        });


        LCQuery<LCObject> query = new LCQuery<>("Eequities");
        query.include("Epic");
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            public void onSubscribe(Disposable disposable) {
                pgbar.setVisibility(View.VISIBLE);
            }
            public void onNext(List<LCObject> equities) {
                pgbar.setVisibility(View.GONE);
                for (LCObject lcObject:equities){
                    infolist.add(new Ininfo(lcObject.getObjectId()
                            ,lcObject.getLCFile("Epic").getUrl()
                            ,lcObject.getString("Ename")
                            ,lcObject.getString("Erate")
                            ,lcObject.getString("Econtent")
                            ,lcObject.getString("Ecircle")
                    ));
                }
                if (investmentAdapter==null){
                    recyclerView.addItemDecoration(new billDecoration(25, false));
                }
                investmentAdapter = new InvestmentAdapter(infolist);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(InvestmentActivity.this);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(investmentAdapter);
            }
            public void onError(Throwable throwable) {
                pgbar.setVisibility(View.GONE);
                Log.e("investmentError",throwable.getMessage());
                Toast.makeText(InvestmentActivity.this,"加载理财产品失败，"+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
            public void onComplete() {
                pgbar.setVisibility(View.GONE);
            }
        });



    }

    public class InvestmentAdapter extends RecyclerView.Adapter<InvestmentAdapter.ViewHolder>{
        private List<Ininfo> list;
        public InvestmentAdapter(List<Ininfo> list){
            this.list=list;
        }
        @NonNull
        @Override
        public InvestmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.projection_item,parent,false);
            InvestmentAdapter.ViewHolder viewHolder =new InvestmentAdapter.ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull InvestmentAdapter.ViewHolder holder, int position) {
            Ininfo ininfo=list.get(position);
            holder.prorate.setText(ininfo.rate);
            holder.proname.setText(ininfo.name);
            Glide.with(InvestmentActivity.this)
                    .load(ininfo.src)
                    .into(holder.propic);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(InvestmentActivity.this,InvestmentInfoActivity.class);
                    intent.putExtra("name",ininfo.name);
                    intent.putExtra("circle",ininfo.circle);
                    intent.putExtra("content",ininfo.content);
                    intent.putExtra("id",ininfo.id);
                    intent.putExtra("pic",ininfo.src);
                    intent.putExtra("rate",ininfo.rate);
                    intent.putExtra("ledgerid",ledgerid);
                    startActivity(intent);
                }
            });
        }
        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            private TextView proname,prorate;
            private ImageView propic;

            public ViewHolder(@NonNull View view){
                super(view);
                proname=(TextView)view.findViewById(R.id.proname);
                prorate=(TextView)view.findViewById(R.id.rate);
                propic=(ImageView)view.findViewById(R.id.imageView);
            }
        }
    }
    public static class Ininfo{
        private String id,src,name,rate,content,circle;
        public Ininfo(String id,String src,String name,String rate,String content,String circle){
            this.id=id;
            this.src=src;
            this.circle=circle;
            this.name=name;
            this.rate=rate;
            this.content=content;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public String getCircle() {
            return circle;
        }

        public String getContent() {
            return content;
        }

        public String getSrc() {
            return src;
        }

        public String getRate() {
            return rate;
        }
    }
}