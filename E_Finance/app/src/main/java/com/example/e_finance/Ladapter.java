package com.example.e_finance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_finance.ui.bill.BillViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class Ladapter extends RecyclerView.Adapter<Ladapter.ViewHolder>{
    private List<Item> data;
    private Boolean edit=false;
    private Ldialog ldialog;
    private BillViewModel billViewModel;
    private String ledgerid;
    public void setData(List<Item> data) {
        this.data = data;
    }
    public void setBillViewModel(BillViewModel billViewModel){
        this.billViewModel = billViewModel;
    }

    public void setEdit(Boolean edit){this.edit=edit;notifyDataSetChanged();}
    public void setLdialog(Ldialog ldialog){
        this.ldialog=ldialog;
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView Lname;
        public ImageView action;
        public ViewHolder(@NonNull View view){
            super(view);
            Lname=view.findViewById(R.id.name);
            action=view.findViewById(R.id.action);
        }
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.ledger_info,parent,false);
        ViewHolder viewHolder = new  ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Item item =data.get(position);
        holder.Lname.setText(item.getName());
        holder.action.setVisibility(View.GONE);

        //初始化启用的账本
        LCUser user=LCUser.getCurrentUser();
        LCQuery<LCObject> query = new LCQuery<>("Eledger");
        query.whereEqualTo("Euser", user);
        query.whereEqualTo("isUsed",true);
        query.getFirstInBackground().subscribe(new Observer<LCObject>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(LCObject lcObject) {
                String lname = lcObject.getString("Lname");
                if (item.getName().equals(lname) && !edit) {
                    holder.action.setVisibility(View.VISIBLE);
                    holder.action.setImageResource(R.drawable.check_logo);
                }
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {}
        });

        //点击后将选中账本改为启动状态，其余账本改为未启用状态
        holder.Lname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ldialog.dismiss();
                LCUser user=LCUser.getCurrentUser();
                LCQuery<LCObject> query = new LCQuery<>("Eledger");
                query.whereEqualTo("Euser", user);
                query.findInBackground().subscribe(new Observer<List<LCObject>>() {
                    public void onSubscribe(Disposable disposable) {}
                    public void onNext(List<LCObject> back) {
                        String[] name=new String[back.size()];
                        for (int i=0;i<back.size();i++){
                            LCObject object=back.get(i);
                            name[i]=object.getString("Lname");
                            if (name[i].equals(item.getName())){
                                object.put("isUsed",true);
                                object.saveInBackground().subscribe(new Observer<LCObject>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {}
                                    @Override
                                    public void onNext(LCObject lcObject) {
                                        billViewModel.refreshData();
                                    }
                                    @Override
                                    public void onError(Throwable e) {}
                                    @Override
                                    public void onComplete() {}
                                });
                            }else {
                                object.put("isUsed",false);
                                object.saveInBackground().subscribe(new Observer<LCObject>() {
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

                        }

                    }
                    public void onError(Throwable throwable) {}
                    public void onComplete() {}
                });
            }
        });
        //点击编辑账本按钮后显示编辑图标
        if (edit) {
            holder.action.setVisibility(View.VISIBLE);
            holder.action.setImageResource(R.drawable.ledger_edit);
            holder.Lname.setOnClickListener(null);
            holder.Lname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LCQuery<LCObject> query = new LCQuery<>("Eledger");
                    query.whereEqualTo("Euser", user);
                    query.whereEqualTo("Lname",holder.Lname.getText());
                    query.getFirstInBackground().subscribe(new Observer<LCObject>() {
                        @Override
                        public void onSubscribe(Disposable d) {}
                        @Override
                        public void onNext(LCObject lcObject) {
                            ldialog.dismiss();
                            ledgerid = lcObject.getObjectId();
                            Intent intent = new Intent(view.getContext(), LedgerInfo.class);
                            intent.putExtra("ledgerid", ledgerid);
                            intent.putExtra("action", "删除账本");
                            view.getContext().startActivity(intent);
                        }
                        @Override
                        public void onError(Throwable e) {}
                        @Override
                        public void onComplete() {}
                    });
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

}
