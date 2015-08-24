package com.gwu_cs6221_paradigm_hechh.syncwatcher;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ConnectionManager {
    public final static String URL_PATH = "/~hechh/sync_server/api/";

    private String sMethod;
    private String serverIP;
    private String sCommand;
    private String sPayload;
    private ConnectionCallback sCallback;

    public String httpRequest(String method, String sIP, String cmd, String payload,
                               ConnectionCallback callback, Context context) {
        sMethod = method;
        serverIP = sIP;
        sCommand = cmd;
        sPayload = payload;
        sCallback = callback;
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        String response;
        if (networkInfo != null && networkInfo.isConnected()) {
            new httpTask().execute();
            response = "connecting";
        }
        else {
            response = "no networks";
        }
        return response;
    }

    private class httpTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return urlRequest();
            } catch (IOException e) {
                return "{\"status\":\"error\", \"info\":\"url_invalid:" + e.getMessage() +"\"}";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            sCallback.callBack(result);
        }
    }

    private String readIt(InputStream stream, int len) throws IOException {
        Reader reader;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    private void writeIt(OutputStream stream, String buffer) throws IOException {
        try {
            BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(stream, "UTF-8"));
            writer.write(buffer);
            writer.flush();
        }
        catch (UnsupportedEncodingException e) {
            sCallback.callBack("{\"status\":\"error\", \"info\":\"bufferWriter\"}");
        }

    }

    private String urlRequest() throws IOException {
        InputStream is = null;
        OutputStream os;
        int len = 4096;

        try {
            URL url = new URL("http://" + serverIP + URL_PATH + sCommand);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod(sMethod);
            conn.setDoInput(true);
            if (!sPayload.equals("")) {
                conn.setDoOutput(true);
                os = conn.getOutputStream();
                writeIt(os, sPayload);
            }
            // Starts the query
            conn.connect();
            conn.getResponseCode();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            return readIt(is, len);

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
