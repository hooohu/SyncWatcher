package com.gwu_cs6221_paradigm_hechh.syncwatcher;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;


public class CreateActivity extends ActionBarActivity implements ConnectionCallback {
    private String serverIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        Intent intent = getIntent();
        serverIP = intent.getStringExtra(LoginActivity.EXT_serverIP);
    }

    public void clickCreate(View nView) {
        EditText userEdit = (EditText) findViewById(R.id.editUserNew);
        EditText passEdit = (EditText) findViewById(R.id.editPassNew);
        EditText retypeEdit = (EditText) findViewById(R.id.editRetypeNew);
        String user = userEdit.getText().toString();
        String pass = passEdit.getText().toString();
        String retype = retypeEdit.getText().toString();
        TextView notice = (TextView) findViewById(R.id.textInfo);
        if (user.equals("") || pass.equals("")) {
            notice.setText("Null String");
            return;
        }
        if (!pass.equals(retype)) {
            notice.setText("Password Mismatch");
            return;
        }
        JSONObject newJson = new JSONObject();
        try {
            newJson.put("pw", pass);
        }
        catch (JSONException e) {
            notice.setText("JSON Put Failed");
        }
        String payload = newJson.toString();
        ConnectionManager hc = new ConnectionManager();
        String response = hc.httpRequest("POST", serverIP, "users/" + user, payload, this, this);
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
            notice.setText("JSON parser failed");
            return;
        }
        if (status.equals("success")) {
            if (info.equals("create_user")) {
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
