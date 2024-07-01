package com.example.e_finance.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_finance.R;
import com.example.e_finance.adapter.TypeManageAdapter;
import com.example.e_finance.entity.Typeitem;

import java.util.ArrayList;
import java.util.List;

public class TypeManageFragment extends Fragment {
    private String[] types,color,image,imgselect,typeid;
    private Boolean state=true;
    private String ledgerid;
    private List<Typeitem> list = new ArrayList<>();
    private TypeManageAdapter typeAdpter;
    private RecyclerView usertype;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_type_manage, container, false);

        usertype =view.findViewById(R.id.usertype);
        typeAdpter = new TypeManageAdapter(list);
        typeAdpter.setFragment(this);
        typeAdpter.setLedgerid(ledgerid);
        typeAdpter.setState(state);
        typeAdpter.setIsmodel(false);

        GridLayoutManager gridLayout=new GridLayoutManager(getActivity(),5);
        usertype.setLayoutManager(gridLayout);
        usertype.setAdapter(typeAdpter);



        return view;
    }
    public void update(String[] Types,String[] Image,String[] Imgselect,String[] Color,Boolean t,String[] id,String ledgerid){
        this.ledgerid=ledgerid;
        typeid=id;
        state=t;
        types=Types;
        image=Image;
        imgselect=Imgselect;
        color=Color;

        list.clear();
        for (int i=0;i<types.length;i++){
            Typeitem typeitem=new Typeitem(types[i],color[i],image[i],imgselect[i],typeid[i],null);
            list.add(typeitem);
        }
        if (typeAdpter!=null){
            typeAdpter.setState(state);
            typeAdpter.notifyDataSetChanged();
        }




    }
}