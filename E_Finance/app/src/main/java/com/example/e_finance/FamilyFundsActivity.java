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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.e_finance.customview.myAlertDialog;
import com.example.e_finance.util.StatusBar;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

public class FamilyFundsActivity extends AppCompatActivity {

    private Spinner spinner;
    private TextView fundsmoney;
    private RecyclerView recyclerView;
    private Button getfunds,addfunds;
    private EditText money;
    private ImageView pgbar;
    private AnimationDrawable ad;
    private List<RAU> records=new ArrayList<>();
    private List<String> familylist=new ArrayList<>();
    private boolean isfirst=true;
    private ArrayAdapter spadapter;
    private FundsRecordAdapter recordAdapter;
    private SimpleDateFormat formatter;
    private String familyid,ledgerid;
    private int spsel=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(FamilyFundsActivity.this);
        statusBar.setColor(R.color.transparent);
        setContentView(R.layout.activity_family_origin);

        setContentView(R.layout.activity_family_funds);

        spinner=findViewById(R.id.spinner);
        fundsmoney=findViewById(R.id.fundsmoney);
        recyclerView=findViewById(R.id.recycleview);
        getfunds=findViewById(R.id.getfunds);
        addfunds=findViewById(R.id.addfunds);
        money=findViewById(R.id.money);

        pgbar=findViewById(R.id.pgbar);

        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+08"));

        ledgerid=getIntent().getStringExtra("ledgerid");

        initview();

