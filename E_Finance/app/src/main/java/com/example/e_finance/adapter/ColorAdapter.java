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

import com.example.e_finance.R;
import com.example.e_finance.TypeEdit;

import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder>{
    private List<String> color;
    private  int selectedPosition = -1;
    public ColorAdapter(List<String> color) {
        this.color = color;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }

    @NonNull
    @Override
    public ColorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.type_color_item, parent, false);
        view.setMinimumHeight(view.getWidth());
        ColorAdapter.ViewHolder viewHolder = new ColorAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ColorAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        if (position==selectedPosition) {
            holder.colorcheck.setVisibility(View.VISIBLE);
            ((TypeEdit)holder.itemView.getContext()).setbg(color.get(position));
        }else {
            holder.colorcheck.setVisibility(View.GONE);
        }

        holder.colorview.getBackground().setColorFilter(Color.parseColor(color.get(position)), PorterDuff.Mode.SRC_IN);

        //点击item时判断是否有点击过，如果没点击过则记录当前点击的位置
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedPosition!=position){
                    selectedPosition=position;
                    ((TypeEdit)v.getContext()).setbg(color.get(position));
                    notifyDataSetChanged();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return color.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        View colorview;
        ImageView colorcheck;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            colorview=itemView.findViewById(R.id.coloritem);
            colorcheck = itemView.findViewById(R.id.colorcheck);
        }
    }
}
