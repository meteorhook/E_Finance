package com.example.e_finance.ui.statistics;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.e_finance.fragment.statisticsChartFragment;

import java.util.ArrayList;
import java.util.List;

public class StatisticsViewModel extends ViewModel {

    private MutableLiveData<List<Fragment>> fragmentList;
    private Boolean isfirst=true;
    public StatisticsViewModel() {
       fragmentList=new MutableLiveData<>();
        List<Fragment> list=new ArrayList<>();
        list.add(new statisticsChartFragment());
        list.add(new statisticsChartFragment());
        list.add(new statisticsChartFragment());
        fragmentList.setValue(list);
    }
    public Boolean getIsfirst() {
        return isfirst;
    }

    public void setIsfirst(Boolean isfirst) {
        this.isfirst = isfirst;
    }

    public MutableLiveData<List<Fragment>> getFragmentList() {
        return fragmentList;
    }
}