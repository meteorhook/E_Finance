package com.example.e_finance;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {
    private List<Billitem> bill;

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView ItemLogo;
        TextView Project,Num,Note;
        View itemcolor;
        public ViewHolder(@NonNull View view){
            super(view);
            ItemLogo=(ImageView)view.findViewById(R.id.ItemLogo);
            Project=(TextView)view.findViewById(R.id.Project);
            Num=(TextView)view.findViewById(R.id.Num);
            itemcolor=(View)view.findViewById(R.id.ItemColor);
            Note=(TextView)view.findViewById(R.id.note);
        }
    }
    public BillAdapter(List<Billitem> bill){
        this.bill=bill;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int
            viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.bill_info,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        Billitem billitem =bill.get(position);
        if (!billitem.getNote().isEmpty()){
            // 如果不为空，显示note
            holder.Note.setVisibility(View.VISIBLE);
            holder.Note.setText(billitem.getNote());
        }else {
            ConstraintLayout.LayoutParams projectParams = (ConstraintLayout.LayoutParams) holder.Project.getLayoutParams();
            projectParams.bottomToBottom = R.id.ItemColor;
            holder.Project.setLayoutParams(projectParams);
        }
        Glide.with(holder.itemView)
                .load(billitem.getItemLogo())
                .into(holder.ItemLogo);
        holder.itemcolor.getBackground().setColorFilter(Color.parseColor(billitem.getColor()), PorterDuff.Mode.SRC_IN);
        holder.Project.setText(billitem.getProject());
        holder.Num.setText(billitem.getNum());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(v.getContext(),BillInfo.class);
                intent.putExtra("BillId",billitem.getId());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bill.size();
    }
}
