package com.gwu_cs6221_paradigm_hechh.syncwatcher;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.content.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;


public class RoomListActivity extends ActionBarActivity implements ConnectionCallback {
    public final static String EXT_roomName = "ext.LoginActivity.roomName";

    private String serverIP;
    private String userName;
    private boolean flagGet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roomlist);
        Intent intent = getIntent();
        serverIP = intent.getStringExtra(LoginActivity.EXT_serverIP);
        userName = intent.getStringExtra(LoginActivity.EXT_userName);
        TextView welcome = (TextView) findViewById(R.id.textWelcome);
        welcome.setText(userName);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    @Override
    public void onBackPressed() {
        TextView text = (TextView) findViewById(R.id.textInfo);
        text.setText("Please use LOG OUT to leave");
    }

    public void clickLogout(View nView) {
        TextView notice = (TextView) findViewById(R.id.textInfo);
        ConnectionManager hc = new ConnectionManager();
        String response = hc.httpRequest("DELETE", serverIP, "login/" + userName, "", this, this);
        notice.setText(response);
    }

    public void updateList() {
        flagGet = true;
        TextView notice = (TextView) findViewById(R.id.textInfo);
        ConnectionManager hc = new ConnectionManager();
        String response = hc.httpRequest("GET", serverIP, "rooms/" + userName, "", this, this);
        notice.setText(response);
    }

    public void clickRefresh(View nView) {
        updateList();
    }

    public void clickEnter(View nView) {
        Button nb = (Button)nView;
        String room = nb.getText().toString();
        Intent nIntent = new Intent(this, ControlActivity.class);
        nIntent.putExtra(LoginActivity.EXT_serverIP, serverIP);
        nIntent.putExtra(LoginActivity.EXT_userName, userName);
        nIntent.putExtra(EXT_roomName, room);
        startActivity(nIntent);
    }

    public void clickNewRoom(View nView) {
        Intent nIntent = new Intent(this, RoomAddActivity.class);
        nIntent.putExtra(LoginActivity.EXT_serverIP, serverIP);
        nIntent.putExtra(LoginActivity.EXT_userName, userName);
        startActivity(nIntent);
    }

    private void processGet(String result) {
        TextView notice = (TextView) findViewById(R.id.textInfo);
        flagGet = false;
        List<String> roomList = new ArrayList<>();
        if (result.charAt(0) == '{') {
            try {
                JSONObject newJson = new JSONObject(result);
                Iterator<?> keys = newJson.keys();
                while (keys.hasNext()) {
                    roomList.add((String)keys.next());
                }
            } catch (JSONException e) {
                notice.setText("JSON List Failed:" + e.getMessage());
                return;
            }
        }
        TableLayout tl = (TableLayout) findViewById(R.id.listRooms);
        tl.removeAllViews();
        for(int row = 0; row < (roomList.size() + 1) / 2; row++) {
            TableRow nr = new TableRow(this);
            for (int i = 0; i < 2; i++) {
                if (row * 2 + i < roomList.size()) {
                    Button nb = new Button(this);
                    nb.setText(roomList.get(row * 2 + i));
                    nb.setTextSize(25);
                    nb.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            clickEnter(v);
                        }
                    });
                    nr.addView(nb);
                }
                else {
                    TextView nt = new TextView(this);
                    nt.setText("   ");
                    nr.addView(nt);
                }
                if (i == 0) {
                    TextView ns = new TextView(this);
                    ns.setText("        ");
                    nr.addView(ns);
                }
            }
            tl.addView(nr);
        }
        notice.setText("Refreshed Complete");
    }

    public void callBack(String result) {
        TextView notice = (TextView) findViewById(R.id.textInfo);
        if (result.equals("")) {
            notice.setText("No Response");
            return;
        }
        if (flagGet) {
            processGet(result);
        }
        else {
            String status;
            String info;
            try {
                JSONObject newJson = new JSONObject(result);
                status = newJson.getString("status");
                info = newJson.getString("info");
            } catch (JSONException e) {
                notice.setText("JSON failed:" + result);
                return;
            }
            if (status.equals("success")) {
                if (info.equals("logout_user")) {
                    finish();
                } else {
                    notice.setText("Success: " + info);
                }
            } else {
                notice.setText("Error:" + info);
            }
        }
    }
}
