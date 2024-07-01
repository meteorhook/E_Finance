package com.example.e_finance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.e_finance.customview.billDecoration;
import com.example.e_finance.util.StatusBar;
import com.iflytek.sparkchain.core.LLM;
import com.iflytek.sparkchain.core.LLMCallbacks;
import com.iflytek.sparkchain.core.LLMConfig;
import com.iflytek.sparkchain.core.LLMError;
import com.iflytek.sparkchain.core.LLMEvent;
import com.iflytek.sparkchain.core.LLMResult;

import java.util.ArrayList;
import java.util.List;

import cn.leancloud.LCUser;

public class AIChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Button confirm;
    private EditText editText;
    private List<ChatMessage> chatMessages=new ArrayList<>();
    private String results="",question="";
    private ChatAdapter adapter;
    private String name="",userpic="",Error="",Event="";

    private Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
//                    Log.d("TAG","start");
                    chatMessages.set(chatMessages.size()-1,new ChatMessage("","","。。。",true));
                    adapter.notifyItemChanged(adapter.getItemCount()-1);
                    break;
                case 1:
//                    Log.d("TAG","continue");
                    chatMessages.set(chatMessages.size()-1,new ChatMessage("","",results+"。。。",true));
                    adapter.notifyItemChanged(adapter.getItemCount()-1);

                    int distance=getTotalVisibleItemsHeight(recyclerView) - recyclerView.getHeight();
                    Log.d("TAG","dis:"+distance);
                    if (distance>0){
                        recyclerView.smoothScrollBy(0, distance+1000);
                    }
//                    recyclerView.smoothScrollToPosition(adapter.getItemCount()-1);
                    break;
                case 2:
