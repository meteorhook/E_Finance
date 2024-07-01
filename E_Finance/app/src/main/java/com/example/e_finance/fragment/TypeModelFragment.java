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
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class TypeModelFragment extends Fragment {
    private String[] types,color,image,imgselect,tid;
    private String ledgerid;
    private Boolean state=true;
    private List<Typeitem> list = new ArrayList<>();
    private TypeManageAdapter typeAdpter;
    private RecyclerView modeltype;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_type_model, container, false);

        modeltype =view.findViewById(R.id.modeltype);
        typeAdpter = new TypeManageAdapter(list);
        typeAdpter.setFragment(this);
        typeAdpter.setLedgerid(ledgerid);
        typeAdpter.setState(state);
        typeAdpter.setIsmodel(true);

        GridLayoutManager gridLayout=new GridLayoutManager(getActivity(),5);
        modeltype.setLayoutManager(gridLayout);
        modeltype.setAdapter(typeAdpter);

        return view;
    }
    public void update(String[] Types,String[] Image,String[] Imgselect,String[] Color,Boolean t,String id,String[] typeid){
        state=t;
        tid=typeid;
        ledgerid=id;
        types=Types;
        image=Image;
        imgselect=Imgselect;
        color=Color;

        list.clear();
        for (int i=0;i<types.length;i++){
            Typeitem typeitem=new Typeitem(types[i],color[i],image[i],imgselect[i],tid[i],null);
            list.add(typeitem);
        }
        if (typeAdpter!=null){
            typeAdpter.setState(state);
            typeAdpter.notifyDataSetChanged();
        }



    }
}