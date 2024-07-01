package com.example.e_finance;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class colorDecoration extends RecyclerView.ItemDecoration {
    private int spacing;
    public colorDecoration(int spacings) {
        //获取需要设置的间距值
        this.spacing = spacings;
    }
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        // 获取当前View的位置
        int position = parent.getChildAdapterPosition(view);
        // 判断是否是最后一个项目
        if (position != state.getItemCount() - 1) {
            //设置bottom padding
            outRect.right = spacing;
        }
        if (position==0){
            outRect.left = spacing;
        }
    }

}
