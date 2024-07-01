package com.example.e_finance.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.e_finance.R;
import com.example.e_finance.TypeEdit;

import java.util.List;

public class TypeIconAdapter extends RecyclerView.Adapter<TypeIconAdapter.ViewHolder>{
    private List<TypeEdit.Icon> icon;
    private  int selectedPosition = -1;
    public TypeIconAdapter(List<TypeEdit.Icon> icon){
        this.icon=icon;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @NonNull
    @Override
    public TypeIconAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.type_icon_item, parent, false);
        view.setMinimumHeight(view.getWidth());
        TypeIconAdapter.ViewHolder viewHolder = new TypeIconAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TypeIconAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        TypeEdit.Icon icon1=icon.get(position);
        holder.ItemLogo.setBackground(null);

        if (position==selectedPosition) {
            Glide.with(holder.ItemLogo.getContext())
                    .load(icon1.geticonselect().trim())
                    .into(holder.ItemLogo);
            ((TypeEdit)holder.itemView.getContext()).settypeImg(icon1);
            holder.ItemColor.getBackground().setColorFilter(Color.parseColor("#C5C5C5"), PorterDuff.Mode.SRC_IN);
        }else {
            Glide.with(holder.ItemLogo.getContext())
                    .load(icon1.geticon().trim())
                    .into(holder.ItemLogo);
            holder.ItemColor.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedPosition!=position){
                    selectedPosition=position;
                    ((TypeEdit)holder.itemView.getContext()).settypeImg(icon1);
                    notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return icon.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView ItemLogo;
        View ItemColor;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ItemColor=itemView.findViewById(R.id.ItemColor);
            ItemLogo=itemView.findViewById(R.id.ItemLogo);
        }
    }
}
