package com.example.e_finance.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.e_finance.fragment.statisticsChartFragment;
import com.example.e_finance.ui.statistics.StatisticsFragment;

import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private List<Fragment> fragmentList;
    private List<String> fragmentDateType;
    private String ledgerid;
    private List<StatisticsFragment.sandeDate> fragmentDate;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity,List<Fragment> fragmentList,List<String> fragmentDateType,List<StatisticsFragment.sandeDate> fragmentDate,String ledgerid) {
        super(fragmentActivity);
        this.fragmentList = fragmentList;
        this.fragmentDateType=fragmentDateType;
        this.fragmentDate=fragmentDate;
        this.ledgerid=ledgerid;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        statisticsChartFragment fragment=(statisticsChartFragment)fragmentList.get(position);
        fragment.setFragmentDateType(fragmentDateType.get(position));
        fragment.setSandeDate(fragmentDate.get(position));
        fragment.setLedgerid(ledgerid);
        return fragment;
    }


    @Override
    public int getItemCount() {
        return fragmentList.size();
    }


}
