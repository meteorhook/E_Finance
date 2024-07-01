package com.example.e_finance;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateBillAdapter extends RecyclerView.Adapter<DateBillAdapter.ViewHolder>{
    private List<List<Billitem>> bill;
    private BillAdapter billAdapter;

    public DateBillAdapter(List<List<Billitem>> bill){
        this.bill=bill;
    }

    @NonNull
    @Override
    public DateBillAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.bill_child_recycleview,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DateBillAdapter.ViewHolder holder, int position) {
        List<Billitem> billitems=bill.get(position);

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy/M/d EEE");
        SimpleDateFormat nowFormat = new SimpleDateFormat("M月d日 EEE");
        SimpleDateFormat Format = new SimpleDateFormat("M月d日");

        try {
            Date date = inputFormat.parse(billitems.get(0).getDate());
            Calendar calendar=Calendar.getInstance();
            Date now=calendar.getTime();
            String outputDate,outputDate1,showdate;
            outputDate = nowFormat.format(now);
            if (now.getYear()==date.getYear()){
                outputDate1 = nowFormat.format(date);
            }else {
                outputDate1 = outputFormat.format(date);
            }

            if (outputDate1.equals(outputDate)){
                showdate=Format.format(date)+" 今天";
            }else {
                showdate=outputDate1;
            }

            holder.title_date.setText(showdate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        billAdapter = new BillAdapter(billitems);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(holder.itemView.getContext());
        holder.childrecycleview.setLayoutManager(linearLayoutManager);
        holder.childrecycleview.setAdapter(billAdapter);
    }

    @Override
    public int getItemCount() {
        return bill.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
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
