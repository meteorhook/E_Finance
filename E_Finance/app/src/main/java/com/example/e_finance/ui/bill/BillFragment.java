package com.example.e_finance.ui.bill;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


import com.example.e_finance.Bill_Add;
import com.example.e_finance.entity.Billitem;
import com.example.e_finance.Billsearch;
import com.example.e_finance.adapter.DateBillAdapter;
import com.example.e_finance.FamilyFundsActivity;
import com.example.e_finance.FamilyinfoActivity;
import com.example.e_finance.Finance;
import com.example.e_finance.InvestmentActivity;
import com.example.e_finance.customview.Ldialog;

import com.example.e_finance.fragment.StatisticsBudgetFragment;
import com.example.e_finance.customview.billDecoration;
import com.example.e_finance.databinding.FragmentBillBinding;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.scwang.smart.refresh.header.MaterialHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;


import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;


public class BillFragment extends Fragment {
    private FragmentBillBinding binding;
    private String ledgerid;
    private TextView ledger, tv;
    private RecyclerView recyclerView;
    private ImageView bg,family;
    private View searchView;
    private Ldialog ldialog;
    private FloatingActionButton BillAdd;
    private BillViewModel billViewModel;
    private DateBillAdapter dateBillAdapter;
    private List<Billitem> Blist = new ArrayList<>();
    private SmartRefreshLayout smartRefreshLayout;
    private ViewPager2 viewPager2;
    private ViewPagerAdapter viewPagerAdapter;
    private CardView familyfunds,Investment;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        billViewModel =
                new ViewModelProvider(this).get(BillViewModel.class);
        binding = FragmentBillBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //绑定控件
        ledger = binding.ledger;
        BillAdd = binding.BillAdd;
        recyclerView = binding.RecyclerView;
        bg = binding.imageView2;
        family=binding.family;
        familyfunds=binding.card1;
        Investment=binding.card2;

        searchView = binding.search;
        viewPager2 = binding.viewPager2;
        //无账单提示文字
        tv = binding.tv;
        //下拉刷新控件
        smartRefreshLayout = binding.smartRefreshLayout;

        Investment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), InvestmentActivity.class);
                intent.putExtra("ledgerid",ledgerid);
                startActivity(intent);
            }
        });

        familyfunds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), FamilyFundsActivity.class);
                intent.putExtra("ledgerid",ledgerid);
                startActivity(intent);
            }
        });

        family.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), FamilyinfoActivity.class);
                startActivity(intent);
            }
        });

        //下拉刷新
        smartRefreshLayout.setRefreshHeader(new MaterialHeader(getActivity()).setColorSchemeColors(Color.parseColor("#E66E4E")));
        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
