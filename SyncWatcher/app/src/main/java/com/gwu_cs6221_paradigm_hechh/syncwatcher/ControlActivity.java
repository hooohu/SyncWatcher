package com.gwu_cs6221_paradigm_hechh.syncwatcher;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ControlActivity extends ActionBarActivity implements ConnectionCallback {
    private String serverIP;
    private String userName;
    private String roomName;
    private boolean flagGet = false;
    private boolean flagSync = false;
    private String statusPlay = "Stopping";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        Intent intent = getIntent();
        serverIP = intent.getStringExtra(LoginActivity.EXT_serverIP);
        userName = intent.getStringExtra(LoginActivity.EXT_userName);
        roomName = intent.getStringExtra(RoomListActivity.EXT_roomName);
        TextView welcome = (TextView) findViewById(R.id.textCtrl);
        welcome.setText(roomName);
        updateList();
    }

    void updateList() {
        flagGet = true;
        ConnectionManager hc = new ConnectionManager();
        String response = hc.httpRequest("GET", serverIP, "devices/" + roomName, "", this, this);
        TextView notice = (TextView) findViewById(R.id.textInfo);
        notice.setText(response);
    }

    public void clickExit(View nView) {
        finish();
    }

    public void clickRefresh(View nView) {
        updateList();
    }

    public void clickDelete(View nView) {
        ConnectionManager hc = new ConnectionManager();
        String response = hc.httpRequest("DELETE", serverIP, "rooms/" + roomName, "", this, this);
        TextView notice = (TextView) findViewById(R.id.textInfo);
        notice.setText(response);
    }

    private void playCommand(String cmd, String ts) {
        TextView notice = (TextView) findViewById(R.id.textInfo);
        JSONObject newJson = new JSONObject();
        try {
            newJson.put("user", userName);
            newJson.put("cmd", cmd);
            newJson.put("ts", ts);
        }
        catch (JSONException e) {
            notice.setText("JSON Put Failed");
        }
        String payload = newJson.toString();
        ConnectionManager hc = new ConnectionManager();
        String response = hc.httpRequest("POST", serverIP, "plays/" + roomName, payload, this, this);
        notice.setText(response);
    }

    public void clickPlay(View nView) {
        Button bt = (Button) findViewById(R.id.buttonPlay);
        switch (statusPlay) {
            case "Stopping":
                statusPlay = "Playing";
                bt.setText("Pause");
                playCommand("play", "0");
                break;
            case "Pausing":
                statusPlay = "Playing";
                bt.setText("Pause");
                playCommand("resume", "");
                break;
            case "Playing":
                statusPlay = "Pausing";
                bt.setText("Resume");
                playCommand("pause", "");
                break;
            default:
                statusPlay = "Stopping";
                bt.setText("Play");
                break;
        }
    }

    public void clickStop(View nView) {
        statusPlay = "Stopping";
        Button bt = (Button) findViewById(R.id.buttonPlay);
        bt.setText("Play");
        playCommand("stop", "");
    }

    public void clickSync(View nView) {
        flagSync = true;
        Button nb = (Button)nView;
        String device = nb.getText().toString();
        ConnectionManager hc = new ConnectionManager();
        String response = hc.httpRequest("GET", serverIP, "plays/" + device, "", this, this);
        TextView notice = (TextView) findViewById(R.id.textInfo);
        notice.setText(response);
    }

    private int parseTimeToInt(String input) {
        String[] parts = input.split(":");
        if (parts.length != 3) {
            TextView notice = (TextView) findViewById(R.id.textInfo);
            notice.setText("Error:Split Time");
            return -1;
        }
        int ret;
        try {
            int th = Integer.parseInt(parts[0]);
            int tm = Integer.parseInt(parts[1]);
            int ts = Integer.parseInt(parts[2]);
            ret = th * 3600 + tm * 60 + ts;
        }
        catch (NumberFormatException e) {
            TextView notice = (TextView) findViewById(R.id.textInfo);
            notice.setText("Error:Time to INT ");
            return -1;
        }
        return ret;
    }

    public void clickSyncAs(View nView) {
        EditText et = (EditText) findViewById(R.id.editTime);
        int ts = parseTimeToInt(et.getText().toString());
        if (ts == -1) {
            et.setText(parseTimeToString(0));
            return;
        }
        if (statusPlay.equals("Playing")) {
            playCommand("play", Integer.toString(ts));
        } else {
            playCommand("sync", Integer.toString(ts));
        }
    }

    private String parseTimeToString(int ts) {
        if (ts >= 0) {
            String tt = Integer.toString(ts / 3600) + ":";
            int tMin = ts % 3600 / 60;
            int tSec = ts % 60;
            tt += ((tMin > 9) ? "" : "0") + Integer.toString(tMin) + ":";
            tt += ((tSec > 9) ? "" : "0") + Integer.toString(tSec);
            return tt;
        }
        else return "Unknown";
    }

    private void processGet(String result) {
        flagGet = false;
        List<String> roomList = new ArrayList<>();
        List<String> timeList = new ArrayList<>();
        TextView notice = (TextView) findViewById(R.id.textInfo);
        if (result.charAt(0) == '{') {
            try {
                JSONObject newJson = new JSONObject(result);
                Iterator<?> keys = newJson.keys();
                while (keys.hasNext()) {
                    String key = (String)keys.next();
                    String ts = newJson.getString(key);
                    int time;
                    try {
                        double td = Double.parseDouble(ts);
                        time = (int)td;
                    }
                    catch (NumberFormatException e) {
                        time = -1;
                    }
                    String timeShow = parseTimeToString(time);
                    roomList.add(key);
                    timeList.add(timeShow);
                }
            } catch (JSONException e) {
                notice.setText("JSON List Failed:" + e.getMessage());
                return;
            }
        }
        TableLayout ll = (TableLayout) findViewById(R.id.listDevices);
        ll.removeAllViews();
        for(int i = 0; i < roomList.size(); i++) {
            TableRow nr= new TableRow(this);
            Button nb = new Button(this);
            nb.setText(roomList.get(i));
            nb.setTextSize(25);
            nb.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    clickSync(v);
                }
            });
            nr.addView(nb);
            TextView ns = new TextView(this);
            ns.setText("        ");
            nr.addView(ns);
            TextView nt = new TextView(this);
            nt.setText(timeList.get(i));
            nt.setTextSize(25);
            nr.addView(nt);
            ll.addView(nr);
        }
        notice.setText("Refreshed Complete");
    }

    private void processSync(String result) {
        TextView notice = (TextView) findViewById(R.id.textInfo);
        String ts;
        flagSync = false;
        try {
            JSONObject newJson = new JSONObject(result);
            ts = newJson.getString("devicetime");
        } catch (JSONException e) {

            notice.setText("JSON Failed in SyncTime");
            return;
        }
        if (ts.equals("")) {
            notice.setText("Sync Too Frequently");
        }
        else {
            if (statusPlay.equals("Playing")) {
                playCommand("play", ts);
            } else {
                playCommand("sync", ts);
            }
        }
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
        else if (flagSync) {
            processSync(result);
        }
        else {
            String status;
            String info;
            try {
                JSONObject newJson = new JSONObject(result);
                status = newJson.getString("status");
                info = newJson.getString("info");
            } catch (JSONException e) {
                notice.setText("JSON Failed: " + result);
                return;
            }
            if (status.equals("success")) {
                switch (info) {
                    case "delete_room":
                        finish();
                        break;
                    case "set_play":
                        notice.setText("Command Sent");
                        break;
                    default:
                        notice.setText("Success: " + info);
                        break;
                }
            } else {
                notice.setText("Error:" + info);
            }
        }
    }
}
