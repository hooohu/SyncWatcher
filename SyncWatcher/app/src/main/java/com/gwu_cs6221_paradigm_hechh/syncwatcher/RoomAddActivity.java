package com.gwu_cs6221_paradigm_hechh.syncwatcher;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class RoomAddActivity extends ActionBarActivity implements ConnectionCallback {
    private String serverIP;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_add);
        Intent intent = getIntent();
        serverIP = intent.getStringExtra(LoginActivity.EXT_serverIP);
        userName = intent.getStringExtra(LoginActivity.EXT_userName);
    }

    public void clickCreate(View nView) {
        EditText roomEdit = (EditText) findViewById(R.id.editAddNew);
        String room = roomEdit.getText().toString();
        TextView notice = (TextView) findViewById(R.id.textInfo);
        if (room.equals("")) {
            notice.setText("Null String");
            return;
        }
        JSONObject newJson = new JSONObject();
        try {
            newJson.put("user", userName);
        }
        catch (JSONException e) {
            notice.setText("JSON Put Failed");
        }
        String payload = newJson.toString();
        ConnectionManager hc = new ConnectionManager();
        String response = hc.httpRequest("POST", serverIP, "rooms/" + room, payload, this, this);
        notice.setText(response);
    }

    public void clickBack(View nView) {
        finish();
    }

    public void callBack(String result) {
        String status;
        String info;
        TextView notice = (TextView) findViewById(R.id.textInfo);
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
            notice.setText("JSON Failed: " + result);
            return;
        }
        if (status.equals("success")) {
            if (info.equals("create_room")) {
                finish();
            }
            else {
                notice.setText("Success: " + info);
            }
        } else {
            notice.setText("Error:" + info);
        }
    }
}
