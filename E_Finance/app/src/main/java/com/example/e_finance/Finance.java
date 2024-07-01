package com.example.e_finance;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.example.e_finance.util.StatusBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.e_finance.databinding.ActivityFinanceBinding;

public class Finance extends AppCompatActivity {

    private ActivityFinanceBinding binding;
    private BottomNavigationView navView;
    private ConstraintLayout constraintLayout;
    private ConstraintLayout view;
    public void HideView(boolean isHide){
        if (navView!=null&&constraintLayout!=null&&view!=null){
            if (isHide){
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 1, 0.75f, 0.5f, 0);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.play(objectAnimator);
                animatorSet.setDuration(500);
                animatorSet.start();
                navView.setVisibility(View.VISIBLE);
                constraintLayout.setBackgroundColor(getResources().getColor(R.color.grey));
            }else {
                constraintLayout.setBackgroundColor(Color.parseColor("#E48B8B"));
                navView.setVisibility(View.INVISIBLE);
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(Finance.this);
        statusBar.setColor(R.color.transparent);

        binding = ActivityFinanceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navView = findViewById(R.id.nav_view);
        constraintLayout=findViewById(R.id.container);
        view=findViewById(R.id.masklayer);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_bill, R.id.navigation_statistics, R.id.navigation_person)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_finance);
        NavigationUI.setupWithNavController(binding.navView, navController);

    }

}