package com.example.e_finance;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class billDecoration extends RecyclerView.ItemDecoration {
        private int spacing;
        private boolean isParent=false,last=false;

        public billDecoration(int spacings) {
            //获取需要设置的间距值
            this.spacing = spacings;
        }
        public billDecoration(int spacings, boolean isParent) {
            //获取需要设置的间距值
            this.spacing = spacings;
            this.isParent=isParent;
        }
        public billDecoration(int spacings, boolean isParent,boolean last) {
            //获取需要设置的间距值
            this.spacing = spacings;
            this.isParent=isParent;
            this.last=last;
        }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        // 获取当前View的位置
        int position = parent.getChildAdapterPosition(view);

        if (isParent){
            outRect.bottom = spacing;
        }//判断是否只为最后一个设置间距
        else if (last) {
            if (position == state.getItemCount() - 1) {
                //设置bottom padding
                outRect.bottom = spacing;
            }
        }
        // 判断是否是最后一个项目
        else if (position != state.getItemCount() - 1) {
            //设置bottom padding
            outRect.bottom = spacing;
        }
    }

}
