package com.example.e_finance;


import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import retrofit2.http.PUT;

public class Register extends AppCompatActivity {
    private ImageView Captchapic;
    private Button Register;
    private String code;
    private EditText PersonName,Password,Captcha;
    private ImageView pgbar;
    private AnimationDrawable ad;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(Register.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_register);

        Captchapic=findViewById(R.id.CaptchaPic);
        Captcha=findViewById(R.id.Captcha);
        PersonName=findViewById(R.id.PersonName);
        Password=findViewById(R.id.Password);
        Register=findViewById(R.id.Register);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        captcha();

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String personName=PersonName.getText().toString();
                String passWord=Password.getText().toString();
                code = FourCodeCaptcha.getInstance().getCode();
                if (personName.isEmpty()) {
                    Toast.makeText(Register.this, "请输入账号", Toast.LENGTH_SHORT).show();
                } else if (personName.length()!=8) {
                    Toast.makeText(Register.this, "请输入8位账号", Toast.LENGTH_SHORT).show();
                } else if (passWord.isEmpty()) {
                    Toast.makeText(Register.this, "请输入密码", Toast.LENGTH_SHORT).show();
                }else if (validate2(passWord)) {
                    Toast.makeText(Register.this, "用户密码不可含有中文", Toast.LENGTH_SHORT).show();
                } else if (passWord.length()<=5) {
                    Toast.makeText(Register.this, "密码不少于6位", Toast.LENGTH_SHORT).show();
                } else if (passWord.length()>=10) {
                    Toast.makeText(Register.this, "密码不大于9位", Toast.LENGTH_SHORT).show();
                } else if (Captcha.getText().toString().isEmpty()) {
                    Toast.makeText(Register.this, "请输入验证码", Toast.LENGTH_SHORT).show();
                } else if (!Captcha.getText().toString().equals(code)) {
                    Toast.makeText(Register.this, "验证码错误", Toast.LENGTH_SHORT).show();
                    Captchapic.setImageBitmap(FourCodeCaptcha.getInstance().createBitmap());
                    Captcha.setText("");
                } else {
                LCUser user = new LCUser();
                user.setUsername(PersonName.getText().toString());
                user.setPassword(Password.getText().toString());
                user.put("photo","http://lc-5r6UPI7d.cn-n1.lcfile.com/SlUVNVeXHvfGhV1oDechxTgIxLzfcOBH/photo.png");
                user.put("nickName", "理财用户");

                user.signUpInBackground().subscribe(new Observer<LCUser>() {
                    public void onSubscribe(Disposable disposable) {pgbar.setVisibility(View.VISIBLE);}
                    public void onNext(LCUser user) {
                        // 注册成功进行登录
                        LCUser.logIn(PersonName.getText().toString(),Password.getText().toString()).subscribe(new Observer<LCUser>() {
                            public void onSubscribe(Disposable disposable) {}
                            public void onNext(LCUser user) {
                                Login.SaveEquipment(user,Register.this);
                                LCObject ledger = new LCObject("Eledger");
                                String[] type={"6544b87f42d451506a168f43","6544b7c544fa007c597ca6a2","6544f6b744fa007c597cb597","6544b8d444fa007c597ca6e3","6544b90144fa007c597ca6e7"};
                                ledger.put("Euser", user);
                                ledger.put("Lname", "日常");
                                ledger.put("Ltype",type);
                                ledger.put("isUsed",true);
                                ledger.saveInBackground().subscribe(new Observer<LCObject>() {
                                    public void onSubscribe(Disposable disposable) {}
                                    public void onNext(LCObject todo) {
                                        pgbar.setVisibility(View.GONE);
                                        Toast.makeText(Register.this, "登录成功", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Register.this, Finance.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    public void onError(Throwable throwable) {}
                                    public void onComplete() {}
                                });
                            }

                            public void onError(Throwable throwable) {
                                pgbar.setVisibility(View.GONE);
                                Toast.makeText(Register.this, "登陆失败," + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            public void onComplete() {
                            }
                        });
                    }
                    public void onError(Throwable throwable) {
                        // 注册失败（通常是因为用户名已被使用）
                        pgbar.setVisibility(View.GONE);
                        Toast.makeText(Register.this, "注册失败," + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    public void onComplete() {}
                });
            }
            }
        });
    }

    //验证码初始化
    private void captcha(){
        //设置四字数字验证码
        Captchapic.setImageBitmap(FourCodeCaptcha.getInstance().createBitmap());
        Captchapic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //生成新的验证码
                code = FourCodeCaptcha.getInstance().getCode();
                Captchapic.setImageBitmap(FourCodeCaptcha.getInstance().createBitmap());
            }
        });
    }

    /**
     * 验证 是否 含有汉字
     *
     * @param text
     */
    private Boolean validate2(String text) {
        if (isChinese(text)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断一个字符串是否含有中文
     */
    public boolean isChinese(String str) {
        if (str == null)
            return false;
        for (char c : str.toCharArray()) {
            if (isChineseChar(c))
                return true;// 有一个中文字符就返回
        }
        return false;
    }

    /**
     * 判断一个字符是否是中文
     */
    public boolean isChineseChar(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;// 根据字节码判断
    }
}