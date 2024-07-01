package com.example.e_finance.adapter;

import android.annotation.SuppressLint;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.e_finance.Bill_Add;
import com.example.e_finance.R;
import com.example.e_finance.TypeManage;
import com.example.e_finance.entity.Typeitem;
import com.example.e_finance.fragment.BillAddFragment;

import java.util.List;

public class TypeAdpter extends RecyclerView.Adapter<TypeAdpter.ViewHolder> {

    private List<Typeitem> type;
    private  int selectedPosition = 0;

    private Boolean state=true,isledger=false;
    private String ledgerid;
    private Fragment fragment;


    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        View typebg;
        public ViewHolder(@NonNull View view) {
            super(view);
            imageView = view.findViewById(R.id.ItemLogo);
            textView = view.findViewById(R.id.typename);
            typebg=view.findViewById(R.id.ItemColor);
        }
    }

    public TypeAdpter(List<Typeitem> type) {
        this.type = type;
    }

    public void setState(Boolean state){
        this.state=state;
    }

    public void setIsledger(Boolean isledger) {
        this.isledger = isledger;
    }

    public void setLedgerid(String ledgerid) {
        this.ledgerid = ledgerid;
    }

    public void setSelectedPosition(int selectedPosition){
        this.selectedPosition=selectedPosition;
    }

    public void setFragment(Fragment fragment){
        this.fragment=fragment;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int
            viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.type, parent, false);
        view.setMinimumHeight(view.getWidth());
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Typeitem typeitem = type.get(position);
        holder.imageView.setBackground(null);

        //如果是添加账本界面则显示结果且不设置监听
        if (isledger){
            Glide.with(holder.itemView.getContext()).load(typeitem.getimageIdselect()).into(holder.imageView);
            holder.typebg.getBackground().setColorFilter(Color.parseColor(typeitem.getColor()), PorterDuff.Mode.SRC_IN);
            holder.textView.setText(typeitem.getType());
            holder.itemView.setOnClickListener(null);
        }
        //如果不是添加账本界面则设置添加账单界面对应监听
        else {
            if (position==selectedPosition) {
                Glide.with(fragment.getActivity()).load(typeitem.getimageIdselect()).into(holder.imageView);
                holder.typebg.getBackground().setColorFilter(Color.parseColor(typeitem.getColor()), PorterDuff.Mode.SRC_IN);
                holder.textView.setText(typeitem.getType());
                ((BillAddFragment)fragment).setBg(typeitem.getColor());
                ((BillAddFragment)fragment).setTypeIcon(typeitem.getimageIdselect());
                ((BillAddFragment)fragment).setTypeName(typeitem.getType());
                ((Bill_Add)fragment.getActivity()).setBillAdd(typeitem.getTypeid());
            }else {
                Glide.with(fragment.getActivity()).load(typeitem.getImageId()).into(holder.imageView);
                holder.typebg.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                holder.textView.setText(typeitem.getType());
            }
            //点击item时判断是否有点击过，如果没点击过则记录当前点击的位置
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selectedPosition!=position){
                        selectedPosition=position;
                        notifyDataSetChanged();
                    }
                }
            });
        }

        //如果是最后一个则启动跳转到分类管理界面
        if (position==type.size()-1){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), TypeManage.class);
                    intent.putExtra("ledgerid", ledgerid);
                    intent.putExtra("state", state);
                    v.getContext().startActivity(intent);
                }
            });
        }

    }
    @Override
    public int getItemCount() {
        return type.size();
    }
}

