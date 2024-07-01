package com.example.e_finance;

import androidx.annotation.NonNull;
import androidx.annotation.NonUiContext;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.iflytek.sparkchain.plugins.mail.Mail;
import com.kyleduo.switchbutton.SwitchButton;

import java.lang.reflect.Member;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class FamilyinfoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Spinner spinner,spinner2;
    private ImageView addmember;
    private TextView allpay,allinput,familyedit;
    private List<Member> list=new ArrayList<>();
    private ArrayList<String> flist=new ArrayList<>();
    private ArrayAdapter spadapter,spadapter2;
    private String familyid,nowfid;
    private ImageView pgbar;
    private AnimationDrawable ad;
    private String[] members;
    private Button familyorigin;
    private boolean isfirst=true;
    private  Family owner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(FamilyinfoActivity.this);
        statusBar.setColor(R.color.transparent);
        setContentView(R.layout.activity_familyinfo);

        recyclerView=findViewById(R.id.recyclerView);
        spinner=findViewById(R.id.spinner);
        spinner2=findViewById(R.id.spinner2);
        addmember=findViewById(R.id.addmember);
        allpay=findViewById(R.id.allpay);
        allinput=findViewById(R.id.allinput);
        familyorigin=findViewById(R.id.familyorigin);
        familyedit=findViewById(R.id.familyedit);

        pgbar=findViewById(R.id.pgbar);

        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        initview();

        familyedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditDialog editDialog=new EditDialog(FamilyinfoActivity.this);
                editDialog.create();
                editDialog.setTitle("家庭信息编辑");
                editDialog.setTv1("家庭名称","请输入家庭名称");
                editDialog.setTv2("家庭成员数量","请输入家庭成员数量");
                editDialog.setEditText(owner.Fname, String.valueOf(owner.getFnum()));
                editDialog.getEditText2().setInputType(InputType.TYPE_CLASS_NUMBER);
                editDialog.setSubmit("完成", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String Name = editDialog.getEditText().getText().toString(), Num = editDialog.getEditText2().getText().toString();
                        if (Name == null) {
                            Toast.makeText(FamilyinfoActivity.this, "请输入家庭名称", Toast.LENGTH_SHORT).show();
                        } else if (Num == null) {
                            Toast.makeText(FamilyinfoActivity.this, "请输入家庭成员数量", Toast.LENGTH_SHORT).show();
                        } else {
                            int fnum = Integer.parseInt(Num);
                            if (fnum<2){
                                Toast.makeText(FamilyinfoActivity.this,"家庭人数至少为2",Toast.LENGTH_SHORT).show();
                            } else if (fnum>10) {
                                Toast.makeText(FamilyinfoActivity.this,"家庭人数至多为10",Toast.LENGTH_SHORT).show();
                            }else {
                                LCObject todo = LCObject.createWithoutData("Efamily", owner.Fid);
                                todo.put("Fname", Name);
                                todo.put("Fnum", fnum);
                                todo.saveInBackground().subscribe(new Observer<LCObject>() {
                                    public void onSubscribe(Disposable disposable) {
                                        pgbar.setVisibility(View.VISIBLE);
                                    }
                                    public void onNext(LCObject savedTodo) {
                                        pgbar.setVisibility(View.GONE);
                                        editDialog.dismiss();
                                        Toast.makeText(FamilyinfoActivity.this,"家庭信息更新成功",Toast.LENGTH_SHORT).show();
                                        initview();
                                    }
                                    public void onError(Throwable throwable) {
                                        pgbar.setVisibility(View.GONE);
                                        editDialog.dismiss();
                                        Log.e("editfamilyError",throwable.getMessage());
                                        Toast.makeText(FamilyinfoActivity.this,"家庭信息更新失败"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                    public void onComplete() {
                                        pgbar.setVisibility(View.GONE);
                                        editDialog.dismiss();
                                    }
                                });
                            }
                        }
                    }
                });
                editDialog.getEditText().requestFocus();
                editDialog.show();
            }
        });

        addmember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog dialog = new BottomSheetDialog(FamilyinfoActivity.this, R.style.BottomSheetEdit);//要加上这个，否则键盘会覆盖一部分弹窗。
                View view = LayoutInflater.from(FamilyinfoActivity.this).inflate(R.layout.note_bottomsheetdialog, null);
                dialog.setContentView(view);
                EditText usernum = view.findViewById(R.id.editText);
                Button certain =  view.findViewById(R.id.button);
                Button cancel = view.findViewById(R.id.cancel);

                usernum.setHint("请输入用户编号");
                certain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (usernum.getText().toString().isEmpty()){
                            Toast.makeText(FamilyinfoActivity.this,"请输入成员编号",Toast.LENGTH_SHORT).show();
                        }else {
                            LCQuery<LCObject> query = new LCQuery<>("Efamily");
                            query.getInBackground(familyid).subscribe(new Observer<LCObject>() {
                                public void onSubscribe(Disposable disposable) {
                                    pgbar.setVisibility(View.VISIBLE);
                                }
                                public void onNext(LCObject family) {
                                    String[] member=family.getJSONArray("Fmember").toArray(new String[0]);
                                    if (member.length<(int)family.getNumber("Fnum")){
                                        if (FamilyOriginFragment.containsString(member,usernum.getText().toString())){
                                            pgbar.setVisibility(View.GONE);
                                            Toast.makeText(FamilyinfoActivity.this,"该用户已是家庭的成员",Toast.LENGTH_SHORT).show();
                                        }else {
                                            LCQuery<LCObject> query = new LCQuery<>("_User");
                                            query.getInBackground(usernum.getText().toString()).subscribe(new Observer<LCObject>() {
                                                public void onSubscribe(Disposable disposable) {}
                                                public void onNext(LCObject todo) {
                                                    String[] newmember=FamilyOriginFragment.addStringToArrayWithStreams(member,usernum.getText().toString());
                                                    family.put("Fmember",newmember);
                                                    family.saveInBackground().subscribe(new Observer<LCObject>() {
                                                        public void onSubscribe(Disposable disposable) {}
                                                        public void onNext(LCObject savedTodo) {
                                                            pgbar.setVisibility(View.GONE);
                                                            Toast.makeText(FamilyinfoActivity.this,"加入家庭成功",Toast.LENGTH_SHORT).show();
                                                            initview();
                                                        }
                                                        public void onError(Throwable throwable) {
                                                            pgbar.setVisibility(View.GONE);
                                                            Log.e("joinfamilyError",throwable.getMessage());
                                                            Toast.makeText(FamilyinfoActivity.this,"加入家庭失败，"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                                        }
                                                        public void onComplete() {
                                                            pgbar.setVisibility(View.GONE);
                                                        }
                                                    });
                                                }
                                                public void onError(Throwable throwable) {
                                                    pgbar.setVisibility(View.GONE);
                                                    if (throwable.getMessage().equals("Could not find user.")) {
                                                        Toast.makeText(FamilyinfoActivity.this, "该用户不存在", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Log.e("searchuserError", throwable.getMessage());
                                                        Toast.makeText(FamilyinfoActivity.this, "查询用户失败，" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                                public void onComplete() {
                                                    pgbar.setVisibility(View.GONE);
                                                }
                                            });
                                        }
                                    }else {
                                        pgbar.setVisibility(View.GONE);
                                        Toast.makeText(FamilyinfoActivity.this,"该家庭已满员",Toast.LENGTH_SHORT).show();
                                    }
                                }
                                public void onError(Throwable throwable) {
                                    pgbar.setVisibility(View.GONE);
                                    Log.e("findfamilyError",throwable.getMessage());
                                    Toast.makeText(FamilyinfoActivity.this,"家庭信息查询失败，"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                                public void onComplete() {
                                    pgbar.setVisibility(View.GONE);
                                }
                            });
                        }
                        dialog.dismiss();
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

        familyorigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(FamilyinfoActivity.this, FamilyOriginActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isfirst){
            initview();
        }
    }

    private void initview(){
        final LCQuery<LCObject> ownerfamily = new LCQuery<>("Efamily");
        ownerfamily.whereEqualTo("Fowner",LCUser.getCurrentUser());
        final   LCQuery<LCObject> unowner = new LCQuery<>("Efamily");
        unowner.whereEqualTo("Fmember", LCUser.getCurrentUser().getObjectId());
        LCQuery<LCObject> query =  LCQuery.or(Arrays.asList(ownerfamily, unowner));
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            public void onSubscribe(Disposable disposable) {
                pgbar.setVisibility(View.VISIBLE);
            }
            public void onNext(List<LCObject> family) {
                if (isfirst){
                    isfirst=false;
                }
                pgbar.setVisibility(View.GONE);
                if (family.isEmpty()){
                    list.clear();
                    if (spadapter!= null){
                        spadapter.clear();
                       spadapter.notifyDataSetChanged();
                    }
                    FamilymemberAdapter familymemberAdapter = new FamilymemberAdapter(list);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FamilyinfoActivity.this);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.setAdapter(familymemberAdapter);
                    familymemberAdapter.setSel(0);
                    myAlertDialog alertDialog=new myAlertDialog(FamilyinfoActivity.this,false);
                    alertDialog.create();
                    alertDialog.setTitle("提示");
                    alertDialog.setMessage("当前用户未加入或未创建家庭，是否前往加入/创建家庭？");
                    alertDialog.setPositive("加入/创建", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            Intent intent=new Intent(FamilyinfoActivity.this, FamilyOriginActivity.class);
                            startActivity(intent);
                        }
                    });
                    alertDialog.setNegative("返回", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                            finish();
                        }
                    });
                    alertDialog.show();
                }else {
                    String userid=LCUser.getCurrentUser().getObjectId();
                    boolean ow=false;
                    List<String> tlist=new ArrayList<>();

                    List<Family> Flist=new ArrayList<>();
                    owner=null;
                    for (LCObject lcObject:family){
                        if (lcObject.getLCObject("Fowner").getObjectId().equals(userid)){
                            ow=true;
                            familyid=lcObject.getObjectId();
                            members=lcObject.getJSONArray("Fmember").toArray(new String[0]);
                            owner=new Family(lcObject.getString("Fname")
                                    ,(int)lcObject.getNumber("Fnum")
                                    ,userid
                                    ,lcObject.getObjectId()
                                    ,members);
                        }else {
                            tlist.add(lcObject.getString("Fname"));
                        }

                        Flist.add(new Family(lcObject.getString("Fname")
                                ,(int)lcObject.getNumber("Fnum")
                                ,lcObject.getLCObject("Fowner").getObjectId()
                                ,lcObject.getObjectId()
                                ,lcObject.getJSONArray("Fmember").toArray(new String[0])));
                    }
                    flist.clear();
                    if (ow){
                        flist.add(owner.Fname);
                    }
                    flist.addAll(tlist);
                    spadapter=new ArrayAdapter<>(FamilyinfoActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,flist);
                    spinner.setAdapter(spadapter);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            boolean is=false;
                            addmember.setVisibility(View.GONE);
                            familyedit.setVisibility(View.GONE);
                            for (Family family1:Flist){
                                if (spinner.getSelectedItem().toString().equals(family1.Fname)){
                                    members=family1.Fmemberid;
                                    nowfid=family1.Fid;
                                    if (family1.Fowner.equals(userid)){
                                        is=true;
                                        addmember.setVisibility(View.VISIBLE);
                                        familyedit.setVisibility(View.VISIBLE);
                                    }
                                    break;
                                }
                            }

                            LCQuery<LCObject> query = new LCQuery<>("_User");
                            query.whereContainedIn("objectId", Arrays.asList(members));
                            boolean finalIs = is;
                            query.findInBackground().subscribe(new Observer<List<LCObject>>() {
                                public void onSubscribe(Disposable disposable) {}
                                public void onNext(List<LCObject> lcObjects) {
                                    list.clear();
                                    for (LCObject lcObject : lcObjects) {
                                        lcObject.getString("nickName");
                                        list.add(new Member(lcObject.getString("nickName"), lcObject.getObjectId()));
                                    }
                                    FamilymemberAdapter familymemberAdapter = new FamilymemberAdapter(list, finalIs);
                                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(FamilyinfoActivity.this);
                                    recyclerView.setLayoutManager(linearLayoutManager);
                                    recyclerView.setAdapter(familymemberAdapter);
                                    familymemberAdapter.setSel(0);
                                }
                                public void onError(Throwable throwable) {}
                                public void onComplete() {}
                            });
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });
                }
            }
            public void onError(Throwable throwable) {
                pgbar.setVisibility(View.GONE);
                Log.e("familydelError",throwable.getMessage());
                Toast.makeText(FamilyinfoActivity.this,"用户家庭获取失败"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
            }
            public void onComplete() {
                pgbar.setVisibility(View.GONE);
            }
        });
    }

    private void getledger(String userid){
        LCQuery<LCObject> query = new LCQuery<>("_User");
        query.getInBackground(userid).subscribe(new Observer<LCObject>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(LCObject todo) {
                LCQuery<LCObject> query = new LCQuery<>("Eledger");
                query.whereEqualTo("Euser", (LCUser)todo);
                query.whereEqualTo("isUsed", true);
                query.getFirstInBackground().subscribe(new Observer<LCObject>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        pgbar.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onNext(LCObject lcObject) {
                        String id = lcObject.getObjectId();
                        getBalance(id);
                    }

                    @Override
                    public void onError(Throwable e) {
                        pgbar.setVisibility(View.GONE);
                        Log.e("ledgerError",e.getMessage());
                        Toast.makeText(FamilyinfoActivity.this,"用户账本查询出错"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
            }
            public void onError(Throwable throwable) {
                pgbar.setVisibility(View.GONE);
                Log.e("ledgerError",throwable.getMessage());
                Toast.makeText(FamilyinfoActivity.this,"用户账本查询出错"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
            }
            public void onComplete() {
            }
        });

    }
    private void getBalance(String ledgers){
        //查询全部账单
        LCQuery<LCObject> query = new LCQuery<>("Ebill");
        query.whereEqualTo("Bledger",ledgers);
        query.limit(1000);
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(List<LCObject> lcObjects) {
                pgbar.setVisibility(View.GONE);
                // 创建一个Calendar实例
                Calendar calendar = Calendar.getInstance();
                // 将Calendar实例的时间设置为23:59:59
                calendar.set(Calendar.HOUR_OF_DAY,23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                Date curDate=calendar.getTime();
                // 将Calendar实例的时间设置为00:00:00
                calendar.set(Calendar.HOUR_OF_DAY,0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                Date today=calendar.getTime();
                // 将Calendar实例的日期设置为这个月的第一天
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                // 从Calendar实例中获取日期并转换为Date类型
                Date firstDayOfMonth = calendar.getTime();
                List<LCObject> nowBill=new ArrayList<>();
                List<LCObject> todayBill=new ArrayList<>();
                todayBill.clear();
                nowBill.clear();

                for (LCObject lc:lcObjects){
                    Date getdate=lc.getDate("Bdate");
                    if (getdate.before(curDate)){
                        if (getdate.after(firstDayOfMonth)){
                            nowBill.add(lc);
                        }
                        if (getdate.after(today)){
                            todayBill.add(lc);
                        }
                    }
                }

                //以下计算日结余
                BigDecimal resultIncome0=new BigDecimal("0");
                BigDecimal resultPay0=new BigDecimal("0");
                BigDecimal result0=new BigDecimal("0");

                for (int i = 0; i < todayBill.size(); i++) {
                    LCObject obj = todayBill.get(i);
                    BigDecimal b=new BigDecimal(obj.getString("Bnum"));
                    if (obj.getBoolean("Bbudget")){
                        if (obj.getBoolean("Bstate")) {
                            resultPay0=resultPay0.add(b);
                            result0 = result0.subtract(b);
                        } else {
                            resultIncome0=resultIncome0.add(b);
                            result0 = result0.add(b);
                        }
                    }
                }

                //以下计算月结余
                BigDecimal resultIncome=new BigDecimal("0");
                BigDecimal resultPay=new BigDecimal("0");
                BigDecimal result=new BigDecimal("0");
                BigDecimal resultBudget=new BigDecimal("0");

                for (int i = 0; i < nowBill.size(); i++) {
                    LCObject obj = nowBill.get(i);
                    BigDecimal b=new BigDecimal(obj.getString("Bnum"));
                    if (obj.getBoolean("Bbudget")){
                        if (obj.getBoolean("Bstate")) {
                            resultBudget = resultBudget.add(b);
                        }
                    }

                    if (obj.getBoolean("Bstate")) {
                        resultPay=resultPay.add(b);
                        result = result.subtract(b);
                    } else {
                        resultIncome=resultIncome.add(b);
                        result = result.add(b);
                    }
                }

                List<String> circle=new ArrayList<>();
                circle.add("月");
                circle.add("日");
                spadapter2=new ArrayAdapter<>(FamilyinfoActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,circle);
                spinner2.setAdapter(spadapter2);

                BigDecimal finalResultPay = resultPay;
                BigDecimal finalResultIncome = resultIncome;
                BigDecimal finalResultPay1 = resultPay0;
                BigDecimal finalResultIncome1 = resultIncome0;
                spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (spinner2.getSelectedItem().toString().equals("月")){
                            allpay.setText(finalResultPay.toString()+"元");
                            allinput.setText(finalResultIncome.toString()+"元");
                        } else if (spinner2.getSelectedItem().toString().equals("日")) {
                            allpay.setText(finalResultPay1.toString()+"元");
                            allinput.setText(finalResultIncome1.toString()+"元");
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });


            }
            @Override
            public void onError(Throwable e) {
                pgbar.setVisibility(View.GONE);
                Log.e("balanceError",e.getMessage());
                Toast.makeText(FamilyinfoActivity.this,"用户收支查询出错"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onComplete() {
                pgbar.setVisibility(View.GONE);
            }
        });
    }
    public class FamilymemberAdapter extends RecyclerView.Adapter<FamilymemberAdapter.ViewHolder>{
        private List<Member> list;
        private boolean isowner=true;
        private int sel=0;
        public FamilymemberAdapter(List<Member> list,boolean isowner){
            this.list=list;
            this.isowner=isowner;
        }
        public FamilymemberAdapter(List<Member> list){
            this.list=list;
        }

        public void setSel(int sel) {
            this.sel = sel;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.familymember_item,parent,false);
           ViewHolder viewHolder =new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Member one=list.get(position);
            holder.Mname.setText(one.Mname);
            holder.Mid.setText(one.Mid);
            if (isowner){
                if (LCUser.getCurrentUser().getObjectId().equals(one.Mid)){
                    holder.action.setText("解散家庭");
                    holder.action.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myAlertDialog myAlertDialog=new myAlertDialog(FamilyinfoActivity.this,false);
                            myAlertDialog.create();
                            myAlertDialog.setTitle("解散家庭");
                            myAlertDialog.setMessage("确认解散家庭吗？家庭中全成员都将从家庭中移除！");
                            myAlertDialog.setNegative("取消", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    myAlertDialog.dismiss();
                                }
                            });
                            myAlertDialog.setPositive("解散", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //删除存取记录
                                    LCQuery<LCObject> record = new LCQuery<>("Erecord");
                                    record.whereEqualTo("Rfamily", LCObject.createWithoutData("Efamily",familyid));
                                    record.deleteAllInBackground().subscribe(new Observer<LCNull>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {}
                                        @Override
                                        public void onNext(LCNull lcNull) {}
                                        @Override
                                        public void onError(Throwable e) {
                                            Log.e("recorddelError",e.getMessage());
                                            Toast.makeText(FamilyinfoActivity.this,"存取记录删除失败",Toast.LENGTH_SHORT).show();
                                        }
                                        @Override
                                        public void onComplete() {
                                        }
                                    });

                                    //解散家庭
                                    LCObject todo = LCObject.createWithoutData("Efamily",familyid);
                                    todo.deleteInBackground().subscribe(new Observer<LCNull>() {
                                        @Override
                                        public void onSubscribe(@NonNull Disposable d) {
                                            myAlertDialog.dismiss();
                                            pgbar.setVisibility(View.VISIBLE);
                                        }

                                        @Override
                                        public void onNext(LCNull response) {
                                            pgbar.setVisibility(View.GONE);
                                            Toast.makeText(FamilyinfoActivity.this,"家庭已解散",Toast.LENGTH_SHORT).show();
                                            initview();
                                        }

                                        @Override
                                        public void onError(@NonNull Throwable e) {
                                            pgbar.setVisibility(View.GONE);
                                            Log.e("familydelError",e.getMessage());
                                           Toast.makeText(FamilyinfoActivity.this,"家庭解散失败",Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onComplete() {
                                            pgbar.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            });
                            myAlertDialog.show();
                        }
                    });
                }else {
                    holder.action.setText("移除成员");
                    holder.action.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myAlertDialog myAlertDialog=new myAlertDialog(FamilyinfoActivity.this,false);
                            myAlertDialog.create();
                            myAlertDialog.setTitle("移除成员");
                            myAlertDialog.setMessage("确认移除该成员吗？该成员将无法查看该家庭中成员的收支情况！");
                            myAlertDialog.setNegative("取消", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    myAlertDialog.dismiss();
                                }
                            });
                            myAlertDialog.setPositive("移除", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    LCObject todo = LCObject.createWithoutData("Efamily",familyid );
                                    List<String> newmembers=new ArrayList<>(Arrays.asList(members));
                                    newmembers.removeIf(s -> s.equals(one.Mid));
                                    todo.put("Fmember",newmembers.toArray(new String[0]));
                                    todo.saveInBackground().subscribe(new Observer<LCObject>() {
                                        public void onSubscribe(Disposable disposable) {
                                            myAlertDialog.dismiss();
                                            pgbar.setVisibility(View.VISIBLE);
                                        }
                                        public void onNext(LCObject savedTodo) {
                                            pgbar.setVisibility(View.GONE);
                                            Toast.makeText(FamilyinfoActivity.this,"该成员已移除",Toast.LENGTH_SHORT).show();
                                            initview();
                                        }
                                        public void onError(Throwable throwable) {
                                            pgbar.setVisibility(View.GONE);
                                            Log.e("memberdelError",throwable.getMessage());
                                            Toast.makeText(FamilyinfoActivity.this,"成员移除失败"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                        public void onComplete() {
                                            pgbar.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            });
                            myAlertDialog.show();
                        }
                    });
                }
            }else {
                if (LCUser.getCurrentUser().getObjectId().equals(one.Mid)){
                    holder.action.setText("退出家庭");
                    holder.action.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myAlertDialog myAlertDialog=new myAlertDialog(FamilyinfoActivity.this,false);
                            myAlertDialog.create();
                            myAlertDialog.setTitle("退出家庭");
                            myAlertDialog.setMessage("确认退出家庭吗？退出家庭后将无法查看该家庭中成员的收支情况！");
                            myAlertDialog.setNegative("取消", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    myAlertDialog.dismiss();
                                }
                            });
                            myAlertDialog.setPositive("退出", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    LCObject todo = LCObject.createWithoutData("Efamily",nowfid);
                                    List<String> newmembers=new ArrayList<>(Arrays.asList(members));
                                    newmembers.removeIf(s -> s.equals(one.Mid));
                                    todo.put("Fmember",newmembers.toArray(new String[0]));
                                    todo.saveInBackground().subscribe(new Observer<LCObject>() {
                                        public void onSubscribe(Disposable disposable) {
                                            myAlertDialog.dismiss();
                                            pgbar.setVisibility(View.VISIBLE);
                                        }
                                        public void onNext(LCObject savedTodo) {
                                            pgbar.setVisibility(View.GONE);
                                            Toast.makeText(FamilyinfoActivity.this,"已退出家庭",Toast.LENGTH_SHORT).show();
                                            initview();
                                        }
                                        public void onError(Throwable throwable) {
                                            pgbar.setVisibility(View.GONE);
                                            Log.e("quitfamilyError",throwable.getMessage());
                                            Toast.makeText(FamilyinfoActivity.this,"退出家庭失败"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                        public void onComplete() {
                                            pgbar.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            });
                            myAlertDialog.show();
                        }
                    });
                }else {
                    holder.action.setVisibility(View.GONE);
                }
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getledger(one.Mid);
                }
            });
            if (sel==0){
                holder.itemView.callOnClick();
                sel=1;
            }
        }
        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            private TextView Mname,Mid;
            private Button action;

            public ViewHolder(@NonNull View view){
                super(view);
                Mname=(TextView)view.findViewById(R.id.membername);
                Mid=(TextView)view.findViewById(R.id.memberid);
                action=(Button) view.findViewById(R.id.action);
            }
        }
        }
    public class Member{
        private String Mname,Mid;
        public Member(String Mname,String Mid){
            this.Mid=Mid;
            this.Mname=Mname;
        }

        public String getMid() {
            return Mid;
        }

        public String getMname() {
            return Mname;
        }
    }

    public class Family{
        private String Fname,Fowner,Fid;
        private int Fnum;
        private String[] Fmemberid;
        public Family(String Fname,int Fnum,String Fowner,String Fid,String[] Fmemberid){
            this.Fid=Fid;
            this.Fmemberid=Fmemberid;
            this.Fowner=Fowner;
            this.Fnum=Fnum;
            this.Fname=Fname;
        }

        public int getFnum() {
            return Fnum;
        }
    }
}