//                    Log.d("TAG","end");
                    chatMessages.set(chatMessages.size()-1,new ChatMessage("","",results,true));
                    adapter.notifyItemChanged(adapter.getItemCount()-1);
                    distance=getTotalVisibleItemsHeight(recyclerView) - recyclerView.getHeight();
                    Log.d("TAG","end_dis:"+distance);
                    if (distance>0){
                        recyclerView.smoothScrollBy(0, distance);
                    }
                    results="";
                    editText.setHint("");
                    confirm.setEnabled(true);
                    break;
                case 3:
                    Toast.makeText(AIChatActivity.this,"出错了，"+Error,Toast.LENGTH_SHORT).show();
                    Error="";
                    break;
                case 4:
                    Toast.makeText(AIChatActivity.this,"出错了，"+Event,Toast.LENGTH_SHORT).show();
                    Event="";
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置状态栏颜色为透明
        StatusBar statusBar = new StatusBar(AIChatActivity.this);
        statusBar.setColor(R.color.transparent);

        setContentView(R.layout.activity_aichat);

        init();
    }
    private void init(){
        LCUser user = LCUser.getCurrentUser();
        userpic=user.getString("photo");
        name=user.getString("nickName");

        recyclerView=findViewById(R.id.recycleView);
        confirm=findViewById(R.id.button);
        editText=findViewById(R.id.editText);
        chatMessages.add(new ChatMessage("","","你好，"+name+"。",true));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter=new ChatAdapter(chatMessages);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new billDecoration(50, false,true));

        LLMConfig llmConfig = LLMConfig.builder();
        llmConfig.domain("generalv3.5");
        llmConfig.url("wss://spark-api.xf-yun.com/v3.5/chat");//如果使用generalv2，domain和url都可缺省，SDK默认；如果使用general，url可缺省，SDK会自动补充；如果是其他，则需要设置domain和url。
        llmConfig.chatID("1");
        llmConfig.maxToken(4096);
        LLM llm = new LLM(llmConfig);

        //注册异步调用
        LLMCallbacks llmCallbacks = new LLMCallbacks() {
            @Override
            public void onLLMResult(LLMResult llmResult, Object usrContext) {
                results+=llmResult.getContent();
                Message msg = Message.obtain();
                msg.what = llmResult.getStatus();
                handler.sendMessage(msg);
//                Log.d("TAG","onLLMResult:" + " " + llmResult.getRole() + " " + llmResult.getContent());
            }
            @Override
            public void onLLMEvent(LLMEvent event, Object usrContext) {
                Event=event.getEventMsg();
                Message msg = Message.obtain();
                msg.what = 4;
                handler.sendMessage(msg);
//                Log.w("TAG","onLLMEvent:" + " " + event.getEventID() + " " + event.getEventMsg());
            }
            @Override
            public void onLLMError(LLMError error, Object usrContext) {
                Error=error.getErrMsg();
                Message msg = Message.obtain();
                msg.what = 3;
                handler.sendMessage(msg);
//                Log.e("TAG","onLLMError:" + " " + error.getErrCode() + " " + error.getErrMsg());
            }
        };
        llm.registerLLMCallbacks(llmCallbacks);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                question=editText.getText().toString();
                if (question.isEmpty()){
                    Toast.makeText(AIChatActivity.this,"请输入您的问题。",Toast.LENGTH_SHORT).show();
                }else {
                    chatMessages.add(new ChatMessage(name,userpic,question,false));
                    chatMessages.add(new ChatMessage("","","。",true));
                    adapter.notifyDataSetChanged();
                    int distance=getTotalVisibleItemsHeight(recyclerView) - recyclerView.getHeight();
                    if (distance>0){
                        recyclerView.smoothScrollBy(0, distance);
                    }
//                    recyclerView.smoothScrollToPosition(adapter.getItemCount()-1);
                    // 异步，无上下文调用
                    llm.arun(question);
                    editText.setText("");
                    editText.setHint("请等待AI回答完成再继续提问");
                    confirm.setEnabled(false);
                    //收起软键盘
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    editText.clearFocus();
                }
            }
        });
    }

    public int getTotalVisibleItemsHeight(RecyclerView recyclerView) {
        int totalHeight = 0;
        final int childCount = recyclerView.getAdapter().getItemCount();
        LinearLayoutManager linearLayoutManager=(LinearLayoutManager)recyclerView.getLayoutManager();
        for (int i = 0; i < childCount; i++) {
//            final View child = recyclerView.getChildAt(i);
            final View child = linearLayoutManager.findViewByPosition(i);
            if (child!=null){
                totalHeight += child.getHeight();
            }
//            try {
//                totalHeight += child.getHeight();
//            }catch (Throwable throwable){
//                Log.e("TAG",throwable.getMessage());
//            }
        }
        return totalHeight;
    }
    public class ChatMessage{
        private Boolean isAI=true;
        private String message,pic,name;
        public ChatMessage(String name,String pic,String message,Boolean isAI){
            this.isAI=isAI;
            this.name=name;
            this.pic=pic;
            this.message=message;
        }

        public Boolean getAI() {
            return isAI;
        }

        public String getName() {
            return name;
        }

        public String getPic() {
            return pic;
        }

        public String getMessage() {
            return message;
        }
    }

    public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{
        private List<ChatMessage> chatMessages;
        public ChatAdapter(List<ChatMessage> chatMessages){
            this.chatMessages=chatMessages;
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item,parent,false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatMessage chat=chatMessages.get(position);
            if (chat.isAI){
                holder.ai.setVisibility(View.VISIBLE);
                holder.user.setVisibility(View.GONE);
                holder.aiMsg.setText(chat.message);
            }else {
                holder.ai.setVisibility(View.GONE);
                holder.user.setVisibility(View.VISIBLE);
                holder.userName.setText(chat.name);
                holder.userMsg.setText(chat.message);
                Glide.with(AIChatActivity.this)
                        .load(chat.pic)
                        .transform(new CircleCrop())
                        .into(holder.userPic);
            }
        }

        //,List<Object> payload
//        @Override
//        public void onBindViewHolder(@NonNull ViewHolder holder, int position,List<Object> payload) {
//            ChatMessage chat=chatMessages.get(position);
//
//            if (payload.isEmpty()){
//                if (chat.isAI){
//                    holder.ai.setVisibility(View.VISIBLE);
//                    holder.user.setVisibility(View.GONE);
//                    holder.aiMsg.setText(chat.message);
//                }else {
//                    holder.ai.setVisibility(View.GONE);
//                    holder.user.setVisibility(View.VISIBLE);
//                    holder.userName.setText(chat.name);
//                    holder.userMsg.setText(chat.message);
//                    Glide.with(AIChatActivity.this)
//                            .load(chat.name)
//                            .transform(new CircleCrop())
//                            .into(holder.userPic);
//                }
//            }else if ("message".equals(payload.get(0))){
//                if (chat.isAI){
//                    holder.ai.setVisibility(View.VISIBLE);
//                    holder.user.setVisibility(View.GONE);
//                    holder.aiMsg.setText(chat.message);
//                }else {
//                    holder.ai.setVisibility(View.GONE);
//                    holder.user.setVisibility(View.VISIBLE);
//                    holder.userName.setText(chat.name);
//                    holder.userMsg.setText(chat.message);
//                }
//            }
//
//
//        }

        @Override
        public int getItemCount() {
            return chatMessages.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private ConstraintLayout ai,user;
            private TextView userMsg,aiMsg,userName;
            private ImageView userPic;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ai=itemView.findViewById(R.id.AIitem);
                user=itemView.findViewById(R.id.Useritem);
                userMsg=itemView.findViewById(R.id.Usermsg);
                aiMsg=itemView.findViewById(R.id.AImsg);
                userName=itemView.findViewById(R.id.Username);
                userPic=itemView.findViewById(R.id.Userpic);
            }
        }
    }
}