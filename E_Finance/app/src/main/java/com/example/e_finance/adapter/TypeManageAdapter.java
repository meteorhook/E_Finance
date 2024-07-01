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
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.e_finance.R;
import com.example.e_finance.TypeEdit;
import com.example.e_finance.TypeManage;
import com.example.e_finance.entity.Typeitem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class TypeManageAdapter extends RecyclerView.Adapter<TypeManageAdapter.ViewHolder>{
    private Fragment fragment;
    private List<Typeitem> type;
    private Boolean state=true;
    private String ledgerid;
    private Boolean ismodel=false;
    public TypeManageAdapter(List<Typeitem> type) {
        this.type = type;
    }

    public void setIsmodel(Boolean ismodel) {
        this.ismodel = ismodel;
    }
    public void setState(Boolean state){
        this.state=state;
    }

    public void setLedgerid(String ledgerid) {
        this.ledgerid = ledgerid;
    }

    public void setFragment(Fragment fragment){
        this.fragment=fragment;
    }
    @NonNull
    @Override
    public TypeManageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.type, parent, false);
        view.setMinimumHeight(view.getWidth());
        TypeManageAdapter.ViewHolder viewHolder = new TypeManageAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TypeManageAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Typeitem typeitem = type.get(position);
        holder.imageView.setBackground(null);
        Glide.with(fragment.getActivity()).load(typeitem.getimageIdselect()).into(holder.imageView);
        holder.typebg.getBackground().setColorFilter(Color.parseColor(typeitem.getColor()), PorterDuff.Mode.SRC_IN);
        holder.textView.setText(typeitem.getType());

        if (!ismodel) {
            //点击跳转到分类编辑界面
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), TypeEdit.class);
                    intent.putExtra("typeid", typeitem.getTypeid());
                    intent.putExtra("tstate",state);
                    intent.putExtra("ledgerid", ledgerid);
                    v.getContext().startActivity(intent);
                }
            });
            //长按删除分类
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (getItemCount() == 1) {
                        Toast.makeText(fragment.getActivity(), "至少要有一个分类", Toast.LENGTH_SHORT).show();
                    } else {
                        LCQuery<LCObject> query = new LCQuery<>("Eledger");
                        query.getInBackground(ledgerid).subscribe(new Observer<LCObject>() {
                            public void onSubscribe(Disposable disposable) {
                                ((TypeManage) fragment.getActivity()).showPgbar();
                            }
                            public void onNext(LCObject ledger) {
                                String[] type = ledger.getJSONArray("Ltype").toArray(new String[0]);
                                List<String> listA = new ArrayList<>(Arrays.asList(type));
                                listA.remove(typeitem.getTypeid());
                                ledger.put("Ltype", listA.toArray(new String[0]));
                                ledger.saveInBackground().subscribe(new Observer<LCObject>() {
                                    public void onSubscribe(Disposable disposable) {}
                                    public void onNext(LCObject save) {
                                        Toast.makeText(fragment.getActivity(), "分类删除成功", Toast.LENGTH_SHORT).show();
                                        // 获取当前fragment的activity引用(TypeManage) getActivity()
                                        ((TypeManage) fragment.getActivity()).Update(state);
                                    }
                                    public void onError(Throwable throwable) {}
                                    public void onComplete() {}
                                });
                            }

                            public void onError(Throwable throwable) {}
                            public void onComplete() {}
                        });

                        LCQuery<LCObject> query2 = new LCQuery<>("Etype");
                        query2.getInBackground(typeitem.getTypeid()).subscribe(new Observer<LCObject>() {
                            @Override
                            public void onSubscribe(Disposable d) {}
                            @Override
                            public void onNext(LCObject lcObject) {
                                if (!lcObject.getBoolean("isModel")) {
                                    lcObject.deleteInBackground().subscribe(new Observer<LCNull>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {}
                                        @Override
                                        public void onNext(LCNull lcNull) {}
                                        @Override
                                        public void onError(Throwable e) {}
                                        @Override
                                        public void onComplete() {}
                                    });
                                }
                            }
                            @Override
                            public void onError(Throwable e) {}
                            @Override
                            public void onComplete() {}
                        });
                    }
                    return true;
                }
            });
        }
        else {
            //点击后添加模板分类至用户账本
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LCQuery<LCObject> query = new LCQuery<>("Eledger");
                    query.getInBackground(ledgerid).subscribe(new Observer<LCObject>() {
                        public void onSubscribe(Disposable disposable) {
                            ((TypeManage) fragment.getActivity()).showPgbar();
                        }
                        public void onNext(LCObject todo) {
                            String[] ltypes = todo.getJSONArray("Ltype").toArray(new String[0]);
                            List<String> list = new ArrayList<>(Arrays.asList(ltypes));
                            LCQuery<LCObject> typeinfo = new LCQuery<>("Etype");
                            typeinfo.whereContainedIn("objectId", list);
                            typeinfo.findInBackground().subscribe(new Observer<List<LCObject>>() {
                                @Override
                                public void onSubscribe(Disposable d) {}
                                @Override
                                public void onNext(List<LCObject> back) {
                                    Boolean isin=false;
                                    //查询当前账本所有分类中是否已有同名分类
                                    for (int i = 0; i < back.size();i++) {
                                        LCObject type = back.get(i);
                                        if (type.getString("TypeName").equals(typeitem.getType())){
                                            Toast.makeText(v.getContext(),"已存在相同名称的分类",Toast.LENGTH_SHORT).show();
                                            isin=true;
                                            break;
                                        }
                                    }
                                    //如果不存在则进行添加分类
                                    if (!isin){
                                        list.add(typeitem.getTypeid());
                                        todo.put("Ltype", list.toArray(new String[0]));
                                        todo.saveInBackground().subscribe(new Observer<LCObject>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {
                                            }
                                            @Override
                                            public void onNext(LCObject lcObject) {
                                                ((TypeManage) fragment.getActivity()).Update(state);
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                            }

                                            @Override
                                            public void onComplete() {
                                            }
                                        });
                                    }
                                }
                                @Override
                                public void onError(Throwable e) {}
                                @Override
                                public void onComplete() {}
                            });
                        }

                        public void onError(Throwable throwable) {
                        }

                        public void onComplete() {
                        }
                    });
                }
            });
        }







    }

    @Override
    public int getItemCount() {
        return type.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
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
}
