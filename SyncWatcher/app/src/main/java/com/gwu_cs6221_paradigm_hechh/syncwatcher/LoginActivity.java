package com.gwu_cs6221_paradigm_hechh.syncwatcher;

import android.support.v7.app.ActionBarActivity;
import android.os.*;
import android.view.*;
import android.content.*;
import android.widget.*;
import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends ActionBarActivity implements ConnectionCallback {

    private String serverIP;
    private String userName;
    private String passWord;
    public final static String EXT_serverIP = "ext.LoginActivity.serverIP";
    public final static String EXT_userName = "ext.LoginActivity.userName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        EditText sIP = (EditText) findViewById(R.id.editServerIP);
        EditText username = (EditText) findViewById(R.id.editUsername);
        EditText password = (EditText) findViewById(R.id.editPassword);
        sIP.setText("192.168.182.88");
        username.setText("new");
        password.setText("new");
    }

    private void updateInfo() {
        EditText sIP = (EditText) findViewById(R.id.editServerIP);
        EditText username = (EditText) findViewById(R.id.editUsername);
        EditText password = (EditText) findViewById(R.id.editPassword);
        serverIP = sIP.getText().toString();
        userName = username.getText().toString();
        passWord = password.getText().toString();
    }

    public void signIn(View nView) {
        updateInfo();
        JSONObject newJson = new JSONObject();
        try {
            newJson.put("pw", passWord);
        }
        catch (JSONException e) {
            TextView notice = (TextView) findViewById(R.id.textNotice);
            notice.setText("JSON Put Failed");
        }
        String payload = newJson.toString();
        ConnectionManager hc = new ConnectionManager();
        String response = hc.httpRequest("POST", serverIP, "login/" + userName, payload, this, this);
        TextView notice = (TextView) findViewById(R.id.textNotice);
        notice.setText(response);
    }

    public void createAccount(View nView) {
        updateInfo();
        Intent nIntent = new Intent(this, CreateActivity.class);
        nIntent.putExtra(EXT_serverIP, serverIP);
        startActivity(nIntent);
    }

    private void activeRoom() {
        Intent nIntent = new Intent(this, RoomListActivity.class);
        nIntent.putExtra(EXT_serverIP, serverIP);
        nIntent.putExtra(EXT_userName, userName);
        startActivity(nIntent);
    }

    public void callBack(String result) {
        TextView notice = (TextView) findViewById(R.id.textNotice);
        String status;
        String info;
        if (result.equals("")) {
            notice.setText("No Response");
            return;
        }
        try {
            JSONObject newJson = new JSONObject(result);
            status = newJson.getString("status");
            info = newJson.getString("info");
        }
        catch (JSONException e) {
            notice.setText("JSON parser failed");
            return;
        }
        if (status.equals("success")) {
            switch (info) {
                case "login_user":
                    notice.setText("Successful Logged Out");
                    activeRoom();
                    break;
                default:
                    notice.setText(info);
                    break;
            }
        } else {
            notice.setText("Error:" + info);
        }
    }
}
