package com.example.e_finance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class TypeShowFragment extends Fragment {
    private RecyclerView alltype;
    private TextView bg,TypeName,Num;
    private ImageView TypeIcon;
    private String ledgerid;
    private int selectview;
    private List<Typeitem> list = new ArrayList<>();
    private Boolean state=true;
    private TypeShowAdapter typeAdpter;
    private String act;
    private String num;
    public TypeShowFragment(Boolean state,List<Typeitem> list,String ledgerid,int selectview,String act) {
        // Required empty public constructor
        this.state=state;
        this.ledgerid=ledgerid;
        this.list.clear();
        this.list.addAll(list);
        this.selectview=selectview;
        this.act=act;
    }
    public TypeShowFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view=inflater.inflate(R.layout.fragment_type_show, container, false);

        alltype=view.findViewById(R.id.alltype);
        bg=view.findViewById(R.id.textView);
        Num=view.findViewById(R.id.Num);
        TypeName=view.findViewById(R.id.TypeName);
        TypeIcon=view.findViewById(R.id.TypeIcon);

        if (typeAdpter==null){
            typeAdpter = new TypeShowAdapter(list,this);
            typeAdpter.setLedgerid(ledgerid);
            typeAdpter.setState(state);
            typeAdpter.setSelectedPosition(selectview);
        }

        GridLayoutManager gridLayout=new GridLayoutManager(getActivity(),5);
        alltype.setLayoutManager(gridLayout);
        alltype.setAdapter(typeAdpter);
        if (act.equals("cycle")){
            ((Cycle_Bill_Edit)this.getActivity()).setNumberText(Num);
        }

        if (num!=null){
            setResult(num);
        }

        return view;
    }
    public void setResult(String text) {
        this.num=text;
        if (Num != null) {
            if (text.length()>=13){
                text=text.substring(0, 11)+"...";
            }
            Num.setText(text);
        }
    }

    public TextView getTextView(){
        return Num;
    }

    public int getSelectview() {
        return selectview;
    }

    public void setBg(String color){
        bg.getBackground().setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN);
    }

    public void setTypeName(String typeName){
        TypeName.setText(typeName);
    }

    public void setTypeIcon(String typeIcon){
        TypeIcon.setBackground(null);
        Glide.with(TypeShowFragment.this).load(typeIcon).into(TypeIcon);
    }

    public void update(Boolean state,List<Typeitem> list,String ledgerid,int selectview){
        this.state=state;
        this.ledgerid=ledgerid;
        this.list.clear();
        this.list.addAll(list);
        this.selectview=selectview;

        if (typeAdpter!=null){
            typeAdpter.setState(state);
            typeAdpter.setLedgerid(ledgerid);
            if (selectview!=-1){
                typeAdpter.setSelectedPosition(selectview);
            }else {
                typeAdpter.setSelectedPosition(0);
            }
            typeAdpter.notifyDataSetChanged();
        }
    }
    public class TypeShowAdapter extends RecyclerView.Adapter<TypeShowAdapter.ViewHolder>{
        private List<Typeitem> type;
        private Boolean state=true;
        private String ledgerid;
        private  int selectedPosition = 0;
        private TypeShowFragment fragment;

        public TypeShowAdapter(List<Typeitem> type,TypeShowFragment fragment) {
            this.type = type;
            this.fragment=fragment;
        }
        public void setState(Boolean state){
            this.state=state;
        }
        public void setLedgerid(String ledgerid) {
            this.ledgerid = ledgerid;
        }
        public void setSelectedPosition(int selectedPosition){
            this.selectedPosition=selectedPosition;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.type, parent, false);
            view.setMinimumHeight(view.getWidth());
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, final @SuppressLint("RecyclerView") int position) {
            Typeitem typeitem = type.get(position);
            holder.textView.setText(typeitem.getType());
            if (position==selectedPosition){
                fragment.setTypeName(typeitem.getType());
                fragment.setBg(typeitem.getColor());
                fragment.setTypeIcon(typeitem.getimageIdselect());
                if (act.equals("cycle")){
                    ((Cycle_Bill_Edit)fragment.getActivity()).setCycletype(typeitem.getTypeid());
                }
                Glide.with(fragment).load(typeitem.getimageIdselect()).into(holder.imageView);
                holder.typebg.getBackground().setColorFilter(Color.parseColor(typeitem.getColor()), PorterDuff.Mode.SRC_IN);
            }else {
                Glide.with(fragment).load(typeitem.getImageId()).into(holder.imageView);
                holder.typebg.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            }
                //点击item时判断是否有点击过，如果没点击过则记录当前点击的位置
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (selectedPosition!=position){
                            selectedPosition=position;
                            selectview=position;
                            notifyDataSetChanged();
                        }
                    }
                });

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

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView textView;
            View typebg;
            public ViewHolder(@NonNull View view) {
                super(view);
                imageView = view.findViewById(R.id.ItemLogo);
                textView = view.findViewById(R.id.typename);
                typebg=view.findViewById(R.id.ItemColor);
                imageView.setBackground(null);
            }
        }
    }
}