//                smartRefreshLayout.finishRefresh();//传入false表示刷新失败
                billViewModel.refreshData();
            }
        });

        //添加账单
        BillAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                billViewModel.setIsfirst(false);
                Intent intent = new Intent(getActivity(), Bill_Add.class);
                intent.putExtra("ledgerid", ledgerid);
                startActivity(intent);
            }
        });

        //账本弹窗，选择账本
        ledger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                billViewModel.setIsfirst(false);
                ldialog = new Ldialog(getContext());
                ldialog.setBillViewModel(billViewModel);
                ldialog.show();
            }
        });

        //显示当前账本
        billViewModel.getName().observe(getViewLifecycleOwner(), ledger::setText);

        //获取当前账本的id
        billViewModel.getlid().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                ledgerid = s;
            }
        });

        //展示当前账本的账单
        billViewModel.getBill().observe(getViewLifecycleOwner(), new Observer<List<Billitem>>() {
            @Override
            public void onChanged(List<Billitem> lists) {
                if (lists.size() != 0) {
                    bg.setVisibility(View.GONE);
                    tv.setVisibility(View.GONE);
                } else {
                    bg.setVisibility(View.VISIBLE);
                    tv.setVisibility(View.VISIBLE);
                }
                Blist.clear();
                Blist.addAll(lists);

                //按日期分组
                Map<String, List<Billitem>> billitemsByDate = Blist.stream()
                        .collect(Collectors.groupingBy(Billitem::getDate));

                List<List<Billitem>> billitemsByDateList = billitemsByDate.entrySet().stream()
                        .map(e -> e.getValue().stream().collect(Collectors.toList()))
                        .collect(Collectors.toList());

                // 按日期从大到小排序
                billitemsByDateList.sort((list1, list2) -> {
                    String date1 = list1.get(0).getDate();
                    String date2 = list2.get(0).getDate();
                    return date2.compareTo(date1); // 注意这里是逆序，所以是date2 - date1
                });

                dateBillAdapter = new DateBillAdapter(billitemsByDateList);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(dateBillAdapter);
            }
        });
        recyclerView.addItemDecoration(new billDecoration(25, true));

        //跳转搜索账单界面
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Billsearch.class);
                //获取控件在屏幕中的坐标
                int location[] = new int[2];
                searchView.getLocationOnScreen(location);
                intent.putExtra("x", location[0]);
                intent.putExtra("y", location[1]);
                intent.putExtra("width", searchView.getWidth());
                intent.putExtra("ledgerid", ledgerid);
                intent.putExtra("ledgername", ledger.getText());
                startActivity(intent);
                getActivity().overridePendingTransition(0, 0);
                billViewModel.setIsfirst(false);
            }
        });

        //进度条,初次加载显示遮罩层
        billViewModel.getShowPgbar().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Finance finance = (Finance) getActivity();
                if (billViewModel.getIsfirst()) {
                    if (aBoolean) {
                        finance.HideView(false);
                        BillAdd.setVisibility(View.GONE);
                    } else {
                        finance.HideView(true);
                        BillAdd.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (!aBoolean) {
                        smartRefreshLayout.finishRefresh();
                    } else {
                        smartRefreshLayout.autoRefresh();
                    }
                }
            }
        });

        //错误提示
        billViewModel.getIsError().observe(getViewLifecycleOwner(), new Observer<Throwable>() {
            @Override
            public void onChanged(Throwable throwable) {
                if (!billViewModel.getIsfirst()) {
                    Toast.makeText(getActivity(), "刷新失败，" + throwable.getMessage() + "，已自动重新加载", Toast.LENGTH_SHORT).show();
                }
                billViewModel.refreshData();
            }
        });

        //预算结余fragment
        billViewModel.getFragmentsList().observe(getViewLifecycleOwner(), new Observer<List<StatisticsBudgetFragment>>() {
            @Override
            public void onChanged(List<StatisticsBudgetFragment> statisticsBudgetFragments) {
                viewPagerAdapter = new ViewPagerAdapter(getActivity(), statisticsBudgetFragments);
                viewPager2.setAdapter(viewPagerAdapter);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (billViewModel.getIsfirst()) {
            billViewModel.setIsfirst(false);
        }
        binding = null;
        //取消观察者
        billViewModel.getName().removeObservers(getViewLifecycleOwner());
        billViewModel.getlid().removeObservers(getViewLifecycleOwner());
        billViewModel.getBill().removeObservers(getViewLifecycleOwner());
        billViewModel.getShowPgbar().removeObservers(getViewLifecycleOwner());
        billViewModel.getIsError().removeObservers(getViewLifecycleOwner());
        billViewModel.getFragmentsList().removeObservers(getViewLifecycleOwner());
    }

    @Override
    public void onStop() {
        super.onStop();
        if (billViewModel.getIsfirst()) {
            billViewModel.setIsfirst(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!billViewModel.getIsfirst()) {
            billViewModel.refreshData();
        }
    }

    public class ViewPagerAdapter extends FragmentStateAdapter {
        private List<StatisticsBudgetFragment> list;

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<StatisticsBudgetFragment> list) {
            super(fragmentActivity);
            this.list = list;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return list.get(position);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}