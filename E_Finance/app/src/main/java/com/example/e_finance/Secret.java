package com.example.e_finance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_finance.util.StatusBar;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class Secret extends AppCompatActivity {
    private View password1,password2,password3,password4;
    private TextView button0, button1, button2, button3, button4, button5,
            button6, button7, button8, button9;
    private ConstraintLayout back, logout;
    private String password="",right,psw="",open;
    private Boolean iscorrect=false,isSet=false,again=false;
    private TextView title,prompt;
    private long mPressedTime = 0; // 用于记录返回键按下时间
    private View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv0:
                    password+="0";
                    showPasswordCount(password.length());
                    break;
                case R.id.tv1:
                    password+="1";
                    showPasswordCount(password.length());
                    break;
                case R.id.tv2:
                    password+="2";
                    showPasswordCount(password.length());
                    break;
                case R.id.tv3:
                    password+="3";
                    showPasswordCount(password.length());
                    break;
                case R.id.tv4:
                    password+="4";
                    showPasswordCount(password.length());
                    break;
                case R.id.tv5:
                    password+="5";
                    showPasswordCount(password.length());
                    break;
                case R.id.tv6:
                    password+="6";
                    showPasswordCount(password.length());
                    break;
                case R.id.tv7:
                    password+="7";
                    showPasswordCount(password.length());
                    break;
                case R.id.tv8:
                    password+="8";
                    showPasswordCount(password.length());
                    break;
                case R.id.tv9:
                    password+="9";
                    showPasswordCount(password.length());
                    break;
                case R.id.back:
                    if (password.length()>1){
                        password=password.substring(0,password.length()-1);
                        showPasswordCount(password.length());
                    }else {
                        password="";
                        showPasswordCount(0);
                    }
                    break;
                case R.id.logout:
                    LCUser.logOut();
                    Intent intent = new Intent(Secret.this, Login.class);
                    //下面2个flags ,可以将原有任务栈清空并将 intent 的目标 Activity 作为任务栈的根 Activity 。
                    //任务栈的 Id 没有变，并没有开辟新的任务栈
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(Secret.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_secret);

        initview();
    }
    private void initview(){
        title=findViewById(R.id.title);
        prompt=findViewById(R.id.prompt);

        password1=findViewById(R.id.v1);
        password2=findViewById(R.id.v2);
        password3=findViewById(R.id.v3);
        password4=findViewById(R.id.v4);

        button0 = findViewById(R.id.tv0);
        button1 = findViewById(R.id.tv1);
        button2 = findViewById(R.id.tv2);
        button3 = findViewById(R.id.tv3);
        button4 = findViewById(R.id.tv4);
        button5 = findViewById(R.id.tv5);
        button6 = findViewById(R.id.tv6);
        button7 = findViewById(R.id.tv7);
        button8 = findViewById(R.id.tv8);
        button9 = findViewById(R.id.tv9);
        back = findViewById(R.id.back);
        logout = findViewById(R.id.logout);

        button0.setOnClickListener(listener);
        button1.setOnClickListener(listener);
        button2.setOnClickListener(listener);
        button3.setOnClickListener(listener);
        button4.setOnClickListener(listener);
        button5.setOnClickListener(listener);
        button6.setOnClickListener(listener);
        button7.setOnClickListener(listener);
        button8.setOnClickListener(listener);
        button9.setOnClickListener(listener);
        back.setOnClickListener(listener);
        logout.setOnClickListener(listener);

        showPasswordCount(0);

        Intent intent=getIntent();
        String get=intent.getStringExtra("setPsw");
        if (get!=null){
            title.setVisibility(View.VISIBLE);
            prompt.setVisibility(View.VISIBLE);
            isSet=true;
            iscorrect=true;
        }
        open=intent.getStringExtra("open");
    }
    private void showPasswordCount(int Count){
        switch (Count){
            case 0:
                password1.getBackground().setColorFilter(Color.parseColor("#CCF5F2F2"), PorterDuff.Mode.SRC_IN);
                password2.getBackground().setColorFilter(Color.parseColor("#CCF5F2F2"), PorterDuff.Mode.SRC_IN);
                password3.getBackground().setColorFilter(Color.parseColor("#CCF5F2F2"), PorterDuff.Mode.SRC_IN);
                password4.getBackground().setColorFilter(Color.parseColor("#CCF5F2F2"), PorterDuff.Mode.SRC_IN);
                break;
            case 1:
                password1.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                password2.getBackground().setColorFilter(Color.parseColor("#CCF5F2F2"), PorterDuff.Mode.SRC_IN);
                password3.getBackground().setColorFilter(Color.parseColor("#CCF5F2F2"), PorterDuff.Mode.SRC_IN);
                password4.getBackground().setColorFilter(Color.parseColor("#CCF5F2F2"), PorterDuff.Mode.SRC_IN);
                break;
            case 2:
                password1.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                password2.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                password3.getBackground().setColorFilter(Color.parseColor("#CCF5F2F2"), PorterDuff.Mode.SRC_IN);
                password4.getBackground().setColorFilter(Color.parseColor("#CCF5F2F2"), PorterDuff.Mode.SRC_IN);
                break;
            case 3:
                password1.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                password2.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                password3.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                password4.getBackground().setColorFilter(Color.parseColor("#CCF5F2F2"), PorterDuff.Mode.SRC_IN);
                break;
            case 4:
                password1.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                password2.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                password3.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                password4.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

                if (isSet){
                    if (!again){
                        prompt.setText("请再次输入");
                        psw=password;
                        password="";
                        showPasswordCount(0);
                        again=true;
                    }else {
                        if (psw.equals(password)){
                            LCUser user=LCUser.getCurrentUser();
                            LCObject todo = new LCObject("Esecret");
                            todo.put("Suser", user);
                            todo.put("Spsw", password);
                            todo.saveInBackground().subscribe(new Observer<LCObject>() {
                                public void onSubscribe(Disposable disposable) {}
                                public void onNext(LCObject todo) {
                                    // 成功保存之后，执行其他逻辑
                                    finish();
                                }
                                public void onError(Throwable throwable) {
                                    // 异常处理
                                }
                                public void onComplete() {}
                            });
                        }else {
                            psw="";
                            password="";
                            prompt.setText("请设置您的密码");
                            showPasswordCount(0);
                            Toast.makeText(Secret.this,"两次密码不一致，请重新输入",Toast.LENGTH_SHORT).show();
                        }
                    }
                }else {
                    if (right != null) {
                        if (password.equals(right)) {
                            if (open!=null){
                                Intent intent = new Intent(Secret.this, Finance.class);
                                startActivity(intent);
                            }
                            finish();
                        } else {
                            password = "";
                            showPasswordCount(0);
                            Toast.makeText(Secret.this, "密码错误，请重新输入", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        LCUser user = LCUser.getCurrentUser();
                        LCQuery<LCObject> query = new LCQuery<>("Esecret");
                        query.whereEqualTo("Suser", user);
                        query.getFirstInBackground().subscribe(new Observer<LCObject>() {
                            @Override
                            public void onSubscribe(Disposable d) {}

                            @Override
                            public void onNext(LCObject lcObject) {
                                right = lcObject.getString("Spsw");
                                if (password.equals(right)) {
                                    if (open!=null){
                                        Intent intent = new Intent(Secret.this, Finance.class);
                                        startActivity(intent);
                                    }
                                    finish();
                                } else {
                                    password = "";
                                    showPasswordCount(0);
                                    Toast.makeText(Secret.this, "密码错误，请重新输入", Toast.LENGTH_SHORT).show();
                                }
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
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (iscorrect){
            super.onBackPressed();
        }else {
            long mNowTime = System.currentTimeMillis(); // 获取当前时间
            if ((mNowTime - mPressedTime) > 2000) {
                // 和前一次按返回键时间差大于2000ms，给出提示并记录这次按键时间
                Toast.makeText(this, "再按一次返回键退出E家理财", Toast.LENGTH_SHORT).show();
                mPressedTime = mNowTime;
            } else {
                // 和前一次按返回键时间差小于等于2000ms，退出应用程序
                moveTaskToBack(true);//将Activity退到后台，注意不是finish()退出。
                this.finish(); // 关闭activity
            }
        }
    }
}