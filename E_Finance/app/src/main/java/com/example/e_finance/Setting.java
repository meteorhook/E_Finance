package com.example.e_finance;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cn.leancloud.LCUser;

public class Setting extends AppCompatActivity {
    private ConstraintLayout E_finance,AccountSetting,DataClear,Feedback;
    private Button Logout;
    private TextView cachedata;
    /**
     * 创建Handler
     * 接收缓存清理消息
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(Setting.this, "清理完成", Toast.LENGTH_SHORT).show();
                    try {
                        cachedata.setText(CacheDataManager.getTotalCacheSize(Setting.this));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        }
    };
    /**
     * 创建内部类，清除缓存
     */
    class clearCache implements Runnable {
        @Override
        public void run() {
            try {
                CacheDataManager.clearAllCache(Setting.this);

                Thread.sleep(1000);

                if (CacheDataManager.getTotalCacheSize(Setting.this).startsWith("0")) {

                    handler.sendEmptyMessage(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(Setting.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_setting);

        AccountSetting=findViewById(R.id.AccountSetting);
        E_finance=findViewById(R.id.E_finance);
        Logout=findViewById(R.id.Logout);
        DataClear=findViewById(R.id.Clear);
        Feedback=findViewById(R.id.Feedback);
        cachedata=findViewById(R.id.cachedata);

        try {
            cachedata.setText(CacheDataManager.getTotalCacheSize(Setting.this));
        } catch (Exception e) {
            e.printStackTrace();
        }

        AccountSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Setting.this, Account_Setting.class);
                startActivity(intent);
            }
        });

        DataClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAlertDialog dialog=new myAlertDialog(Setting.this,false);
                dialog.create();
                dialog.setTitle("清除数据");
                dialog.setMessage("确定清除E家理财的全部缓存数据吗？");
                dialog.setNegative("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.setPositive("清除", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread(new clearCache()).start();
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        Feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAlertDialog dialog=new myAlertDialog(Setting.this,false);
                dialog.create();
                dialog.setTitle("意见反馈");
                dialog.setMessage("请添加E家理财官方意见反馈QQ群：808248669。\n联系群主或管理员进行意见反馈。");
                dialog.setPositive("前往加群", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String key="poVrHVhaj78nnOopkix2McMPJrbkOAIL";
                        Intent intent = new Intent();
                        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D" + key));
                        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
                        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            // 未安装手Q或安装的版本不支持
                            Toast.makeText(Setting.this,"请先安装QQ",Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });
                dialog.setNegative("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        E_finance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Setting.this, E_Finance_Intro.class);
                startActivity(intent);
            }
        });

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LCUser.logOut();
                Intent intent = new Intent(Setting.this, Login.class);
                //下面2个flags ,可以将原有任务栈清空并将 intent 的目标 Activity 作为任务栈的根 Activity 。
                //任务栈的 Id 没有变，并没有开辟新的任务栈
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("userlogout",true);
                startActivity(intent);
            }
        });


    }
}