package com.example.e_finance;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class LedgerInfoFragment extends Fragment {
    private String[] types,color,image,imgselect;
    private String ledgerid;
    private Boolean state=true;
    private RecyclerView ledgertype;
    private TypeAdpter typeAdpter;
    private List<Typeitem> list = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_ledger_info, container, false);

        ledgertype = view.findViewById(R.id.ledgertype);

        typeAdpter = new TypeAdpter(list);
        typeAdpter.setLedgerid(ledgerid);
        typeAdpter.setState(state);
        typeAdpter.setIsledger(true);

        GridLayoutManager gridLayout=new GridLayoutManager(getActivity(),5);
        ledgertype.setLayoutManager(gridLayout);
        ledgertype.setAdapter(typeAdpter);


        return view;
    }
    public void update(String[] Types,String[] Image,String[] Imgselect,String[] Color,String lid,Boolean s){
        state=s;
        ledgerid=lid;
        types=Types;
        image=Image;
        imgselect=Imgselect;
        color=Color;

        list.clear();
        for (int i=0;i<types.length;i++){
            Typeitem typeitem=new Typeitem(types[i],color[i],image[i],imgselect[i],"",null);
            list.add(typeitem);
        }
        if (typeAdpter!=null){
            typeAdpter.setState(state);
            typeAdpter.notifyDataSetChanged();
        }

    }
}