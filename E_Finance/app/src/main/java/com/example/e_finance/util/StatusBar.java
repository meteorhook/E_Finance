package com.example.e_finance.util;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

//状态栏颜色控制工具类
public class StatusBar {
    private Activity activity;

    //初始化activity
    public StatusBar(Activity activity){
        this.activity = activity;
    }

    //将状态栏设置为传入的color
    public void setColor(int color){
        if (Build.VERSION.SDK_INT >= 21) {
            View view = activity.getWindow().getDecorView();
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(color));
        }
    }

    //隐藏状态栏
    public void hide(){
        if (Build.VERSION.SDK_INT >= 21) {
            activity.getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    //设置状态栏字体颜色
    public void setTextColor(boolean isDarkBackground){
        View decor = activity.getWindow().getDecorView();
        if (isDarkBackground) {
            //黑暗背景字体浅色
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        } else {
            //高亮背景字体深色
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    //设置导航栏颜色
    public void setNavigationBarColor(int color) {
        Window window = activity.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        int ui = window.getDecorView().getSystemUiVisibility();
        window.getDecorView().setSystemUiVisibility(ui);
        window.setNavigationBarColor(color);
    }
}
