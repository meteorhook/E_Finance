package com.example.e_finance.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.e_finance.R;
import com.example.e_finance.adapter.TypeAdpter;
import com.example.e_finance.entity.Typeitem;

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