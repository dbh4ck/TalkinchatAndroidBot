package com.dbh4ck.talkinchatbot.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.dbh4ck.talkinchatbot.MainApp;
import com.dbh4ck.talkinchatbot.R;
import com.dbh4ck.talkinchatbot.service.OperationController;
import com.dbh4ck.talkinchatbot.utils.Utils;
import com.google.android.material.color.MaterialColors;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import static com.dbh4ck.talkinchatbot.utils.Constants.*;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText userNameEditText, userPasswordEditText, roomNameEditText;
    private String room = "";
    private CheckBox rememberMeCheckBox;
    public ProgressDialog progressDialog;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userNameEditText = findViewById(R.id.bot_username);
        userPasswordEditText = findViewById(R.id.bot_password);
        roomNameEditText = findViewById(R.id.room_name);
        rememberMeCheckBox = findViewById(R.id.remember_me_check_box);

        checkIfCredentialsExist();
        Button loginBtn = findViewById(R.id.btn_login);
        loginBtn.setOnClickListener(this);
    }

    private void checkIfCredentialsExist() {
        String user = MainApp.getMainApp().getSharedPrefs().getString(USERNAME, "");
        String pass = MainApp.getMainApp().getSharedPrefs().getString(PASSWORD, "");
        String room = MainApp.getMainApp().getSharedPrefs().getString(ROOM, "");

        userNameEditText.setText(user);
        userPasswordEditText.setText(pass);
        roomNameEditText.setText(room);
    }

    @Override
    public void onClick(View v) {
        String user = userNameEditText.getText().toString().trim();
        String pwd = userPasswordEditText.getText().toString().trim();
        room = roomNameEditText.getText().toString().trim().toLowerCase(Locale.ROOT);

        if(user.isEmpty() || pwd.isEmpty() || room.isEmpty()){
            Toast.makeText(this, "Please enter all fields!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(rememberMeCheckBox.isChecked()){
            Utils.getInstance().persistCredentials(user, pwd, room);
        }

        OperationController.getController().attemptLogin(user, pwd, room);
        showProgressBar(MainActivity.this, "Signing In...");

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String result) {
        if(result.equals(SUCCESS)){
            dismissProgressBar();
            Toast.makeText(MainActivity.this, "Logged In", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, ChatViewActivity.class).putExtra("room", room));
        }else if(result.equals(FAILED)){
            Toast.makeText(MainActivity.this, "Unauthorized", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void showProgressBar(Context context, String message){
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setIndeterminate(false);
        progressDialog.show();
        ProgressBar progressbar = (ProgressBar) progressDialog.findViewById(android.R.id.progress);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            progressbar.getIndeterminateDrawable().setColorFilter(MaterialColors.getColor(progressbar, R.attr.colorPrimary), PorterDuff.Mode.SRC_IN);
        }
    }

    private void dismissProgressBar(){
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
}