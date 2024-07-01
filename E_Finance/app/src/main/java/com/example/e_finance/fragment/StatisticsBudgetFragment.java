package com.example.e_finance;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_finance.ui.bill.BillViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class StatisticsBudgetFragment extends Fragment {
    private boolean iscount;
    private TextView month,balance,income,pay,date,remaining,unused,total,totaltext,budget,count;
    private RingProgressBar ringProgressBar;
    private BubbleProgressView progressBar;
    private int progress=-1;
    private float mprogress=-1f;
    private Number[] numbers;
    private String monthinfo,budgetid,ledgerid,budgetName,remainder,today,dateStr;
    private Double monthPay;

    public void setLedgerid(String ledgerid) {
        this.ledgerid = ledgerid;
    }

    public void Reset(){
        if (budget!=null&&remaining!=null&&unused!=null&&total!=null&&date!=null){
            budgetid="0";
            budget.setText("点击设置预算");
            remaining.setText("0");
            unused.setText("0");
            total.setText("0");
            date.setVisibility(View.GONE);
            progress=-1;
        }
    }
    public void setbudget(String budgetid, String budgetName, String remainder, String today, Double monthPay,String dateStr){
        this.budgetid=budgetid;
        this.budgetName=budgetName;
        this.remainder=remainder;
        this.today=today;
        this.monthPay=monthPay;
        this.dateStr=dateStr;
        if (budget!=null&&remaining!=null&&unused!=null&&total!=null&&date!=null){
            budget.setText(budgetName);
            remaining.setText(remainder);
            unused.setText(today);
            total.setText(monthPay.toString());
            date.setText(dateStr);
            date.setVisibility(View.VISIBLE);
        }


    }

    public void setProgress(int progress) {
        this.progress = progress;
        if (ringProgressBar!=null) {
            ringProgressBar.setmTargetProgress(progress);
            ringProgressBar.setmProgress(0);
            ringProgressBar.invalidate();
        }
    }

    public void setMprogress(float mprogress,String str) {
        this.mprogress = mprogress;
        if (progressBar!=null){
            progressBar.setProgress(mprogress,str);
        }
    }

    public void setMonthinfo(String monthinfo) {
        this.monthinfo = monthinfo;
        if (month!=null){
            month.setText(monthinfo+"结余");
        }
    }

    public void setNumbers(Number[] numbers) {
        this.numbers = numbers;
        if (balance!=null&&income!=null&&pay!=null){
            Number in = numbers[0];
            if (in.toString().endsWith(".0")) {
                in = in.intValue();
            }
            income.setText("收入:" + in);

            Number p = numbers[1];
            if (p.toString().endsWith(".0")) {
                p = p.intValue();
            }
            pay.setText("支出:" + p);

            Number number = numbers[2];
            if (number.toString().endsWith(".0")) {
                number = number.intValue();
            }
            balance.setText(number + "");
        }
    }

    public StatisticsBudgetFragment(boolean iscount) {
        this.iscount=iscount;
    }
    public StatisticsBudgetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        if (iscount){
            view=inflater.inflate(R.layout.fragment_statistics_budget, container, false);
            month=view.findViewById(R.id.month);
            balance=view.findViewById(R.id.Balance);
            income=view.findViewById(R.id.Income);
            pay=view.findViewById(R.id.Pay);
            progressBar=view.findViewById(R.id.progressBar);
            month.setText(monthinfo+"结余");
        }else {
            view=inflater.inflate(R.layout.budget_layout, container, false);
            date=view.findViewById(R.id.date);
            remaining=view.findViewById(R.id.remaining);
            unused=view.findViewById(R.id.unused);
            total=view.findViewById(R.id.total);
            totaltext=view.findViewById(R.id.textView2);
            budget=view.findViewById(R.id.budget);
            count=view.findViewById(R.id.Count);
            ringProgressBar=view.findViewById(R.id. ringProgressBar);

            if (budgetid!=null){
                budget.setText(budgetName);
                remaining.setText(remainder);
                unused.setText(today);
                total.setText(monthPay.toString());
                date.setText(dateStr);
                date.setVisibility(View.VISIBLE);
            }
            if (progress!=-1){
                ringProgressBar.setmTargetProgress(progress);
            }

            count.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(getActivity(),Bill_Budget.class);
                    intent.putExtra("budgetid",budgetid);
                    intent.putExtra("ledgerid",ledgerid);
                    startActivity(intent);
                }
            });
        }

        return view;
    }
}