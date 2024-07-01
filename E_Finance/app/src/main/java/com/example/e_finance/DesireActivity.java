package com.example.e_finance;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_finance.util.StatusBar;

import java.util.ArrayList;
import java.util.List;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class DesireActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DesireAdapter desireAdapter;
    private TextView uncomplete,all,add,nulltv;
    private List<DesireItem> desireItems=new ArrayList<>();
    private List<DesireItem> desireItemsUn=new ArrayList<>();
    private Boolean isall=false;
    private ImageView pgbar;
    private AnimationDrawable ad;
    private LinearLayout nullbg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(DesireActivity.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_desire);

        uncomplete=findViewById(R.id.uncomplete);
        all=findViewById(R.id.all);
        add=findViewById(R.id.add);
        nullbg=findViewById(R.id.null_layout);
        nulltv=findViewById(R.id.nulltv);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        recyclerView=findViewById(R.id.recycleview);

        GridLayoutManager gridLayout=new GridLayoutManager(DesireActivity.this,2);
        recyclerView.setLayoutManager(gridLayout);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DesireDialog dialog=new DesireDialog(DesireActivity.this);
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                dialog.show();
            }
        });

        getDesire();
    }
    private void getDesire(){
        desireItems.clear();
        desireItemsUn.clear();
        LCQuery<LCObject> query = new LCQuery<>("Edesire");
        query.whereEqualTo("Euser", LCUser.getCurrentUser());
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            public void onSubscribe(Disposable disposable) {
                pgbar.setVisibility(View.VISIBLE);
            }
            public void onNext(List<LCObject> desires) {
                pgbar.setVisibility(View.GONE);
                    for (LCObject lcObject:desires){
                        String name=lcObject.getString("Dname"),num=lcObject.getString("Dnum");
                        Boolean state=lcObject.getBoolean("Dstate");
                        DesireItem desireItem=new DesireItem(lcObject.getObjectId(),name,num,state);
                        desireItems.add(desireItem);
                        if (!state){
                            desireItemsUn.add(desireItem);
                        }
                    }
            }
            public void onError(Throwable throwable) {
                pgbar.setVisibility(View.GONE);
                Toast.makeText(DesireActivity.this,"心愿数据加载失败,"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
            public void onComplete() {
                pgbar.setVisibility(View.GONE);

                uncomplete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isall=false;
                        uncomplete.setTextColor(Color.BLACK);
                        all.setTextColor(Color.LTGRAY);
                        desireAdapter=new DesireAdapter(desireItemsUn);
                        desireAdapter.notifyDataSetChanged();
                        recyclerView.setAdapter(desireAdapter);

                        if (desireItemsUn.size()==0){
                            nullbg.setVisibility(View.VISIBLE);
                            nulltv.setText("暂无待实现心愿");
                        }else {
                            nullbg.setVisibility(View.GONE);
                        }
                    }
                });

                all.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isall=true;
                        all.setTextColor(Color.BLACK);
                        uncomplete.setTextColor(Color.LTGRAY);
                        desireAdapter=new DesireAdapter(desireItems);
                        desireAdapter.notifyDataSetChanged();
                        recyclerView.setAdapter(desireAdapter);

                        if (desireItems.size()==0){
                            nullbg.setVisibility(View.VISIBLE);
                            nulltv.setText("暂无心愿");
                        }else {
                            nullbg.setVisibility(View.GONE);
                        }
                    }
                });

                if (isall){
                    all.callOnClick();
                }else {
                    uncomplete.callOnClick();
                }
            }
        });
    }
    public void SaveDesire(String desireId, String desireName, String desireNum, Boolean desireState){
        LCObject desire = LCObject.createWithoutData("Edesire", desireId);
        desire.put("Dname",desireName);
        desire.put("Dnum",desireNum);
        desire.put("Dstate",desireState);
        desire.saveInBackground().subscribe(new Observer<LCObject>() {
            public void onSubscribe(Disposable disposable) {
                pgbar.setVisibility(View.VISIBLE);
            }
            public void onNext(LCObject savedTodo) {
                getDesire();
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });
    }

    public class DesireItem{
        private String desireName,desireNum,desireId;
        private Boolean desireState;
        public DesireItem(String desireId,String desireName,String desireNum,Boolean desireState){
            this.desireId=desireId;
            this.desireName=desireName;
            this.desireNum=desireNum;
            this.desireState=desireState;
        }

        public String getDesireName() {
            return desireName;
        }

        public String getDesireNum() {
            return desireNum;
        }

        public Boolean getDesireState() {
            return desireState;
        }
    }
    public class DesireAdapter extends RecyclerView.Adapter<DesireAdapter.ViewHolder>{
        private List<DesireItem> desireItems;
        public DesireAdapter(List<DesireItem> desireItems){
            this.desireItems=desireItems;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.desire_item_layout,parent,false);
            ViewHolder viewHolder=new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DesireItem desireItem=desireItems.get(position);
            holder.desiretitle.setText(desireItem.desireName);
            holder.desirenum.setText(desireItem.desireNum);
            holder.desirecomplete.setSelected(desireItem.desireState);
            if (desireItem.desireState){
                holder.desirecomplete.setText("已实现");
            }else {
                holder.desirecomplete.setText("实\t\t现");
            }

            holder.desirecomplete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!v.isSelected()){
                        SaveDesire(desireItem.desireId,desireItem.desireName,desireItem.desireNum,true);
                    }else {
                        SaveDesire(desireItem.desireId,desireItem.desireName,desireItem.desireNum,false);
                    }
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DesireDialog dialog=new DesireDialog(DesireActivity.this);
                    dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    dialog.setDesireItem(desireItem);
                    dialog.setTitle("修改心愿");
                    dialog.setCancelStr("删除心愿");
                    dialog.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return desireItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView desiretitle,desirenum,desirecomplete;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                desirecomplete=itemView.findViewById(R.id.desirecomplete);
                desirenum=itemView.findViewById(R.id.desirenum);
                desiretitle=itemView.findViewById(R.id.desiretitle);
            }
        }
    }
    public class DesireDialog extends Dialog{
        private TextView title,cancel,submit;
        private EditText desirename,desirenum;
        private ImageView DisMiss;
        private String titleStr="添加心愿",cancelStr="取消";
        private DesireItem desireItem;

        public void setDesireItem(DesireItem desireItem) {
            this.desireItem = desireItem;
        }

        public DesireDialog(@NonNull Context context) {
            super(context);
        }

        public void setTitle(String titleStr) {
            this.titleStr=titleStr;
        }

        public void setCancelStr(String cancelStr) {
            this.cancelStr = cancelStr;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.desire_add_dialog_layout);
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            //点击返回关闭弹窗
            setCancelable(false);
            //点击弹窗外部关闭弹窗
            setCanceledOnTouchOutside(false);

            WindowManager m = getWindow().getWindowManager();
            Display d = m.getDefaultDisplay();
            WindowManager.LayoutParams p = getWindow().getAttributes();
            Point size = new Point();
            d.getSize(size);
            p.width = (int) ((size.x)*0.9);        //设置为屏幕的0.9倍宽度
            getWindow().setAttributes(p);

            title=findViewById(R.id.title);
            cancel=findViewById(R.id.cancel);
            submit=findViewById(R.id.submit);
            desirename=findViewById(R.id.desirename);
            desirenum=findViewById(R.id.desirenum);
            DisMiss=findViewById(R.id.dismiss);

            DisMiss.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            title.setText(titleStr);
            cancel.setText(cancelStr);

            if (desireItem==null){
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                    }
                });

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String Name=desirename.getText().toString(),Num=desirenum.getText().toString();
                        if (Name.isEmpty()){
                            Toast.makeText(getContext(),"请输入心愿名称",Toast.LENGTH_SHORT).show();
                        }else if (Num.isEmpty()){
                            Toast.makeText(getContext(),"请输入心愿金额",Toast.LENGTH_SHORT).show();
                        }else {
                            CreateDesire(Name,Num);
                        }

                    }
                });
            }else {
                desirename.setText(desireItem.desireName);
                desirenum.setText(desireItem.desireNum);

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DeleteDesire(desireItem.desireId);
                        dismiss();
                    }
                });

                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String Name=desirename.getText().toString(),Num=desirenum.getText().toString();
                        if (Name==null){
                            Toast.makeText(getContext(),"请输入心愿名称",Toast.LENGTH_SHORT).show();
                        }else if (Num==null){
                            Toast.makeText(getContext(),"请输入心愿金额",Toast.LENGTH_SHORT).show();
                        }else {
                            SaveDesire(desireItem.desireId,Name,Num,desireItem.desireState);
                            dismiss();
                        }
                    }
                });
            }

            desirename.requestFocus();
        }
        public void DeleteDesire(String desireId){
            LCObject desire = LCObject.createWithoutData("Edesire", desireId);
            desire.deleteInBackground().subscribe(new Observer<LCNull>() {
                @Override
                public void onSubscribe(Disposable d) {
                    pgbar.setVisibility(View.VISIBLE);
                }
                @Override
                public void onNext(LCNull lcNull) {
                    getDesire();
                    dismiss();
                }
                @Override
                public void onError(Throwable e) {
                    pgbar.setVisibility(View.GONE);
                    Toast.makeText(DesireActivity.this,"心愿删除失败，"+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onComplete() {}
            });
        }
        public void CreateDesire(String desireName,String desireNum){
            LCObject desire=new LCObject("Edesire");
            desire.put("Euser",LCUser.getCurrentUser());
            desire.put("Dname",desireName);
            desire.put("Dnum",desireNum);
            desire.put("Dstate",false);
            desire.saveInBackground().subscribe(new Observer<LCObject>() {
                @Override
                public void onSubscribe(Disposable d) {
                    pgbar.setVisibility(View.VISIBLE);
                }
                @Override
                public void onNext(LCObject lcObject) {
                    getDesire();
                    dismiss();
                }
                @Override
                public void onError(Throwable e) {
                    pgbar.setVisibility(View.GONE);
                    Toast.makeText(DesireActivity.this,"心愿创建失败，"+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onComplete() {}
            });
        }
        public void SaveDesire(String desireId, String desireName, String desireNum, Boolean desireState){
            LCObject desire = LCObject.createWithoutData("Edesire", desireId);
            desire.put("Dname",desireName);
            desire.put("Dnum",desireNum);
            desire.put("Dstate",desireState);
            desire.saveInBackground().subscribe(new Observer<LCObject>() {
                public void onSubscribe(Disposable disposable) {
                    pgbar.setVisibility(View.VISIBLE);
                }
                public void onNext(LCObject savedTodo) {
                    getDesire();
                }
                public void onError(Throwable throwable) {
                    pgbar.setVisibility(View.GONE);
                }
                public void onComplete() {}
            });
        }
    }
}