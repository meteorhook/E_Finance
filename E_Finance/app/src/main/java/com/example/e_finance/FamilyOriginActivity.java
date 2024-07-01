package com.example.e_finance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class FamilyOriginActivity extends AppCompatActivity {

    private Adapter adapter;
    private List<FamilyOriginFragment> fragmentlist=new ArrayList<>();
    private ViewPager2 fragment;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(FamilyOriginActivity.this);
        statusBar.setColor(R.color.transparent);
        setContentView(R.layout.activity_family_origin);

        fragment=findViewById(R.id.fragment);
        tabLayout=findViewById(R.id.tab_layout);



        if (fragmentlist.size()==0){
            fragmentlist.add(new FamilyOriginFragment(true));
            fragmentlist.add(new FamilyOriginFragment(false));
            adapter=new Adapter(FamilyOriginActivity.this,fragmentlist);
            fragment.setAdapter(adapter);
            new TabLayoutMediator(tabLayout, fragment, (tab, position) -> {
                // 为每个标签设置文本
                switch (position){
                    case 0:
                        tab.setText("加入家庭");
                        break;
                    case 1:
                        tab.setText("创建家庭");
                        break;
                }
            }).attach(); // 连接 TabLayout 和 ViewPager2
        }
    }

    public class Adapter extends FragmentStateAdapter {
        private List<FamilyOriginFragment> fragmentList;
        public Adapter(@NonNull FragmentActivity fragmentActivity, List<FamilyOriginFragment> fragmentList) {
            super(fragmentActivity);
            this.fragmentList=fragmentList;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getItemCount() {
            return fragmentList.size();
        }
    }
}