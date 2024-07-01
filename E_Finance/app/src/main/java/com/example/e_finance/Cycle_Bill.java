package com.example.e_finance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kyleduo.switchbutton.SwitchButton;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.operators.completable.CompletableLift;

public class Cycle_Bill extends AppCompatActivity {
    private FloatingActionButton CycleAdd;
    private ImageView pgbar;
    private AnimationDrawable ad;
    private String ledgerid;
    private ConstraintLayout notCycle;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(Cycle_Bill.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_cycle_bill);
        CycleAdd=findViewById(R.id.typeA);
        notCycle=findViewById(R.id.layout3);
        recyclerView=findViewById(R.id.RecyclerView);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        CycleAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Cycle_Bill.this, Cycle_Bill_Edit.class);
                startActivity(intent);
            }
        });

        recyclerView.addItemDecoration(new billDecoration(25, true));
        initView();
    }
    private void initView(){
        GetLedger();
    }
    private void GetLedger(){
        LCUser user = LCUser.getCurrentUser();
        LCQuery<LCObject> query = new LCQuery<>("Eledger");
        query.whereEqualTo("Euser", user);
        query.whereEqualTo("isUsed", true);
        query.getFirstInBackground().subscribe(new Observer<LCObject>() {
            @Override
            public void onSubscribe(Disposable d) {
                pgbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNext(LCObject lcObject) {
                ledgerid = lcObject.getObjectId();
                GetCycle(ledgerid);
            }

            @Override
            public void onError(Throwable e) {
                pgbar.setVisibility(View.GONE);
                Toast.makeText(Cycle_Bill.this, "用户账本信息加载失败，" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
            }
        });
    }
    private void GetCycle(String ledgerid){
        LCQuery<LCObject> query = new LCQuery<>("Ecycle");
        query.whereEqualTo("Cledger", ledgerid);
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            private Boolean isin=true;
            private List<CycleBill> cycleBills=new ArrayList<>();
            @Override
            public void onSubscribe(Disposable d) {
                pgbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNext(List<LCObject> lcObjects) {
                if (lcObjects.size()==0){
                    isin=false;
                }
                cycleBills.clear();
                List<String> typeid=new ArrayList<>(lcObjects.size());
                for (LCObject lcObject:lcObjects){
                    typeid.add(lcObject.getString("Ctype"));
                    CycleBill cycleBill=new CycleBill(
                            lcObject.getObjectId()
                            ,lcObject.getString("Ctype")
                            ,lcObject.getString("Cdate")
                            ,lcObject.getString("Cnotes")
                            ,lcObject.getString("Cnum")
                            ,lcObject.getBoolean("Cstate")
                            ,lcObject.getBoolean("Cbstate"));
                    cycleBills.add(cycleBill);
                }
                LCQuery<LCObject> typeinfo = new LCQuery<>("Etype");
                typeinfo.whereContainedIn("objectId", typeid);
                typeinfo.findInBackground().subscribe(new Observer<List<LCObject>>() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onNext(List<LCObject> lcObjects) {
                        // 使用流收集LCObject的属性到Typeitem对象，并使用TypeId作为键创建Map
                        Map<String, Typeitem> typeitemMap = lcObjects.stream()
                                .map(lcObject -> new Typeitem(
                                        lcObject.getString("TypeName"),
                                        lcObject.getString("TypeColor"),
                                        null, // 这里您可能需要一个默认值或者处理null的情况
                                        lcObject.getString("TypeImgSelect"),
                                        lcObject.getObjectId(),
                                        lcObject.getBoolean("Tstate")
                                ))
                                .collect(Collectors.toMap(
                                        Typeitem::getTypeid, // 使用Typeid作为键
                                        typeitem -> typeitem  // 值就是Typeitem对象本身
                                ));

                        // 现在，我们可以简化循环逻辑，因为我们已经有了按TypeId组织的Typeitem Map
                        for (CycleBill cycleBill : cycleBills) {
                            Typeitem typeitem = typeitemMap.get(cycleBill.getTypeid());
                            if (typeitem != null) {
                                cycleBill.setCycleType(typeitem);
                            }
                        }

                        //按日期分组
                        Map<String, List<CycleBill>> cycleitemsByDate = cycleBills.stream()
                                .collect(Collectors.groupingBy(CycleBill::getBillCycle));

                        List<List<CycleBill>> cycleitemsByDateList = cycleitemsByDate.entrySet().stream()
                                .map(e -> e.getValue().stream().collect(Collectors.toList()))
                                .collect(Collectors.toList());

                        // 按日期从大到小排序
                        cycleitemsByDateList.sort((list1, list2) -> {
                            String date1 = list1.get(0).getBillCycle();
                            String date2 = list2.get(0).getBillCycle();
                            return date1.compareTo(date2); // 注意这里是正序，所以是date1 - date2
                        });

                        CycleBillItemAdapter cycleBillItemAdapter= new CycleBillItemAdapter(cycleitemsByDateList);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Cycle_Bill.this);
                        recyclerView.setLayoutManager(linearLayoutManager);
                        recyclerView.setAdapter(cycleBillItemAdapter);
                    }
                    @Override
                    public void onError(Throwable e) {
                        pgbar.setVisibility(View.GONE);
                        Toast.makeText(Cycle_Bill.this,"加载失败，"+e.getMessage(),Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }

                    @Override
                    public void onComplete() {
                        pgbar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                pgbar.setVisibility(View.GONE);
                Toast.makeText(Cycle_Bill.this,"加载失败，"+e.getMessage(),Toast.LENGTH_SHORT).show();
                onBackPressed();
            }

            @Override
            public void onComplete() {
                pgbar.setVisibility(View.GONE);
                if (!isin){
                    notCycle.setVisibility(View.VISIBLE);
                }else {
                    notCycle.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        GetCycle(ledgerid);
    }

    public class CycleBill{
        private String CycleNote,CycleNum,CycleLedger,BillCycle,Typeid,Cycleid;
        private Boolean CycleState,CycleBillState;
        private Typeitem CycleType;
        public CycleBill(String Cycleid,String Typeid,String BillCycle,String CycleNote,String CycleNum,Boolean CycleState,Boolean CycleBillState){
            this.Cycleid=Cycleid;
            this.Typeid=Typeid;
            this.CycleNote=CycleNote;
            this.CycleNum=CycleNum;
            this.CycleState=CycleState;
            this.CycleBillState=CycleBillState;
            this.BillCycle=BillCycle;
        }
        public void setCycleType(Typeitem cycleType) {
            CycleType = cycleType;
        }

        public void setCycleLedger(String cycleLedger) {
            CycleLedger = cycleLedger;
        }

        public String getCycleid() {
            return Cycleid;
        }

        public String getTypeid() {
            return Typeid;
        }

        public String getBillCycle() {
            return BillCycle;
        }

        public String getCycleNote() {
            return CycleNote;
        }

        public String getCycleNum() {
            return CycleNum;
        }

        public Typeitem getCycleType() {
            return CycleType;
        }

        public Boolean getCycleBillState() {
            return CycleBillState;
        }

        public Boolean getCycleState() {
            return CycleState;
        }

        public String getCycleLedger() {
            return CycleLedger;
        }
    }

    public class CycleBillAdapter extends RecyclerView.Adapter<CycleBillAdapter.ViewHolder>{
        private List<CycleBill> list;
        public CycleBillAdapter(List<CycleBill> list){
            this.list=list;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.bill_info,parent,false);
            ViewHolder viewHolder =new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CycleBill cycleBill =list.get(position);
            if (!cycleBill.getCycleNote().isEmpty()){
                // 如果不为空，显示note
                holder.CNote.setVisibility(View.VISIBLE);
                holder.CNote.setText(cycleBill.getCycleNote());
            }else {
                ConstraintLayout.LayoutParams projectParams = (ConstraintLayout.LayoutParams) holder.CTypename.getLayoutParams();
                projectParams.bottomToBottom = R.id.ItemColor;
                holder.CTypename.setLayoutParams(projectParams);
            }
            Glide.with(holder.itemView)
                    .load(cycleBill.getCycleType().getimageIdselect())
                    .into(holder.ItemLogo);
            holder.itemcolor.getBackground().setColorFilter(Color.parseColor(cycleBill.getCycleType().getColor()), PorterDuff.Mode.SRC_IN);
            holder.CTypename.setText(cycleBill.getCycleType().getType());

            if (cycleBill.CycleBillState){
                holder.CNum.setText("-"+cycleBill.getCycleNum());
            }else {
                holder.CNum.setText("+"+cycleBill.getCycleNum());
            }

            holder.cycleState.setChecked(cycleBill.getCycleState());
            String cycle="----/--/--";
            Calendar calendar=Calendar.getInstance();
            Calendar curDate=Calendar.getInstance();
            String Billcycle=cycleBill.getBillCycle();
            calendar.set(Calendar.MINUTE,0);
            calendar.set(Calendar.SECOND, 0);
            if (Billcycle.startsWith("每月")){
                try {
                    int number = Integer.parseInt(Billcycle.substring(2,Billcycle.length()-1));
                    // 如果到这里没有异常，那么str是一个有效的整数
                    if (number>calendar.getActualMaximum(Calendar.DAY_OF_MONTH)){
                        calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    }else {
                        calendar.set(Calendar.DAY_OF_MONTH,number);
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    if (calendar.before(curDate)){
                        calendar.add(Calendar.MONTH,1);
                    }
                    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy/MM/dd");
                    Date date=calendar.getTime();
                    cycle= simpleDateFormat.format(date);
                } catch (NumberFormatException e) {
                    // str不是一个有效的整数，处理异常
                    e.printStackTrace();
                }
            } else if (Billcycle.startsWith("每日")) {
                try {
                    int number = Integer.parseInt(Billcycle.substring(2,Billcycle.length()-1));
                    // 如果到这里没有异常，那么str是一个有效的整数
                    calendar.set(Calendar.HOUR_OF_DAY,number);
                    if (calendar.before(curDate)){
                        calendar.add(Calendar.DAY_OF_MONTH,1);
                    }
                    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy/MM/dd HH:mm");
                    Date date=calendar.getTime();
                    cycle= simpleDateFormat.format(date);
                } catch (NumberFormatException e) {
                    // str不是一个有效的整数，处理异常
                    e.printStackTrace();
                }
            }
            holder.nextDate.setText(cycle);

            holder.cycleState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    LCObject updata = LCObject.createWithoutData("Ecycle",cycleBill.getCycleid());
                    updata.put("Cstate", isChecked);
                    updata.saveInBackground().subscribe(new Observer<LCObject>() {
                        public void onSubscribe(Disposable disposable) {
                            pgbar.setVisibility(View.VISIBLE);
                        }
                        public void onNext(LCObject savedTodo) {
                            pgbar.setVisibility(View.GONE);
                            if (savedTodo.getBoolean("Cstate")){
                                Toast.makeText(Cycle_Bill.this,"周期账单已启用",Toast.LENGTH_SHORT).show();
                                // 创建约束条件：可选地设置任务的网络状态、电池状态等
                                Constraints constraints = new Constraints.Builder()
                                        .setRequiredNetworkType(NetworkType.CONNECTED) // 网络连接时才执行任务
                                        .build();
                                // 创建输入数据
                                Data data = new Data.Builder()
                                        .putString("cycle",cycleBill.getBillCycle())
                                        .putString("cycleid",savedTodo.getObjectId())
                                        .build();
                                long timeDiff = calendar.getTimeInMillis() - curDate.getTimeInMillis();
                                WorkRequest dailyWorkRequest =
                                        new OneTimeWorkRequest.Builder(CycleWorker.class)
                                                .setConstraints(constraints)
                                                .setInputData(data)
                                                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                                                .addTag(savedTodo.getObjectId())
                                                .build();
                                WorkManager.getInstance(getApplicationContext()).enqueue(dailyWorkRequest);
                            }else {
                                Toast.makeText(Cycle_Bill.this,"周期账单已关闭",Toast.LENGTH_SHORT).show();
                                WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(savedTodo.getObjectId());
                            }
                        }
                        public void onError(Throwable throwable) {
                            pgbar.setVisibility(View.GONE);
                            Toast.makeText(Cycle_Bill.this,"关闭周期账单失败，"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                        public void onComplete() {
                        }
                    });
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(v.getContext(), Cycle_Bill_Edit.class);
                    intent.putExtra("ledgerid",ledgerid);
                    intent.putExtra("CBstate",cycleBill.getCycleBillState());
                    intent.putExtra("note",cycleBill.getCycleNote());
                    intent.putExtra("cycle",cycleBill.getBillCycle());
                    intent.putExtra("number",cycleBill.getCycleNum());
                    intent.putExtra("typeid",cycleBill.getTypeid());
                    intent.putExtra("cycleid",cycleBill.getCycleid());
                    intent.putExtra("lastCstate",cycleBill.getCycleState());
                    v.getContext().startActivity(intent);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    myAlertDialog myalertDialog = new myAlertDialog(Cycle_Bill.this, false);
                    myalertDialog.create();
                    myalertDialog.setTitle("删除提示");
                    myalertDialog.setMessage("删除该周期账单后无法恢复，确定删除吗？");
                    //设置取消按钮
                    myalertDialog.setNegative("取消", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myalertDialog.dismiss();
                        }
                    });
                    myalertDialog.setPositive("删除", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LCObject cycle = LCObject.createWithoutData("Ecycle", cycleBill.getCycleid());
                            cycle.deleteInBackground().subscribe(new Observer<LCNull>() {
                                @Override
                                public void onSubscribe(@NonNull Disposable d) {
                                    pgbar.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onNext(LCNull response) {
                                    pgbar.setVisibility(View.GONE);
                                    WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(cycleBill.getCycleid());
                                    Toast.makeText(Cycle_Bill.this,"删除成功",Toast.LENGTH_SHORT).show();
                                    GetCycle(ledgerid);
                                    myalertDialog.dismiss();
                                }

                                @Override
                                public void onError(@NonNull Throwable e) {
                                    pgbar.setVisibility(View.GONE);
                                    Toast.makeText(Cycle_Bill.this,"周期账单删除失败，"+e.getMessage(),Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onComplete() {}
                            });
                        }
                    });
                    myalertDialog.show();
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            private ImageView ItemLogo;
            private TextView CTypename,CNum,CNote;
            private View itemcolor;
            private ConstraintLayout stateLayout;
            private SwitchButton cycleState;
            private TextView nextDate;

            public ViewHolder(@NonNull View view){
                super(view);
                ItemLogo=(ImageView)view.findViewById(R.id.ItemLogo);
                CTypename=(TextView)view.findViewById(R.id.Project);
                CNum=(TextView)view.findViewById(R.id.Num);
                itemcolor=(View)view.findViewById(R.id.ItemColor);
                CNote=(TextView)view.findViewById(R.id.note);
                cycleState=(SwitchButton) view.findViewById(R.id.CycleSwitch);
                nextDate=(TextView)view.findViewById(R.id.cycleDate);
                stateLayout=(ConstraintLayout) view.findViewById(R.id.layout2);
                stateLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public class CycleBillItemAdapter extends RecyclerView.Adapter<CycleBillItemAdapter.ViewHolder>{
        private List<List<CycleBill>> cycleItems;
        private CycleBillAdapter cycleBillAdapter;
        public CycleBillItemAdapter(List<List<CycleBill>> cycleItems){
            this.cycleItems=cycleItems;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.bill_child_recycleview,parent,false);
            ViewHolder viewHolder=new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            List<CycleBill> cycle=cycleItems.get(position);
            holder.title_date.setText(cycle.get(0).BillCycle);
            cycleBillAdapter = new CycleBillAdapter(cycle);
            LinearLayoutManager linearLayoutManager=new LinearLayoutManager(holder.itemView.getContext());
            holder.childrecycleview.setLayoutManager(linearLayoutManager);
            holder.childrecycleview.setAdapter(cycleBillAdapter);
        }

        @Override
        public int getItemCount() {
            return cycleItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            private TextView title_date;
            private RecyclerView childrecycleview;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                title_date=itemView.findViewById(R.id.title_date);
                childrecycleview=itemView.findViewById(R.id.child_recycleview);

                //判断是否是初始化，如果是则设置间距，不是则不设置间距
                if(childrecycleview.getItemDecorationCount()==0){
                    childrecycleview.addItemDecoration(new billDecoration(25));
                }
            }
        }
    }
}