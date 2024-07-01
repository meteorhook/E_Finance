package com.example.e_finance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.e_finance.util.StatusBar;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;

import cn.leancloud.LCObject;
import cn.leancloud.LCUser;
import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class InvestmentInfoActivity extends AppCompatActivity {

    private TextView projectname,procircle,procontent,prorate,countdown;
    private ImageView propic;
    private Button action;
    private ImageView pgbar;
    private AnimationDrawable ad;
    private String name,circle,content,id,pic,rate,ledgerid;
    private boolean iswithdraw=false;

    private Handler handler;
    private Runnable countdownRunnable;
    private Date targetDate; // 设置你的目标日期

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(InvestmentInfoActivity.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_investment_info);

        projectname=findViewById(R.id.proname);
        procircle=findViewById(R.id.circle);
        procontent=findViewById(R.id.content);
        propic=findViewById(R.id.propic);
        prorate=findViewById(R.id.rate);
        action=findViewById(R.id.action);
        countdown=findViewById(R.id.countdown);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        Intent intent=getIntent();
        name=intent.getStringExtra("name");
        circle=intent.getStringExtra("circle");
        content=intent.getStringExtra("content");
        id=intent.getStringExtra("id");
        pic=intent.getStringExtra("pic");
        rate=intent.getStringExtra("rate");
        ledgerid=intent.getStringExtra("ledgerid");
        iswithdraw=intent.getBooleanExtra("iswithdraw",false);

        projectname.setText(name);
        prorate.setText(rate);
        procircle.setText(circle);
        procontent.setText("产品详情\n"+content);
        Glide.with(InvestmentInfoActivity.this)
                .load(pic)
                .into(propic);

        if (iswithdraw){
            countdown.setVisibility(View.VISIBLE);
            String num= intent.getStringExtra("useqnum");
            String useqid= intent.getStringExtra("useqID");
            Date useqddl=new Date(intent.getLongExtra("useqddl",0));

            targetDate = useqddl;

            handler = new Handler();
            countdownRunnable = new Runnable() {
                @Override
                public void run() {
                    updateCountdown();
                    handler.postDelayed(this, 1000); // 每秒更新一次
                }
            };

            // 开始倒计时
            handler.post(countdownRunnable);


            action.setText("提取收益");
            action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)  {
                    //计算结果
                    BigDecimal capital=new BigDecimal(num);
                    String percentageString = rate;
                    String decimalString = percentageString.replace("%", ""); // 去除百分号
                    BigDecimal rate = new BigDecimal(decimalString).divide(new BigDecimal("100")); // 转换为小数

                    Calendar calendar = Calendar.getInstance();
                    Date now=calendar.getTime();
                    Date ddl=useqddl;
                    calendar.setTime(ddl);
                    if (circle.equals("年")){
                        calendar.add(Calendar.YEAR,-1);
                    } else if (circle.equals("月")) {
                        calendar.add(Calendar.MONTH,-1);
                    }
                    Date past=calendar.getTime();

                    // 计算时间差（毫秒）
                    long timeToNow = now.getTime() - past.getTime();
                    long timeToDdl = ddl.getTime() - past.getTime();

                    BigDecimal withdraw;
                    if (timeToNow<timeToDdl){
                        BigDecimal bigRatio = BigDecimal.valueOf((double) timeToNow / timeToDdl).setScale(2, RoundingMode.HALF_UP); // 保留两位小数
                        withdraw = capital.add(capital.multiply(rate).multiply(bigRatio));
                    }else {
                        withdraw=capital.add(capital.multiply(rate));
                    }
                    withdraw = withdraw.setScale(2, RoundingMode.HALF_UP);
                    LCObject bill = new LCObject("Ebill");
                    bill.put("Bdate", new Date(System.currentTimeMillis()));
                    bill.put("Bnum", withdraw.stripTrailingZeros().toPlainString());
                    bill.put("Bnotes",name+"收益");
                    bill.put("Bledger", ledgerid);
                    bill.put("Bstate", false);
                    bill.put("Btype", "663cc296e62a672ee1872a6d");
                    bill.put("Bbudget",true);
                    bill.saveInBackground().subscribe(new Observer<LCObject>() {
                        public void onSubscribe(Disposable disposable) {}
                        public void onNext(LCObject todo) {
                            pgbar.setVisibility(View.GONE);
                            Toast.makeText(InvestmentInfoActivity.this, "收益提取成功，已自动记账", Toast.LENGTH_SHORT).show();
                        }
                        public void onError(Throwable throwable) {
                            pgbar.setVisibility(View.GONE);
                            Log.e("billaddError",throwable.getMessage());
                            Toast.makeText(InvestmentInfoActivity.this, "添加账单失败," + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        public void onComplete() {
                            pgbar.setVisibility(View.GONE);
                        }
                    });

                    //删除投资
                    LCObject todo = LCObject.createWithoutData("Euseq", useqid);
                    todo.deleteInBackground().subscribe(new Observer<LCNull>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {}
                        @Override
                        public void onNext(LCNull response) {
                            finish();
                        }
                        @Override
                        public void onError(@NonNull Throwable e) {
                            Log.e("useqError",e.getMessage());
                            Toast.makeText(InvestmentInfoActivity.this, "用户投资删除失败"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onComplete() {}
                    });

                }
            });
        }else {
            countdown.setVisibility(View.GONE);
            action.setText("立即投资");
            action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BottomSheetDialog dialog = new BottomSheetDialog(InvestmentInfoActivity.this, R.style.BottomSheetEdit);//要加上这个，否则键盘会覆盖一部分弹窗。
                    View view = LayoutInflater.from(InvestmentInfoActivity.this).inflate(R.layout.note_bottomsheetdialog, null);
                    dialog.setContentView(view);
                    EditText money = view.findViewById(R.id.editText);
                    Button certain =  view.findViewById(R.id.button);
                    Button cancel = view.findViewById(R.id.cancel);
                    money.setHint("请输入投资金额");
                    money.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

                    certain.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (money.getText().toString().isEmpty()){
                                Toast.makeText(InvestmentInfoActivity.this,"请输入投资金额",Toast.LENGTH_SHORT).show();
                            }else {
                                Calendar calendar = Calendar.getInstance();
                                if (circle.equals("年")){
                                    calendar.add(Calendar.YEAR,1);
                                } else if (circle.equals("月")) {
                                    calendar.add(Calendar.MONTH,1);
                                }
                                Date ddl=calendar.getTime();

                                LCObject todo = new LCObject("Euseq");
                                todo.put("Eddl",ddl);
                                todo.put("Euser", LCUser.getCurrentUser());
                                todo.put("Enum",money.getText().toString());
                                todo.put("Eequities",LCObject.createWithoutData("Eequities",id));
                                todo.saveInBackground().subscribe(new Observer<LCObject>() {
                                    public void onSubscribe(Disposable disposable) {
                                        pgbar.setVisibility(View.VISIBLE);
                                    }
                                    public void onNext(LCObject todo) {
                                        LCObject bill = new LCObject("Ebill");
                                        bill.put("Bdate", new Date(System.currentTimeMillis()));
                                        bill.put("Bnum", money.getText().toString());
                                        bill.put("Bnotes",name);
                                        bill.put("Bledger", ledgerid);
                                        bill.put("Bstate", true);
                                        bill.put("Btype", "6544f7a844fa007c597cb5e1");
                                        bill.put("Bbudget",true);
                                        bill.saveInBackground().subscribe(new Observer<LCObject>() {
                                            public void onSubscribe(Disposable disposable) {}
                                            public void onNext(LCObject todo) {
                                                pgbar.setVisibility(View.GONE);
                                                Toast.makeText(InvestmentInfoActivity.this, "投资成功，已自动记账", Toast.LENGTH_SHORT).show();
                                                Intent intent1=new Intent(InvestmentInfoActivity.this, MyInvestmentActivity.class);
                                                intent1.putExtra("ledgerid",ledgerid);
                                                startActivity(intent1);
                                                finish();
                                            }
                                            public void onError(Throwable throwable) {
                                                pgbar.setVisibility(View.GONE);
                                                Log.e("billaddError",throwable.getMessage());
                                                Toast.makeText(InvestmentInfoActivity.this, "添加账单失败," + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                            public void onComplete() {
                                                pgbar.setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                    public void onError(Throwable throwable) {
                                        pgbar.setVisibility(View.GONE);
                                        Log.e("investmentError",throwable.getMessage());
                                        Toast.makeText(InvestmentInfoActivity.this, "投资失败," + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    public void onComplete() {
                                        pgbar.setVisibility(View.GONE);
                                    }
                                });
                                dialog.dismiss();
                            }
                        }
                    });

                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    //启动输入法
                    InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        view.requestFocus();
                        imm.showSoftInput(view, 0);
                    }
                    dialog.show();
                }
            });
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler!=null){
            handler.removeCallbacks(countdownRunnable); // 确保在Activity销毁时停止倒计时
        }
    }

    private void updateCountdown() {
        long diff = targetDate.getTime() - new Date().getTime();

        if (diff <= 0) {
            // 目标日期已到或已过
            countdown.setText("时间到！");
            handler.removeCallbacks(countdownRunnable); // 停止倒计时
        } else {
            // 计算天数、小时、分钟和秒
            int days = (int) (diff / (1000 * 60 * 60 * 24));
            int hours = (int) ((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
            int minutes = (int) ((diff % (1000 * 60 * 60)) / (1000 * 60));
            int seconds = (int) ((diff % (1000 * 60)) / 1000);

            // 更新UI
            countdown.setText("距离收益还有\n"+days + "天 " + hours + "小时 " + minutes + "分钟 " + seconds + "秒");
        }
    }
}