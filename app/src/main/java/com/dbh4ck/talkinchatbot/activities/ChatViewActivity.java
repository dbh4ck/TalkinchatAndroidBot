package com.dbh4ck.talkinchatbot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.dbh4ck.talkinchatbot.R;
import com.dbh4ck.talkinchatbot.adapter.ChatAdapter;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatViewActivity extends AppCompatActivity {

    private ChatAdapter chatAdapter;
    private final List<String> messages = new ArrayList<>();
    private String roomName = "";
    private TextView titleTv;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_view);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }

        Intent intent = getIntent();
        if(intent != null){
            roomName = intent.getStringExtra("room");
        }

        EventBus.getDefault().register(this);

        titleTv = findViewById(R.id.title_room);
        titleTv.setText(String.format(Locale.getDefault(), "Currently Joined In %s", roomName));
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        chatAdapter = new ChatAdapter();

        recyclerView.setAdapter(chatAdapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String result) {
        messages.add(result);
        chatAdapter.updateMessages(messages);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}