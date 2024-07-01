package com.example.e_finance.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.e_finance.R;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class FamilyOriginFragment extends Fragment {
    private boolean isjoin=true;
    private ConstraintLayout join,create;
    private ImageView pgbar;
    private AnimationDrawable ad;

    public FamilyOriginFragment() {
        // Required empty public constructor
    }

    public FamilyOriginFragment(boolean isjoin) {
        this.isjoin=isjoin;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_family_origin, container, false);
        // Inflate the layout for this fragment
        join=view.findViewById(R.id.joinfamily);
        create=view.findViewById(R.id.createfamily);
        pgbar=view.findViewById(R.id.pgbar);

        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        if (isjoin){
            join.setVisibility(View.VISIBLE);
            create.setVisibility(View.GONE);

            Button joinfamily=join.findViewById(R.id.confirm);
            EditText familyid=join.findViewById(R.id.familyid);

            joinfamily.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    familyid.clearFocus();
                    String id=familyid.getText().toString();
                    if (id.isEmpty()){
                        Toast.makeText(getActivity(),"请输入家庭编号",Toast.LENGTH_SHORT).show();
                    }else {
                        LCQuery<LCObject> query = new LCQuery<>("Efamily");
                        query.getInBackground(id).subscribe(new Observer<LCObject>() {
                            public void onSubscribe(Disposable disposable) {
                                pgbar.setVisibility(View.VISIBLE);
                            }
                            public void onNext(LCObject family) {
                                String[] member=family.getJSONArray("Fmember").toArray(new String[0]);
                                if (member.length<(int)family.getNumber("Fnum")){
                                    if (containsString(member,LCUser.getCurrentUser().getObjectId())){
                                        pgbar.setVisibility(View.GONE);
                                        Toast.makeText(getActivity(),"你已是该家庭的成员",Toast.LENGTH_SHORT).show();
                                    }else {
                                        String[] newmember=addStringToArrayWithStreams(member,LCUser.getCurrentUser().getObjectId());
                                        family.put("Fmember",newmember);
                                        family.saveInBackground().subscribe(new Observer<LCObject>() {
                                            public void onSubscribe(Disposable disposable) {}
                                            public void onNext(LCObject savedTodo) {
                                                pgbar.setVisibility(View.GONE);
                                                Toast.makeText(getActivity(),"加入家庭成功",Toast.LENGTH_SHORT).show();
                                                getActivity().finish();
                                            }
                                            public void onError(Throwable throwable) {
                                                pgbar.setVisibility(View.GONE);
                                                Log.e("joinfamilyError",throwable.getMessage());
                                                Toast.makeText(getActivity(),"加入家庭失败，"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                            public void onComplete() {
                                                pgbar.setVisibility(View.GONE);
                                            }
                                        });
                                    }
                                }else {
                                    pgbar.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(),"该家庭已满员",Toast.LENGTH_SHORT).show();
                                }
                            }
                            public void onError(Throwable throwable) {
                                pgbar.setVisibility(View.GONE);
                                if (throwable.getMessage().equals("Object is not found.")){
                                    Toast.makeText(getActivity(),"家庭不存在，请检查家庭编号是否输入正确",Toast.LENGTH_SHORT).show();
                                }else {
                                    Log.e("findfamilyError",throwable.getMessage());
                                    Toast.makeText(getActivity(),"家庭信息查询失败，"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                              }
                            public void onComplete() {
                                pgbar.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            });

        }else {
            join.setVisibility(View.GONE);
            create.setVisibility(View.VISIBLE);
            Button createfamily=create.findViewById(R.id.create);
            EditText familyname=create.findViewById(R.id.familyname);
            EditText familynum=create.findViewById(R.id.familynum);

            createfamily.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fname=familyname.getText().toString();
                    String fnumber=familynum.getText().toString();
                    int fnum=0;
                    if (fnumber.isEmpty()) {
                        fnum = 2;
                    } else {
                        fnum = Integer.parseInt(familynum.getText().toString());
                    }

                    if (fname.isEmpty()) {
                        fname = LCUser.getCurrentUser().getString("nickName") + "的家庭";
                    }

                    if (fnum<2){
                        Toast.makeText(getActivity(),"家庭人数至少为2",Toast.LENGTH_SHORT).show();
                    } else if (fnum>10) {
                        Toast.makeText(getActivity(),"家庭人数至多为10",Toast.LENGTH_SHORT).show();
                    } else {
                        LCQuery<LCObject> query = new LCQuery<>("Efamily");
                        query.whereEqualTo("Fowner", LCUser.getCurrentUser());
                        String finalFname = fname;
                        int finalFnum = fnum;
                        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
                            public void onSubscribe(Disposable disposable) {
                                pgbar.setVisibility(View.VISIBLE);
                            }
                            public void onNext(List<LCObject> res) {
                                if (res.size()>0){
                                    pgbar.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(),"每个用户只能创建一个家庭",Toast.LENGTH_SHORT).show();
                                }else {
                                    LCObject family = new LCObject("Efamily");
                                    family.put("Fmember",new String[]{LCUser.getCurrentUser().getObjectId()});
                                    family.put("Fname", finalFname);
                                    family.put("Fnum", finalFnum);
                                    family.put("Fowner",LCUser.getCurrentUser());
                                    family.put("Ffunds","0");
                                    family.saveInBackground().subscribe(new Observer<LCObject>() {
                                        public void onSubscribe(Disposable disposable) {}
                                        public void onNext(LCObject todo) {
                                            pgbar.setVisibility(View.GONE);
                                            Toast.makeText(getActivity(),"创建成功，家庭编号已自动复制到剪贴板",Toast.LENGTH_SHORT).show();
                                            // 获取系统剪贴板服务
                                            ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                            // 创建ClipData对象
                                            ClipData clipData = ClipData.newPlainText("E家理财家庭编号", todo.getObjectId());

                                            // 将ClipData对象设置到剪贴板
                                            if (clipboardManager != null) {
                                                clipboardManager.setPrimaryClip(clipData);
                                            }
                                            getActivity().finish();
                                        }
                                        public void onError(Throwable throwable) {
                                            // 异常处理
                                            pgbar.setVisibility(View.GONE);
                                            if (throwable.getMessage().contains("A unique field was given a value that is already taken.")){
                                                Toast.makeText(getActivity(),"创建家庭失败，家庭名已存在",Toast.LENGTH_SHORT).show();
                                            }else {
                                                Toast.makeText(getActivity(),"创建家庭失败，"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                                                Log.e("FamilycreateError",throwable.getMessage().toString());
                                            }
                                        }
                                        public void onComplete() {
                                            pgbar.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            }
                            public void onError(Throwable throwable) {
                                pgbar.setVisibility(View.GONE);
                                Log.e("findfamilyError",throwable.getMessage());
                                Toast.makeText(getActivity(),"家庭信息查询失败，"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                            public void onComplete() {
                                pgbar.setVisibility(View.GONE);
                            }
                        });


                    }
                }
            });
        }


        return view;
    }
    public static boolean containsString(String[] member, String target) {
        for (String s : member) {
            if (s.equals(target)) {
                return true;
            }
        }
        return false;
    }

    public static String[] addStringToArrayWithStreams(String[] original, String toAdd) {
        return Stream.concat(Arrays.stream(original), Stream.of(toAdd))
                .toArray(String[]::new);
    }
}