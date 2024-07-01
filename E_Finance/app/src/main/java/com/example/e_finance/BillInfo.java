package com.example.e_finance;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.e_finance.customview.DashedLineView;
import com.example.e_finance.customview.myAlertDialog;
import com.example.e_finance.util.StatusBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class BillInfo extends AppCompatActivity {
    private String billId;
    private View typeBg,dateRing,ledgerRing;
    private TextView typeName, typeNum, typeNote, typeDate, typeLedger;
    private FloatingActionButton billEdit, billDelete;
    private ImageView typeIcon;
    private ConstraintLayout billInfoBg;
    private CheckBox budget;
    private DashedLineView dashedLineView;
    private String billledgerid,billnote,billnumber,billtypeid;
    private Long billdate;
    private Boolean billstate;
    private ImageView pgbar;
    private AnimationDrawable ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(BillInfo.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_bill_info);

        billInfoBg=findViewById(R.id.background);

        dashedLineView=findViewById(R.id.dashedLineView);

        typeBg=findViewById(R.id.view3);
        dateRing=findViewById(R.id.view5);
        ledgerRing=findViewById(R.id.view6);

        typeName=findViewById(R.id.textView2);
        typeNum=findViewById(R.id.num);
        typeNote=findViewById(R.id.note);
        typeDate=findViewById(R.id.typeDate);
        typeLedger=findViewById(R.id.typeLedger);
        typeIcon=findViewById(R.id.billIcon);
        budget=findViewById(R.id.isbudget);

        billEdit=findViewById(R.id.floatingActionButton2);
        billDelete=findViewById(R.id.floatingActionButton);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        Intent intent=getIntent();
        billId=intent.getStringExtra("BillId");
        initview(billId);

        //该方法要放在onCreate里面，不能放在监听器里
        ActivityResultLauncher launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == Activity.RESULT_OK){
                    initview(result.getData().getStringExtra("BillId"));
                    billId=result.getData().getStringExtra("BillId");
                }
            }
        });

        billEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1=new Intent(BillInfo.this,Bill_Add.class);

                intent1.putExtra("ledgerid",billledgerid);
                intent1.putExtra("state",billstate);
                intent1.putExtra("note",billnote);
                intent1.putExtra("date",billdate);
                intent1.putExtra("number",billnumber);
                intent1.putExtra("typeid",billtypeid);
                intent1.putExtra("billid",billId);

                launcher.launch(intent1);
            }
        });

        billDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAlertDialog myAlertDialog=new myAlertDialog(BillInfo.this);
                myAlertDialog.create();
                //设置弹窗信息
                myAlertDialog.setMessage("确认删除吗，删除后不可恢复");
                myAlertDialog.setTitle("删除账单");
                //设置取消按钮
                myAlertDialog.setNegative("取消",new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myAlertDialog.dismiss();
                    }
                });
                //设置确认按钮
                myAlertDialog.setPositive("删除",new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deletbill(billId);
                    }
                });
                myAlertDialog.show();
            }
        });

    }
    private void initview(String billid){
        LCQuery<LCObject> query = new LCQuery<>("Ebill");
        query.getInBackground(billid).subscribe(new Observer<LCObject>() {
            public void onSubscribe(Disposable disposable) {
                pgbar.setVisibility(View.VISIBLE);
            }
            public void onNext(LCObject ebill) {
                billnote=ebill.getString("Bnotes");
                if (billnote.isEmpty()){
                    typeNote.setVisibility(View.GONE);
                    dashedLineView.setVisibility(View.GONE);
                }else {
                    typeNote.setVisibility(View.VISIBLE);
                    dashedLineView.setVisibility(View.VISIBLE);
                    typeNote.setText(billnote);
                }

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                formatter.setTimeZone(TimeZone.getTimeZone("GMT+08"));
                Date date = ebill.getDate("Bdate");
                String Date = formatter.format(date);
                typeDate.setText(Date);
                billdate=date.getTime();

                billtypeid = ebill.getString("Btype");
                LCQuery<LCObject> query = new LCQuery<>("Etype");
                query.getInBackground(billtypeid).subscribe(new Observer<LCObject>() {
                    public void onSubscribe(Disposable disposable) {
                    }
                    public void onNext(LCObject typeinfo) {
                        pgbar.setVisibility(View.GONE);
                        String color = typeinfo.getString("TypeColor");
                        String typename = typeinfo.getString("TypeName");
                        String icon = typeinfo.getString("TypeImgSelect");
                        billstate = typeinfo.getBoolean("Tstate");
                        billnumber=ebill.getString("Bnum");
                        String num;
                        if (billstate) {
                            num = "-" + billnumber;
                        } else {
                            num = "+" + billnumber;
                        }
                        typeNum.setText(num);
                        typeName.setText(typename);
                        billInfoBg.setBackgroundColor(Color.parseColor(color));
                        typeBg.getBackground().setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN);
                        dateRing.getBackground().setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN);
                        ledgerRing.getBackground().setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_IN);

                        Glide.with(BillInfo.this)
                                .load(icon)
                                .into(typeIcon);
                    }
                    public void onError(Throwable throwable) {Toast.makeText(BillInfo.this,"加载失败"+throwable.getMessage(),Toast.LENGTH_SHORT).show();}
                    public void onComplete() {
                        pgbar.setVisibility(View.GONE);
                    }
                });

                billledgerid = ebill.getString("Bledger");
                LCQuery<LCObject> query2 = new LCQuery<>("Eledger");
                query2.getInBackground(billledgerid).subscribe(new Observer<LCObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {}
                    @Override
                    public void onNext(LCObject lcObject) {
                        String ledgername=lcObject.getString("Lname");
                        typeLedger.setText(ledgername);
                    }
                    @Override
                    public void onError(Throwable e) {Toast.makeText(BillInfo.this,"加载失败"+e.getMessage(),Toast.LENGTH_SHORT).show();}
                    @Override
                    public void onComplete() {}
                });

                budget.setChecked(!ebill.getBoolean("Bbudget"));
                budget.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ebill.put("Bbudget",!isChecked);
                        ebill.saveInBackground().subscribe(new Observer<LCObject>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                pgbar.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onNext(LCObject lcObject) {
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
                });
            }
            public void onError(Throwable throwable) {
                Toast.makeText(BillInfo.this,"加载失败"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
            }
            public void onComplete() {}
        });
    }

    private void deletbill(String billid){
        LCObject todo = LCObject.createWithoutData("Ebill", billid);
        todo.deleteInBackground().subscribe(new Observer<LCNull>() {
            @Override
            public void onSubscribe(Disposable d) {
                pgbar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onNext(LCNull lcNull) {
                pgbar.setVisibility(View.GONE);
                finish();
            }
            @Override
            public void onError(Throwable e) {
                Toast.makeText(BillInfo.this,"删除失败"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onComplete() {
                pgbar.setVisibility(View.GONE);
            }
        });
    }
}