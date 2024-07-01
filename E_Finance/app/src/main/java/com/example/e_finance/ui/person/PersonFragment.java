package com.example.e_finance.ui.person;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import com.example.e_finance.AIChatActivity;
import com.example.e_finance.Cycle_Bill;
import com.example.e_finance.DesireActivity;
import com.example.e_finance.R;
import com.example.e_finance.SecretPassword;
import com.example.e_finance.Setting;
import com.example.e_finance.databinding.FragmentPersonBinding;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.iflytek.sparkchain.core.SparkChain;
import com.iflytek.sparkchain.core.SparkChainConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Random;

import cn.leancloud.LCUser;

public class PersonFragment extends Fragment {
    private ImageView Setting,UserPic;
    private TextView UserName,UserDate,SecretPsw,CycleBill,Policy,desireNum,userid;
    private FragmentPersonBinding binding;
    private BottomSheetDialog dialog;
    private ImageView Panda;
    private TextView prompt;
    private ConstraintLayout desirelayout;
    private ProgressBar progressBar;
    private int get;
    private PersonViewModel personViewModel;
    private String appId="",apiKey="",apiSecret="";


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        personViewModel =
                new ViewModelProvider(this).get(PersonViewModel.class);

        binding = FragmentPersonBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Setting =binding.Setting;
        CycleBill =binding.CycleBill;
        SecretPsw =binding.SecretPsw;
        UserPic = binding.PersonImg;
        UserDate = binding.Duration;
        UserName = binding.PersonName;
        Policy=binding.Policy;
        Panda=binding.Panda;
        prompt=binding.prompt;
        desirelayout=binding.desirelayout;
        progressBar=binding.progressBar;
        desireNum=binding.desireNum;
        userid=binding.userid;

        initUser();

        Random random = new Random();
        int randomNumber = random.nextInt(5);
        personViewModel.setImg(randomNumber);
        get = randomNumber;
        Panda.setImageBitmap(readBitMap(getContext(),personViewModel.getList().get(get)));
        prompt.setText(personViewModel.getPrompt().get(get));

        Setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(getActivity(), Setting.class);
                startActivity(intent);
            }
        });

        SecretPsw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(getActivity(), SecretPassword.class);
                startActivity(intent);
            }
        });

        CycleBill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(getActivity(), Cycle_Bill.class);
                startActivity(intent);
            }
        });

        Policy.setOnClickListener(new View.OnClickListener() {
            private ScrollView scrollView;
            @Override
            public void onClick(View v) {
                if (dialog==null){
                    dialog=new BottomSheetDialog(getContext(), R.style.BottomSheetEdit);//要加上这个，否则键盘会覆盖一部分弹窗。
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.policy_bottomsheetdialog, null);
                    dialog.setContentView(view);
                    scrollView=view.findViewById(R.id.scrollView);
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

        desirelayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(getActivity(), DesireActivity.class);
                startActivity(intent);
            }
        });

        Panda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //初始化讯飞AI
                SparkChainConfig config =  SparkChainConfig.builder()
                        .appID(appId)
                        .apiKey(apiKey)
                        .apiSecret(apiSecret);//从平台获取的授权appid，apikey,apisecrety

                try {
                    int ret = SparkChain.getInst().init(getActivity().getApplicationContext(), config);
//                    Log.d("TAG","sdk init:"+ret);
                    if (ret==0){
                        Intent intent=new Intent(getActivity(), AIChatActivity.class);
                        startActivity(intent);
                    }else {
                        Toast.makeText(getActivity(), "AI初始化失败，错误码："+ret, Toast.LENGTH_SHORT).show();
                    }
                }catch (Throwable throwable){
//                    Log.d("TAG1",throwable.getMessage());
                    Log.e("AIinitError",throwable.getMessage());
                    Toast.makeText(getActivity(), "AI初始化失败", Toast.LENGTH_SHORT).show();
                }

//                personViewModel.setImg(get+1);
//                get= personViewModel.getImg();
//                Panda.setImageBitmap(readBitMap(getContext(),personViewModel.getList().get(get)));
//                prompt.setText(personViewModel.getPrompt().get(get));
            }
        });


        personViewModel.getData().observe(getViewLifecycleOwner(), new Observer<Number[]>() {
            @Override
            public void onChanged(Number[] numbers) {

                int all= (int) numbers[0],complete=(int) numbers[1];
                if (all==0){
                    desireNum.setText("未设置");
                    progressBar.setProgress(100);
                }else {
                    BigDecimal bigall=new BigDecimal(all);
                    BigDecimal bigcomplete=new BigDecimal(complete);
                    int result=bigcomplete.divide(bigall,3, RoundingMode.HALF_DOWN).multiply(new BigDecimal(100)).intValue();
                    progressBar.setProgress(result,true);
                    desireNum.setText(complete+"/"+all);
                }

            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        personViewModel.getData().removeObservers(getViewLifecycleOwner());
    }

    @Override
    public void onResume() {
        super.onResume();
        initUser();
        if (personViewModel.getIsfirst()){
            personViewModel.setIsfirst(false);
        }else {
            personViewModel.getDesire();
        }
    }

    private void initUser() {
        LCUser user = LCUser.getCurrentUser();
        UserName.setText(user.getString("nickName"));
        Glide.with(getActivity())
                .load(user.getString("photo"))
                .transform(new CircleCrop())
                .into(UserPic);
        Date date1 = user.getDate("createdAt");
        Date date2 = new Date(System.currentTimeMillis());

        long cunt = date2.getTime() - date1.getTime();
        long oneday = 1000 * 24 * 60 * 60;
        int dayCount = (int) (cunt / oneday) + 1;
        UserDate.setText("坚持理财的第" + dayCount + "天");
        userid.setText("编号："+user.getObjectId());
    }

    /**
     * 以最省内存的方式读取本地资源的图片
     * @param context
    * @param resId
     * @return
     */
    public static Bitmap readBitMap(Context context, int resId){
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = false;
        // 可以复用之前用过的bitmap
        opt.inBitmap = null;

        // 是该bitmap缓存是否可变，如果设置为true，将可被inBitmap复用
        opt.inMutable = true;

        // 表示这个bitmap的像素密度，当inDensity为0时，系统默认赋值为屏幕当前像素密度
        opt.inDensity = (int) dm.density;

        // 表示要被画出来时的目标像素密度，当inTargetDensity为0时，系统默认赋值为屏幕当前像素密度
        opt.inTargetDensity = opt.inDensity;

        // 表示实际设备的像素密度，通常不需要设置，保持为0即可
        opt.inScreenDensity = 0;

        // 这个参数可以改变bitmap分辨率大小，inSampleSize >= 1。
        // 假如：图片的宽和高分别是width、height，那么图片解码生成的bitmap的宽度是：width / inSampleSize，高度是：height / inSampleSize
        // inSampleSize影响bitmap的分辨率，从而影响bitmap占用内存的大小。
        opt.inSampleSize = 2;

        // 表示图片是否可以被缩放
        opt.inScaled = true;

        // A R G B 四个颜色通道 每个通道占8位
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;

//        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
//        InputStream is = context.getResources().openRawResource(resId);
//        return BitmapFactory.decodeStream(is, null, opt);
        // 使用设置好的options对象来解码资源中的bitmap
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId, opt);
        return bitmap;
    }
}