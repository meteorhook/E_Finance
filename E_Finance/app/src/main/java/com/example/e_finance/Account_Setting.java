package com.example.e_finance;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.MutableLiveData;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.leancloud.LCFile;
import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import cn.leancloud.types.LCNull;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class Account_Setting extends AppCompatActivity {
    private ConstraintLayout PersonName_Edit,PswEdit,PicChange,emaillayout;
    private TextView UserName,emailbinding;
    private ImageView UserPic;
    private Button deleteUser;
    private ActivityResultLauncher<Intent> launcher ;
    private ActivityResultLauncher<String> permissionlauncher;
    private long mPressedTime = 0; // 用于记录返回键按下时间
    private Boolean ledger=false,type=false,bill=false,cyclebill=false,budget=false,desire=false,secret=false,photo=false,equip=false;
    private MutableLiveData<Boolean> userdelete=new MutableLiveData<>();
    private String Email="";
    private ImageView pgbar;
    private AnimationDrawable ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(Account_Setting.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_account_setting);

        PicChange=findViewById(R.id.bg1);
        UserPic=findViewById(R.id.PersonImg);
        PersonName_Edit=findViewById(R.id.PersonName_Edit);
        UserName=findViewById(R.id.PersonName);
        PswEdit=findViewById(R.id.PswEdit);
        emaillayout=findViewById(R.id.email);
        emailbinding=findViewById(R.id.emailbinding);
        deleteUser=findViewById(R.id.deleteUser);

        pgbar=findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        initUser();

        //图片选择启动器
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent intent = result.getData();
            int resultCode = result.getResultCode();
        });

        //权限申请启动器
        permissionlauncher =registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                result ->{
                    if (result) {
                        //取得读取文件权限
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R ||
                                Environment.isExternalStorageManager()) {
                            //取得访问全部文件权限
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            launcher.launch(intent);
                        } else {
                            //未取得访问全部文件权限
                            myAlertDialog myAlertDialog=new myAlertDialog(Account_Setting.this,false);
                            myAlertDialog.create();
                            myAlertDialog.setTitle("权限申请");
                            myAlertDialog.setMessage("上传头像功能需要开启全部文件访问权限");
                            myAlertDialog.setCanceledOnTouchOutside(true);
                            myAlertDialog.setPositive("开启权限", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    myAlertDialog.dismiss();
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    "com.example.e_finance"
//                                    intent.setData(Uri.fromParts("package",getPackageName(),null));
                                    startActivity(intent);
                                }
                            });
                            myAlertDialog.setNegative("取消", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    myAlertDialog.dismiss();
                                    Toast.makeText(Account_Setting.this, "用户未授权", Toast.LENGTH_SHORT).show();
                                }
                            });
                            myAlertDialog.show();
                        }
                    }
                });

        //修改头像
        PicChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionlauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });

        //修改用户名
        PersonName_Edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myAlertDialog myAlertDialog = new myAlertDialog(Account_Setting.this, false);
                myAlertDialog.create();
                myAlertDialog.setCanceledOnTouchOutside(false);
                myAlertDialog.setContentView(R.layout.nickname_dialog);
                EditText editText = myAlertDialog.findViewById(R.id.nickname);
                TextView positive = myAlertDialog.findViewById(R.id.Positive);
                TextView negative = myAlertDialog.findViewById(R.id.Negative);

                editText.setText(UserName.getText().toString());
                editText.requestFocus();

                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (editText.getText().toString().isEmpty()){
                            Toast.makeText(Account_Setting.this,"请输入用户昵称",Toast.LENGTH_SHORT).show();
                        }else {
                            ResetUserInfo(false,true, editText.getText().toString());
                            myAlertDialog.dismiss();
                        }
                    }
                });

                negative.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myAlertDialog.dismiss();
                    }
                });
                myAlertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                myAlertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                myAlertDialog.show();
            }
        });

        //修改用户密码
        PswEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAlertDialog myAlertDialog=new myAlertDialog(Account_Setting.this,false);
                myAlertDialog.create();
                myAlertDialog.setTitle("修改密码");
                myAlertDialog.setMessage("修改密码功能将给您绑定的邮箱发送一封邮件，然后您可以点击链接进行操作，请在修改密码前确认您已绑定邮箱");
                myAlertDialog.setPositive("确认修改", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email=LCUser.getCurrentUser().getEmail();
                        if (email==null){
                            Toast.makeText(Account_Setting.this, "请先绑定邮箱", Toast.LENGTH_SHORT).show();
                            myAlertDialog.dismiss();
                            emaillayout.callOnClick();
                        }else {
                            //String email="pikahulk@163.com";
                            LCUser.requestPasswordResetInBackground(email).subscribe(new Observer<LCNull>() {
                                public void onSubscribe(Disposable disposable) {}
                                @Override
                                public void onNext(LCNull lcNull) {
                                    // 成功调用
                                    Toast.makeText(Account_Setting.this, "邮件发送成功", Toast.LENGTH_SHORT).show();
                                    myAlertDialog.dismiss();
                                    if (email.contains("@qq.com")){
                                        PackageManager packageManager = getPackageManager();
                                        String packageName = "com.tencent.mobileqq";//要打开应用的包名,这里打开QQ
                                        Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(packageName);
                                        if (launchIntentForPackage != null)
                                            startActivity(launchIntentForPackage);
                                        else{
                                            Toast.makeText(Account_Setting.this, "手机未安装该应用", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                public void onError(Throwable throwable) {
                                    // 调用出错
                                    Toast.makeText(Account_Setting.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    myAlertDialog.dismiss();
                                }
                                public void onComplete() {}
                            });
                        }
                    }
                });
                myAlertDialog.setNegative("取消", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myAlertDialog.dismiss();
                    }
                });
                myAlertDialog.show();
            }
        });

        //用户邮箱绑定
        emaillayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetDialog dialog = new BottomSheetDialog(Account_Setting.this, R.style.BottomSheetEdit);//要加上这个，否则键盘会覆盖一部分弹窗。
                View view = LayoutInflater.from(Account_Setting.this).inflate(R.layout.note_bottomsheetdialog, null);
                dialog.setContentView(view);
                EditText useremail = view.findViewById(R.id.editText);
                Button certain =  view.findViewById(R.id.button);
                Button cancel = view.findViewById(R.id.cancel);

                useremail.setHint("请输入邮箱");
                useremail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                if (LCUser.getCurrentUser().getEmail()!=null){
                    useremail.setText(LCUser.getCurrentUser().getEmail());
                }
                certain.setText("验证邮箱");
                certain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email=useremail.getText().toString();
                        if (email.isEmpty()){
                            Toast.makeText(Account_Setting.this, "请输入邮箱", Toast.LENGTH_SHORT).show();
                        } else if (!email.contains("@")){
                            Toast.makeText(Account_Setting.this, "请输入正确的邮箱地址", Toast.LENGTH_SHORT).show();
                            useremail.setText("");
                        } else if (email.equals(Email)&&LCUser.getCurrentUser().getBoolean("emailVerified")) {
                            Toast.makeText(Account_Setting.this, "该邮箱已验证", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            ResetUserInfo(true,false,email);
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

        //注销账号
        deleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAlertDialog dialog=new myAlertDialog(Account_Setting.this,false);
                dialog.create();
                dialog.setTitle("注销账号");
                dialog.setMessage("确认注销账号吗？该操作无法撤销且将删除该账号全部数据！");
                dialog.setPositive("注销", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long mNowTime = System.currentTimeMillis(); // 获取当前时间

                        if ((mNowTime - mPressedTime) > 2000) {
                            // 和前一次按确认键时间差大于2000ms，给出提示并记录这次按键时间
                            Toast.makeText(Account_Setting.this, "再按一次注销以确认注销账号", Toast.LENGTH_SHORT).show();
                            mPressedTime = mNowTime;
                        } else {
                            // 和前一次按确认键时间差小于等于2000ms，确认操作
                            pgbar.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                            //查询账本
                            LCUser user=LCUser.getCurrentUser();
                            LCQuery<LCObject> ledgerinfo=new LCQuery<>("Eledger");
                            ledgerinfo.whereEqualTo("Euser",user);
                            ledgerinfo.findInBackground().subscribe(new Observer<List<LCObject>>() {
                                public void onSubscribe(Disposable disposable) {}
                                public void onNext(List<LCObject> userledegrs) {
                                    List<String> typeid=new ArrayList<>();
                                    List<String> ledgerid=new ArrayList<>();
                                    for (LCObject lcObject:userledegrs){
                                        String[] one=lcObject.getJSONArray("Ltype").toArray(new String[0]);
                                        ledgerid.add(lcObject.getObjectId());
                                        typeid.addAll(Arrays.asList(one));
                                    }

                                    //删除账本
                                    deleteLedger(userledegrs);

                                    //删除分类
                                    deleteFunction(null,"Etype","objectId",null,typeid);

                                    //删除账单
                                    deleteFunction(null,"Ebill","Bledger",null,ledgerid);

                                    //删除用户周期账单
                                    deleteFunction(null,"Ecycle","Cledger",null,ledgerid);

                                    //删除预算
                                    deleteFunction(null,"Ebudget","Bledger",null,ledgerid);
                                }
                                public void onError(Throwable throwable) {
                                   Log.e("查询用户账本失败",throwable.getMessage());
                                }
                                public void onComplete() {}
                            });

                            //取消全部定时任务
                            WorkManager workManager = WorkManager.getInstance(getApplicationContext());
                            workManager.cancelAllWork();

                            //删除心愿
                            deleteFunction(user,"Edesire","Euser",null,null);

                            //删除隐私密码
                            deleteFunction(user,"Esecret","Suser",null,null);

                            //删除用户头像文件
                            deleteFunction(null,"_File","name",user.getObjectId()+".png",null);

                            //删除用户设备信息
                            deleteFunction(user,"EuserAndequipment","Euser",null,null);
                        }
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

        userdelete.observe(this, new androidx.lifecycle.Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean){
                    //全部数据操作完之后删除用户
                    deleteUser();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userdelete.removeObservers(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initUser();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode){
            case RESULT_OK:
                Uri uri = data.getData();
                String path = getRealPathFromURI(this, uri);
                String name=LCUser.getCurrentUser().getObjectId()+".png";
                LCFile file;
                try {
                    file = LCFile.withAbsoluteLocalPath(name, path);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                file.saveInBackground().subscribe(new Observer<LCFile>() {
                    private String Gurl;
                    public void onSubscribe(Disposable disposable) {}
                    public void onNext(LCFile newfile) {
                        //获取url
                        Gurl = newfile.getUrl();
                        LCQuery<LCFile> query=new LCQuery<>("_File");
                        query.whereEqualTo("name",name);
                        query.getFirstInBackground().subscribe(new Observer<LCFile>() {
                            public void onSubscribe(Disposable disposable) {}
                            public void onNext(LCFile userfile) {
                                if (!userfile.getObjectId().equals("653bc20942d451506a14b80f")){
                                    //删除旧图片
                                    userfile.deleteInBackground().subscribe(new Observer<LCNull>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {}
                                        @Override
                                        public void onNext(LCNull lcNull) {
                                            //更新头像数据
                                            ResetUserInfo(false,false,Gurl);
                                        }
                                        @Override
                                        public void onError(Throwable e) {}
                                        @Override
                                        public void onComplete() {}
                                    });
                                }
                            }
                            public void onError(Throwable throwable) {}
                            public void onComplete() {}
                        });
                    }

                    public void onError(Throwable throwable) {}
                    public void onComplete() {}
                });
                break;
            case RESULT_CANCELED:
                Toast.makeText(Account_Setting.this,"未选择头像",Toast.LENGTH_SHORT).show();
                break;
        }
    }
    private void initUser(){
        LCUser userinfo=LCUser.getCurrentUser();
        //同步本地数据
        userinfo.fetchInBackground().subscribe(new Observer<LCObject>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(LCObject todo) {
                LCUser usernow=LCUser.getCurrentUser();
                UserName.setText(usernow.getString("nickName"));
                Glide.with(Account_Setting.this)
                        .load(usernow.getString("photo"))
                        .transform(new CircleCrop())
                        .into(UserPic);
                if (usernow.getEmail()!=null&&usernow.getBoolean("emailVerified")){
                    emailbinding.setText("已绑定");
                    Email=usernow.getEmail();
                }else {
                    emailbinding.setText("未绑定");
                }
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });
    }
    private void ResetUserInfo(Boolean isEmail,Boolean isNickname,String Gurl){
        LCUser currentUser=LCUser.getCurrentUser();
        LCObject todo = LCObject.createWithoutData("_User", currentUser.getObjectId());
        if (isNickname){
            todo.put("nickName",Gurl);
        } else if (isEmail) {
            todo.put("email",Gurl);
        } else {
            todo.put("photo",Gurl);
        }
        todo.saveInBackground().subscribe(new Observer<LCObject>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(LCObject savedTodo) {
                if (isNickname){
                    UserName.setText(Gurl);
                } else if (isEmail) {
                    if (!savedTodo.getBoolean("emailVerified")){
                        LCUser.requestEmailVerifyInBackground(Gurl).subscribe(new Observer<LCNull>() {
                            public void onSubscribe(Disposable disposable) {}
                            @Override
                            public void onNext(LCNull lcNull){
                                // 成功调用
                                Toast.makeText(Account_Setting.this, "验证邮件发送成功,请前往邮箱确认后再继续操作", Toast.LENGTH_SHORT).show();
                                if (Gurl.contains("@qq.com")){
                                    PackageManager packageManager = getPackageManager();
                                    String packageName = "com.tencent.mobileqq";//要打开应用的包名,这里打开QQ
                                    Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(packageName);
                                    if (launchIntentForPackage != null)
                                        startActivity(launchIntentForPackage);
                                    else{
                                        Toast.makeText(Account_Setting.this, "手机未安装该应用", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            public void onError(Throwable throwable) {
                                // 调用出错
                                Toast.makeText(Account_Setting.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            public void onComplete() {}
                        });
                    }else {
                        Toast.makeText(Account_Setting.this, "验证邮件发送成功,请前往邮箱确认后再继续操作", Toast.LENGTH_SHORT).show();
                        if (Gurl.contains("@qq.com")){
                            PackageManager packageManager = getPackageManager();
                            String packageName = "com.tencent.mobileqq";//要打开应用的包名,这里打开QQ
                            Intent launchIntentForPackage = packageManager.getLaunchIntentForPackage(packageName);
                            if (launchIntentForPackage != null)
                                startActivity(launchIntentForPackage);
                            else{
                                Toast.makeText(Account_Setting.this, "手机未安装该应用", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }



                } else {
                    Glide.with(Account_Setting.this)
                            .asBitmap()     // 加载为Bitmap
                            .load(Gurl)
                            .transform(new CircleCrop())
                            .into(UserPic);
                }
                //同步本地数据
                currentUser.fetchInBackground().subscribe(new Observer<LCObject>() {
                    public void onSubscribe(Disposable disposable) {}
                    public void onNext(LCObject todo) {}
                    public void onError(Throwable throwable) {}
                    public void onComplete() {}
                });
            }
            public void onError(Throwable throwable) {
                Toast.makeText(Account_Setting.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();
//                System.out.println("errror"+throwable.getMessage());
            }
            public void onComplete() {}
        });
    }
    public static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI,
                new String[]{MediaStore.Images.ImageColumns.DATA},//
                null, null, null);
        if (cursor == null) result = contentURI.getPath();
        else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(index);
            cursor.close();
        }
        return result;
    }
    private void deleteUser(){
        LCUser user=LCUser.getCurrentUser();
        user.deleteInBackground().subscribe(new Observer<LCNull>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(LCNull lcNull) {
                pgbar.setVisibility(View.GONE);
                Toast.makeText(Account_Setting.this,"用户已注销",Toast.LENGTH_SHORT).show();
                LCUser.logOut();
                Intent intent=new Intent(Account_Setting.this, Login.class);
                //下面2个flags ,可以将原有任务栈清空并将 intent 的目标 Activity 作为任务栈的根 Activity 。
                //任务栈的 Id 没有变，并没有开辟新的任务栈
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            @Override
            public void onError(Throwable e) {
                if (e.getMessage().equals("Too many requests.")){
                    deleteUser();
                }else {
                    pgbar.setVisibility(View.GONE);
                }
                Log.e("用户注销失败",e.getMessage());
            }
            @Override
            public void onComplete() {}
        });
    }
    private void deleteLedger(List<LCObject> userledegrs){
        LCObject.deleteAllInBackground(userledegrs).subscribe(new Observer<LCNull>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(LCNull lcNull) {
                ledger=true;
                userdelete.setValue(ledger&&type&&bill&&cyclebill&&budget&&desire&&secret&&photo&&equip);
            }
            @Override
            public void onError(Throwable e) {
                if (e.getMessage().equals("Too many requests.")){
                    deleteLedger(userledegrs);
                }
                Log.e("账本删除失败",e.getMessage());
            }
            @Override
            public void onComplete() {}
        });
    }
    private void deleteFunction(LCUser user,String ClassName,String key,String name,List<String> delids){
        LCQuery<LCObject> delete=new LCQuery<>(ClassName);
        if (delids!=null){
            delete.whereContainedIn(key,delids);
        }else if (ClassName.equals("_File")){
            delete.whereEqualTo(key,name);
        }else {
            delete.whereEqualTo(key,user);
        }

        if (ClassName.equals("Ebill")){
            delete.limit(1000);
        } else if (ClassName.equals("Etype")) {
            delete.whereEqualTo("isModel",false);
        }
        delete.deleteAllInBackground().subscribe(new Observer<LCNull>() {
            @Override
            public void onSubscribe(Disposable d) {}
            @Override
            public void onNext(LCNull lcNull) {
                if (ClassName.equals("Etype")){
                    type=true;
                } else if (ClassName.equals("Ebill")) {
                    bill=true;
                }else if (ClassName.equals("Ecycle")) {
                    cyclebill=true;
                }else if (ClassName.equals("Ebudget")) {
                    budget=true;
                }else if (ClassName.equals("Edesire")) {
                    desire=true;
                }else if (ClassName.equals("Esecret")) {
                    secret=true;
                }else if (ClassName.equals("_File")) {
                    photo=true;
                }else if (ClassName.equals( "EuserAndequipment")) {
                    equip=true;
                }
                userdelete.setValue(ledger&&type&&bill&&cyclebill&&budget&&desire&&secret&&photo&&equip);
            }
            @Override
            public void onError(Throwable e) {
                if (e.getMessage().equals("Too many requests.")){
                    deleteFunction(user,ClassName,key,name,delids);
                }
                Log.e("删除"+ClassName+"失败",e.getMessage());
//                System.out.println("删除"+ClassName+"失败"+e.getMessage());
            }
            @Override
            public void onComplete() {}
        });
    }
}