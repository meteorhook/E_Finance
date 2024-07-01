package com.example.e_finance.ui.person;

import android.graphics.Color;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import com.example.e_finance.DesireActivity;
import com.example.e_finance.R;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class PersonViewModel extends ViewModel {
    private List<Integer> list=new ArrayList<>(5);
    private List<String> prompt=new ArrayList<>(5);
    private int img=1,all=0,complete=0;
    private MutableLiveData<Number[]> data;
    private Boolean isfirst=true;
    public PersonViewModel() {
      list.add(R.drawable.panda);
      list.add(R.drawable.panda1);
      list.add(R.drawable.panda4);
      list.add(R.drawable.panda3);
      list.add(R.drawable.panda2);
      prompt.add("记得每天都来记账哦~");
      prompt.add("结余卡片左滑设置预算~");
      prompt.add("周期账单可以自动记账哦~");
      prompt.add("查询账单可以试试统计~");
      prompt.add("要不要设置一个心愿呢~");
      data=new MutableLiveData<>();

      getDesire();
    }

    public List<Integer> getList() {
        return list;
    }

    public void setImg(int img) {
        this.img = img;
        if (img>=list.size()){
            this.img=0;
        }
    }

    public MutableLiveData<Number[]> getData() {
        return data;
    }

    public List<String> getPrompt() {
        return prompt;
    }

    public int getImg() {
        return img;
    }

    public Boolean getIsfirst() {
        return isfirst;
    }

    public void setIsfirst(Boolean isfirst) {
        this.isfirst = isfirst;
    }

    public void getDesire(){
        all=0;
        complete=0;
        LCQuery<LCObject> query = new LCQuery<>("Edesire");
        query.whereEqualTo("Euser", LCUser.getCurrentUser());
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            public void onSubscribe(Disposable disposable) {}
            public void onNext(List<LCObject> desires) {
                for (LCObject lcObject:desires){
                    Boolean state=lcObject.getBoolean("Dstate");
                    all++;
                    if (state){
                        complete++;
                    }
                }
                Number[] num={all,complete};
                data.setValue(num);
            }
            public void onError(Throwable throwable) {}
            public void onComplete() {}
        });
    }
}