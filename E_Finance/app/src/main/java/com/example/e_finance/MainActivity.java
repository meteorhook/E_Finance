package com.example.e_finance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import cn.leancloud.LeanCloud;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private String androidId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(MainActivity.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_main);

        //初始化leancloud
//        LeanCloud.setLogLevel(LCLogger.Level.DEBUG);
        LeanCloud.initialize(this, ,);

        androidId = getAndroidId(MainActivity.this);
        if (LCUser.getCurrentUser()!=null){
            LCUser user=LCUser.getCurrentUser();
            LCQuery<LCObject> query = new LCQuery<>("EuserAndequipment");
            query.whereEqualTo("Euser",user);
            query.getFirstInBackground().subscribe(new Observer<LCObject>() {
                @Override
                public void onSubscribe(Disposable d) {}
                @Override
                public void onNext(LCObject getUser) {
                    if (!androidId.equals(getUser.getString("EandroidId"))){
                        LCUser.logOut();
                        startActivity(new Intent(MainActivity.this, Login.class));
                        finish();
                    }else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(300); // 延迟0.3秒
                                    LCQuery<LCObject> query = new LCQuery<>("Esecret");
                                    query.whereEqualTo("Suser", user);
                                    query.getFirstInBackground().subscribe(new Observer<LCObject>() {
                                        private boolean isin=false;
                                        @Override
                                        public void onSubscribe(Disposable d) {}
                                        @Override
                                        public void onNext(LCObject lcObject) {
                                            isin=true;
                                        }
                                        @Override
                                        public void onError(Throwable e) {
                                            Log.e("用户隐私密码查询失败",e.getMessage());
                                        }
                                        @Override
                                        public void onComplete() {
                                            if (isin) {
                                                Intent intent = new Intent(MainActivity.this, Secret.class);
                                                intent.putExtra("open", "open");
                                                startActivity(intent);
                                                overridePendingTransition(0, 0);
                                                finish();
                                            } else {
                                                Intent intent=new Intent(MainActivity.this, Finance.class);
                                                startActivity(intent);
                                                overridePendingTransition(0, 0);
                                                finish();
                                            }
                                        }
                                    });
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
                @Override
                public void onError(Throwable e) {}
                @Override
                public void onComplete() {}
            });
        }
        else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(300); // 延迟0.3秒
                        startActivity(new Intent(MainActivity.this, Login.class));
                        overridePendingTransition(0,0);
                        finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }


    }
    public static String getAndroidId(Context context){
        String androidId = Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return androidId;
    }
}
