package com.example.e_finance;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class TypeEdit extends AppCompatActivity {
    private TextView textView,namelength,tittle;
    private ImageView typeImg;
    private Button typeSave;
    private EditText typeName;
    private String color,typename,typeimg,typeimgselect,id;
    private Boolean tstate,callclick=false;
    private RecyclerView typecolor,alltypeicon;
    private List<String> allcolor=new ArrayList<>();
    private ColorAdapter colorAdapter;
    private TypeIconAdapter typeIconAdapter;
    private List<Icon> allicon=new ArrayList<>();
    private  Icon icon;
    private ImageView pgbar;
    private AnimationDrawable ad;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(TypeEdit.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_type_edit);

        typeName = findViewById(R.id.TypeName);
        typeImg=findViewById(R.id.TypeIcon);
        typeSave = findViewById(R.id.typeSave);
        textView = findViewById(R.id.textView);
        namelength=findViewById(R.id.namelength);
        tittle=findViewById(R.id.tittle);
        typecolor = findViewById(R.id.allcolor);
        alltypeicon =findViewById(R.id.alltypeicon);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        //初始化颜色和图标
        colorInit();
        iconInit();

        //为分类名称设置输入监听，实时显示当前字符数，最大为4
        typeName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                namelength.setText(s.length()+"/4");
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        //根据传递分类类型设置标题
        Intent intent=getIntent();
        tstate=intent.getBooleanExtra("tstate",true);
        if (tstate) {
            tittle.setText("支出分类");
        }else {
            tittle.setText("收入分类");
        }

        //保存当前分类
        typeSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //保存当前分类
                if (typeName.getText().toString().isEmpty()){
                    Toast.makeText(TypeEdit.this,"分类名称不能为空",Toast.LENGTH_SHORT).show();
                }else {
                    savetype();
                }
            }
        });

    }

    //初始化颜色选择recycleview
    private void colorInit(){
        LCQuery<LCObject> query = new LCQuery<>("Ecolor");
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<LCObject> ecolor) {
                for (int i=0;i<ecolor.size();i++){
                    LCObject object =ecolor.get(i);
                    allcolor.add(object.getString("Ecolor"));
                }
                colorAdapter=new ColorAdapter(allcolor);
                Intent intent=getIntent();
                //如果当前不是自定义分类，则执行查询，否则设置默认分类状态
                if (!intent.getBooleanExtra("iscustom",false)){
                    initView();
                }else {
                    LinearLayoutManager layoutManager = new LinearLayoutManager(TypeEdit.this);
                    layoutManager.setOrientation(RecyclerView.HORIZONTAL);
                    typecolor.setLayoutManager(layoutManager);
                    typecolor.addItemDecoration(new colorDecoration(25));
                    colorAdapter.setSelectedPosition(0);
                    typecolor.setAdapter(colorAdapter);
                }

               }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });
    }

    //初始化icon选择recycleview
    private void iconInit(){
        LCQuery<LCObject> query = new LCQuery<>("EbillTypeIcon");
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<LCObject> typeicons) {
                String[] typeIconSelect=new String[typeicons.size()];
                String[] typeIcon = new String[typeicons.size()];
                for (int i=0;i<typeicons.size();i++){
                    LCObject object = typeicons.get(i);
                    typeIcon[i]=object.getString("typeIcon");
                    typeIconSelect[i] = object.getString("typeIconSelect");
                    icon=new Icon(typeIcon[i],typeIconSelect[i]);
                    allicon.add(icon);
                }
                typeIconAdapter=new TypeIconAdapter(allicon);
                Intent intent=getIntent();
                //如果当前不是自定义分类，则执行查询，否则设置默认分类状态
                if (!intent.getBooleanExtra("iscustom",false)){
                    initView();
                }else {
                    GridLayoutManager gridLayout=new GridLayoutManager(TypeEdit.this,5);
                    alltypeicon.setLayoutManager(gridLayout);
                    typeIconAdapter.setSelectedPosition(0);
                    alltypeicon.setAdapter(typeIconAdapter);
                }
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });

    }

    public class Icon {
        private  String icon,iconselect;
        public Icon(String icon, String iconselect) {
            this.icon = icon;
            this.iconselect=iconselect;
        }
        public  String geticon() {
            return icon;
        }
        public  String geticonselect() {return iconselect;}
    }

    public void settypeImg(Icon icon){
        typeImg.setBackground(null);
        Glide.with(this)
                .load(icon.geticonselect().trim())
//                .listener(new RequestListener<Drawable>() {
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                        e.printStackTrace();
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                        return false;
//                    }
//                })
                .into(typeImg);
        typeimg=icon.geticon();
        typeimgselect=icon.geticonselect();
    }

    public void setbg(String bgcolor){
        textView.getBackground().setColorFilter(Color.parseColor(bgcolor), PorterDuff.Mode.SRC_IN);
        color=bgcolor;
    }

    //通过传递来的id查询该分类，并将其数据展示
    private void initView(){
        Intent intent=getIntent();
        id=intent.getStringExtra("typeid");
        LCQuery<LCObject> query = new LCQuery<>("Etype");
        query.whereEqualTo("objectId",id);
        query.getFirstInBackground().subscribe(new Observer<LCObject>() {
            @Override
            public void onSubscribe(Disposable d) {
                pgbar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onNext(LCObject lcObject) {
                color = lcObject.getString("TypeColor");
                typename = lcObject.getString("TypeName");
                typeimg = lcObject.getString("TypeImg");
                typeimgselect = lcObject.getString("TypeImgSelect");
                tstate=lcObject.getBoolean("Tstate");
                typeName.setText(typename);

                int selectview=allcolor.indexOf(color);
                int selecticon=-1;
                for (int i = 0; i < allicon.size(); i++) {
                    Icon icon = allicon.get(i);
                    if (icon.geticon().equals(typeimg)) {
                        selecticon = i; // 更新位置
                        break;
                    }
                }

                //设置颜色recycleview
                LinearLayoutManager layoutManager = new LinearLayoutManager(TypeEdit.this);
                layoutManager.setOrientation(RecyclerView.HORIZONTAL);
                typecolor.setLayoutManager(layoutManager);
                ((LinearLayoutManager)typecolor.getLayoutManager()).scrollToPositionWithOffset(selectview,0);
                typecolor.addItemDecoration(new colorDecoration(25));
                colorAdapter.setSelectedPosition(selectview);
                typecolor.setAdapter(colorAdapter);

                //设置图标recycleview
                GridLayoutManager gridLayout=new GridLayoutManager(TypeEdit.this,5);
                alltypeicon.setLayoutManager(gridLayout);
                ((LinearLayoutManager)alltypeicon.getLayoutManager()).scrollToPositionWithOffset(selecticon,0);
                typeIconAdapter.setSelectedPosition(selecticon);
                alltypeicon.setAdapter(typeIconAdapter);

            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(TypeEdit.this,"加载失败"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                pgbar.setVisibility(View.GONE);
            }
        });
    }

    //保存当前界面的type
    private void savetype(){
        Intent intent=getIntent();
        String ledgerid=intent.getStringExtra("ledgerid");
        LCQuery<LCObject> query = new LCQuery<>("Eledger");
        query.getInBackground(ledgerid).subscribe(new Observer<LCObject>() {
            @Override
            public void onSubscribe(Disposable d) {
                pgbar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onNext(LCObject lcObject) {
                String[] type= lcObject.getJSONArray("Ltype").toArray(new String[0]);
                List<String> listB = new ArrayList<>(Arrays.asList(type));
                LCQuery<LCObject> typeinfo = new LCQuery<>("Etype");
                typeinfo.whereContainedIn("objectId", Arrays.asList(type));
                typeinfo.findInBackground().subscribe(new Observer<List<LCObject>>() {
                    public void onSubscribe(Disposable disposable) {}
                    public void onNext(List<LCObject> back) {
                        //获取当前用户输入的分类
                        typename=typeName.getText().toString();
                        Boolean isin=false;
                        //查询当前账本所有分类中是否已有同名分类
                        for (int i = 0; i < back.size();i++) {
                            LCObject type = back.get(i);
                            if (type.getString("TypeName").equals(typename)){
                                //如果存在同名则判断用户是否修改过分类
                                String allinfo=type.getString("TypeColor")
                                        +type.getString("TypeImg")
                                        +type.getString("TypeImgSelect")
                                        +type.getBoolean("Tstate");
                                String userinfo=color+typeimg+typeimgselect+tstate;
                                if (allinfo.equals(userinfo)){
                                    Toast.makeText(TypeEdit.this,"保存成功",Toast.LENGTH_SHORT).show();
                                    finish();
                                }else {
                                    Toast.makeText(TypeEdit.this,"已存在相同名称的分类",Toast.LENGTH_SHORT).show();
                                }
                                isin=true;
                                break;
                            }
                        }
                        //如果不存在则进行保存
                        if (!isin){
                            //从账本中删除旧分类并将新分类数据添加到账本和分类表中
                            listB.remove(id);
                            LCObject edit = new LCObject("Etype");
                            edit.put("TypeColor", color);
                            edit.put("TypeName", typename);
                            edit.put("TypeImg", typeimg);
                            edit.put("TypeImgSelect", typeimgselect);
                            edit.put("Tstate",tstate);
                            edit.put("isModel", false);
                            edit.saveInBackground().subscribe(new Observer<LCObject>() {
                                public void onSubscribe(Disposable disposable) {}
                                public void onNext(LCObject save) {
                                    listB.add(save.getObjectId());
                                    String[] result = listB.toArray(new String[0]);
                                    LCObject todo = LCObject.createWithoutData("Eledger", ledgerid);
                                    todo.put("Ltype",result);
                                    todo.saveInBackground().subscribe(new Observer<LCObject>() {
                                        public void onSubscribe(Disposable disposable) {}
                                        public void onNext(LCObject savedTodo) {
                                            Toast.makeText(TypeEdit.this,"保存成功",Toast.LENGTH_SHORT).show();
                                            finish();
                                        }
                                        public void onError(Throwable throwable) {Toast.makeText(TypeEdit.this,"保存失败"+throwable.getMessage(),Toast.LENGTH_SHORT).show();}
                                        public void onComplete() {
                                        }
                                    });

                                }
                                public void onError(Throwable throwable) {
                                }
                                public void onComplete() {
                                }
                            });

                            //删除旧分类
                            LCQuery<LCObject> query2 = new LCQuery<>("Etype");
                            query2.getInBackground(id).subscribe(new Observer<LCObject>() {
                                @Override
                                public void onSubscribe(Disposable d) {}
                                @Override
                                public void onNext(LCObject lcObject) {
                                    if (!lcObject.getBoolean("isModel")){
                                        lcObject.deleteInBackground().subscribe(new Observer<LCNull>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {}
                                            @Override
                                            public void onNext(LCNull lcNull) {}
                                            @Override
                                            public void onError(Throwable e) {
                                                Toast.makeText(TypeEdit.this,"删除旧分类失败"+e.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
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
                    }
                    public void onError(Throwable throwable) {}
                    public void onComplete() {
                        pgbar.setVisibility(View.GONE);
                    }
                });

            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {
                pgbar.setVisibility(View.GONE);
            }
        });
    }

}