        getfunds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (money.getText().toString().isEmpty()){
                    Toast.makeText(FamilyFundsActivity.this,"请输入金额",Toast.LENGTH_SHORT).show();
                }
                else {
                    LCQuery<LCObject> query = new LCQuery<>("Efamily");
                    query.getInBackground(familyid).subscribe(new Observer<LCObject>() {
                        public void onSubscribe(Disposable disposable) {
                            pgbar.setVisibility(View.VISIBLE);
                        }
                        public void onNext(LCObject todo) {
                            BigDecimal funds=new BigDecimal(todo.getString("Ffunds"));
                            BigDecimal get=new BigDecimal(money.getText().toString());
                            if (funds.compareTo(get)==-1) {//支出金额大于余额
                                Toast.makeText(FamilyFundsActivity.this, "支取金额大于基金总额", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                BigDecimal result = funds.subtract(get);
                                LCObject todo2 = LCObject.createWithoutData("Efamily",familyid);
                                todo2.put("Ffunds",result.stripTrailingZeros().toPlainString());
                                todo2.saveInBackground().subscribe(new Observer<LCObject>() {
                                    public void onSubscribe(Disposable disposable) {}
                                    public void onNext(LCObject todo) {

                                        LCObject todo3 = new LCObject("Erecord");
                                        todo3.put("Rfamily",LCObject.createWithoutData("Efamily",familyid));
                                        todo3.put("Rnum",get.stripTrailingZeros().toPlainString());
                                        todo3.put("Rstate",true);
                                        todo3.put("Ruser",LCUser.getCurrentUser());
                                        todo3.saveInBackground().subscribe(new Observer<LCObject>() {
                                            public void onSubscribe(Disposable disposable) {}
                                            public void onNext(LCObject todo) {
                                                pgbar.setVisibility(View.GONE);
                                                initview();
                                            }
                                            public void onError(Throwable throwable) {
                                                pgbar.setVisibility(View.GONE);
                                                Log.e("recordError",throwable.getMessage());
                                                Toast.makeText(FamilyFundsActivity.this,"记录写入失败"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                            public void onComplete() {
                                                pgbar.setVisibility(View.GONE);
                                            }
                                        });

                                        LCObject bill = new LCObject("Ebill");
                                        bill.put("Bdate", new Date(System.currentTimeMillis()));
                                        bill.put("Bnum", get.stripTrailingZeros().toPlainString());
                                        bill.put("Bnotes","");
                                        bill.put("Bledger", ledgerid);
                                        bill.put("Bstate", false);//收入
                                        bill.put("Btype", "663bceff0b2d9e69f2774a47");
                                        bill.put("Bbudget",true);
                                        bill.saveInBackground().subscribe(new Observer<LCObject>() {
                                            public void onSubscribe(Disposable disposable) {}
                                            public void onNext(LCObject todo) {
                                                pgbar.setVisibility(View.GONE);
                                                Toast.makeText(FamilyFundsActivity.this, "金额支取成功，已自动记账", Toast.LENGTH_SHORT).show();
                                            }
                                            public void onError(Throwable throwable) {
                                                pgbar.setVisibility(View.GONE);
                                                Log.e("billaddError",throwable.getMessage());
                                                Toast.makeText(FamilyFundsActivity.this, "添加账单失败," + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                            public void onComplete() {
                                                pgbar.setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                    public void onError(Throwable throwable) {
                                        pgbar.setVisibility(View.GONE);
                                        Log.e("fundsgetError",throwable.getMessage());
                                        Toast.makeText(FamilyFundsActivity.this,"金额支取失败"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                    public void onComplete() {
                                        pgbar.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                        public void onError(Throwable throwable) {
                            pgbar.setVisibility(View.GONE);
                            Log.e("familyError",throwable.getMessage());
                            Toast.makeText(FamilyFundsActivity.this,"家庭信息加载失败，"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                        public void onComplete() {
                            pgbar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });

        addfunds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (money.getText().toString().isEmpty()){
                    Toast.makeText(FamilyFundsActivity.this,"请输入金额",Toast.LENGTH_SHORT).show();
                } else {
                    LCQuery<LCObject> query = new LCQuery<>("Efamily");
                    query.getInBackground(familyid).subscribe(new Observer<LCObject>() {
                        public void onSubscribe(Disposable disposable) {
                            pgbar.setVisibility(View.VISIBLE);
                        }
                        public void onNext(LCObject todo) {
                            BigDecimal funds=new BigDecimal(todo.getString("Ffunds"));
                            BigDecimal increase=new BigDecimal(money.getText().toString());
                            BigDecimal result = funds.add(increase);
                            LCObject todo2 = LCObject.createWithoutData("Efamily",familyid);
                            todo2.put("Ffunds",result.stripTrailingZeros().toPlainString());
                            todo2.saveInBackground().subscribe(new Observer<LCObject>() {
                                public void onSubscribe(Disposable disposable) {
                                    pgbar.setVisibility(View.VISIBLE);
                                }
                                public void onNext(LCObject todo) {

                                    LCObject todo3 = new LCObject("Erecord");
                                    todo3.put("Rfamily",LCObject.createWithoutData("Efamily",familyid));
                                    todo3.put("Rnum",increase.stripTrailingZeros().toPlainString());
                                    todo3.put("Rstate",false);
                                    todo3.put("Ruser",LCUser.getCurrentUser());
                                    todo3.saveInBackground().subscribe(new Observer<LCObject>() {
                                        public void onSubscribe(Disposable disposable) {}
                                        public void onNext(LCObject todo) {
                                            pgbar.setVisibility(View.GONE);
                                            initview();
                                        }
                                        public void onError(Throwable throwable) {
                                            pgbar.setVisibility(View.GONE);
                                            Log.e("recordError",throwable.getMessage());
                                            Toast.makeText(FamilyFundsActivity.this,"记录写入失败"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                        public void onComplete() {
                                            pgbar.setVisibility(View.GONE);
                                        }
                                    });

                                    LCObject bill = new LCObject("Ebill");
                                    bill.put("Bdate", new Date(System.currentTimeMillis()));
                                    bill.put("Bnum", increase.stripTrailingZeros().toPlainString());
                                    bill.put("Bnotes","");
                                    bill.put("Bledger", ledgerid);
                                    bill.put("Bstate", true);//支出
                                    bill.put("Btype", "663bcecc482841355737a36a");
                                    bill.put("Bbudget",true);
                                    bill.saveInBackground().subscribe(new Observer<LCObject>() {
                                        public void onSubscribe(Disposable disposable) {}
                                        public void onNext(LCObject todo) {
                                            pgbar.setVisibility(View.GONE);
                                            Toast.makeText(FamilyFundsActivity.this, "金额存入成功，已自动记账", Toast.LENGTH_SHORT).show();
                                        }
                                        public void onError(Throwable throwable) {
                                            pgbar.setVisibility(View.GONE);
                                            Log.e("billaddError",throwable.getMessage());
                                            Toast.makeText(FamilyFundsActivity.this, "添加账单失败," + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                        public void onComplete() {
                                            pgbar.setVisibility(View.GONE);
                                        }
                                    });
                                }
                                public void onError(Throwable throwable) {
                                    pgbar.setVisibility(View.GONE);
                                    Log.e("fundsgetError",throwable.getMessage());
                                    Toast.makeText(FamilyFundsActivity.this,"金额存入失败"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                                public void onComplete() {
                                    pgbar.setVisibility(View.GONE);
                                }
                            });
                        }
                        public void onError(Throwable throwable) {
                            pgbar.setVisibility(View.GONE);
                            Log.e("familyError",throwable.getMessage());
                            Toast.makeText(FamilyFundsActivity.this,"家庭信息加载失败，"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                        public void onComplete() {
                            pgbar.setVisibility(View.GONE);
                        }
                    });




                }
            }
        });
    }
    private void initview(){
        final LCQuery<LCObject> ownerfamily = new LCQuery<>("Efamily");
        ownerfamily.whereEqualTo("Fowner",LCUser.getCurrentUser());
        final   LCQuery<LCObject> unowner = new LCQuery<>("Efamily");
        unowner.whereEqualTo("Fmember", LCUser.getCurrentUser().getObjectId());
        LCQuery<LCObject> query =  LCQuery.or(Arrays.asList(ownerfamily, unowner));
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            public void onSubscribe(Disposable disposable) {
                pgbar.setVisibility(View.VISIBLE);
            }
            public void onNext(List<LCObject> family) {
                if (isfirst){
                    isfirst=false;
                }
                if (family.isEmpty()){
                    pgbar.setVisibility(View.GONE);
                    if (spadapter!= null){
                        spadapter.clear();
                        spadapter.notifyDataSetChanged();
                    }
                    records.clear();
                    recordAdapter = new FundsRecordAdapter(records);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FamilyFundsActivity.this);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.setAdapter(recordAdapter);
                    myAlertDialog alertDialog=new myAlertDialog(FamilyFundsActivity.this,false);
                    alertDialog.create();
                    alertDialog.setTitle("提示");
                    alertDialog.setMessage("当前用户未加入或未创建家庭，是否前往加入/创建家庭？");
                    alertDialog.setPositive("加入/创建", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            Intent intent=new Intent(FamilyFundsActivity.this, FamilyOriginActivity.class);
                            startActivity(intent);
                        }
                    });
                    alertDialog.setNegative("返回", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            finish();
                        }
                    });
                    alertDialog.show();
                }else {
                    String userid=LCUser.getCurrentUser().getObjectId();
                    boolean ow=false;
                    String ownerfamily="";
                    List<String> tlist=new ArrayList<>();
                    Map<String,LCObject> familyinfo=new HashMap<>();

                    for (LCObject lcObject:family){
                        if (lcObject.getLCObject("Fowner").getObjectId().equals(userid)){
                            ow=true;
                            ownerfamily=lcObject.getString("Fname");
                        }else {
                            tlist.add(lcObject.getString("Fname"));
                        }
                        familyinfo.put(lcObject.getString("Fname"),lcObject);
                    }
                    familylist.clear();
                    if (ow){
                        familylist.add(ownerfamily);
                    }
                    familylist.addAll(tlist);
                    spadapter=new ArrayAdapter<>(FamilyFundsActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,familylist);
                    spinner.setAdapter(spadapter);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            spsel=i;
                            LCObject fobj=familyinfo.get(spinner.getSelectedItem().toString());
                            fundsmoney.setText(fobj.getString("Ffunds")+"元");
                            familyid= fobj.getObjectId();
                            LCQuery<LCObject> query2 = new LCQuery<>("Erecord");
                            query2.whereEqualTo("Rfamily",fobj);
                            query2.include("Ruser.nickName");
                            query2.orderByDescending("createdAt");
                            query2.findInBackground().subscribe(new Observer<List<LCObject>>() {
                                public void onSubscribe(Disposable disposable) {
                                }
                                public void onNext(List<LCObject> lcObjects) {
                                    records.clear();
                                    for (LCObject lcObject : lcObjects) {
                                        String state="存入了";
                                        if (lcObject.getBoolean("Rstate")){
                                            state="支取了";
                                        }
                                        String r=lcObject.getLCObject("Ruser").getString("nickName")
                                                +"在"+formatter.format(lcObject.getCreatedAt())
                                                + state
                                                +lcObject.getString("Rnum")+"元";

                                        records.add(new RAU(r,lcObject.getLCObject("Ruser").getString("photo")));
                                    }
                                    pgbar.setVisibility(View.GONE);
                                    recordAdapter = new FundsRecordAdapter(records);
                                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FamilyFundsActivity.this);
                                    recyclerView.setLayoutManager(linearLayoutManager);
                                    recyclerView.setAdapter(recordAdapter);
                                }
                                public void onError(Throwable throwable) {
                                    pgbar.setVisibility(View.GONE);
                                    Log.e("recordError",throwable.getMessage());
                                    Toast.makeText(FamilyFundsActivity.this,"存取记录查询失败，"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                                public void onComplete() {
                                    pgbar.setVisibility(View.GONE);
                                }
                            });
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });
                    spinner.setSelection(spsel);
                }

            }
            public void onError(Throwable throwable) {
                pgbar.setVisibility(View.GONE);
                Log.e("familydelError",throwable.getMessage());
                Toast.makeText(FamilyFundsActivity.this,"用户家庭获取失败"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
            }
            public void onComplete() {
                pgbar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isfirst){
            initview();
        }
    }

    public class FundsRecordAdapter extends RecyclerView.Adapter<FundsRecordAdapter.ViewHolder>{
        private List<RAU> list;
        public FundsRecordAdapter(List<RAU> list){
            this.list=list;
        }
        @NonNull
        @Override
        public FundsRecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.funds_record_item,parent,false);
            FundsRecordAdapter.ViewHolder viewHolder =new FundsRecordAdapter.ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RAU record=list.get(position);
            holder.record.setText(record.record);
            Glide.with(FamilyFundsActivity.this)
                    .load(record.img)
                    .transform(new CircleCrop())
                    .into(holder.userpic);
        }
        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            private TextView record;
            private ImageView userpic;

            public ViewHolder(@NonNull View view){
                super(view);
                record=(TextView)view.findViewById(R.id.record);
                userpic=(ImageView)view.findViewById(R.id.userpic);
            }
        }
    }

    private class RAU{
        private String record;
        private String img;
        public RAU(String record,String img){
            this.img=img;
            this.record=record;
        }
    }
}