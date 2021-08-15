package com.dbh4ck.talkinchatbot.service;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.widget.Toast;
import com.dbh4ck.talkinchatbot.MainApp;
import com.dbh4ck.talkinchatbot.utils.Constants;
import com.dbh4ck.talkinchatbot.utils.Utils;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Handler;

import static com.dbh4ck.talkinchatbot.utils.Constants.*;

public class OperationController {

    private static OperationController _instance = null;
    public WebSocket webSocket;
    public WebSocketFactory webSocketFactory;

    private OperationController(){
        webSocketFactory = new WebSocketFactory();
    }

    public static OperationController getController() {
        if (_instance == null) {
            _instance = new OperationController();
        }
        return _instance;
    }

    public void attemptLogin(String userName, String passWord, String roomName) {
        try {
            if (webSocket != null && webSocket.isOpen()) {
                this.webSocket.disconnect();
            }
            this.webSocket = this.webSocketFactory.createSocket(SOCKET_URL);
            this.webSocket.addListener(new SocketEventListener(webSocket, userName, passWord, roomName));
            this.webSocket.setPingInterval(60 * 1000);
            String buildInfo = Utils.getInstance().getBuildInfo();
            String androidId = Utils.getInstance().getAndroidId();

            if (androidId == null) {
                androidId = "null";
            }
            this.webSocket.addHeader("m", buildInfo);
            this.webSocket.addHeader("i", androidId);
            new Thread(
                    () -> {
                        try {
                            this.webSocket.connect();
                        } catch (WebSocketException e) {
                            e.printStackTrace();
                        }
                    }
            ).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleSocketMsg(WebSocket webSocket, String userName, String roomName, String rawTxt) {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(rawTxt);
            String objHandlerStr = jsonObject.getString(HANDLER);

            // On Success Login, make Bot Join Group
            if(objHandlerStr.equals(EVENT_LOGIN)){
                if(jsonObject.getString(TYPE).equals(SUCCESS)){
                    if(webSocket != null && webSocket.isOpen()){
                        webSocket.sendText(Utils.getInstance().prepareJsonForGroupJoin(roomName));
                    }
                    EventBus.getDefault().post(SUCCESS);
                }
                if(jsonObject.getString(TYPE).equals(FAILED)){
                    String reason = jsonObject.getString(REASON);
                    EventBus.getDefault().post(FAILED);
                }
            }

            if(objHandlerStr.equals(EVENT_ROOM)){
                try{
                    if(jsonObject.getString(TYPE).equals(MSG_TYPE_TEXT)){
                        if(jsonObject.getString("from") != null || jsonObject.getString("body") != null) {
                            String sender = jsonObject.getString("from");
                            String body = jsonObject.getString("body");
                            @SuppressLint("SimpleDateFormat") SimpleDateFormat s = new SimpleDateFormat("hh:mm a");
                            final String msgAtTime = s.format(new Date());
                            String messageStr = String.format(Locale.getDefault(), "%s [%s]: %s", msgAtTime, sender, body);
                            EventBus.getDefault().post(messageStr);
                        }
                    }
                }
                catch (JSONException ex){
                    ex.printStackTrace();
                }

            }

            // Welcome User -- Bot Event
            if(objHandlerStr.equals(EVENT_ROOM)){
                if(jsonObject.getString(TYPE).equals(USER_JOINED)){
                    sendGroupMsg(webSocket, jsonObject.getString(NAME), jsonObject.getString(USERNAME) + ": Welcome \uD83D\uDC7B");
                }
            }

            // Handle Rest Room Events
            if(objHandlerStr.equals(EVENT_ROOM)){
                if(jsonObject.getString(TYPE).equals(MSG_TYPE_TEXT)){
                    // To make bot join other Groups

                    if(jsonObject.getString(MSG_BODY).equals("bot")){
                        sendGroupMsg(webSocket, jsonObject.getString(ROOM), "hey There! I'm Online Now (via Android)");
                    }

                    if(jsonObject.getString(MSG_BODY).startsWith("!join ")){
                        String groupToJoin = jsonObject.getString(MSG_BODY).toLowerCase();
                        if(webSocket != null && webSocket.isOpen()){
                            webSocket.sendText(Utils.getInstance().prepareJsonForGroupJoin(groupToJoin.substring(6)));
                        }
                    }

                    // Handle the Spin
                    if(jsonObject.getString(MSG_BODY).equalsIgnoreCase(".s") || jsonObject.getString(MSG_BODY).equalsIgnoreCase("spin")){

                        Random r = new Random();
                        int randomItem = r.nextInt(Constants.SPIN_MSG_STRINGS.size());
                        String randomElement = Constants.SPIN_MSG_STRINGS.get(randomItem);
                        sendGroupMsg(webSocket, jsonObject.getString(ROOM), randomElement);
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(MainApp.getMainApp(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendGroupMsg(WebSocket webSocket, String roomName, String msgBody) {
        String str = "";

        if (webSocket != null && webSocket.isOpen()){
            webSocket.sendText(Utils.getInstance().prepareJsonForSendGroupMsg(roomName, msgBody, str, str));
        }
    }

}
