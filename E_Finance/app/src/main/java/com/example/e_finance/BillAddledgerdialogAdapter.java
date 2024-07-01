package com.example.e_finance;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class BillAddledgerdialogAdapter extends RecyclerView.Adapter<BillAddledgerdialogAdapter.ViewHolder> {
    private List<Item> data;
    private BottomSheetDialog ldialog;
    private  int selectedPosition = -1;
    private String act="bill";
    public BillAddledgerdialogAdapter (String act){
        this.act=act;
    }
    public BillAddledgerdialogAdapter (){

    }
    public void setLdialog(BottomSheetDialog ldialog){
        this.ldialog=ldialog;
    }
    public void setData(List<Item> data) {
        this.data = data;
    }
    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.ledger_info,parent,false);
        BillAddledgerdialogAdapter.ViewHolder viewHolder = new BillAddledgerdialogAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Item item =data.get(position);
        holder.Lname.setText(item.getName());

        if (position==selectedPosition){
            holder.action.setVisibility(View.VISIBLE);
            holder.action.setImageResource(R.drawable.check_logo);
        }else {
            holder.action.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition=position;
                if (act.equals("bill")){
                    ((Bill_Add)v.getContext()).setLedgerid(item.getLedgerid());
                    ((Bill_Add)v.getContext()).updatetype();
                }
                else if (act.equals("cycle")){
                    ((Cycle_Bill_Edit)v.getContext()).setLedgerid(item.getLedgerid());
                    ((Cycle_Bill_Edit)v.getContext()).GetType(item.getLedgerid());
                }

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
                                    public void onNext(LCObject lcObject) {}
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
                ldialog.dismiss();
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView Lname;
        public ImageView action;
        public ViewHolder(@NonNull View view){
            super(view);
            Lname=view.findViewById(R.id.name);
            action=view.findViewById(R.id.action);
        }
    }
}
