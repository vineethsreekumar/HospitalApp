package com.androidexample.broadcastreceiver;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by admin on 26/02/18.
 */

public class WSConnection {
    public static JSONObject getResult(String urlStr, String requestStr, Context context) {
        String urlString = urlStr;
        String jsonString = requestStr;
        JSONObject rootObject = null;
        boolean hasConnection = false;
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        hasConnection = true;
                        break;
                    }

        }
        if(hasConnection){
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                //Log.e("contentlength", Integer.toString(jsonString.length()));
                //conn.setRequestProperty("Content-Length", Integer.toString(jsonString.length()));
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(3000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                Log.e("url in WS", urlString);
                Log.e("request json", jsonString);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter((conn.getOutputStream())));
                writer.write(jsonString, 0, jsonString.length());
                writer.flush();
                writer.close();

                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                JSONParser parser = new JSONParser();
                Object object = parser.parse(br);
                org.json.simple.JSONObject jsonObj = (org.json.simple.JSONObject) object;
                rootObject = new JSONObject(jsonObj.toJSONString());
                Log.e("response from ws", rootObject.toString());
                conn.disconnect();
            } catch (Exception e) {
                rootObject = new JSONObject();

                }


            }

        return rootObject;
    }
}
