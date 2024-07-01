package com.example.e_finance;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class CustomApplication extends Application {
    /**
     * 当前Acitity个数
     */
    private int countActivity = 0;
    /**
     * 是否进入后台
     */
    private boolean isBackground = false;
    private boolean showSecret = false;
    private String androidId;

    @Override
    public void onCreate() {
        super.onCreate();
        //监听应用进入后台回到前台
        initBackgroundCallBack();
    }


    private void initBackgroundCallBack() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                //后台白屏且禁止截图
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                countActivity++;
                if (countActivity == 1 && isBackground) {
                    isBackground = false;
                    //说明应用重新进入了前台
                    if (showSecret){
                        Intent intent=new Intent(activity, Secret.class);
                        activity.startActivity(intent);
                    }
                }

                androidId = MainActivity.getAndroidId(activity);

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
                                UserLogout(activity);
                            }
                        }
                        @Override
                        public void onError(Throwable e) {
                            Log.e("用户设备查询失败",e.getMessage());
                        }
                        @Override
                        public void onComplete() {}
                    });
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                countActivity--;
                if (countActivity <= 0 && !isBackground) {
                    isBackground = true;
                    //说明应用进入了后台
                    LCUser user=LCUser.getCurrentUser();
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
                        public void onError(Throwable e) {}

                        @Override
                        public void onComplete() {
                            if (isin){
                                showSecret=true;
                            }else {
                                showSecret=false;
                            }
                        }
                    });
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    private void UserLogout(Activity activity){
        LCUser.logOut();
        Intent intent = new Intent(activity, Login.class);
        //下面2个flags ,可以将原有任务栈清空并将 intent 的目标 Activity 作为任务栈的根 Activity 。
        //任务栈的 Id 没有变，并没有开辟新的任务栈
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Toast.makeText(activity, "用户已在其他地方登录", Toast.LENGTH_SHORT).show();
        intent.putExtra("userlogout",true);
        startActivity(intent);
    }
}
