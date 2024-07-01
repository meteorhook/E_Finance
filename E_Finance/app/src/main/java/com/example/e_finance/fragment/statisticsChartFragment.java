package com.example.e_finance.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bin.david.form.annotation.SmartColumn;
import com.bin.david.form.annotation.SmartTable;
import com.bin.david.form.data.CellInfo;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.format.IFormat;
import com.bin.david.form.data.format.bg.ICellBackgroundFormat;
import com.bin.david.form.data.format.draw.BitmapDrawFormat;
import com.bin.david.form.data.style.LineStyle;
import com.bin.david.form.data.table.TableData;
import com.bin.david.form.listener.OnColumnItemClickListener;
import com.bin.david.form.utils.DensityUtils;
import com.bumptech.glide.Glide;
import com.example.e_finance.BillstatisticsinfoActivity;
import com.example.e_finance.R;
import com.example.e_finance.customview.MyLineChart;
import com.example.e_finance.customview.MyMarkerView;
import com.example.e_finance.statisticsChartViewModel;
import com.example.e_finance.ui.statistics.StatisticsFragment;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.loper7.date_time_picker.DateTimeConfig;
import com.loper7.date_time_picker.DateTimePicker;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class statisticsChartFragment extends Fragment {
    private com.example.e_finance.statisticsChartViewModel statisticsChartViewModel;
    private MyLineChart mLineChart;
    private PieChart pieChart;
    private ConstraintLayout paybg,incomebg,balancebg,linechartView,piechartView,tableView;
    private LinearLayout chartContainview;
    private ImageView Previous,Next,notChart;
    private TextView pay,paynum,income,incomenum,balance,balancenum,date,tabletitle,notCharttext;
    private RecyclerView typeStatistics;
    private LineDataSet set1;
    private PieDataSet set2;
    private ArrayList<Entry> values = new ArrayList();
    private String fragmentDateType,color="#666666",showType,ledgerid;
    private StatisticsFragment.sandeDate sandeDate;
    private MyMarkerView marker;
    private Date dateStart,dateEnd;
    private Boolean isfirst=true,isfirst2=true;
    private com.bin.david.form.core.SmartTable<BillStatistics> smartTable;
    private ImageView pgbar;
    private AnimationDrawable ad;
    public void setFragmentDateType(String fragmentDateType) {
        this.fragmentDateType = fragmentDateType;
    }

    public void setLedgerid(String ledgerid) {
        this.ledgerid=ledgerid;
        if (statisticsChartViewModel!=null){
            statisticsChartViewModel.setLedgerid(ledgerid);
            statisticsChartViewModel.refreshData();
        }
    }

    public void setSandeDate(StatisticsFragment.sandeDate sandeDate) {
        this.sandeDate = sandeDate;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        statisticsChartViewModel =
                new ViewModelProvider(this).get(statisticsChartViewModel.class);

        View view=inflater.inflate(R.layout.fragment_statistics_chart, container, false);

        mLineChart =view.findViewById(R.id.lineChart);
        pieChart=view.findViewById(R.id.pieChart);
        smartTable=view.findViewById(R.id.table);

        paybg=view.findViewById(R.id.paybg);
        incomebg=view.findViewById(R.id.incomebg);
        balancebg=view.findViewById(R.id.balancebg);
        linechartView=view.findViewById(R.id.linechartView);
        piechartView=view.findViewById(R.id.piechartView);
        tableView=view.findViewById(R.id.tableView);

        chartContainview=view.findViewById(R.id.chartContainview);

        pay=view.findViewById(R.id.pay);
        paynum=view.findViewById(R.id.payNum);
        income=view.findViewById(R.id.income);
        incomenum=view.findViewById(R.id.incomeNum);
        balance=view.findViewById(R.id.balance);
        balancenum=view.findViewById(R.id.balanceNum);

        tabletitle=view.findViewById(R.id.tv3);

        notCharttext=view.findViewById(R.id.notCharttext);

        date=view.findViewById(R.id.date);

        Previous=view.findViewById(R.id.Previous);
        Next=view.findViewById(R.id.Next);
        notChart=view.findViewById(R.id.notChart);

        typeStatistics=view.findViewById(R.id.typeStatistics);

        pgbar=view.findViewById(R.id.pgbar);
        ad=(AnimationDrawable)pgbar.getDrawable();
        pgbar.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.start();
            }
        }, 100);

        initView();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        statisticsChartViewModel.getLineData().removeObservers(getViewLifecycleOwner());
        statisticsChartViewModel.getBalance().removeObservers(getViewLifecycleOwner());
        statisticsChartViewModel.getPieData().removeObservers(getViewLifecycleOwner());
        statisticsChartViewModel.getTypeStatistics().removeObservers(getViewLifecycleOwner());
        statisticsChartViewModel.getBillStatistics().removeObservers(getViewLifecycleOwner());
        statisticsChartViewModel.getShowPgbar().removeObservers(getViewLifecycleOwner());
        statisticsChartViewModel.getIsError().removeObservers(getViewLifecycleOwner());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (statisticsChartViewModel!=null){
            statisticsChartViewModel.refreshData();
        }
    }

    private void initView(){
        //初始化fragment日期类型
        statisticsChartViewModel.setLedgerid(ledgerid);
        statisticsChartViewModel.setFragmentDateType(fragmentDateType);
        statisticsChartViewModel.setSandeDate(sandeDate);
        formatDateSet();
        refreshNum();

        //显示进度条
        statisticsChartViewModel.getShowPgbar().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean){
                    pgbar.setVisibility(View.VISIBLE);
                }else {
                    pgbar.setVisibility(View.GONE);
                }
            }
        });

        //错误提示
        statisticsChartViewModel.getIsError().observe(getViewLifecycleOwner(), new Observer<Throwable>() {
            @Override
            public void onChanged(Throwable throwable) {
                Toast.makeText(getActivity(),"加载失败，"+throwable.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

        switch (fragmentDateType){
            case "month":
                setmLineChart();
                setPiechart();
                setTypeStatistics();
                setTable();
                tabletitle.setText("月报表");
                break;
            case "year":
                setmLineChart();
                setPiechart();
                setTypeStatistics();
                setTable();
                tabletitle.setText("年报表");
                break;
            case "custom":
                setPiechart();
                setTypeStatistics();
                linechartView.setVisibility(View.GONE);
                tableView.setVisibility(View.GONE);
                Previous.setImageResource(R.drawable.left_arrow_grey);
                Next.setImageResource(R.drawable.right_arrow_grey);
                Previous.setClickable(false);
                Next.setClickable(false);
                break;
        }

        Previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDate(false);
            }
        });

        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDate(true);
            }
        });

        paybg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paybg.setClickable(false);
                incomebg.setClickable(true);
                balancebg.setClickable(true);
                showType="pay";
                statisticsChartViewModel.setType(showType);
                statisticsChartViewModel.refreshData();

                piechartView.setVisibility(View.VISIBLE);
                tableView.setVisibility(View.GONE);
                if (!paynum.getText().toString().equals("0")){
                    chartContainview.setVisibility(View.VISIBLE);
                }

                //设置背景
                paybg.setBackgroundResource(R.drawable.top_radius_btn);
                incomebg.setBackgroundResource(R.drawable.top_white_line_left_radius_grey_bg);
                balancebg.setBackgroundResource(R.drawable.left_bottom_radius_grey_bg2);
                //设置字体颜色
                pay.setTextColor(Color.parseColor("#FFAB00"));
                paynum.setTextColor(Color.parseColor("#FFAB00"));

                //其余颜色设为黑灰色
                income.setTextColor(Color.BLACK);
                incomenum.setTextColor(Color.parseColor(color));
                balance.setTextColor(Color.BLACK);
                balancenum.setTextColor(Color.parseColor(color));
            }
        });

        incomebg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paybg.setClickable(true);
                incomebg.setClickable(false);
                balancebg.setClickable(true);
                showType="income";
                statisticsChartViewModel.setType(showType);
                statisticsChartViewModel.refreshData();

                piechartView.setVisibility(View.VISIBLE);
                tableView.setVisibility(View.GONE);
                if (!incomenum.getText().toString().equals("0")){
                    chartContainview.setVisibility(View.VISIBLE);
                }
                //设置背景
                incomebg.setBackgroundResource(R.drawable.top_radius_btn);
                paybg.setBackgroundResource(R.drawable.right_bottom_radius_grey_bg);
                balancebg.setBackgroundResource(R.drawable.left_bottom_radius_grey_bg);

                //设置字体颜色
                income.setTextColor(Color.parseColor("#FFAB00"));
                incomenum.setTextColor(Color.parseColor("#FFAB00"));

                //其余颜色设为黑灰色
                pay.setTextColor(Color.BLACK);
                paynum.setTextColor(Color.parseColor(color));
                balance.setTextColor(Color.BLACK);
                balancenum.setTextColor(Color.parseColor(color));
            }
        });

        balancebg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paybg.setClickable(true);
                incomebg.setClickable(true);
                balancebg.setClickable(false);
                showType="balance";
                statisticsChartViewModel.setType(showType);
                statisticsChartViewModel.refreshData();

                if (fragmentDateType.equals("custom")){
                    notChart.setImageResource(R.drawable.notchart);
                    notCharttext.setText("没有更多报表啦");
                    chartContainview.setVisibility(View.GONE);
                    notChart.setVisibility(View.VISIBLE);
                    notCharttext.setVisibility(View.VISIBLE);
                }else if (!balancenum.getText().toString().equals("0")){
                    piechartView.setVisibility(View.GONE);
                    tableView.setVisibility(View.VISIBLE);
                    chartContainview.setVisibility(View.VISIBLE);
                }

                //设置背景
                balancebg.setBackgroundResource(R.drawable.top_radius_btn);
                paybg.setBackgroundResource(R.drawable.right_bottom_radius_grey_bg2);
                incomebg.setBackgroundResource(R.drawable.top_white_line_right_radius_grey_bg);

                //设置字体颜色
                balance.setTextColor(Color.parseColor("#FFAB00"));
                balancenum.setTextColor(Color.parseColor("#FFAB00"));

                //其余颜色设为黑灰色
                income.setTextColor(Color.BLACK);
                incomenum.setTextColor(Color.parseColor(color));
                pay.setTextColor(Color.BLACK);
                paynum.setTextColor(Color.parseColor(color));
            }
        });

        paybg.callOnClick();

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog();
            }
        });
    }

    private void changeDate(Boolean isNext){
        Date date1 = sandeDate.getEndDate();
        Date date2 = sandeDate.getStartDate();
        Calendar calendar=Calendar.getInstance(TimeZone.getTimeZone("GMT+08"));
        switch (fragmentDateType) {
            case "month":
                if (isNext){
                    calendar.setTime(date1);
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int year2, month2;
                    if (month == Calendar.DECEMBER) {
                        year2 = year + 1;
                        month2 = Calendar.JANUARY;
                    } else {
                        year2 = year;
                        month2 = month + 1;
                    }
                    calendar.set(year2, month2, 1, 0, 0, 0);
                    date2=calendar.getTime();
                    sandeDate=new StatisticsFragment.sandeDate(date1,date2);
                }else {
                    calendar.setTime(date2);
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int year2, month2;
                    if (month == Calendar.JANUARY) {
                        year2 = year - 1;
                        month2 = Calendar.DECEMBER;
                    } else {
                        year2 = year;
                        month2 = month - 1;
                    }
                    calendar.set(year2, month2, 1, 0, 0, 0);
                    date1=calendar.getTime();
                    sandeDate=new StatisticsFragment.sandeDate(date1,date2);
                }
                break;
            case "year":
                if (isNext){
                    calendar.setTime(date1);
                    calendar.add(Calendar.YEAR,1);
                    date2=calendar.getTime();
                    sandeDate=new StatisticsFragment.sandeDate(date1,date2);
                }else {
                    calendar.setTime(date2);
                    calendar.add(Calendar.YEAR,-1);
                    date1=calendar.getTime();
                    sandeDate=new StatisticsFragment.sandeDate(date1,date2);
                }
                break;
            case "custom":
                if (isNext){
                    calendar.setTime(date1);
                    calendar.add(Calendar.YEAR,2);
                    calendar.add(Calendar.MONTH,6);
                    date2=calendar.getTime();
                    sandeDate=new StatisticsFragment.sandeDate(date1,date2);
                }else {
                    calendar.setTime(date2);
                    calendar.add(Calendar.YEAR,-2);
                    calendar.add(Calendar.MONTH,-6);
                    date1=calendar.getTime();
                    sandeDate=new StatisticsFragment.sandeDate(date1,date2);
                }
                break;
        }
        formatDateSet();
        statisticsChartViewModel.setSandeDate(sandeDate);
        statisticsChartViewModel.refreshData();
    }

    private void refreshNum() {
        if (statisticsChartViewModel.getBalance().hasObservers()) {
            statisticsChartViewModel.getBalance().removeObservers(getViewLifecycleOwner());
        }
        statisticsChartViewModel.getBalance().observe(getViewLifecycleOwner(), new Observer<Number[]>() {
            @Override
            public void onChanged(Number[] numbers) {
                Number in = numbers[0];
                if (in.toString().endsWith(".0")) {
                    in = in.intValue();
                }
                incomenum.setText(in + "");
                Number p = numbers[1];
                if (p.toString().endsWith(".0")) {
                    p = p.intValue();
                }
                paynum.setText(p + "");

                Number number = numbers[2];
                if (number.toString().endsWith(".0")) {
                    number = number.intValue();
                }
                balancenum.setText(number + "");


                if (!fragmentDateType.equals("custom")){
                    notChart.setImageResource(R.drawable.notbill);
                    notCharttext.setText("暂无数据");
                }
                //设置饼图的饼心显示的文字,及收支结余为零时显示图片
                switch (showType){
                    case "pay":
                        if (paynum.getText().toString().equals("0")){
                            notChart.setVisibility(View.VISIBLE);
                            notCharttext.setVisibility(View.VISIBLE);
                            chartContainview.setVisibility(View.GONE);
                        } else {
                            notChart.setVisibility(View.GONE);
                            notCharttext.setVisibility(View.GONE);
                            piechartView.setVisibility(View.VISIBLE);
                            tableView.setVisibility(View.GONE);
                            chartContainview.setVisibility(View.VISIBLE);
                            pieChart.setCenterText(new SpannableString("总支出\n"+p));
                        }
                        break;
                    case "income":
                        if (incomenum.getText().toString().equals("0")){
                            notChart.setVisibility(View.VISIBLE);
                            notCharttext.setVisibility(View.VISIBLE);
                            chartContainview.setVisibility(View.GONE);
                        } else {
                            notChart.setVisibility(View.GONE);
                            notCharttext.setVisibility(View.GONE);
                            piechartView.setVisibility(View.VISIBLE);
                            tableView.setVisibility(View.GONE);
                            chartContainview.setVisibility(View.VISIBLE);
                            pieChart.setCenterText(new SpannableString("总收入\n"+in));
                        }
                        break;
                    case "balance":
                        if (balancenum.getText().toString().equals("0")) {
                            notChart.setVisibility(View.VISIBLE);
                            notCharttext.setVisibility(View.VISIBLE);
                            chartContainview.setVisibility(View.GONE);
                        } else if (!fragmentDateType.equals("custom")) {
                            notChart.setVisibility(View.GONE);
                            notCharttext.setVisibility(View.GONE);
                            piechartView.setVisibility(View.GONE);
                            tableView.setVisibility(View.VISIBLE);
                            chartContainview.setVisibility(View.VISIBLE);
                        } else {
                            notChart.setVisibility(View.VISIBLE);
                            notCharttext.setVisibility(View.VISIBLE);
                            chartContainview.setVisibility(View.GONE);
                        }
                        break;
                }

            }
        });
    }

    private void showDateDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetEdit);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.statistics_date_bottomsheetdialog, null);
        dialog.setContentView(view);
        TextView title = view.findViewById(R.id.title);
        TextView finish = view.findViewById(R.id.finish);

        TextView dateinfo = view.findViewById(R.id.dateinfo);
        TextView backnow = view.findViewById(R.id.backnow);

        TextView startDate = view.findViewById(R.id.startDate);
        TextView endDate = view.findViewById(R.id.endDate);
        TextView tv = view.findViewById(R.id.tv);

        DateTimePicker dateTimePicker = view.findViewById(R.id.dateTimePicker);
        switch (fragmentDateType) {
            case "month":
                startDate.setVisibility(View.GONE);
                endDate.setVisibility(View.GONE);
                tv.setVisibility(View.GONE);
                dateinfo.setVisibility(View.VISIBLE);
                backnow.setVisibility(View.VISIBLE);

                title.setText("选择日期");
                backnow.setText("本月");
                dateTimePicker.setDisplayType(new int[]{
                        DateTimeConfig.YEAR,//显示年
                        DateTimeConfig.MONTH,//显示月
                });
                dateTimePicker.setLayout(R.layout.year_month_layout);
                break;
            case "year":
                startDate.setVisibility(View.GONE);
                endDate.setVisibility(View.GONE);
                tv.setVisibility(View.GONE);
                dateinfo.setVisibility(View.VISIBLE);
                backnow.setVisibility(View.VISIBLE);

                title.setText("选择年份");
                backnow.setText("本年");
                dateTimePicker.setDisplayType(new int[]{
                        DateTimeConfig.YEAR,//显示年
                });
                dateTimePicker.setLayout(R.layout.year_layout);
                break;
            case "custom":
                startDate.setVisibility(View.VISIBLE);
                endDate.setVisibility(View.VISIBLE);
                tv.setVisibility(View.VISIBLE);
                dateinfo.setVisibility(View.GONE);
                backnow.setVisibility(View.GONE);

                title.setText("选择日期");
                dateTimePicker.setDisplayType(new int[]{
                        DateTimeConfig.YEAR,//显示年
                        DateTimeConfig.MONTH,//显示月
                        DateTimeConfig.DAY,//显示日
                });
                dateTimePicker.setLayout(R.layout.year_month_day_layout);
                break;
        }
        dateTimePicker.showLabel(true);
        dateTimePicker.setTextSize(20, 20);
        dateTimePicker.setThemeColor(Color.parseColor("#FFC107"));
        if (isfirst2&&fragmentDateType.equals("custom")){
            dateTimePicker.setDefaultMillisecond(System.currentTimeMillis());
        }else {
            dateTimePicker.setDefaultMillisecond(sandeDate.getStartDate().getTime());
        }
        //设置时间格式
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+08"));

        dateTimePicker.setOnDateTimeChangedListener(new Function1<Long, Unit>() {
            @Override
            public Unit invoke(Long aLong) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeZone(TimeZone.getTimeZone("GMT+08"));

                calendar.setTimeInMillis(aLong);
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                int year2, month2;
                Date datenow;
                if (fragmentDateType.equals("month")) {
                    calendar.set(year, month, 1, 0, 0, 0);
                    dateStart = calendar.getTime();

                    calendar.set(year, month, day, 0, 0, 0);
                    datenow = calendar.getTime();

                    if (month == Calendar.DECEMBER) {
                        year2 = year + 1;
                        month2 = Calendar.JANUARY;
                    } else {
                        year2 = year;
                        month2 = month + 1;
                    }
                    calendar.set(year2, month2, 1, 0, 0, 0);
                    dateEnd = calendar.getTime();

                    String result = formatter.format(dateStart) + "-" + formatter.format(datenow);
                    dateinfo.setText(result);
                } else if (fragmentDateType.equals("year")) {
                    calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0);
                    dateStart = calendar.getTime();
                    calendar.set(year, Calendar.DECEMBER, 31, 0, 0, 0);
                    datenow = calendar.getTime();

                    calendar.set(year + 1, Calendar.JANUARY, 1, 0, 0, 0);
                    dateEnd = calendar.getTime();

                    String result = formatter.format(dateStart) + "-" + formatter.format(datenow);
                    dateinfo.setText(result);
                }

                return null;
            }
        });

        backnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateTimePicker.setDefaultMillisecond(System.currentTimeMillis());
            }
        });

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDate.setSelected(true);
                startDate.setTextColor(Color.parseColor("#E66E4E"));
                if (endDate.isSelected()) {
                    endDate.setSelected(false);
                    endDate.setTextColor(Color.BLACK);
                    dateTimePicker.setOnDateTimeChangedListener(null);
                    dateTimePicker.setDefaultMillisecond(System.currentTimeMillis());
                }

                dateTimePicker.setOnDateTimeChangedListener(new Function1<Long, Unit>() {
                    @Override
                    public Unit invoke(Long aLong) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(aLong);
                        Date date = calendar.getTime();
                        dateStart = calendar.getTime();
                        //格式转换
                        String createDate = formatter.format(date);
                        startDate.setText(createDate);
                        return null;
                    }
                });
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endDate.setSelected(true);
                endDate.setTextColor(Color.parseColor("#E66E4E"));
                if (startDate.isSelected()) {
                    startDate.setSelected(false);
                    startDate.setTextColor(Color.BLACK);
                    dateTimePicker.setOnDateTimeChangedListener(null);
                    dateTimePicker.setDefaultMillisecond(System.currentTimeMillis());
                }

                dateTimePicker.setOnDateTimeChangedListener(new Function1<Long, Unit>() {
                    @Override
                    public Unit invoke(Long aLong) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(aLong);
                        Date date = calendar.getTime();

                        calendar.setTimeInMillis(aLong + (24 * 60 * 60 * 1000));
                        dateEnd = calendar.getTime();
                        //格式转换
                        String createDate = formatter.format(date);
                        endDate.setText(createDate);
                        return null;
                    }
                });

            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fragmentDateType.equals("custom")){
                    if (startDate.getText().toString().equals("开始时间") && endDate.getText().toString().equals("结束时间")) {
                        dialog.dismiss();
                    } else if (startDate.getText().toString().equals("开始时间")) {
                        Toast.makeText(getActivity(), "请选择开始时间", Toast.LENGTH_SHORT).show();
                    } else if (endDate.getText().toString().equals("结束时间")) {
                        Toast.makeText(getActivity(), "请选择结束时间", Toast.LENGTH_SHORT).show();
                    } else if (dateEnd.getTime() < dateStart.getTime()) {
                        Toast.makeText(getActivity(), "结束日期需要大于等于起始日期", Toast.LENGTH_SHORT).show();
                    } else {
                        sandeDate = new StatisticsFragment.sandeDate(dateStart, dateEnd);
                        formatDateSet();
                        statisticsChartViewModel.setSandeDate(sandeDate);
                        statisticsChartViewModel.refreshData();
                        if (isfirst2){
                            isfirst2 = false;
                            Previous.setImageResource(R.drawable.left_arrow);
                            Previous.setClickable(true);
                            Next.setImageResource(R.drawable.right_arrow);
                            Next.setClickable(true);
                        }
                        dialog.dismiss();
                    }
                }else {
                    sandeDate = new StatisticsFragment.sandeDate(dateStart, dateEnd);
                    formatDateSet();
                    statisticsChartViewModel.setSandeDate(sandeDate);
                    statisticsChartViewModel.refreshData();
                    isfirst2 = false;
                    dialog.dismiss();
                }

            }
        });

        dialog.show();
    }

    private void formatDateSet() {
        //设置时间格式
        SimpleDateFormat formatter = new SimpleDateFormat();
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+08"));
        String result = "";
        switch (fragmentDateType) {
            case "month":
                if (isfirst) {
                    isfirst = false;
                }
                formatter = new SimpleDateFormat("yyyy年MM月");
                result = formatter.format(sandeDate.getStartDate());
                break;
            case "year":
                if (isfirst) {
                    isfirst = false;
                }
                formatter = new SimpleDateFormat("yyyy年");
                result = formatter.format(sandeDate.getStartDate());
                break;
            case "custom":
                if (isfirst) {
                    isfirst = false;
                    result = "全部时间";
                } else {
                    formatter = new SimpleDateFormat("yyyy年MM月dd日");
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(sandeDate.getEndDate().getTime());
                    calendar.add(Calendar.DATE, -1);
                    Date end = calendar.getTime();
                    result = formatter.format(sandeDate.getStartDate()) + "-" + formatter.format(end);
                    date.setTextSize(12);
                }
                break;
        }
        date.setText(result);
        if (marker!=null){
            if (fragmentDateType.equals("month")) {
                SimpleDateFormat formatter1 = new SimpleDateFormat("MM月");
                String head = formatter1.format(sandeDate.getStartDate());
                marker.setHead(head);
            } else {
                SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy年");
                String head = formatter1.format(sandeDate.getStartDate());
                marker.setHead(head);
            }
            marker.setSandeDate(sandeDate);
        }

    }

    private void setmLineChart() {
        //后台绘制
        mLineChart.setDrawGridBackground(true);
        //设置背景为白色
        mLineChart.setGridBackgroundColor(Color.WHITE);
        //设置描述文本
        mLineChart.getDescription().setEnabled(false);
        //设置支持触控手势
        mLineChart.setTouchEnabled(true);
        // 是否可以缩放
        mLineChart.setScaleYEnabled(true);
        mLineChart.setScaleXEnabled(false);
        //是否可以拖动
        mLineChart.setDragYEnabled(true);
        mLineChart.setDragXEnabled(false);
        //设置顶部偏移，负数无效
        mLineChart.setExtraTopOffset(20f);

        //设置markview
        marker = new MyMarkerView(getContext(), R.layout.markview);
        marker.setChartView(mLineChart);
        mLineChart.setMarker(marker);

        if (fragmentDateType.equals("month")) {
            marker.setIsMonth(true);
            SimpleDateFormat formatter = new SimpleDateFormat("MM月");
            String head = formatter.format(sandeDate.getStartDate());
            marker.setHead(head);
        } else {
            marker.setIsMonth(false);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年");
            String head = formatter.format(sandeDate.getStartDate());
            marker.setHead(head);
        }
        marker.setSandeDate(sandeDate);

        //获取左侧侧坐标轴
        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setDrawAxisLine(false);
        leftAxis.setTextColor(Color.GRAY);
        leftAxis.setLabelCount(4, false);
        leftAxis.setDrawZeroLine(true);
        //设置所有垂直Y轴的的网格线是否显示
        leftAxis.setDrawGridLines(false);
        //设置0轴
        leftAxis.setDrawZeroLine(true);

        //将右边那条线隐藏
        mLineChart.getAxisRight().setEnabled(false);

        //获取X轴
        XAxis xAxis = mLineChart.getXAxis();
        //设置X轴的位置，可上可下
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        //将垂直于X轴的网格线和X轴隐藏
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        //设置X轴上lable颜色和大小
        xAxis.setTextSize(8f);
        xAxis.setTextColor(Color.GRAY);
        if (fragmentDateType.equals("year")) {
            xAxis.setLabelCount(5, true);//设置内容显示多少个
        } else {
            xAxis.setLabelCount(7, true);//设置内容显示多少个
        }
        xAxis.setAvoidFirstLastClipping(true);//是否避免第一个和最后一个数据不显示

        if (statisticsChartViewModel.getLineData().hasObservers()) {
            statisticsChartViewModel.getLineData().removeObservers(getViewLifecycleOwner());
        }
        statisticsChartViewModel.getLineData().observe(getViewLifecycleOwner(), new Observer<ArrayList<Entry>>() {
            @Override
            public void onChanged(ArrayList<Entry> entries) {
                //初始高亮为空
                mLineChart.highlightValue(null);

                values.clear();
                values.addAll(entries);
                float maxY = entries.stream().map(Entry::getY).max(Float::compare).orElse(0f);
                float minY = entries.stream().map(Entry::getY).min(Float::compare).orElse(0f);

                if (maxY == 0) {
                    leftAxis.setAxisMaximum(maxY);
                } else {
                    leftAxis.setAxisMaximum(maxY + 2f);
                }
                leftAxis.setAxisMinimum(minY);


                //设置数据
                setLineData(values);
                mLineChart.invalidate();
            }
        });

        //不显示图例
        mLineChart.getLegend().setEnabled(false);
    }

    private void setLineData(ArrayList<Entry> values) {
        if (mLineChart.getData() != null && mLineChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mLineChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mLineChart.getData().notifyDataChanged();
            mLineChart.notifyDataSetChanged();
        } else {
            // 创建一个数据集,并给它一个类型
            set1 = new LineDataSet(values, null);

            // 在这里设置线
            set1.setColor(Color.parseColor("#FFAB00"));//线颜色
            set1.setCircleColor(Color.parseColor("#FFAB00"));//点颜色
            set1.setDrawCircleHole(true);//是否绘制空心点
            set1.setCircleHoleRadius(2f);//空心点半径
            set1.setCircleHoleColor(Color.WHITE);//空心点颜色
            set1.setLineWidth(2f);//线宽度
            set1.setCircleRadius(4f);//圆圈半径


            set1.setDrawFilled(true);//设置允许填充
            set1.setFillColor(Color.parseColor("#F6DD91"));//设置填充背景

            //设置渐变
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                //设置渐变
//                Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.line_chart_gradient);
//                set1.setFillDrawable(drawable);
//            } else {
//                set1.setFillColor(Color.BLACK);
//            }

            set1.setDrawHighlightIndicators(false);//高亮点指示器是否绘制

            set1.setDrawValues(false);//设置不显示数据点的值

            LineData data = new LineData(set1);
            mLineChart.setData(data);//设置数据

        }
    }

    private void setPiechart(){
        pieChart.setExtraOffsets(30f,10f,30f,10f);//设置偏移量防止显示不全
        pieChart.setDrawHoleEnabled(true);//显示饼心,默认显示
        pieChart.setHoleRadius(55f);//设置饼心的半径，默认为50%
        pieChart.setHoleColor(Color.WHITE);//设置饼心的颜色
        pieChart.setDrawCenterText(true);//是否显示在饼心的文本
        pieChart.setCenterTextSize(12);//设置饼心字体大小
        pieChart.setDrawEntryLabels(false);//设置是否显示标签
        pieChart.getLegend().setEnabled(false);//设置是否显示图例
        pieChart.getDescription().setEnabled(false); //设置是否显示描述文本
        pieChart.setDrawHoleEnabled(true);//启用透明圆
        pieChart.setTransparentCircleRadius(65f);//设置透明圆的半径，默认为比饼心的半径大5%
        pieChart.setTransparentCircleAlpha(255); //设置透明圆的透明度，默认为100，255=不透明，0=全透明
        pieChart.setTransparentCircleColor(Color.parseColor("#F5F2F2"));//设置透明圆的颜色

        //选中后中心展示分类名称及分类金额
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry pie=(PieEntry)e;
                String s=String.valueOf(pie.getValue());
                if (s.endsWith(".0")) {
                    s=String.valueOf((int) pie.getValue());
                }
                pieChart.setCenterText(pie.getLabel()+"\n"+s);
            }

            @Override
            public void onNothingSelected() {
                switch (showType){
                    case "pay":
                        pieChart.setCenterText(new SpannableString("总支出\n"+paynum.getText()));
                        break;
                    case "income":
                        pieChart.setCenterText(new SpannableString("总收入\n"+incomenum.getText()));
                        break;
                }
            }
        });


        if (statisticsChartViewModel.getPieData().hasObservers()) {
            statisticsChartViewModel.getPieData().removeObservers(getViewLifecycleOwner());
        }
        statisticsChartViewModel.getPieData().observe(getViewLifecycleOwner(), new Observer<Map<PieEntry,Integer>>() {
            @Override
            public void onChanged(Map<PieEntry,Integer> Map) {
                pieChart.highlightValue(null);//设置初始不显示高亮
                ArrayList<PieEntry> pieEntries=new ArrayList<>(Map.keySet());
                ArrayList<Integer> colors=new ArrayList<>(Map.values());
                setPieData(pieEntries,colors);
                pieChart.invalidate();
            }
        });

    }

    private void setPieData(ArrayList<PieEntry> values,ArrayList<Integer> colors){
        if (pieChart.getData() != null && pieChart.getData().getDataSetCount() > 0) {
            set2 =  (PieDataSet) pieChart.getData().getDataSet();
            set2.setValues(values);
            set2.setColors(colors);
            pieChart.getData().notifyDataChanged();
            pieChart.notifyDataSetChanged();
        }else {
            set2=new PieDataSet(values,null);//设置数据
            set2.setColors(colors);//设置颜色

//        dataSet.setValueFormatter(new PercentFormatter());//设置数据格式，返回值为百分比
            //设置数据格式，返回值为标签值
            set2.setValueFormatter(new ValueFormatter() {
                @Override
                public String getPieLabel(float value, PieEntry pieEntry) {
                    return pieEntry.getLabel();
                }
            });
            set2.setValueTextSize(10f);//设置显示数据字体大小
            set2.setValueTextColor(Color.BLACK);//设置显示数据字体颜色

//        dataSet.setValueLineColor(Color.BLACK);//设置折线颜色
            set2.setUsingSliceColorAsValueLineColor(true);//设置折线颜色为饼块颜色
            //设置数据线距离图像内部园心的距离，以百分比来计算
            set2.setValueLinePart1OffsetPercentage(100f);
            //当valuePosition在外部时，表示行前半部分的长度(即折线靠近圆的那端长度)
            set2.setValueLinePart1Length(0.5f);
            ///当valuePosition位于外部时，表示行后半部分的长度*(即折线靠近百分比那端的长度)
            set2.setValueLinePart2Length(0.1f);
            //设置Y值的位置在圆外
            set2.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

            set2.setSliceSpace(1f);//饼块之间的间隔

            PieData data=new PieData(set2);
            pieChart.setData(data);//设置数据
        }
    }

    private void setTypeStatistics(){
        if (statisticsChartViewModel.getTypeStatistics().hasObservers()) {
            statisticsChartViewModel.getTypeStatistics().removeObservers(getViewLifecycleOwner());
        }
        statisticsChartViewModel.getTypeStatistics().observe(getViewLifecycleOwner(), new Observer<List<TypeStatisticsItem>>() {
            @Override
            public void onChanged(List<TypeStatisticsItem> typeStatisticsItems) {
                TypeStatisticsAdapter typeStatisticsAdapter=new TypeStatisticsAdapter(typeStatisticsItems);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                typeStatistics.setLayoutManager(layoutManager);
                typeStatistics.setAdapter(typeStatisticsAdapter);
            }
        });
    }

    private void setTable(){
        smartTable.getConfig().setShowXSequence(false);//设置是否显示顶部序号列
        smartTable.getConfig().setShowYSequence(false);//设置是否显示左侧序号列
        smartTable.getConfig().setShowTableTitle(false);//设置是否显示标题
        smartTable.getConfig().setContentGridStyle(new LineStyle(0, android.R.color.white));//设置表格网格样式
        smartTable.getConfig().setColumnTitleGridStyle(new LineStyle(0, android.R.color.white));//设置表头网格样式
        //设置行背景灰白交替
        smartTable.getConfig().setContentCellBackgroundFormat(new ICellBackgroundFormat<CellInfo>() {
            @Override
            public void drawBackground(Canvas canvas, Rect rect, CellInfo cellInfo, Paint paint) {
                if(cellInfo.row%2==0){
                    int color=ContextCompat.getColor(getContext(),R.color.content_bg);
                    paint.setColor(color);
                    canvas.drawRect(rect,paint);
                }
            }
            @Override
            public int getTextColor(CellInfo cellInfo) {
                int color=ContextCompat.getColor(getContext(),R.color.brown_dark);
                return color;
            }
        });

        int width = DensityUtils.dp2px(getContext(),20); //指定图标大小
        int height = DensityUtils.dp2px(getContext(),25); //指定图标大小
        Column<String> info=new Column<String>("", "dateInfo", new BitmapDrawFormat<String>(width,height) {
            @Override
            protected Bitmap getBitmap(String s, String value, int position) {
                Drawable drawable=ContextCompat.getDrawable(getActivity(),R.drawable.bill_statistics_info_right);
                int w = drawable.getIntrinsicWidth();
                int h = drawable.getIntrinsicHeight();
                Bitmap.Config config =
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565;
                Bitmap bitmap = Bitmap.createBitmap(w, h, config);
                //注意，下面三行代码要用到，否则在View或者SurfaceView里的canvas.drawBitmap会看不到图
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, w, h);
                drawable.draw(canvas);
                return bitmap;
            }
        });
        info.setFixed(true);
        //设置跳转列点击事件
        info.setOnColumnItemClickListener(new OnColumnItemClickListener<String>() {
            @Override
            public void onClick(Column<String> column, String value, String s, int position) {
                int dateValue= Integer.parseInt(value);
                boolean isMonth=false;
                if (fragmentDateType.equals("month")){
                    isMonth=true;
                }
                Intent intent=new Intent(getActivity(), BillstatisticsinfoActivity.class);
                Calendar calendar=Calendar.getInstance(TimeZone.getTimeZone("GMT+08"));
                calendar.setTime(sandeDate.getStartDate());
                int year=calendar.get(Calendar.YEAR);
                int month=calendar.get(Calendar.MONTH);
                Date date1,date2;
                if (isMonth){
                    calendar.set(year,month,dateValue,0,0,0);
                    date1=calendar.getTime();
                    calendar.add(Calendar.DATE,1);
                    date2=calendar.getTime();
                }else {
                    calendar.set(year,dateValue-1,1,0,0,0);
                    date1=calendar.getTime();
                    int m=calendar.getMaximum(Calendar.DAY_OF_MONTH);
                    calendar.add(Calendar.DATE,m);
                    date2=calendar.getTime();
                }
                intent.putExtra("dateStart",date1.getTime());
                intent.putExtra("dateEnd",date2.getTime());
                intent.putExtra("isMonth",isMonth);
                getActivity().startActivity(intent);
            }
        });

        Column<String> dateColumn = new Column<>("时间", "date");
        dateColumn.setFast(true);
        dateColumn.setFormat(new IFormat<String>() {
            @Override
            public String format(String s) {
                if (fragmentDateType.equals("month")){
                    return s+"日";
                }
                if (fragmentDateType.equals("year")){
                    return s+"月";
                }
                return null;
            }
        });
        Column<String> incomeColumn = new Column<>("收入", "income");
        Column<String> payColumn = new Column<>("支出", "pay");
        Column<String> balanceColumn = new Column<>("结余", "balance");

        statisticsChartViewModel.getBillStatistics().observe(getViewLifecycleOwner(), new Observer<List<BillStatistics>>() {
            @Override
            public void onChanged(List<BillStatistics> billStatistics) {
                // 按日期从小到大排序
                billStatistics.sort((list1, list2) -> {
                    String date1 = list1.date;
                    String date2 = list2.date;
                    int d1=Integer.parseInt(date1);
                    int d2=Integer.parseInt(date2);
                    return d2-d1;
                });

                TableData<BillStatistics> tableData = new TableData<>("账单统计",billStatistics,dateColumn,incomeColumn,payColumn,balanceColumn,info);
                smartTable.setTableData(tableData);
                smartTable.invalidate();
            }
        });

    }

    private class TypeStatisticsAdapter extends RecyclerView.Adapter<TypeStatisticsAdapter.ViewHolder>{
        private List<TypeStatisticsItem> list;
        public TypeStatisticsAdapter(List<TypeStatisticsItem> list){
            this.list=list;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.type_statistics_item,parent,false);
            TypeStatisticsAdapter.ViewHolder viewHolder = new TypeStatisticsAdapter.ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TypeStatisticsItem typeStatisticsItem=list.get(position);
            holder.itemcolor.getBackground().setColorFilter(Color.parseColor(typeStatisticsItem.color), PorterDuff.Mode.SRC_IN);
            Glide.with(holder.itemView)
                    .load(typeStatisticsItem.ItemLogo)
                    .into(holder.ItemLogo);
            holder.project.setText(typeStatisticsItem.project);
            holder.frequency.setText(typeStatisticsItem.frequency+"(笔)");

            String s=typeStatisticsItem.getProportion();
            holder.proportion.setText(s);
            s=s.substring(0,s.length()-1);
            if (s.contains(".")){
                int i=s.indexOf(".");
                s=s.substring(0,i);
            }
            int progress = Integer.parseInt(s);
            holder.progressBar.setProgress(progress);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isMonth=false;
                    if (fragmentDateType.equals("Month")){
                        isMonth=true;
                    }
                    Intent intent=new Intent(v.getContext(),BillstatisticsinfoActivity.class);
                    intent.putExtra("dateStart",sandeDate.getStartDate().getTime());
                    intent.putExtra("dateEnd",sandeDate.getEndDate().getTime());
                    intent.putExtra("isMonth",isMonth);
                    intent.putExtra("typeid",typeStatisticsItem.typeid);
                    intent.putExtra("typename",typeStatisticsItem.project);
                    v.getContext().startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ItemLogo;
            TextView project,frequency,proportion;
            View itemcolor;
            ProgressBar progressBar;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                itemcolor=itemView.findViewById(R.id.ItemColor);
                ItemLogo=itemView.findViewById(R.id.ItemLogo);
                project=itemView.findViewById(R.id.Project);
                frequency=itemView.findViewById(R.id.frequency);
                proportion=itemView.findViewById(R.id.proportion);
                progressBar=itemView.findViewById(R.id.progressBar);
            }
        }
    }

    public static class TypeStatisticsItem{
        private String project,ItemLogo,color,typeid;
        private int frequency;
        private Float proportion;
        public TypeStatisticsItem(String project,String ItemLogo,int frequency,String color,Float proportion,String typeid){
            this.proportion=proportion;
            this.project=project;
            this.frequency=frequency;
            this.ItemLogo=ItemLogo;
            this.color=color;
            this.typeid=typeid;
        }
        public String getProportion() {
            DecimalFormat df = new DecimalFormat("#.##%");
            return df.format(proportion);
        }
    }

    @SmartTable(name="账单统计")
    public static class BillStatistics {
        @SmartColumn(id =1,name = "时间")
        private String date;
        @SmartColumn(id=2,name="收入")
        private String income;
        @SmartColumn(id=3,name="支出")
        private String pay;
        @SmartColumn(id=4,name="结余")
        private String balance;
        @SmartColumn(id=5,name="详情")
        private String dateInfo;
        public BillStatistics(String date,String income,String pay,String balance){
            this.date=String.valueOf(Integer.parseInt(date));
            this.pay=pay;
            this.income=income;
            this.balance=balance;
            this.dateInfo=String.valueOf(Integer.parseInt(date));
            if (income.endsWith(".0")){
                this.income=income.substring(0,income.length()-2);
            }
            if (pay.endsWith(".0")){
                this.pay=pay.substring(0,pay.length()-2);
            }
            if (balance.endsWith(".0")){
                this.balance=balance.substring(0,balance.length()-2);
            }
        }
    }

}