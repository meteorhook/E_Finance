package com.example.e_finance;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_finance.util.StatusBar;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class E_Finance_Intro extends AppCompatActivity {
    private TextView protocol;
    private BottomSheetDialog dialog;
    private ImageView pgbar;
    private AnimationDrawable ad;
    private Button checknew;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(E_Finance_Intro.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_efinance_intro);

        protocol=findViewById(R.id.Agreemnet);
        checknew=findViewById(R.id.checknew);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        protocol.setOnClickListener(new View.OnClickListener() {
            private ScrollView scrollView;
            @Override
            public void onClick(View v) {
                if (dialog==null){
                    dialog=new BottomSheetDialog(E_Finance_Intro.this, R.style.BottomSheetEdit);//要加上这个，否则键盘会覆盖一部分弹窗。
                    View view = LayoutInflater.from(E_Finance_Intro.this).inflate(R.layout.policy_bottomsheetdialog, null);
                    dialog.setContentView(view);
                    TextView title=view.findViewById(R.id.title);
                    scrollView=view.findViewById(R.id.scrollView);
                    TextView message=view.findViewById(R.id.message);
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

        checknew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pgbar.setVisibility(View.VISIBLE);
                // 使用Handler延迟1秒后隐藏pgbar
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pgbar.setVisibility(View.INVISIBLE);
                        Toast.makeText(E_Finance_Intro.this,"你已经在使用我们的最新版本了！",Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }
        });
    }
}