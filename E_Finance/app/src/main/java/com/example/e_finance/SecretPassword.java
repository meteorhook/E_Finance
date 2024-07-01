package com.example.e_finance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.e_finance.util.StatusBar;
import com.kyleduo.switchbutton.SwitchButton;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class SecretPassword extends AppCompatActivity {
    private SwitchButton usepassword;
    private ConstraintLayout constraintLayout;
    private LCObject getSecret;
    private ImageView pgbar;
    private boolean isfirst=true;
    private AnimationDrawable ad;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(SecretPassword.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_secret_password);


        constraintLayout=findViewById(R.id.layout);
        usepassword=findViewById(R.id.Secret_Switch);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        initview();
    }
    private void initview(){
        LCUser user=LCUser.getCurrentUser();
        LCQuery<LCObject> query = new LCQuery<>("Esecret");
        query.whereEqualTo("Suser", user);
        query.getFirstInBackground().subscribe(new Observer<LCObject>() {
            private boolean isin=false;
            @Override
            public void onSubscribe(Disposable d) {
                pgbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNext(LCObject lcObject) {
                getSecret=lcObject;
                isin=true;
            }

            @Override
            public void onError(Throwable e) {
                pgbar.setVisibility(View.GONE);
                Toast.makeText(SecretPassword.this,"信息加载失败,"+e.getMessage(),Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onComplete() {
                pgbar.setVisibility(View.GONE);
                if (isin){
                    usepassword.setChecked(true);
                }else {
                    usepassword.setChecked(false);
                }
               setchecklistener();
            }
        });

        constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usepassword.setChecked(!usepassword.isChecked());
            }
        });


    }

    private void setchecklistener(){
        usepassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    Intent intent =new Intent(SecretPassword.this, Secret.class);
                    intent.putExtra("setPsw","secret");
                    startActivity(intent);
                }else {
                    getSecret.deleteInBackground().subscribe(new Observer<LCNull>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            pgbar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onNext(LCNull lcNull) {
                            Toast.makeText(SecretPassword.this,"关闭成功",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Throwable e) {
                            pgbar.setVisibility(View.GONE);
                            Toast.makeText(SecretPassword.this,"关闭失败",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete() {
                            pgbar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (isfirst){
            isfirst=false;
        }else {
            LCUser user=LCUser.getCurrentUser();
            LCQuery<LCObject> query = new LCQuery<>("Esecret");
            query.whereEqualTo("Suser", user);
            query.getFirstInBackground().subscribe(new Observer<LCObject>() {
                private boolean isin=false;
                @Override
                public void onSubscribe(Disposable d) {
                    pgbar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onNext(LCObject lcObject) {
                    getSecret=lcObject;
                    isin=true;
                }

                @Override
                public void onError(Throwable e) {
                    pgbar.setVisibility(View.GONE);
                    Toast.makeText(SecretPassword.this,"信息加载失败,"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onComplete() {
                    pgbar.setVisibility(View.GONE);
                    usepassword.setOnCheckedChangeListener(null);
                    if (isin){
                        usepassword.setChecked(true);
                    }else {
                        usepassword.setChecked(false);
                    }
                    setchecklistener();
                }
            });
        }
    }

}