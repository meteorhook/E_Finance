package com.example.e_finance;

import android.graphics.Color;
import android.graphics.PorterDuff;

import android.os.Bundle;

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



public class BillAddFragment extends Fragment {
    private RecyclerView alltype;
    private TextView textView,bg,TypeName,Num;
    private ImageView TypeIcon;
    private String ledgerid;
    private int selectview;
    private List<Typeitem> list = new ArrayList<>();
    private Boolean state=true;
    private TypeAdpter typeAdpter;

    public BillAddFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_bill_add, container, false);

        textView=view.findViewById(R.id.Num);
        alltype=view.findViewById(R.id.alltype);
        bg=view.findViewById(R.id.textView);
        Num=view.findViewById(R.id.Num);
        TypeName=view.findViewById(R.id.TypeName);
        TypeIcon=view.findViewById(R.id.TypeIcon);

        typeAdpter = new TypeAdpter(list);
        typeAdpter.setFragment(this);
        typeAdpter.setLedgerid(ledgerid);
        typeAdpter.setState(state);

        GridLayoutManager gridLayout=new GridLayoutManager(getActivity(),5);
        alltype.setLayoutManager(gridLayout);
        alltype.setAdapter(typeAdpter);
        ((Bill_Add)this.getActivity()).setbilladdtext(Num);
        return view;

    }

    public void setResult(String text) {
        if (textView != null) {
            if (text.length()>=13){
                text=text.substring(0, 11)+"...";
            }
            textView.setText(text);
        }
    }

    public void setBg(String color){
        bg.getBackground().setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN);
    }

    public void setTypeName(String typeName){
        TypeName.setText(typeName);
    }

    public void setTypeIcon(String typeIcon){
        TypeIcon.setBackground(null);
        Glide.with(getActivity()).load(typeIcon).into(TypeIcon);
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

}