package com.example.e_finance;


import static java.lang.System.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.icu.text.CaseMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQToken;
import com.tencent.connect.common.Constants;
import com.tencent.stat.StatConfig;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.leancloud.LCFile;
import cn.leancloud.LCLogger;
import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import cn.leancloud.LeanCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


public class Login extends AppCompatActivity {
    private Button QQ_login, Register, Login;
    private EditText PersonName, Password;
    private static final String APP_ID = "102070940";//官方获取的APPID
    private Tencent mTencent;
    private String name, url;
    private MyUiListener myUiListener;
    private ImageView pgbar;
    private AnimationDrawable ad;
    private CheckBox protocol;
    private BottomSheetDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(Login.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_login);

        Tencent.setIsPermissionGranted(true);
        //传入参数APPID和全局Context上下文
        mTencent = Tencent.createInstance(APP_ID, Login.this.getApplicationContext());

        QQ_login = findViewById(R.id.QQ_login);
        Register = findViewById(R.id.Register);
        Login = findViewById(R.id.Login);
        PersonName = findViewById(R.id.PersonName);
        Password = findViewById(R.id.Password);
        protocol=findViewById(R.id.protocol);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        if (getIntent().getBooleanExtra("userlogout",false)){
            protocol.setChecked(true);
        }

        protocol.setOnClickListener(new View.OnClickListener() {
            private ScrollView scrollView;
            @Override
            public void onClick(View v) {
                protocol.setChecked(false);
                if (dialog==null){
                    dialog=new BottomSheetDialog(Login.this, R.style.BottomSheetEdit);//要加上这个，否则键盘会覆盖一部分弹窗。
                    View view = LayoutInflater.from(Login.this).inflate(R.layout.policy_bottomsheetdialog, null);
                    dialog.setContentView(view);
                    dialog.setCanceledOnTouchOutside(false);
                    scrollView=view.findViewById(R.id.scrollView);
                    ConstraintLayout protocolaction=view.findViewById(R.id.protocolaction);
                    TextView title=view.findViewById(R.id.title);
                    TextView Negative=view.findViewById(R.id.Negative);
                    TextView Positive=view.findViewById(R.id.Positive);
                    TextView message=view.findViewById(R.id.message);
                    protocolaction.setVisibility(View.VISIBLE);
                    message.setText(getResources().getString(R.string.protocol));
                    title.setText("用户协议");
                    scrollView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (scrollView.getScrollY()==0) {      //canScrollVertically(-1)的值表示是否能向下滚动，false表示已经滚动到顶部
                                scrollView.requestDisallowInterceptTouchEvent(false);
                            }else{
                                scrollView.requestDisallowInterceptTouchEvent(true);
                            }
                            return false;
                        }
                    });
                    Negative.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            protocol.setChecked(false);
                        }
                    });
                    Positive.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            protocol.setChecked(true);
                        }
                    });
                    dialog.show();
                }else {
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            // smoothScrollTo 方法接受两个参数：x轴和y轴的位置
                            // 要滚动到顶部，y轴位置应该是0
                            scrollView.smoothScrollTo(0, 0);
                        }
                    });
                    dialog.show();
                }
            }
        });

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String personName = PersonName.getText().toString();
                String passWord = Password.getText().toString();
                if (personName.isEmpty()) {
                    Toast.makeText(Login.this, "请输入账号", Toast.LENGTH_SHORT).show();
                } else if (passWord.isEmpty()) {
                    Toast.makeText(Login.this, "请输入密码", Toast.LENGTH_SHORT).show();
                } else if (!protocol.isChecked()){
                    Toast.makeText(Login.this, "请同意用户协议", Toast.LENGTH_SHORT).show();
                } else if (personName.contains("@")){
                    LCUser.loginByEmail(personName, passWord).subscribe(new Observer<LCUser>() {
                        public void onSubscribe(Disposable disposable) {
                            pgbar.setVisibility(View.VISIBLE);
                        }
                        public void onNext(LCUser user) {
                            // 登录成功
                            pgbar.setVisibility(View.GONE);
                            SaveEquipment(user,Login.this);
                            Toast.makeText(Login.this, "登陆成功，快去记账吧", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, Finance.class);
                            startActivity(intent);
                            finish();
                        }
                        public void onError(Throwable throwable) {
                            // 登录失败（可能是密码错误）
                            pgbar.setVisibility(View.GONE);
                            Toast.makeText(Login.this, "登陆失败," + throwable.getMessage()+"，请重试", Toast.LENGTH_SHORT).show();
                        }
                        public void onComplete() {}
                    });
                }else {
                    LCUser.logIn(personName, passWord).subscribe(new Observer<LCUser>() {
                        public void onSubscribe(Disposable disposable) {
                            pgbar.setVisibility(View.VISIBLE);
                        }
                        public void onNext(LCUser user) {
                            pgbar.setVisibility(View.GONE);
                            SaveEquipment(user,Login.this);
                            Toast.makeText(Login.this, "登陆成功，快去记账吧", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, Finance.class);
                            startActivity(intent);
                            finish();
                        }
                        public void onError(Throwable throwable) {
                            pgbar.setVisibility(View.GONE);
                            Toast.makeText(Login.this, "登陆失败," + throwable.getMessage()+"，请重试", Toast.LENGTH_SHORT).show();
                        }
                        public void onComplete() {}
                    });
                }
            }
        });

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (protocol.isChecked()){
                    Intent intent = new Intent(Login.this, Register.class);
                    startActivity(intent);
                }else {
                    Toast.makeText(Login.this,"请同意用户协议",Toast.LENGTH_SHORT).show();
                }

            }
        });

        QQ_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mTencent.isSessionValid()) {
                    if (!protocol.isChecked()){
                        Toast.makeText(Login.this, "请同意用户协议", Toast.LENGTH_SHORT).show();
                    }else {
                        QQlogin();
                    }
                } else {
                    Toast.makeText(Login.this, "已登录", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void QQlogin() {
        myUiListener = new MyUiListener();
        //all表示获取所有权限
        mTencent.login(Login.this, "all", myUiListener);
    }

    private class MyUiListener implements IUiListener {
        @Override
        public void onComplete(Object response) {
            try {
                JSONObject login = (JSONObject) response;
                //登录成功返回的信息
                String openID = login.getString("openid");
                String accessToken = login.getString("access_token");
                String expires = login.getString("expires_in");
                //登录成功后，设置openid、访问令牌accessToken信息，用于获取用户信息
                mTencent.setOpenId(openID);
                mTencent.setAccessToken(accessToken, expires);
//                QQToken qqToken = mTencent.getQQToken();
                getUserInfo();
                pgbar.setVisibility(View.VISIBLE);
            } catch (Exception e) {
            }
        }

        @Override
        public void onError(UiError uiError) {
            Toast.makeText(Login.this, "授权失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(Login.this, "授权取消", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onWarning(int i) {
        }
    }

    public void getUserInfo() throws SocketException {
//        异步获取QQ用户信息，用回调监听
        UserInfo userinfo = new UserInfo(Login.this, mTencent.getQQToken());
        userinfo.getUserInfo(new UserInfoListener());
    }

    private class UserInfoListener implements IUiListener {
        @Override
        public void onCancel() {}
        @Override
        public void onWarning(int i) {
        }

        @Override
        public void onComplete(Object obj) {
            JSONObject obj2 = (JSONObject) obj;
            try {
                name = obj2.getString("nickname");
                url = obj2.getString("figureurl_qq_2");
                Map<String, Object> thirdPartyData = new HashMap<String, Object>();

                thirdPartyData.put("expires_in", mTencent.getExpiresIn());
                thirdPartyData.put("openid", mTencent.getOpenId());
                thirdPartyData.put("access_token", mTencent.getAccessToken());

                LCUser.loginWithAuthData(thirdPartyData, "QQ").subscribe(new Observer<LCUser>() {
                    public void onSubscribe(Disposable disposable) {
                        pgbar.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onNext(LCUser user) {
                        SaveEquipment(user,Login.this);
                        if (user.getString("photo")==null){
                            user.put("photo", url);
                            LCFile file = new LCFile(
                                    user.getObjectId()+".png",
                                    url,
                                    new HashMap<String, Object>()
                            );
                            file.saveInBackground().subscribe(new Observer<LCFile>() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                }
                                @Override
                                public void onNext(LCFile lcFile) {
                                }
                                @Override
                                public void onError(Throwable e) {
                                }
                                @Override
                                public void onComplete() {
                                }
                            });
                        }
                        if (user.getString("nickName")==null){
                            user.put("nickName", name);
                        }
                        user.saveInBackground().subscribe(new Observer<LCObject>() {
                            @Override
                            public void onSubscribe(Disposable d) {}
                            @Override
                            public void onNext(LCObject lcObject) {
                                mTencent.logout(Login.this);
                                pgbar.setVisibility(View.GONE);

                                LCQuery<LCObject> query = new LCQuery<>("Eledger");
                                query.whereEqualTo("Euser", user);
                                query.findInBackground().subscribe(new Observer<List<LCObject>>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {}

                                    @Override
                                    public void onNext(List<LCObject> lcObjects) {
                                        if (lcObjects.size()==0){
                                            CreateNewLedger(user);
                                        }else {
                                            Toast.makeText(Login.this, "登录成功", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(Login.this, Finance.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {}

                                    @Override
                                    public void onComplete() {}
                                });
                            }

                            @Override
                            public void onError(Throwable e) {
                            }

                            @Override
                            public void onComplete() {
                                pgbar.setVisibility(View.GONE);
                            }
                        });

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(Login.this, "QQ登录失败" + e.getMessage()+"，请重试", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onComplete() {
                        pgbar.setVisibility(View.GONE);
                    }
                });

            } catch (Exception e) {
                Toast.makeText(Login.this,"出错了，"+e.getMessage()+"，请重试",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(UiError arg0) {
            // TODO Auto-generated method stub
        }

    }

    public static void SaveEquipment(LCUser user,Context context){
        LCQuery query=new LCQuery("EuserAndequipment");
        query.whereEqualTo("Euser",user);
        query.getFirstInBackground().subscribe(new Observer<LCObject>() {
            private boolean isin=false;
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(LCObject lcObject) {
                isin=true;
                lcObject.put("EandroidId", MainActivity.getAndroidId(context));
                lcObject.saveInBackground().subscribe(new Observer<LCObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {}
                    @Override
                    public void onNext(LCObject lcObject) {}
                    @Override
                    public void onError(Throwable e) {}
                    @Override
                    public void onComplete() {}
                });
            }
            @Override
            public void onError(Throwable e) {}
            @Override
            public void onComplete() {
                if (!isin){
                    LCObject saveinfo=new LCObject("EuserAndequipment");
                    saveinfo.put("Euser",user);
                    saveinfo.put("EandroidId",MainActivity.getAndroidId(context));
                    saveinfo.saveInBackground().subscribe(new Observer<LCObject>() {
                        @Override
                        public void onSubscribe(Disposable d) {}
                        @Override
                        public void onNext(LCObject lcObject) {}
                        @Override
                        public void onError(Throwable e) {}
                        @Override
                        public void onComplete() {}
                    });
                }
            }
        });
    }

    private void CreateNewLedger(LCUser user){
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
                Toast.makeText(Login.this, "登录成功", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Login.this, Finance.class);
                startActivity(intent);
                finish();
            }
            public void onError(Throwable throwable) {
                CreateNewLedger(user);
            }
            public void onComplete() {

            }
        });
    }

    /**
     * 在调用Login的Activity或者Fragment中重写onActivityResult方法
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_LOGIN) {
            Tencent.onActivityResultData(requestCode, resultCode, data, myUiListener);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}