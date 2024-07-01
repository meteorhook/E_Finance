package com.example.e_finance;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class Billsearch_ledgerAdapter extends RecyclerView.Adapter<Billsearch_ledgerAdapter.ViewHolder> {
    private List<Item> ledger;
    private List<String> ledgerid=new ArrayList<>();
    private List<String> ledgerName=new ArrayList<>();
    private int selectview=-1;
    private Boolean isselectall=false;

    public List<String> getLedgerid() {
        return ledgerid;
    }

    public List<String> getLedgerName() {
        return ledgerName;
    }

    public void setSelectview(int selectview) {
        this.selectview = selectview;
    }

    public void setIsselectall(Boolean isselectall) {
        this.isselectall = isselectall;
    }

    public Billsearch_ledgerAdapter(List<Item> ledger){
        this.ledger=ledger;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
            LayoutInflater.from(parent.getContext()).inflate(R.layout.billsearch_ledger_item,parent,false);
        Billsearch_ledgerAdapter.ViewHolder viewHolder = new Billsearch_ledgerAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Item item =ledger.get(position);
        holder.Lname.setText(item.getName());

        if (isselectall&&position==0){
            holder.Lname.setSelected(true);
            holder.Lname.setTextColor(Color.parseColor("#E66E4E"));

            ledgerid.clear();
            ledgerName.clear();
            for (int i=1;i<ledger.size();i++){
                Item item2 =ledger.get(i);
                ledgerid.add(item2.getLedgerid());
            }
            ledgerName.add(item.getName());
        }else if (position==selectview){
            holder.Lname.setSelected(true);
            holder.Lname.setTextColor(Color.parseColor("#E66E4E"));

            ledgerid.clear();
            ledgerName.clear();
            ledgerid.add(item.getLedgerid());
            ledgerName.add(item.getName());
        }else {
            holder.Lname.setSelected(false);
            holder.Lname.setTextColor(Color.BLACK);
        }

        holder.Lname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.isSelected()){
                    v.setSelected(false);
                    holder.Lname.setTextColor(Color.BLACK);

                    ledgerid.remove(item.getLedgerid());
                    ledgerName.remove(item.getName());
                }else {
//                    Toast.makeText(v.getContext(), "ss"+holder.Lname.getText(),Toast.LENGTH_SHORT).show();

                    selectview=holder.getLayoutPosition();
                    if (isselectall){
                        isselectall=false;
                        notifyDataSetChanged();
                    }else if (holder.getLayoutPosition()==0){
                        isselectall=true;
                        notifyDataSetChanged();
                    }else {
                        v.setSelected(true);
                        holder.Lname.setTextColor(Color.parseColor("#E66E4E"));

                        ledgerid.add(item.getLedgerid());
                        ledgerName.add(item.getName());
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return ledger.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView Lname;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Lname=itemView.findViewById(R.id.name);
        }
    }
}
