package com.example.e_finance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.e_finance.fragment.TypeManageFragment;
import com.example.e_finance.fragment.TypeModelFragment;
import com.example.e_finance.util.StatusBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class TypeManage extends AppCompatActivity {
    private TypeManageFragment typeManageFragment = new TypeManageFragment();
    private TypeModelFragment typeModelFragment = new TypeModelFragment();
    private TextView typeA, typeR,tv3,tv4;
    private ConstraintLayout userfragment,modelfragment;
    private Boolean move = true,t=false;
    private String[] model;
    private String ledgerid;
    private Float x = 0f;
    private Button customTypeAdd;
    private String[] modelA = {"6544b7c544fa007c597ca6a2", "6544b80644fa007c597ca6a7", "6544b83842d451506a168f39",
            "6544b87f42d451506a168f43", "6544b8ad42d451506a168f52", "6544f67044fa007c597cb58a",
            "6544f6b744fa007c597cb597", "6544f70744fa007c597cb5b2", "6544f7a844fa007c597cb5e1",
            "6544f7d542d451506a169dd3", "6544f83f44fa007c597cb606", "6544f88044fa007c597cb617",
            "6544fd6344fa007c597cb756", "6544fdd844fa007c597cb771", "6544fe1642d451506a169f87",
            "6544fe4744fa007c597cb78b", "6544fe7e44fa007c597cb798", "6544ff0444fa007c597cb7b6"};
    private String[] modelR = {"6544b8d444fa007c597ca6e3", "6544b90144fa007c597ca6e7", "6544f5eb42d451506a169d73",
            "6544f62942d451506a169d7e", "6544fd9942d451506a169f6f", "6544fec644fa007c597cb7aa",
            "6545000342d451506a16a00b", "6545012142d451506a16a061", "6545017344fa007c597cb874"};

    private ImageView pgbar;
    private AnimationDrawable ad;
    private Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    initView(ledgerid, true);
                    break;
                case 1:
                    initView(ledgerid, false);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(TypeManage.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_type_manage);

        typeA = findViewById(R.id.typeA);
        typeR = findViewById(R.id.typeR);
        tv3=findViewById(R.id.tv3);
        tv4=findViewById(R.id.tv4);
        customTypeAdd=findViewById(R.id.customTypeAdd);
        userfragment = findViewById(R.id.userfragment);
        modelfragment =findViewById(R.id.modelfragment);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        tv3.setVisibility(View.GONE);
        tv4.setVisibility(View.GONE);

        Intent intent = TypeManage.this.getIntent();
        ledgerid = intent.getStringExtra("ledgerid");
        t = intent.getBooleanExtra("state",true);

        customTypeAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(TypeManage.this, TypeEdit.class);
                intent.putExtra("iscustom",true);
                intent.putExtra("ledgerid",ledgerid);
                intent.putExtra("tstate",move);
                startActivity(intent);
            }
        });

        View view = findViewById(R.id.symbol);

        typeA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv3.setVisibility(View.GONE);
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = Message.obtain();
                            msg.what = 0;
                            handler.sendMessage(msg);
                        }
                    });
                    thread.start();

                if (!move) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            x = typeR.getX() - typeA.getX();
                            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "alpha", 1, 0.75f, 0.5f, 0, 1);
                            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view, "translationX", x, 0);
                            AnimatorSet animatorSet = new AnimatorSet();
                            animatorSet.play(objectAnimator1)
                                    .with(objectAnimator2);
                            animatorSet.setDuration(700);
                            animatorSet.start();
                            move = true;
                        }
                    });
                }
            }
        });

        typeR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv3.setVisibility(View.GONE);
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = Message.obtain();
                            msg.what = 1;
                            handler.sendMessage(msg);
                        }
                    });
                    thread.start();

                if (move) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            x = typeR.getX() - typeA.getX();
                            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(view, "alpha", 1, 0.75f, 0.5f, 0, 1);
                            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(view, "translationX", 0, x);
                            AnimatorSet animatorSet = new AnimatorSet();
                            animatorSet.play(objectAnimator1)
                                    .with(objectAnimator2);
                            animatorSet.setDuration(700);
                            animatorSet.start();
                            move = false;
                        }
                    });
                }
            }
        });

        if (t){
            typeA.callOnClick();
        }else {
            typeR.callOnClick();
        }

    }

    //查询当前账本所拥有的分类并传递数据给fragment
    private void initView(String Lid, Boolean Tstate) {
        LCQuery<LCObject> query = new LCQuery<>("Eledger");
        query.getInBackground(Lid).subscribe(new Observer<LCObject>() {
            public void onSubscribe(Disposable disposable) {
                pgbar.setVisibility(View.VISIBLE);
            }
            public void onNext(LCObject ledger) {
                String[] type = ledger.getJSONArray("Ltype").toArray(new String[0]);

                LCQuery<LCObject> typeinfo = new LCQuery<>("Etype");
                typeinfo.whereContainedIn("objectId", Arrays.asList(type));
                typeinfo.whereEqualTo("Tstate", Tstate);
                typeinfo.findInBackground().subscribe(new Observer<List<LCObject>>() {
                    public void onSubscribe(Disposable disposable) {
                    }

                    public void onNext(List<LCObject> back) {
                        pgbar.setVisibility(View.GONE);
                        if (back.size()==0){
                            tv3.setVisibility(View.VISIBLE);
                        }
                        String[] typeName = new String[back.size()];
                        String[] typeColor = new String[back.size()];
                        String[] typeImg = new String[back.size()];
                        String[] typeImgSelect = new String[back.size()];
                        String[] id=new String[back.size()];

                        for (int i = 0; i < back.size(); i++) {
                            LCObject type = back.get(i);
                            typeName[i] = type.getString("TypeName");
                            typeColor[i] = type.getString("TypeColor");
                            typeImg[i] = type.getString("TypeImg");
                            typeImgSelect[i] = type.getString("TypeImgSelect");
                            id[i]=type.getObjectId();
                        }

                        List<String> listA = new ArrayList<>(Arrays.asList(id));
                        if (Tstate) {
                            List<String> listB = new ArrayList<>(Arrays.asList(modelA));
                            listB.removeAll(listA);
                            model = null;
                            model = listB.toArray(new String[0]);
                        } else {
                            List<String> listB = new ArrayList<>(Arrays.asList(modelR));
                            listB.removeAll(listA);
                            model = null;
                            model = listB.toArray(new String[0]);
                        }
                        modelType(model, Tstate);

                        getSupportFragmentManager().beginTransaction().replace(R.id.userfragment, typeManageFragment).commit();
                        typeManageFragment.update(typeName, typeImg, typeImgSelect, typeColor,Tstate,id,Lid);
                    }

                    public void onError(Throwable throwable) {
                    }

                    public void onComplete() {
                    }
                });

            }

            public void onError(Throwable throwable) {
            }

            public void onComplete() {
                pgbar.setVisibility(View.GONE);
            }
        });
    }

    //查询当前所有未使用的模板分类并传递数据给fragment
    private void modelType(String[] model, Boolean Tstate) {
        if (model.length==0){
            tv4.setVisibility(View.VISIBLE);
        }else {
            tv4.setVisibility(View.GONE);
        }
        LCQuery<LCObject> typeinfo = new LCQuery<>("Etype");
        typeinfo.whereContainedIn("objectId", Arrays.asList(model));
        typeinfo.whereEqualTo("Tstate", Tstate);
        typeinfo.findInBackground().subscribe(new Observer<List<LCObject>>() {
            public void onSubscribe(Disposable disposable) {
                pgbar.setVisibility(View.VISIBLE);
            }

            public void onNext(List<LCObject> back) {
                pgbar.setVisibility(View.GONE);
                String[] typeName = new String[back.size()];
                String[] typeColor = new String[back.size()];
                String[] typeImg = new String[back.size()];
                String[] typeImgSelect = new String[back.size()];
                String[] typeId = new String[back.size()];

                for (int i = 0; i < back.size(); i++) {
                    LCObject type = back.get(i);
                    typeName[i] = type.getString("TypeName");
                    typeColor[i] = type.getString("TypeColor");
                    typeImg[i] = type.getString("TypeImg");
                    typeImgSelect[i] = type.getString("TypeImgSelect");
                    typeId[i]=type.getObjectId();
                }

                getSupportFragmentManager().beginTransaction().replace(R.id.modelfragment, typeModelFragment).commit();
                typeModelFragment.update(typeName, typeImg, typeImgSelect, typeColor,Tstate,ledgerid,typeId);
            }

            public void onError(Throwable throwable) {
            }

            public void onComplete() {
                pgbar.setVisibility(View.GONE);
            }
        });
    }

    public void Update(Boolean st){
        if (st){
            typeA.callOnClick();
        }else {
            typeR.callOnClick();
        }
    }
    public void showPgbar(){
        if (pgbar!=null){
            pgbar.setVisibility(View.VISIBLE);
        }
    }

    //启动时刷新数据
    @Override
    public void onRestart() {
        super.onRestart();
        if (move){
            typeA.callOnClick();
        }else {
            typeR.callOnClick();
        }
    }

}