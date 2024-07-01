package com.example.e_finance.ui.statistics;


import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.e_finance.ViewPagerAdapter;
import com.example.e_finance.databinding.FragmentStatisticsBinding;
import com.example.e_finance.statisticsChartFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class StatisticsFragment extends Fragment{
    private FragmentStatisticsBinding binding;
    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private List<Fragment> fragmentList=new ArrayList<>();
    private List<String> fragmentDateType=new ArrayList<>();
    private List<sandeDate> fragmentDate=new ArrayList<>();
    private ViewPagerAdapter viewPagerAdapter;
    private StatisticsViewModel statisticsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        statisticsViewModel =
                new ViewModelProvider(this).get(StatisticsViewModel.class);

        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        statisticsViewModel.getFragmentList().observe(getViewLifecycleOwner(), new androidx.lifecycle.Observer<List<Fragment>>() {
            @Override
            public void onChanged(List<Fragment> fragments) {
                fragmentList=fragments;
            }
        });
        fragmentDateType.add("month");
        fragmentDateType.add("year");
        fragmentDateType.add("custom");

        viewPager2=binding.viewPager;
        tabLayout=binding.tabLayout;

        LCUser user = LCUser.getCurrentUser();
        LCQuery<LCObject> query = new LCQuery<>("Eledger");
        query.whereEqualTo("Euser", user);
        query.whereEqualTo("isUsed", true);
        query.getFirstInBackground().subscribe(new Observer<LCObject>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(LCObject lcObject) {
                // 获取当前日期的Calendar实例
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+08"));
                int Year=calendar.get(Calendar.YEAR);
                int Month=calendar.get(Calendar.MONTH);
                int Year2,Month2;
                Date nowDay,nowYear,nextYear,nowMonth,nextMonth,initDay;

                nowDay=calendar.getTime();

                calendar.set(Year,Calendar.JANUARY,1,0,0,0);
                nowYear=calendar.getTime();

                calendar.set(Year+1,Calendar.JANUARY,1,0,0,0);
                nextYear=calendar.getTime();

                calendar.set(Year,Month,1,0,0,0);
                nowMonth=calendar.getTime();

                if (Month== Calendar.DECEMBER){
                    Year2=Year+1;
                    Month2=Calendar.JANUARY;
                }else {
                    Year2=Year;
                    Month2=Month+1;
                }
                calendar.set(Year2,Month2,1,0,0,0);
                nextMonth=calendar.getTime();

                initDay=new Date(0);

                fragmentDate.add(new sandeDate(nowMonth,nextMonth));
                fragmentDate.add(new sandeDate(nowYear,nextYear));
                fragmentDate.add(new sandeDate(initDay,nowDay));

                for (int i=0;i<3;i++){
                    statisticsChartFragment fragment=(statisticsChartFragment)fragmentList.get(i);
                    fragment.setLedgerid(lcObject.getObjectId());
                }
                viewPagerAdapter = new ViewPagerAdapter(getActivity(), fragmentList,fragmentDateType,fragmentDate,lcObject.getObjectId());
                viewPager2.setAdapter(viewPagerAdapter);
                new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
                    // 为每个标签设置文本
                    switch (position){
                        case 0:tab.setText("月");break;
                        case 1:tab.setText("年");break;
                        case 2:tab.setText("自定义");break;
                    }
                }).attach(); // 连接 TabLayout 和 ViewPager2
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        statisticsViewModel.getFragmentList().removeObservers(getViewLifecycleOwner());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!statisticsViewModel.getIsfirst()){
            for (int i = 0; i < 3; i++) {
                statisticsChartFragment fragment = (statisticsChartFragment) fragmentList.get(i);
                fragment.onResume();
            }
        }else {
            statisticsViewModel.setIsfirst(false);
        }
    }

    static public class sandeDate{
        private Date startDate,endDate;
        public sandeDate(Date startDate,Date endDate){
            this.startDate=startDate;
            this.endDate=endDate;
        }

        public Date getStartDate() {
            return startDate;
        }

        public Date getEndDate() {
            return endDate;
        }
    }
}